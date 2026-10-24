package com.mdkj.controller;

import com.mdkj.dto.SeckillDetailInsertDTO;
import com.mdkj.dto.SeckillDetailPageDTO;
import com.mdkj.dto.SeckillDetailUpdateDTO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.mdkj.entity.SeckillDetail;
import com.mdkj.service.SeckillDetailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 秒杀明细表 控制层。
 *
 * @author YouTao
 * @since v1.0.0
 */
@RestController
@Tag(name = "秒杀明细表接口")
@RequestMapping("/api/v1/seckillDetail")
public class SeckillDetailController {

    @Autowired
    private SeckillDetailService seckillDetailService;

    @Operation(summary = "新增 - 单条",description = "新增一条秒杀明细记录")
    @PostMapping("/insert")
    public boolean insert(@Validated @RequestBody SeckillDetailInsertDTO dto){
        return seckillDetailService.insert(dto);
    }

    @Operation(summary = "查询 - 单条" ,description = "按主键查询一个明细记录")
    @GetMapping("/select/{id}")
    public SeckillDetail select(@PathVariable("id") Long id){
        return seckillDetailService.select(id);
    }

    @Operation(summary = "查询 - 分页" ,description = "分页条件查询明细记录")
    @GetMapping("/page")
    public PageVO<SeckillDetail> page(@Validated @ParameterObject SeckillDetailPageDTO dto){
        return seckillDetailService.page(dto);
    }

    @Operation(summary = "修改 - 单条修改", description = "按主键修改一条秒杀明细记录")
    @PutMapping("update")
    public boolean update(@Validated @RequestBody SeckillDetailUpdateDTO dto) {
        return seckillDetailService.update(dto);
    }

    @Operation(summary = "删除 - 单条删除", description = "按主键删除一条秒杀明细记录")
    @DeleteMapping("delete/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return seckillDetailService.delete(id);
    }

    @Operation(summary = "删除 - 批量删除", description = "按主键批量删除秒杀明细记录")
    @DeleteMapping("deleteBatch")
    public boolean deleteBatch(@RequestParam("ids") List<Long> ids) {
        return seckillDetailService.deleteBatch(ids);
    }
}
