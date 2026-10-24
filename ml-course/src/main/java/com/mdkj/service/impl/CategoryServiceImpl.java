package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.loader.Loader;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mdkj.dto.*;
import com.mdkj.entity.Category;
import com.mdkj.entity.Course;
import com.mdkj.exception.ServiceException;
import com.mdkj.result.ResultCode;
import com.mdkj.vo.CategorySimpleListVO;
import com.mdkj.vo.CourseSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.mapper.CategoryMapper;
import com.mdkj.service.CategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.mdkj.entity.table.CategoryTableDef.CATEGORY;

/**
 * 类别表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public boolean insert(CategoryInsertDTO dto) {
        String title = dto.getTitle();
        //标题查重
        if (QueryChain.of(mapper)
                .where(CATEGORY.TITLE.eq(title))
                .exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }
        //组装实体类
        Category category = BeanUtil.copyProperties(dto, Category.class);
        category.setInfo(StrUtil.isEmpty(dto.getInfo()) ? "暂无描述" : dto.getInfo());
        category.setCreated(LocalDateTime.now());
        category.setUpdated(LocalDateTime.now());
        //DB添加
        if (mapper.insert(category) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        return true;
    }

    @Override
    public Category select(Long id) {
        Category category = mapper.selectOneById(id);
        if (ObjectUtil.isNull(category)) {
            throw new ServiceException(ResultCode.CATEGORY_NOT_FOUND, id + "号课程类别数据不存在");
        }
        return category;
    }

    @Override
    public List<CategorySimpleListVO> simpleList() {
        return QueryChain.of(mapper)
                .orderBy(CATEGORY.IDX.asc(), CATEGORY.ID.desc())
                .withRelations()
                .listAs(CategorySimpleListVO.class);
    }

    @Override
    public PageVO<Category> page(CategoryPageDTO dto) {
        QueryChain<Category> queryChain = QueryChain.of(mapper)
                .orderBy(CATEGORY.IDX.asc(), CATEGORY.ID.desc());

        // title条件
        String title = dto.getTitle();
        if (ObjectUtil.isNotNull(title)) {
            queryChain.where(CATEGORY.TITLE.like(title));
        }

        // DB分页并转为VO
        Page<Category> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Category> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(CategoryUpdateDTO dto) {
        String title = dto.getTitle();
        Long id = dto.getId();

        // 检查课程类别是否存在
        this.existsById(id);

        // 标题查重
        if (QueryChain.of(mapper)
                .where(CATEGORY.TITLE.eq(title))
                .and(CATEGORY.ID.ne(id))
                .exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }

        // 组装实体类
        Category category = mapper.selectOneById(dto.getId());
        BeanUtil.copyProperties(dto, category);
        category.setUpdated(LocalDateTime.now());
        //DB修改
        if (!UpdateChain.of(category)
                .where(CATEGORY.ID.eq(category.getId()))
                .update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Override
    public boolean delete(Long id) {
        // 检查课程类别是否存在
        this.existsById(id);

        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Override
    public boolean deleteBatch(List<Long> ids) {
        // 检查课程类别是否存在
        if (QueryChain.of(mapper)
                .where(CATEGORY.ID.in(ids))
                .count() < ids.size()) {
            throw new ServiceException(ResultCode.CATEGORY_NOT_FOUND, "至少一个课程类别数据不存在");
        }

        // delete from category where id in (?, ?, ?)
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    //检查列别是否存在
    private void existsById(Long id) {
        if (!QueryChain.of(mapper)
                .where(CATEGORY.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.CATEGORY_NOT_FOUND, id + "号课程类别数据不存在");
        }
    }


}
