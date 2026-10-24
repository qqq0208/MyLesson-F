package com.mdkj.service;

import com.mdkj.dto.KillDTO;
import com.mdkj.dto.SeckillInsertDTO;
import com.mdkj.dto.SeckillPageDTO;
import com.mdkj.dto.SeckillUpdateDTO;
import com.mdkj.vo.PageVO;
import com.mdkj.vo.SeckillSimpleListVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.Seckill;

import java.util.List;

/**
 * 秒杀表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface SeckillService extends IService<Seckill> {
    boolean insert(SeckillInsertDTO dto);
    Seckill select(Long id);
    List<SeckillSimpleListVO> simpleList();
    PageVO<Seckill> page(SeckillPageDTO dto);
    boolean update(SeckillUpdateDTO dto);
    boolean delete(Long id);
    boolean deleteBatch(List<Long> ids);
    //查询今日前N条秒杀活动
    List<Seckill> near(Long n);
    //秒杀指定商品
    boolean kill(KillDTO dto);
}
