package com.mdkj.service;

import com.mdkj.dto.OrderInsertDTO;
import com.mdkj.dto.OrderPageDTO;
import com.mdkj.dto.OrderUpdateDTO;
import com.mdkj.dto.PrePayDTO;
import com.mdkj.excel.OrderDetailExcelDTO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface OrderService extends IService<Order> {
    boolean insert(OrderInsertDTO dto);
    Order select(Long id);
    PageVO<Order> page(OrderPageDTO dto);
    boolean update(OrderUpdateDTO dto);
    boolean delete(Long id);
    boolean deleteBatch(List<Long> ids);
    Map<String,Object> statistics();
    //预支付请求--给前端小程序
    String prePay(PrePayDTO dto);

    //预支付请求回调 -- 给支付宝
    boolean updateStatusBySn(String sn, Integer status);

    //根据订单编号 查询订单状态
    boolean checkStatusBySn(String sn);

}
