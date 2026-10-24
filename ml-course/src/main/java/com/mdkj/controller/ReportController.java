package com.mdkj.controller;

import com.mdkj.dto.ReportInsertDTO;
import com.mdkj.dto.ReportPageDTO;
import com.mdkj.dto.ReportUpdateDTO;
import com.mdkj.vo.PageVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.mdkj.entity.Report;
import com.mdkj.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

/**
 * 举报表 控制层。
 *
 * @author YouTao
 * @since v1.0.0
 */
@RestController
@Tag(name = "举报表接口")
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(summary = "新增 - 单条新增", description = "新增一条举报记录")
    @PostMapping("insert")
    public boolean insert(@Validated @RequestBody ReportInsertDTO dto) {
        return reportService.insert(dto);
    }

    @Operation(summary = "查询 - 单条查询", description = "按主键查询一条举报记录")
    @GetMapping("select/{id}")
    public Report select(@PathVariable("id") Long id) {
        return reportService.select(id);
    }

    @Operation(summary = "查询 - 分页查询", description = "分页查询举报记录")
    @GetMapping("page")
    public PageVO<Report> page(@Validated ReportPageDTO dto) {
        return reportService.page(dto);
    }

    @Operation(summary = "修改 - 单条修改", description = "按主键修改一条举报记录")
    @PutMapping("update")
    public boolean update(@Validated @RequestBody ReportUpdateDTO dto) {
        return reportService.update(dto);
    }

    @Operation(summary = "删除 - 单条删除", description = "按主键删除一条举报记录")
    @DeleteMapping("delete/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return reportService.delete(id);
    }

    @Operation(summary = "删除 - 批量删除", description = "按主键批量删除举报记录")
    @DeleteMapping("deleteBatch")
    public boolean deleteBatch(@RequestParam("ids") List<Long> ids) {
        return reportService.deleteBatch(ids);
    }

    @Operation(summary = "删除 - 用户举报", description = "按用户ID删除用户的举报记录")
    @DeleteMapping("deleteByUserId/{userId}")
    public boolean deleteBatch(@PathVariable("userId") Long userId) {
        return reportService.deleteByUserId(userId);
    }

    @Operation(summary = "删除 - 批量用户举报", description = "按多个用户ID删除一条举报记录")
    @DeleteMapping("deleteByUserIds")
    public boolean deleteByUserIds(@RequestParam("userIds") List<Long> userIds) {
        return reportService.deleteByUserIds(userIds);
    }
}
