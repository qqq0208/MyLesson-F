package com.mdkj.controller;

import com.mdkj.dto.CategoryInsertDTO;
import com.mdkj.dto.CategoryPageDTO;
import com.mdkj.dto.CategoryUpdateDTO;
import com.mdkj.entity.Category;
import com.mdkj.vo.CategorySimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.mdkj.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 类别表 控制层。
 *
 * @author YouTao
 * @since v1.0.0
 */
@RestController
@Tag(name = "类别表接口")
@RequestMapping("/api/v1/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "新增 - 单条数据",description = "新增一条类别记录")
    @PostMapping("/insert")
    public boolean insert(@Validated @RequestBody CategoryInsertDTO dto){
        return categoryService.insert(dto);
    }

    @Operation(summary = "查询 - 单条查询", description = "按主键查询一条类别记录")
    @GetMapping("select/{id}")
    public Category select(@PathVariable("id") Long id) {
        return categoryService.select(id);
    }

    @Operation(summary = "查询 - 简单列表", description = "查询全部类别记录，仅返回简单信息")
    @GetMapping("simpleList")
    public List<CategorySimpleListVO> simpleList() {
        return categoryService.simpleList();
    }

    @Operation(summary = "查询 - 分页查询", description = "分页查询类别记录")
    @GetMapping("page")
    public PageVO<Category> page(@Validated @ParameterObject CategoryPageDTO dto) {
        return categoryService.page(dto);
    }

    @Operation(summary = "修改 - 单条修改", description = "按主键修改一条类别记录")
    @PutMapping("update")
    public boolean update(@Validated @RequestBody CategoryUpdateDTO dto) {
        return categoryService.update(dto);
    }

    @Operation(summary = "删除 - 单条删除", description = "按主键删除一条类别记录")
    @DeleteMapping("delete/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return categoryService.delete(id);
    }

    @Operation(summary = "删除 - 批量删除", description = "按主键批量删除类别记录")
    @DeleteMapping("deleteBatch")
    public boolean deleteBatch(@RequestParam("ids") List<Long> ids) {
        return categoryService.deleteBatch(ids);
    }
}
