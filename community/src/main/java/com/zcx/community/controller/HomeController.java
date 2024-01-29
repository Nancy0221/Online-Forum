package com.zcx.community.controller;

import com.zcx.community.entity.DiscussPost;
import com.zcx.community.entity.Page;
import com.zcx.community.service.DiscussPostService;
import com.zcx.community.service.LikeService;
import com.zcx.community.service.UserService;
import com.zcx.community.util.CommunityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.*;

@Controller
public class HomeController implements CommunityConstants {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping(value = "/index")
    // 这个方法的参数往往都是由SpringMVC的dispatchServlet帮我们初始化的，page的数据也是它给我们注入进来的，它也会自动把page装进model
    //      所以，在thymeleaf中可以直接访问Page对象中的数据
    public String getIndexPage(Model model, Page page) {
        // 设置分页
        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        // 封装post和user
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post: list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

}
