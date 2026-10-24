package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mdkj.component.MyRedis;
import com.mdkj.constant.ML;
import com.mdkj.dto.*;
import com.mdkj.exception.ServiceException;
import com.mdkj.mapper.SeckillDetailMapper;
import com.mdkj.result.ResultCode;
import com.mdkj.vo.PageVO;
import com.mdkj.vo.SeckillSimpleListVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.entity.Seckill;
import com.mdkj.mapper.SeckillMapper;
import com.mdkj.service.SeckillService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mdkj.entity.table.SeckillDetailTableDef.SECKILL_DETAIL;
import static com.mdkj.entity.table.SeckillTableDef.SECKILL;
import static com.mybatisflex.core.query.QueryMethods.curDate;
import static com.mybatisflex.core.query.QueryMethods.date;

/**
 * 秒杀表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements SeckillService {

    @Autowired
    private SeckillDetailMapper seckillDetailMapper;
    @Autowired
    private MyRedis myRedis;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @Override
    public boolean insert(SeckillInsertDTO dto) {
        String title = dto.getTitle();
        LocalDateTime startTime = dto.getStartTime();
        // 标题和开始时间查重
        // select count(*) from seckill where title = ? and start_time = ?
        if (QueryChain.of(mapper)
                .where(SECKILL.TITLE.eq(title))
                .and(SECKILL.START_TIME.eq(startTime))
                .exists()) {
            throw new ServiceException(ResultCode.SECKILL_REPEAT, startTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) + title + "的秒杀活动重复");
        }
        // 组装实体类
        Seckill seckill = BeanUtil.copyProperties(dto, Seckill.class);
        seckill.setInfo(StrUtil.isEmpty(dto.getInfo()) ? "暂无描述。" : dto.getInfo());
        seckill.setCreated(LocalDateTime.now());
        seckill.setUpdated(LocalDateTime.now());
        //更新DB
        if (mapper.insert(seckill) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }

        return true;
    }

    @Override
    public Seckill select(Long id) {
        Seckill seckill = mapper.selectOneWithRelationsById(id);
        if (ObjectUtil.isNull(seckill)) {
            throw new ServiceException(ResultCode.SECKILL_NOT_FOUND, id + "号秒杀活动不存在");
        }
        return seckill;
    }

    @Override
    public List<SeckillSimpleListVO> simpleList() {
        return QueryChain.of(mapper)
                .withRelations()
                .listAs(SeckillSimpleListVO.class);
    }

    @Override
    public PageVO<Seckill> page(SeckillPageDTO dto) {
        QueryChain<Seckill> queryChain = QueryChain.of(mapper);

        // title条件
        String title = dto.getTitle();
        if (ObjectUtil.isNotNull(title)) {
            queryChain.where(SECKILL.TITLE.like(title));
        }

        // DB分页并转为VO
        Page<Seckill> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Seckill> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(SeckillUpdateDTO dto) {
        Long id = dto.getId();
        String title = dto.getTitle();
        // 检查秒杀活动是否存在
        this.existsById(id);
        // 标题查重
        // select count(*) from seckill where title = ? and id <> ?
        if (QueryChain.of(mapper)
                .where(SECKILL.TITLE.eq(title))
                .and(SECKILL.ID.ne(id))
                .exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }
        // 组装实体类
        Seckill seckill = BeanUtil.copyProperties(dto, Seckill.class);
        // update seckill set title = ?, info = ?, start_time = ?, end_time = ?, updated = ? where id = ?
        seckill.setUpdated(LocalDateTime.now());
        if (!UpdateChain.of(seckill)
                .where(SECKILL.ID.eq(id))
                .update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        //检查秒杀活动是否存在
        this.existsById(id);
        //删除秒杀活动关联的秒杀明细数据
        UpdateChain.of(seckillDetailMapper)
                .where(SECKILL_DETAIL.FK_SECKILL_ID.eq(id))
                .remove();
        //删除本表
        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Override
    public boolean deleteBatch(List<Long> ids) {
        //检查秒杀活动是否存在
        if (QueryChain.of(mapper)
                .where(SECKILL.ID.in(ids))
                .count() < ids.size()) {
            throw new ServiceException(ResultCode.SECKILL_NOT_FOUND, "至少一个秒杀活动不存在");
        }
        //删除秒杀活动关联的秒杀明细数据
        UpdateChain.of(seckillDetailMapper)
                .where(SECKILL_DETAIL.FK_SECKILL_ID.in(ids))
                .remove();
        //删除本表
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Override
    public List<Seckill> near(Long n) {
        return QueryChain.of(mapper)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .orderBy(SECKILL.START_TIME.asc())
                .limit(n)
                .withRelations()
                .list();
    }

    @Override
    public boolean kill(KillDTO dto) {
        Long fkSeckillId = dto.getFkSeckillId();
        Long fkCourseId = dto.getFkCourseId();
        //检查秒杀活动是否存在
        Seckill seckill = mapper.selectOneById(fkSeckillId);
        if (ObjectUtil.isNull(seckill)) {
            throw new ServiceException(ResultCode.SECKILL_NOT_FOUND, "秒杀活动不存在");
        }
        //根据秒杀活动状态决定方案
        Integer status = seckill.getStatus();
        System.out.println("当前秒杀活动状态=====>"+status);
        if (status.equals(ML.Seckill.NOT_START)) {
            throw new ServiceException(ResultCode.SECKILL_NOT_START, "秒杀活动未开始");
        } else if (status.equals(ML.Seckill.STARTED)) {
            //开始秒杀
            final String KEY = ML.Redis.SECKILL_COURSE_COUNT_PREFIX + fkCourseId;
            //加锁：30秒后过期 线程如果存活状态下 10秒自动续期一次
            RLock lock = redissonClient.getLock("skLock");
            lock.lock(30, TimeUnit.SECONDS);
            try {
                //判断库存是否充足
                if(Integer.parseInt(myRedis.get(KEY)) > 0){
                    //扣减库存
                    myRedis.incr(KEY,-1);
                    //向MQ发送消息 发送到订单服务 消息内容为 订单对象 OrderMessage
                    OrderMessage orderMessage = new OrderMessage();
                    orderMessage.setFkUserId(dto.getFkUserId());
                    orderMessage.setFkCourseId(fkCourseId);
                    orderMessage.setSkPrice(dto.getSkPrice());
                    orderMessage.setPrice(dto.getPrice());
                    rocketMQTemplate.convertAndSend("ml-topic:ml-tag",orderMessage);
                    System.out.println("MQ:秒杀商品成功====>");
                    return true;
                }else{
                    throw new ServiceException(ResultCode.SERVER_ERROR, "库存不足，秒杀失败");
                }
            }finally {
                //释放锁
                lock.unlock();
            }
        } else if (status.equals(ML.Seckill.ENDED)) {
            throw new ServiceException(ResultCode.SECKILL_NOT_START, "秒杀活动已结束");
        } else {
            throw new ServiceException(ResultCode.SERVER_ERROR, "秒杀活动状态异常");
        }
    }

    private void existsById(Long id) {
        // select count(*) from seckill where id = ?
        if (!QueryChain.of(mapper)
                .where(SECKILL.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.SECKILL_NOT_FOUND, id + "号秒杀活动不存在");
        }
    }
}
