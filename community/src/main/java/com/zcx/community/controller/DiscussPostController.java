package com.zcx.community.controller;

import com.zcx.community.entity.*;
import com.zcx.community.event.EventProducer;
import com.zcx.community.service.CommentService;
import com.zcx.community.service.DiscussPostService;
import com.zcx.community.service.LikeService;
import com.zcx.community.service.UserService;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.CommunityUtils;
import com.zcx.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstants {

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping(value = "/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        // 获得用户
        User user = hostHolder.getUser();
        // 判空
        if (user == null) {
            return CommunityUtils.getJSONString(-1, "你还没有登录");
        }
        if (StringUtils.isBlank(title)) {
            return CommunityUtils.getJSONString(-1, "标题不能为空");
        }
        if (StringUtils.isBlank(content)) {
            return CommunityUtils.getJSONString(-1, "内容不能为空");
        }
        // 创建帖子对象
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        // 存到db
        discussPostService.addDiscussPost(discussPost);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        return CommunityUtils.getJSONString(0, "发布成功");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 查到帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 查到作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 某人对某实体的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            // 遍历commentList去构造commentVOList
            for (Comment comment : commentList) {
                // 一个comment一个map
                // VO: View Object
                Map<String, Object> commentVO = new HashMap<>();
                // 评论
                commentVO.put("comment", comment);
                // 作者
                commentVO.put("user", userService.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("likeCount", likeCount);
                // 某人对某实体的点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, comment.getId());
                commentVO.put("likeStatus", likeStatus);

                // 查询该条评论的回复列表
                List<Comment> replayList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replayVOList = new ArrayList<>();
                if (replayList != null){
                    // 遍历replyList去构造replyVOList
                    for (Comment reply :replayList) {
                        Map<String, Object> replyVO = new HashMap<>();
                        // 回复
                        replyVO.put("reply", reply);
                        // 作者
                        replyVO.put("user", userService.findUserById(reply.getUserId()));
                        // 回复的目标用户
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeCount", likeCount);
                        // 某人对某实体的点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, reply.getId());
                        replyVO.put("likeStatus", likeStatus);

                        replayVOList.add(replyVO);
                    }
                }
                // 把replyVOList装进commentVO以防丢失
                commentVO.put("replies", replayVOList);
                // 回复数量
                int replyCount = commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("replyCount", replyCount);
                // 把整个commentVO装进list
                commentVOList.add(commentVO);
            }
        }
        model.addAttribute("comments", commentVOList);
        return "/site/discuss-detail";
    }
}
