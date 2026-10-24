package com.mdkj.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mdkj.component.MyRedis;
import com.mdkj.constant.ML;
import com.mdkj.entity.Course;
import com.mdkj.entity.Seckill;
import com.mdkj.entity.SeckillDetail;
import com.mdkj.exception.ServiceException;
import com.mdkj.feign.CourseFeign;
import com.mdkj.mapper.SeckillMapper;
import com.mdkj.result.Result;
import com.mdkj.result.ResultCode;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mdkj.entity.table.SeckillTableDef.SECKILL;
import static com.mybatisflex.core.query.QueryMethods.*;

/**
 * @author 油桃老师
 **/
@Component
public class SeckillJob {
    @Autowired
    private MyRedis myRedis;
    @Autowired
    private SeckillMapper seckillMapper;
    @Autowired
    private CourseFeign courseFeign;

    //秒杀活动预热-->每天早上7点开启
    @XxlJob("initSeckill")
    public void initSeckill() {
        System.out.println("秒杀活动预热开始");
        XxlJobHelper.log("秒杀活动预热开始");
        //查询当天所有的秒杀活动
        List<Seckill> todaySeckills = QueryChain.of(seckillMapper)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .orderBy(SECKILL.START_TIME.asc())
                .withRelations()
                .list();
        if (CollUtil.isEmpty(todaySeckills)) {
            XxlJobHelper.log("今日暂无秒杀活动、缓存预热跳过");
            XxlJobHelper.handleSuccess("秒杀活动预热完成（无数据）");
            return;
        }
        //查询秒杀商品的ID列表（以便于后续批量查询）
        List<Long> courseIds = new ArrayList<>();
        todaySeckills.forEach(seckill -> {
            //通过当前秒杀对象 获取 获取秒杀明细集合
            List<SeckillDetail> seckillDetails = seckill.getSeckillDetails();
            if (CollUtil.isNotEmpty(seckillDetails)) {
                seckillDetails.forEach(seckillDetail -> {
                    //获取课程ID
                    Long fkCourseId = seckillDetail.getFkCourseId();
                    Integer skCount = seckillDetail.getSkCount();
                    //缓存每个商品的库存（12小时过期、07点开始缓存 18:00结束）
                    myRedis.setEx(ML.Redis.SECKILL_COURSE_COUNT_PREFIX + fkCourseId, skCount.toString(), 12, TimeUnit.HOURS);
                    //将商品ID存入集合中
                    courseIds.add(fkCourseId);
                });
            }
            System.out.println("秒杀活动ID===>" + seckill.getId());
            System.out.println("查询商品数量===>" + seckillDetails.size());
            XxlJobHelper.log("秒杀活动ID:" + seckill.getId() + "--查询到商品数量：" + seckillDetails.size());
        });
        //预热秒杀商品
        courseIds.forEach(courseId -> {
            //远程调用课程服务 查询课程信息
            Result<Course> courseResult = courseFeign.select(courseId);
            if (ObjectUtil.isNull(courseResult)) {
                throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "远程调用课程服务失败");
            }
            Course course = courseResult.getData();
            if (ObjectUtil.isNull(course)) {
                throw new ServiceException(ResultCode.COURSE_NOT_FOUND, "课程不存在");
            }
            //将课程信息存入到Redis中（预热）
            myRedis.setEx(ML.Redis.SECKILL_COURSE_INFO_PREFIX + courseId, JSON.toJSONString(course), 12, TimeUnit.HOURS);
            System.out.println("秒杀活动商品" + courseId + "预热完成");
            XxlJobHelper.log("秒杀活动商品" + courseId + "预热完成");
        });
        System.out.println("秒杀活动预热结束 共预热"+courseIds.size()+"个商品");
        XxlJobHelper.log("秒杀活动预热结束 共预热"+courseIds.size()+"个商品");
    }

    //开启上午场秒杀 -->8点
    @XxlJob("startMorningSeckill")
    public void startMorningSeckill(){
        System.out.println("准备开启今日上午场秒杀活动");
        //修改当天上午场的秒杀活动状态
        UpdateChain.of(seckillMapper)
                .set(SECKILL.STATUS,ML.Seckill.STARTED)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .and(SECKILL.TITLE.eq("上午场"))
                .update();
        System.out.println("上午场秒杀活动已开启");
        XxlJobHelper.handleSuccess("上午场秒杀活动已开启");

    }

    //关闭上午场秒杀 -->9点
    @XxlJob("stopMorningSeckill")
    public void stopMorningSeckill(){
        System.out.println("准备关闭今日上午场秒杀活动");
        //修改当天上午场的秒杀活动状态
        UpdateChain.of(seckillMapper)
                .set(SECKILL.STATUS,ML.Seckill.ENDED)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .and(SECKILL.TITLE.eq("上午场"))
                .update();
        System.out.println("上午场秒杀活动已关闭");
        XxlJobHelper.handleSuccess("上午场秒杀活动已关闭");
    }

    //开启中午场秒杀 -->12点
    @XxlJob("startNoonSeckill")
    public void startNoonSeckill(){
        System.out.println("准备开启今日中午场秒杀活动");
        //修改当天上午场的秒杀活动状态
        UpdateChain.of(seckillMapper)
                .set(SECKILL.STATUS,ML.Seckill.STARTED)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .and(SECKILL.TITLE.eq("中午场"))
                .update();
        System.out.println("中午场秒杀活动已开启");
        XxlJobHelper.handleSuccess("中午场秒杀活动已开启");
    }

    //关闭中午场秒杀 -->13点
    @XxlJob("stopNoonSeckill")
    public void stopNoonSeckill(){
        System.out.println("准备关闭今日中午场秒杀活动");
        //修改当天上午场的秒杀活动状态
        UpdateChain.of(seckillMapper)
                .set(SECKILL.STATUS,ML.Seckill.ENDED)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .and(SECKILL.TITLE.eq("中午场"))
                .update();
        System.out.println("中午场秒杀活动已关闭");
        XxlJobHelper.handleSuccess("中午场秒杀活动已关闭");
    }

    @XxlJob("startAfterNoonSeckill")
    public void startAfterNoonSeckill() {
        System.out.println("准备开启今日下午场的秒杀活动");
        // 修改当天的下午场的秒杀活动状态
        UpdateChain.of(seckillMapper)
                .set(SECKILL.STATUS, ML.Seckill.STARTED)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .and(SECKILL.TITLE.eq("下午场"))
                .update();

        System.out.println("下午场的秒杀活动已开启");
        XxlJobHelper.handleSuccess("下午场的秒杀活动开启成功");
    }

    @XxlJob("stopAfterNoonSeckill")
    public void stopAfterNoonSeckill() {
        System.out.println("准备关闭今日下午场的秒杀活动");
        // 修改当天的下午场的秒杀活动状态
        UpdateChain.of(seckillMapper)
                .set(SECKILL.STATUS, ML.Seckill.ENDED)
                .where(date(SECKILL.START_TIME).eq(curDate()))
                .and(SECKILL.TITLE.eq("下午场"))
                .update();

        System.out.println("下午场的秒杀活动已关闭");
        XxlJobHelper.handleSuccess("下午场的秒杀活动关闭成功");
    }

}
