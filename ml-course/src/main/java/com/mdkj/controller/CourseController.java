package com.mdkj.controller;

import com.mdkj.dto.CourseInsertDTO;
import com.mdkj.dto.CoursePageDTO;
import com.mdkj.dto.CourseUpdateDTO;

import com.mdkj.es.CourseDoc;
import com.mdkj.result.Result;
import com.mdkj.vo.CourseSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.mdkj.entity.Course;
import com.mdkj.service.CourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 课程表 控制层。
 *
 * @author YouTao
 * @since v1.0.0
 */
@RestController
@Tag(name = "课程表接口")
@RequestMapping("/api/v1/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Operation(summary = "添加 - 添加课程", description = "添加一条课程记录")
    @PostMapping("/insert")
    public boolean insert(@Validated @RequestBody CourseInsertDTO dto) {
        return courseService.insert(dto);
    }

    @Operation(summary = "查询 - 单条数据", description = "按主键查询单条课程 关联查询 类别 季次")
    @GetMapping("/select/{id}")
    public Course select(@PathVariable("id") Long id) {
        return courseService.select(id);
    }

    @Operation(summary = "查询 - 简单列表", description = "查询全部课程记录、只返回简单信息")
    @GetMapping("/simpleList")
    public List<CourseSimpleListVO> simpleList() {
        return courseService.simpleList();
    }

    @Operation(summary = "查询 - 条件分页", description = "分页查询课程记录")
    @GetMapping("/page")
    public PageVO<Course> page(@Validated @ParameterObject CoursePageDTO dto) {
        return courseService.page(dto);
    }

    @Operation(summary = "修改 - 单条数据", description = "按主键修改课程记录")
    @PutMapping("/update")
    public boolean update(@Validated @RequestBody CourseUpdateDTO dto) {
        return courseService.update(dto);
    }

    @Operation(summary = "删除 - 单条数据", description = "按主键删除课程记录")
    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return courseService.delete(id);
    }

    @Operation(summary = "删除 - 多条数据", description = "按主键删除多条课程记录")
    @DeleteMapping("/deleteBatch")
    public boolean deleteBatch(@RequestParam("ids") List<Long> ids) {
        return courseService.deleteBatch(ids);
    }

    @Operation(summary = "上传 - 课程封面", description = "上传课程封面图片")
    @PostMapping("/uploadCover/{id}")
    public Result<String> uploadCover(@RequestParam("coverFile") MultipartFile coverFile, @PathVariable("id") Long id) {
        return new Result<>(courseService.uploadCover(coverFile, id));
    }

    @Operation(summary = "上传 - 课程摘要", description = "上传课程封面摘要")
    @PostMapping("/uploadSummary/{id}")
    public Result<String> uploadSummary(@RequestParam("summaryFile") MultipartFile summaryFile, @PathVariable("id") Long id) {
        return new Result<>(courseService.uploadSummary(summaryFile, id));
    }

    @Operation(summary = "搜索 - 课程列表", description = "按课程名称或作者名称分页搜索课程信息")
    @GetMapping("search")
    public PageVO<CourseDoc> search(@Validated @ParameterObject CoursePageDTO dto) {
        return courseService.search(dto);
    }

}
