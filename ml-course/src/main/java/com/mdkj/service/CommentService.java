package com.mdkj.service;

import com.mdkj.dto.CommentInsertDTO;
import com.mdkj.dto.CommentPageDTO;
import com.mdkj.dto.CommentUpdateDTO;
import com.mdkj.vo.CommentSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.Comment;

import java.util.List;

/**
 * 评论表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface CommentService extends IService<Comment> {
    //添加 - 单条
    boolean insert (CommentInsertDTO dto);

    //查询 - 单条
    Comment select(Long id);

    List<CommentSimpleListVO> simpleList();
    PageVO<Comment> page(CommentPageDTO dto);
    boolean update(CommentUpdateDTO dto);

    //删除
    boolean delete(Long id);

    //删除多条
    boolean deleteBatch(List<Long> ids);

    //删除 - 用户评论
    boolean deleteByUserId(Long userId);

    //删除 - 批删用户评论
    boolean deleteByUserIds(List<Long> userIds);
}
