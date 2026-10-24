package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mdkj.dto.CommentInsertDTO;
import com.mdkj.dto.CommentPageDTO;
import com.mdkj.dto.CommentUpdateDTO;
import com.mdkj.entity.User;
import com.mdkj.exception.ServiceException;
import com.mdkj.feign.UserFeign;
import com.mdkj.result.Result;
import com.mdkj.result.ResultCode;
import com.mdkj.vo.CommentSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.entity.Comment;
import com.mdkj.mapper.CommentMapper;
import com.mdkj.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.mdkj.entity.table.CommentTableDef.COMMENT;

/**
 * 评论表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserFeign userFeign;

    @Override
    public boolean insert(CommentInsertDTO dto) {
        //获取dto中用户ID
        Long fkUserId = dto.getFkUserId();
        //远程调用 获取用户信息
        Result<User> userResult = userFeign.select(fkUserId);
        if (ObjectUtil.isNull(userResult)) {
            throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "用户微服务远程调用失败 请联系管理员");
        }
        //取出User信息
        User user = userResult.getData();
        if (ObjectUtil.isNull(user)) {
            throw new ServiceException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        //组装实体
        Comment comment = BeanUtil.copyProperties(dto, Comment.class);
        comment.setNickname(user.getNickname());
        comment.setAvatar(user.getAvatar());
        comment.setProvince(user.getProvince());
        comment.setCreated(LocalDateTime.now());
        comment.setUpdated(LocalDateTime.now());
        //DB添加
        if (mapper.insert(comment) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        return true;
    }

    @Override
    public Comment select(Long id) {
        //指定联查字段
        RelationManager.addQueryRelations("episode");
        //关联查询
        Comment comment = mapper.selectOneWithRelationsById(id);
        if(ObjectUtil.isNull(comment)){
            throw new ServiceException(ResultCode.COMMENT_NOT_FOUND,"评论不存在");
        }
        return comment;
    }

    @Override
    public List<CommentSimpleListVO> simpleList() {
        return QueryChain.of(mapper)
                .listAs(CommentSimpleListVO.class);
    }

    @Override
    public PageVO<Comment> page(CommentPageDTO dto) {
        // 指定联查字段
        RelationManager.addQueryRelations("episode");
        QueryChain<Comment> queryChain = QueryChain.of(mapper);

        // pid条件
        Long pid = dto.getPid();
        if (ObjectUtil.isNotNull(pid)) {
            queryChain.where(COMMENT.PID.eq(pid));
        }

        // fkEpisodeId条件
        Long episodeId = dto.getFkEpisodeId();
        if (ObjectUtil.isNotNull(episodeId)) {
            queryChain.where(COMMENT.FK_EPISODE_ID.eq(episodeId));
        }

        // fkUserId条件
        Long userId = dto.getFkUserId();
        if (ObjectUtil.isNotNull(userId)) {
            queryChain.where(COMMENT.FK_USER_ID.eq(userId));
        }

        // DB分页并转为VO
        Page<Comment> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Comment> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(CommentUpdateDTO dto) {
        Long id = dto.getId();
        Long fkUserId = dto.getFkUserId();

        // 检查评论是否存在
        this.existsById(id);

        // 组装实体类
        Comment comment = BeanUtil.copyProperties(dto, Comment.class);
        Result<User> userResult = userFeign.select(fkUserId);
        if (ObjectUtil.isNull(userResult)) {
            throw new ServiceException(ResultCode.OPEN_FEIGN_ERROR, "用户微服务远程调用失败，请联系管理员。");
        }
        User user = userResult.getData();
        if (ObjectUtil.isNull(user)) {
            throw new ServiceException(ResultCode.USER_NOT_FOUND, fkUserId + "号用户数据不存在");
        }
        comment.setNickname(user.getNickname());
        comment.setAvatar(user.getAvatar());
        comment.setProvince(user.getProvince());
        comment.setUpdated(LocalDateTime.now());
        if (!UpdateChain.of(comment)
                .where(COMMENT.ID.eq(comment.getId()))
                .update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Override
    public boolean delete(Long id) {
        //检查评论是否存在
        this.existsById(id);
        //删除全部子评论
        UpdateChain.of(mapper)
                .where(COMMENT.PID.eq(id))
                .remove();
        //删除本评论
        if(mapper.deleteById(id) <= 0){
            throw new ServiceException(ResultCode.MYSQL_ERROR,"数据库删除失败");
        }

        return false;
    }

    @Override
    public boolean deleteBatch(List<Long> ids) {
        //检查评论是否存在
        if(QueryChain.of(mapper)
                .where(COMMENT.ID.in(ids))
                .count() < ids.size()){
            throw new ServiceException(ResultCode.COMMENT_NOT_FOUND,"至少一个评论不存在");
        }
        //删除全部子评论
        UpdateChain.of(mapper)
                .where(COMMENT.PID.in(ids))
                .remove();
        //删除本评论
        if(mapper.deleteBatchByIds(ids) <= 0){
            throw new ServiceException(ResultCode.MYSQL_ERROR,"数据库删除失败");
        }
        return true;
    }

    @Override
    public boolean deleteByUserId(Long userId) {
        //查询该用户的所有评论ID列表
        List<Long> commentIds = QueryChain.of(mapper)
                .select(COMMENT.ID)
                .where(COMMENT.FK_USER_ID.eq(userId))
                .listAs(Long.class);
        //如果没有评论记录 直接返回true
        if(CollUtil.isEmpty(commentIds)){
            return true;
        }

        //根据评论ID列表 查询 子评论ID列表
        List<Long> subCommentIds = QueryChain.of(mapper)
                .select(COMMENT.ID)
                .where(COMMENT.PID.in(commentIds))
                .listAs(Long.class);

        //如果子评论id列表不为空 将子评论列表 整合进父评论列表（本质上就是一张评论表）
        if(CollUtil.isNotEmpty(subCommentIds)){
            //将子评论的集合 加入进父评论的集合
            commentIds.addAll(subCommentIds);
        }

        //删除组合后的评论ID数据
        if(mapper.deleteBatchByIds(commentIds) <= 0){
            throw new ServiceException(ResultCode.MYSQL_ERROR,"数据库删除失败");
        }
        return true;
    }

    @Override
    public boolean deleteByUserIds(List<Long> userIds) {
        //查询该用户的所有评论ID列表
        List<Long> commentIds = QueryChain.of(mapper)
                .select(COMMENT.ID)
                .where(COMMENT.FK_USER_ID.in(userIds))
                .listAs(Long.class);
        //如果没有评论记录 直接返回true
        if(CollUtil.isEmpty(commentIds)){
            return true;
        }
        //根据评论ID列表 查询 子评论ID列表
        List<Long> subCommentIds = QueryChain.of(mapper)
                .select(COMMENT.ID)
                .where(COMMENT.PID.in(commentIds))
                .listAs(Long.class);

        //如果子评论id列表不为空 将子评论列表 整合进父评论列表（本质上就是一张评论表）
        if(CollUtil.isNotEmpty(subCommentIds)){
            //将子评论的集合 加入进父评论的集合
            commentIds.addAll(subCommentIds);
        }

        //删除组合后的评论ID数据
        if(mapper.deleteBatchByIds(commentIds) <= 0){
            throw new ServiceException(ResultCode.MYSQL_ERROR,"数据库删除失败");
        }
        return true;
    }

    private void existsById(Long id) {
        // select count(*) from comment where id = ?
        if (!QueryChain.of(mapper)
                .where(COMMENT.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.COMMENT_NOT_FOUND, id + "号评论数据不存在");
        }
    }
}
