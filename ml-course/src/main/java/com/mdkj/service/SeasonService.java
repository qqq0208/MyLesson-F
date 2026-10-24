package com.mdkj.service;

import com.mdkj.dto.SeasonInsertDTO;
import com.mdkj.dto.SeasonPageDTO;
import com.mdkj.dto.SeasonUpdateDTO;
import com.mdkj.vo.PageVO;
import com.mdkj.vo.SeasonSimpleListVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.Season;

import java.util.List;

/**
 * 季次表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface SeasonService extends IService<Season> {
    //添加 - 单条
    boolean insert(SeasonInsertDTO dto);

    //查询 - 单条
    Season select(Long id);

    //查询 - 简单列表
    List<SeasonSimpleListVO> simpleList();

    //查询 - 条件分页
    PageVO<Season> page(SeasonPageDTO dto);

    //修改 - 单条
    boolean update(SeasonUpdateDTO dto);

    //删除 - 单条
    boolean delete(Long id);

    //删除 - 多条
    boolean deleteBatch(List<Long> ids);
}
