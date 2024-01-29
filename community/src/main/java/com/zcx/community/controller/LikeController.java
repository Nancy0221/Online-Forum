package com.zcx.community.controller;

import com.zcx.community.entity.Event;
import com.zcx.community.entity.User;
import com.zcx.community.event.EventProducer;
import com.zcx.community.service.LikeService;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.CommunityUtils;
import com.zcx.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstants {

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/like")
    // 异步请求不需要返回网页
    @ResponseBody
    // 点赞
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtils.getJSONString(-1, "没有登录", null);
        }
        // 调用service来点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 获取点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 获取点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 把这两个值封装一下，传给页面
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        if (likeStatus == 1 && user.getId() != entityUserId) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }
        // 正确返回0，提示返回null，并把数据返回给页面
        return CommunityUtils.getJSONString(0, null, map);
    }

}
