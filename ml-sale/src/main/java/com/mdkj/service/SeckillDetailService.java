package com.mdkj.service;

import com.mdkj.dto.SeckillDetailInsertDTO;
import com.mdkj.dto.SeckillDetailPageDTO;
import com.mdkj.dto.SeckillDetailUpdateDTO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.SeckillDetail;

import java.util.List;

/**
 * 秒杀明细表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface SeckillDetailService extends IService<SeckillDetail> {
    //添加秒杀明细
    boolean insert(SeckillDetailInsertDTO dto);
    //查询单条明细
    SeckillDetail select(Long id);
    //查询明细分页
    PageVO<SeckillDetail> page(SeckillDetailPageDTO dto);
    //修改
    boolean update(SeckillDetailUpdateDTO dto);
    //删除
    boolean delete(Long id);
    //批删
    boolean deleteBatch(List<Long> ids);
}
