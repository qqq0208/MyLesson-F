package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mdkj.constant.ML;
import com.mdkj.dto.OrderInsertDTO;
import com.mdkj.dto.OrderPageDTO;
import com.mdkj.dto.OrderUpdateDTO;
import com.mdkj.dto.PrePayDTO;
import com.mdkj.entity.*;
import com.mdkj.excel.OrderDetailExcelDTO;
import com.mdkj.exception.ServiceException;
import com.mdkj.feign.CourseFeign;
import com.mdkj.feign.UserFeign;
import com.mdkj.mapper.OrderDetailMapper;
import com.mdkj.result.Result;
import com.mdkj.result.ResultCode;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.mapper.OrderMapper;
import com.mdkj.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mdkj.entity.table.CartTableDef.CART;
import static com.mdkj.entity.table.OrderDetailTableDef.ORDER_DETAIL;
import static com.mdkj.entity.table.OrderTableDef.ORDER;
import static com.mdkj.entity.table.UserTableDef.USER;
import static com.mybatisflex.core.query.QueryMethods.*;

/**
 * 订单表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    private UserFeign userFeign;
    @Autowired
    private CourseFeign courseFeign;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public boolean insert(OrderInsertDTO dto) {
        Long fkUserId = dto.getFkUserId();
        //获取用户信息
        Result<User> userResult = userFeign.select(fkUserId);
        if (ObjectUtil.isNull(userResult)) {
            throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "用户微服务远程调用失败，请联系管理员。");
        }
        User user = userResult.getData();
        if (ObjectUtil.isNull(user)) {
            throw new ServiceException(ResultCode.USER_NOT_FOUND, fkUserId + "号用户数据不存在");
        }
        //组装实体类
        Order order = BeanUtil.copyProperties(dto, Order.class);
        order.setSn(RandomUtil.randomNumbers(19));
        order.setUsername(user.getUsername());
        order.setInfo(StrUtil.isEmpty(dto.getInfo()) ? "暂无描述。" : dto.getInfo());
        order.setCreated(LocalDateTime.now());
        order.setUpdated(LocalDateTime.now());
        //DB添加
        if (mapper.insert(order) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        return false;
    }

    @Override
    public Order select(Long id) {
        RelationManager.addQueryRelations("orderDetails");
        Order order = mapper.selectOneWithRelationsById(id);
        if (ObjectUtil.isNull(order)) {
            throw new ServiceException(ResultCode.ORDER_NOT_FOUND, id + "号订单数据不存在");
        }
        return order;
    }

    @Override
    public PageVO<Order> page(OrderPageDTO dto) {
        RelationManager.addQueryRelations("orderDetails");
        QueryChain<Order> queryChain = QueryChain.of(mapper)
                .orderBy(ORDER.UPDATED.desc());
        // fkCourseId条件
        Long fkUserId = dto.getFkUserId();
        if (ObjectUtil.isNotNull(fkUserId)) {
            queryChain.where(ORDER.FK_USER_ID.eq(fkUserId));
        }
        // sn条件
        String sn = dto.getSn();
        if (ObjectUtil.isNotEmpty(sn)) {
            queryChain.where(ORDER.SN.eq(sn));
        }
        // status条件
        Integer status = dto.getStatus();
        if (ObjectUtil.isNotNull(status)) {
            queryChain.where(ORDER.STATUS.eq(status));
        }
        // username条件
        String username = dto.getUsername();
        if (ObjectUtil.isNotNull(username)) {
            queryChain.where(ORDER.USERNAME.like(username));
        }
        // DB分页并转为VO
        Page<Order> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Order> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(OrderUpdateDTO dto) {
        Long id = dto.getId();
        //检查订单是否存在
        this.existsById(id);
        // 组装实体类
        Order order = BeanUtil.copyProperties(dto, Order.class);
        order.setUpdated(LocalDateTime.now());
        //DB修改
        if (!UpdateChain.of(order)
                .where(ORDER.ID.eq(order.getId()))
                .update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        //检查订单是否存在
        this.existsById(id);
        //根据订单ID删除订单明细相关数据
        UpdateChain.of(orderDetailMapper)
                .where(ORDER_DETAIL.FK_ORDER_ID.eq(id))
                .remove();
        //删除订单数据
        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean deleteBatch(List<Long> ids) {
        //检查订单是否存在
        if (QueryChain.of(mapper)
                .where(ORDER.ID.in(ids))
                .count() < ids.size()) {
            throw new ServiceException(ResultCode.ORDER_NOT_FOUND, "至少一个订单记录不存在");
        }
        //根据订单ID删除订单明细相关数据
        UpdateChain.of(orderDetailMapper)
                .where(ORDER_DETAIL.FK_ORDER_ID.in(ids))
                .remove();
        //删除订单数据
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Override
    public Map<String, Object> statistics() {
        //创建最终返回的Map集合
        Map<String, Object> result = new HashMap<>();
        //统计订单支付方式比例
        List<Map> payTypeCount = QueryChain.of(mapper)
                .select(ORDER.PAY_TYPE.as("name"), QueryMethods.count().as("value"))
                .groupBy(ORDER.PAY_TYPE)
                .orderBy(ORDER.PAY_TYPE.asc())
                .listAs(Map.class);
        result.put("payTypeCount", payTypeCount);
        //统计今日订单数
        double todayCount = QueryChain.of(mapper)
                .where(dateDiff(currentDate(), dateFormat(ORDER.CREATED, "%Y-%m-%d")).eq(0))
                .count();
        result.put("todayCount", todayCount);
        //统计昨日订单数
        long yesterdayCount = QueryChain.of(mapper)
                .where(dateDiff(currentDate(), dateFormat(ORDER.CREATED, "%Y-%m-%d")).eq(1))
                .count();
        result.put("yesterdayCount", yesterdayCount);
        //统计今年订单数
        double thisYearCount = QueryChain.of(mapper)
                .where(year(ORDER.CREATED).eq(year(currentDate())))
                .count();
        result.put("thisYearCount", thisYearCount);
        //统计去年订单数
        LocalDate lastYearStart = LocalDate.now().minusYears(1).withDayOfYear(1);
        LocalDate thisYearStart = LocalDate.now().withDayOfYear(1);
        long lastYearCount = QueryChain.of(mapper)
                .where(ORDER.CREATED.ge(lastYearStart).getColumn().lt(thisYearStart))
                .count();
        result.put("lastYearCount", lastYearCount);
        //统计日增长率
        result.put("dayIncrease", increase(todayCount, yesterdayCount));
        //统计年增长率、
        result.put("yearIncrease", increase(thisYearCount, lastYearCount));
        return result;
    }

    //预支付下单  本质上就是在订单表以及订单明细表插入订单数据
    @Override
    public String prePay(PrePayDTO dto) {
        //获取用户ID和课程ID的集合
        Long fkUserId = dto.getFkUserId();
        List<Long> courseIds = dto.getCourseIds();
        //根据用户id查询订单表 只返回订单ID的集合
        List<Long> orderIds = QueryChain.of(mapper)
                .select(ORDER.ID)
                .where(ORDER.FK_USER_ID.eq(fkUserId))
                .listAs(Long.class);
        //判空
        if(CollUtil.isNotEmpty(orderIds)){
            //根据订单ID的集合 查询订单明细表 只返回明细（课程 商品）的ID集合（已购买）
            List<Long> courserIdList = QueryChain.of(orderDetailMapper)
                    .select(ORDER_DETAIL.FK_COURSE_ID)
                    .where(ORDER_DETAIL.FK_ORDER_ID.in(orderIds))
                    .listAs(Long.class);
            //判断是否存在重复购买课程的现象 这2个集合 有重复不分 代表重复购买
            courserIdList.retainAll(courseIds);
            if(CollUtil.isNotEmpty(courserIdList)){
                throw new ServiceException(ResultCode.ORDER_DETAIL_REPEAT,"订单明细重复");
            }
        }
        //组装实体类
        Order order = BeanUtil.copyProperties(dto, Order.class);
        String sn = RandomUtil.randomNumbers(19);
        order.setSn(sn);
        order.setPayType(ML.Order.NO_PAY);
        order.setStatus(ML.Order.UNPAID);
        order.setPayAmount(0.0);
        order.setInfo("暂无描述。");
        Result<User> userResult = userFeign.select(fkUserId);
        if (ObjectUtil.isNull(userResult)) {
            throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "用户微服务远程调用失败，请联系管理员。");
        }
        User user = userResult.getData();
        if (ObjectUtil.isNull(user)) {
            throw new ServiceException(ResultCode.USER_NOT_FOUND, fkUserId + "号用户数据不存在");
        }
        order.setUsername(user.getUsername());
        order.setCreated(LocalDateTime.now());
        order.setUpdated(LocalDateTime.now());
        // DB添加订单表记录
        if (mapper.insert(order) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        // 批量添加订单明细表记录
        Long orderId = order.getId();
        List<OrderDetail> orderDetails = new ArrayList<>();
        //遍历传递过来的 课程id集合
        for (Long courseId : courseIds) {
            //根据课程ID 获取课程信息
            Result<Course> courseResult = courseFeign.select(courseId);
            if (ObjectUtil.isNull(courseResult)) {
                throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "课程微服务远程调用失败，请联系管理员。");
            }
            Course course = courseResult.getData();
            if (ObjectUtil.isNull(course)) {
                throw new ServiceException(ResultCode.COURSE_NOT_FOUND, courseId + "号课程数据不存在");
            }
            //组装订单明细数据
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setFkCourseId(courseId);
            orderDetail.setFkOrderId(orderId);
            orderDetail.setCourseTitle(course.getTitle());
            orderDetail.setCourseCover(course.getCover());
            orderDetail.setCoursePrice(course.getPrice());
            orderDetail.setCreated(LocalDateTime.now());
            orderDetail.setUpdated(LocalDateTime.now());
            orderDetails.add(orderDetail);
        }
        //DB添加
        if (orderDetailMapper.insertBatch(orderDetails) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        //删除购物车记录（可选）
        /*UpdateChain.of(Cart.class)
                .where(CART.FK_USER_ID.eq(fkUserId))
                .and(CART.FK_COURSE_ID.in(courseIds))
                .remove();*/
        //将订单发送到MQ 延迟15分钟发出 如果15分钟收到收消息 则认为已超时 修改订单状态
        return sn;
    }

    @Override
    public boolean updateStatusBySn(String sn, Integer status) {
        // 根据订单编号更新订单状态
        if (!UpdateChain.of(mapper)
                .set(ORDER.STATUS, status)
                .set(ORDER.UPDATED, LocalDateTime.now())
                .where(ORDER.SN.eq(sn))
                .update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "更新订单状态失败");
        }
        return true;
    }

    @Override
    public boolean checkStatusBySn(String sn) {
        // 根据订单编号查询订单记录（仅查询支付成功的订单）
        Order order = QueryChain.of(mapper)
                .where(ORDER.SN.eq(sn))
                .and(ORDER.STATUS.eq(ML.Order.PAID))
                .one();
        return ObjectUtil.isNotNull(order);
    }


    //计算a到b的增长率
    private String increase(double a, double b) {
        if (b == 0) {
            return a > b ? "100.00" : a < b ? "-100" : "0";
        }
        return String.format("%.2f", (a - b) / b);
    }

    private void existsById(Long id) {
        // select count(*) from `order` where id = ?
        if (!QueryChain.of(mapper)
                .where(ORDER.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.ORDER_NOT_FOUND, id + "号订单记录不存在");
        }
    }
}
