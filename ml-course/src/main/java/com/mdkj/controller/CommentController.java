package com.mdkj.controller;

import com.mdkj.dto.CommentInsertDTO;
import com.mdkj.dto.CommentPageDTO;
import com.mdkj.dto.CommentUpdateDTO;
import com.mdkj.vo.CommentSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.mdkj.entity.Comment;
import com.mdkj.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 评论表 控制层。
 *
 * @author YouTao
 * @since v1.0.0
 */
@RestController
@Tag(name = "评论表接口")
@RequestMapping("/api/v1/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "添加 - 评论记录",description = "添加一条评论记录")
    @PostMapping("/insert")
    public boolean insert(@Validated @RequestBody CommentInsertDTO dto){
        return commentService.insert(dto);
    }

    @Operation(summary = "查询 - 单条",description = "按主键查询一条评论记录以及关联的集次")
    @GetMapping("/select/{id}")
    public Comment select(@PathVariable("id") Long id){
        return commentService.select(id);
    }

    @Operation(summary = "查询 - 简单列表", description = "查询全部评论记录，仅返回简单信息")
    @GetMapping("simpleList")
    public List<CommentSimpleListVO> simpleList() {
        return commentService.simpleList();
    }

    @Operation(summary = "查询 - 分页查询", description = "分页查询评论记录")
    @GetMapping("page")
    public PageVO<Comment> page(@Validated CommentPageDTO dto) {
        return commentService.page(dto);
    }

    @Operation(summary = "修改 - 单条修改", description = "按主键修改一条评论记录")
    @PutMapping("update")
    public boolean update(@Validated @RequestBody CommentUpdateDTO dto) {
        return commentService.update(dto);
    }

    @Operation(summary = "删除 - 单条删除", description = "按主键删除一条评论记录")
    @DeleteMapping("delete/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return commentService.delete(id);
    }

    @Operation(summary = "删除 - 批量删除", description = "按主键批量删除评论记录")
    @DeleteMapping("deleteBatch")
    public boolean deleteBatch(@RequestParam("ids") List<Long> ids) {
        return commentService.deleteBatch(ids);
    }

    @Operation(summary = "删除 - 用户评论",description = "按用户主键删除评论记录")
    @DeleteMapping("/deleteByUserId/{userId}")
    public boolean deleteByUserId(@PathVariable("userId") Long userId){
        return commentService.deleteByUserId(userId);
    }

    @Operation(summary = "删除 - 批量用户评论",description = "按用户主键批量删除评论记录")
    @DeleteMapping("/deleteByUserIds")
    public boolean deleteByUserIds(@RequestParam("userIds") List<Long> userIds){
        return commentService.deleteByUserIds(userIds);
    }


}
