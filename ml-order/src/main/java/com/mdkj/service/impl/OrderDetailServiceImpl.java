package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mdkj.constant.ML;
import com.mdkj.dto.OrderDetailInsertDTO;
import com.mdkj.dto.OrderDetailPageDTO;
import com.mdkj.dto.OrderDetailUpdateDTO;
import com.mdkj.entity.Course;
import com.mdkj.entity.Order;
import com.mdkj.excel.OrderDetailExcelDTO;
import com.mdkj.exception.ServiceException;
import com.mdkj.feign.CourseFeign;
import com.mdkj.mapper.OrderMapper;
import com.mdkj.result.Result;
import com.mdkj.result.ResultCode;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.entity.OrderDetail;
import com.mdkj.mapper.OrderDetailMapper;
import com.mdkj.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.mdkj.entity.table.OrderDetailTableDef.ORDER_DETAIL;

/**
 * 订单明细表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

    @Autowired
    private CourseFeign courseFeign;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public boolean insert(OrderDetailInsertDTO dto) {
        Long fkOrderId = dto.getFkOrderId();
        Long fkCourseId = dto.getFkCourseId();
        //订单明细查重
        if (QueryChain.of(mapper)
                .where(ORDER_DETAIL.FK_ORDER_ID.eq(fkOrderId))
                .and(ORDER_DETAIL.FK_COURSE_ID.eq(fkCourseId))
                .exists()) {
            throw new ServiceException(ResultCode.ORDER_DETAIL_REPEAT, "订单明细已重复，" + fkOrderId + "号订单已经添加了" + fkCourseId + "号课程");
        }
        //获取课程信息
        Result<Course> courseResult = courseFeign.select(fkCourseId);
        if (ObjectUtil.isNull(courseResult)) {
            throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "课程微服务远程调用失败，请联系管理员。");
        }
        Course course = courseResult.getData();
        if (ObjectUtil.isNull(course)) {
            throw new ServiceException(ResultCode.COURSE_NOT_FOUND, fkCourseId + "号课程数据不存在");
        }
        //组装实体类
        OrderDetail orderDetail = BeanUtil.copyProperties(dto, OrderDetail.class);
        orderDetail.setCourseTitle(course.getTitle());
        orderDetail.setCoursePrice(course.getPrice());
        orderDetail.setCourseCover(course.getCover());
        orderDetail.setCreated(LocalDateTime.now());
        orderDetail.setUpdated(LocalDateTime.now());
        orderDetail.setSn(orderMapper.selectOneById(fkOrderId).getSn());
        //DB添加
        if (mapper.insert(orderDetail) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        return true;
    }

    @Override
    public OrderDetail select(Long id) {
        RelationManager.addQueryRelations("order");
        OrderDetail orderDetail = mapper.selectOneWithRelationsById(id);
        if (orderDetail == null) {
            throw new ServiceException(ResultCode.ORDER_DETAIL_NOT_FOUND, id + "号订单明细数据不存在");
        }
        return orderDetail;
    }

    @Override
    public PageVO<OrderDetail> page(OrderDetailPageDTO dto) {
        RelationManager.addQueryRelations("order");
        QueryChain<OrderDetail> queryChain = QueryChain.of(mapper)
                .where(ORDER_DETAIL.FK_ORDER_ID.eq(dto.getOrderId()))
                .orderBy(ORDER_DETAIL.UPDATED.desc());
        // courseTitle条件
        String courseTitle = dto.getCourseTitle();
        if (ObjectUtil.isNotNull(courseTitle)) {
            queryChain.where(ORDER_DETAIL.COURSE_TITLE.like(courseTitle));
        }
        // DB分页并转为VO
        Page<OrderDetail> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<OrderDetail> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(OrderDetailUpdateDTO dto) {
        Long id = dto.getId();
        Long fkOrderId = dto.getFkOrderId();
        Long fkCourseId = dto.getFkCourseId();
        // 检查订单明细记录是否存在
        this.existsById(id);
        if (QueryChain.of(mapper)
                .where(ORDER_DETAIL.FK_ORDER_ID.eq(dto.getFkOrderId()))
                .and(ORDER_DETAIL.FK_COURSE_ID.eq(dto.getFkCourseId()))
                .and(ORDER_DETAIL.ID.ne(dto.getId()))
                .exists()) {
            throw new ServiceException(ResultCode.ORDER_DETAIL_REPEAT, "订单明细已重复，" + fkOrderId + "号订单已经添加了" + fkCourseId + "号课程");
        }
        //获取课程信息
        Result<Course> courseResult = courseFeign.select(fkCourseId);
        if (ObjectUtil.isNull(courseResult)) {
            throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "课程微服务远程调用失败，请联系管理员。");
        }
        Course course = courseResult.getData();
        if (ObjectUtil.isNull(course)) {
            throw new ServiceException(ResultCode.COURSE_NOT_FOUND, fkCourseId + "号课程数据不存在");
        }
        // 组装实体类
        OrderDetail orderDetail = BeanUtil.copyProperties(dto, OrderDetail.class);
        orderDetail.setCourseTitle(course.getTitle());
        orderDetail.setCoursePrice(course.getPrice());
        orderDetail.setCourseCover(course.getCover());
        orderDetail.setUpdated(LocalDateTime.now());
        orderDetail.setSn(orderMapper.selectOneById(fkOrderId).getSn());
        //DB修改
        if (!UpdateChain.of(orderDetail)
                .where(ORDER_DETAIL.ID.eq(orderDetail.getId()))
                .update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Override
    public boolean delete(Long id) {
        // 检查订单明细记录是否存在
        this.existsById(id);

        // delete from order_detail where id = ?
        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Override
    public boolean deleteBatch(List<Long> ids) {
        // 检查订单明细记录是否存在
        // select count(*) from order_detail where id in (?, ?, ?)
        if (QueryChain.of(mapper)
                .where(ORDER_DETAIL.ID.in(ids))
                .count() < ids.size()) {
            throw new ServiceException(ResultCode.ORDER_DETAIL_NOT_FOUND, "至少一个订单明细记录不存在");
        }

        // delete from order_detail where id in (?, ?, ?)
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Override
    public List<OrderDetailExcelDTO> getExcelData() {
        //查询全部订单明细记录
        List<OrderDetail> orderDetails = mapper.selectAllWithRelations();
        //数据拆分转换
        List<OrderDetailExcelDTO> excelDTOS = orderDetails.stream().map(orderDetail -> {
            OrderDetailExcelDTO dto = BeanUtil.copyProperties(orderDetail, OrderDetailExcelDTO.class);
            Order order = orderDetail.getOrder();
            dto.setSn(order.getSn());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setPayAmount(order.getPayAmount());
            dto.setPayType(ML.Order.payTypeFormat(order.getPayType()));
            dto.setInfo(order.getInfo());
            dto.setStatus(ML.Order.statusFormat(order.getStatus()));
            dto.setUsername(order.getUsername());
            dto.setCreated(order.getCreated());
            dto.setUpdated(order.getUpdated());
            return dto;
        }).collect(Collectors.toList());
        return excelDTOS;
    }

    private void existsById(Long id) {
        // select count(*) from order_detail where id = ?
        if (!QueryChain.of(mapper)
                .where(ORDER_DETAIL.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.ORDER_DETAIL_NOT_FOUND, id + "号订单明细记录不存在");
        }
    }
}
