package com.zcx.community.controller;

import com.zcx.community.annotation.LoginRequired;
import com.zcx.community.entity.Comment;
import com.zcx.community.entity.DiscussPost;
import com.zcx.community.entity.Page;
import com.zcx.community.entity.User;
import com.zcx.community.service.*;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.CommunityUtils;
import com.zcx.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstants {

    @Value(("${community.path.upload}"))
    private String uploadPath;

    @Value(("${community.path.domain}"))
    private String domainPath;

    @Value(("${server.servlet.context-path}"))
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    // MultipartFile：Spring MVC专门用来接收文件的
    // Model：需要向页面返回数据
    public String uploadHeader(MultipartFile headerImage, Model model) {
        User user = hostHolder.getUser();
        if (user == null) {
            model.addAttribute("error", "未登录，无法上传");
            return "/site/setting";
        }
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }
        // 得到用户上传时文件的文件名
        String filename = headerImage.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            model.addAttribute("error", "文件名不能为空");
            return "/site/setting";
        }
        // 获取文件的后缀名的起始index，从“.”处截取
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            model.addAttribute("error", "文件后缀不能为空");
            return "/site/setting";
        }
        // 获取文件的后缀名
        String suffix = filename.substring(index);
        if (StringUtils.isBlank(suffix) || (!".png".equals(suffix) && !".jpg".equals(suffix) && !".jpeg".equals(suffix))) {
            model.addAttribute("error", "文件格式不正确（仅支持.png.jpg.jpeg）");
            return "/site/setting";
        }
        // 生成随机的文件名，以防冲突
        filename = CommunityUtils.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            // 把当前文件中的内容写入到dest这个位置上
            headerImage.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }
        // 更新当前用户头像的路径（web访问路径：http://localhost:8080/community/user/header/xxx.png）
        String headerUrl = domainPath + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    // 返回值是void：它像浏览器返回的不是网页也不是字符串，而是一个二进制的数据
    //      因此我们需要一个流来手动向浏览器输出
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 拿到文件后缀起始index
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return;
        }
        // 拿到文件的后缀
        String suffix = fileName.substring(index + 1);
        if (StringUtils.isBlank(suffix) || (!"png".equals(suffix) && !"jpg".equals(suffix) && !"jpeg".equals(suffix))) {
            return;
        }
        // 响应图片
        response.setContentType("image/" + suffix);
        try (   // 写在这里的变量会被try catch自动加一个finally，然后再调用close()（前提是它有close方法）
                // 获取文件的输入流（为了读文件）
                FileInputStream fileInputStream = new FileInputStream(fileName);
                // 获取输出流
                OutputStream outputStream = response.getOutputStream();
        ) {
            // 声明缓冲区，一次输出1024个字节
            byte[] buffer = new byte[1024];
            int b = 0;
            // fileInputStream.read(buffer)：从文件读到buffer中
            // read()会返回读到的字节数，返回-1就代表没读到
            while ((b = fileInputStream.read(buffer)) != -1) {
                // 从0写到b
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取头像失败，服务器发生异常", e);
        }
    }

    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String confirmPassword, String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword, confirmPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        // 查询该用户
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        // 传入用户的各种信息
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注某个实体
        boolean hasFollowed = false;
        // 没登陆的话默认是false
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

    @GetMapping("/myPosts")
    public String getMyPosts(int userId, Model model, Page page) {
        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(userId));
        page.setPath("/user/myPosts?userId=" + userId);
        User user = userService.findUserById(userId);
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post: list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("counts", page.getRows());
        model.addAttribute("user", user);
        return "/site/my-post";
    }

    @GetMapping("/myReplies")
    public String getMyReplies(int userId, Model model, Page page) {
        page.setLimit(5);
        page.setRows(commentService.findCommentCountByUserId(ENTITY_TYPE_POST, userId));
        page.setPath("/user/myReplies?userId=" + userId);
        User user = userService.findUserById(userId);
        List<Comment> list = commentService.findCommentsByUserId(ENTITY_TYPE_POST, userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> comments = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                map.put("title", discussPostService.findDiscussPostById(comment.getEntityId()).getTitle());
                comments.add(map);
            }
        }
        model.addAttribute("comments", comments);
        model.addAttribute("counts", page.getRows());
        model.addAttribute("user", user);
        return "/site/my-reply";
    }
}
