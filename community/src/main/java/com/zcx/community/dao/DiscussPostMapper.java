package com.zcx.community.dao;

import com.zcx.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // @Param：用来给变量起别名。如果需要动态的拼sql（在<if>中使用），并且这个方法有且只有这一个参数，这个时候一定要加该注解。
    //      在这个例子中，我们有三个参数，那么也可以不取别名
    // userId = 0的时候就不使用这个userId
    // offset：起始行的行号
    // limit：每页的数据量
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    // 查询一共有多少帖子
    int selectDiscussPostRows(@Param("userId") int userId);

    // 插入帖子
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(@Param("id") int id);

    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);
}
