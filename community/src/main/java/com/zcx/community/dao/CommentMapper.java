package com.zcx.community.dao;

import com.zcx.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
// 为了满足分页查询的目标，我们要得到评论这个list，还有这个评论的总数
public interface CommentMapper {

    // 要查询某个课程的评论，还是某篇帖子的评论，还是某个评论的评论？
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    List<Comment> selectCommentsByUserId(@Param("entityType") int entityType, @Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByUserId(@Param("entityType") int entityType, @Param("userId") int userId);

    int insertComment(Comment comment);

    int deleteCommentById(@Param("id") int id);

    Comment selectCommentById(@Param("id") int id);
}
