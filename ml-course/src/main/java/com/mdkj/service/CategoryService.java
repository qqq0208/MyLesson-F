package com.mdkj.service;

import com.mdkj.dto.*;
import com.mdkj.entity.Category;
import com.mdkj.vo.CategorySimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 类别表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface CategoryService extends IService<Category> {
    //添加 - 单条
    boolean insert(CategoryInsertDTO dto);

    //查询 - 单条
    Category select(Long id);

    //查询 - 简单列表
    List<CategorySimpleListVO> simpleList();

    //查询 - 条件分页
    PageVO<Category> page(CategoryPageDTO dto);

    //修改 - 单条
    boolean update(CategoryUpdateDTO dto);

    //删除 - 单条
    boolean delete(Long id);

    //删除 - 多条
    boolean deleteBatch(List<Long> ids);
}
