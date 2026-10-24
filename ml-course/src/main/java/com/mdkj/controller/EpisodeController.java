package com.mdkj.controller;

import com.mdkj.dto.EpisodeExcelDTO;
import com.mdkj.dto.EpisodeInsertDTO;
import com.mdkj.dto.EpisodePageDTO;
import com.mdkj.dto.EpisodeUpdateDTO;
import com.mdkj.result.Result;
import com.mdkj.util.EasyExcelUtil;
import com.mdkj.vo.EpisodeSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.mdkj.entity.Episode;
import com.mdkj.service.EpisodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 集次表 控制层。
 *
 * @author YouTao
 * @since v1.0.0
 */
@RestController
@Tag(name = "集次表接口")
@RequestMapping("/api/v1/episode")
public class EpisodeController {

    @Autowired
    private EpisodeService episodeService;

    @Operation(summary = "新增 - 单条新增", description = "新增一条集次记录")
    @PostMapping("insert")
    public boolean insert(@Validated @RequestBody EpisodeInsertDTO dto) {
        return episodeService.insert(dto);
    }

    @Operation(summary = "查询 - 单条查询", description = "按主键查询一条集次记录")
    @GetMapping("select/{id}")
    public Episode select(@PathVariable("id") Long id) {
        return episodeService.select(id);
    }

    @Operation(summary = "查询 - 简单列表", description = "查询全部集次记录，仅返回简单信息")
    @GetMapping("simpleList")
    public List<EpisodeSimpleListVO> simpleList() {
        return episodeService.simpleList();
    }

    @Operation(summary = "查询 - 分页查询", description = "分页查询集次记录")
    @GetMapping("page")
    public PageVO<Episode> page(@Validated @ParameterObject EpisodePageDTO dto) {
        return episodeService.page(dto);
    }

    @Operation(summary = "修改 - 单条修改", description = "按主键修改一条集次记录")
    @PutMapping("update")
    public boolean update(@Validated @RequestBody EpisodeUpdateDTO dto) {
        return episodeService.update(dto);
    }

    @Operation(summary = "删除 - 单条删除", description = "按主键删除一条集次记录")
    @DeleteMapping("delete/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return episodeService.delete(id);
    }

    @Operation(summary = "删除 - 批量删除", description = "按主键批量删除集次记录")
    @DeleteMapping("deleteBatch")
    public boolean deleteBatch(@RequestParam("ids") List<Long> ids) {
        return episodeService.deleteBatch(ids);
    }

    @Operation(summary = "上传 - 集次封面", description = "按主键修改集次的封面图片")
    @PostMapping("/uploadVideoCover/{id}")
    public Result<String> uploadVideoCover(@RequestParam("videoCoverFile") MultipartFile videoCoverFile, @PathVariable("id") Long id) {
        return new Result<>(episodeService.uploadVideoCover(videoCoverFile, id));
    }

    @Operation(summary = "修改 - 集次视频", description = "按主键修改集次的视频")
    @PostMapping("/uploadVideo/{id}")
    public Result<String> uploadVideo(@RequestParam("videoFile") MultipartFile videoFile, @PathVariable("id") Long id) {
        return new Result<>(episodeService.uploadVideo(videoFile, id));
    }

    @Operation(summary = "下载 - 数据报表",description = "打印集次相关的Excel报表数据")
    @GetMapping("/excel")
    public void excel(HttpServletResponse response){
        EasyExcelUtil.download(response,"集次统计表",episodeService.getExcelData());
        //return episodeService.getExcelData();
    }
}
