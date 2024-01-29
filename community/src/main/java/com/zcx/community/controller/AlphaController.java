package com.zcx.community.controller;

import com.zcx.community.service.AlphaService;
import com.zcx.community.util.CommunityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
//@RestController("/alpha")
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    // 不加@ResponseBody，返回的就是文本版的html网页
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        System.out.println(request);
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String value = request.getHeader(headerName);
            System.out.println(headerName + " " + value);
        }
        // get请求路径中的参数
        System.out.println(request.getParameter("code"));
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = null;
        try {
            // 在网页中写东西
            writer = response.getWriter();
            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @RequestMapping(value = "/students", method = RequestMethod.GET)
    @ResponseBody
    // @RequestParam：该变量来自请求路径，是可选的parameter
    public String getStudents(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "1") int limit) {
        return "some students" + current + " " + limit;
    }

    @RequestMapping(value = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    // @PathVariable中的id要与请求路径中的对应，然后把请求路径中的id值赋给int id
    public String getStudent(@PathVariable("id") int id) {
        return "a student" + " " + id;
    }

    @RequestMapping(value = "/student", method = RequestMethod.POST)
    @ResponseBody
    // name和age与请求路径中的名称对应上就可以了
    public String saveString(String name, int age) {
        return "success" + " " + name + " " + age;
    }

    @RequestMapping(value = "/teacher", method = RequestMethod.GET)
    // 注入到模板引擎中
    // 第一个参数：模板中的名称
    // 第二个参数：注入的值
    public ModelAndView getTeacher(ModelAndView modelAndView) {
        modelAndView.addObject("name","ym");
        modelAndView.addObject("age", 21);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    // 和上一个getTeacher大同小异，这种方法更简洁（推荐）
    @RequestMapping(value = "/school", method = RequestMethod.GET)
    // dispatchServlet在调用该方法时发现我们有Model对象，会自动实例化这个对象，然后传给我们
    public String getSchool(Model model) {
        model.addAttribute("name", "贵州大学");
        model.addAttribute("age", 119);
        return "/demo/view";
    }

    // 相应JSON数据（异步请求）
    // Java对象 -> JSON字符串 -> JS对象
    @RequestMapping(value = "/emp", method = RequestMethod.GET) 
    @ResponseBody // 返回JSON字符串
    // dispatchServelet调用此方法时，会自动把map转换为JSON字符串，发送给浏览器
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "ym");
        emp.put("salary", 8000);
        emp.put("age", 21);
        return emp;
    }

    @RequestMapping(value = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> emps = new ArrayList<>();
        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("name", "ym");
        emp1.put("salary", 8000);
        emp1.put("age", 21);
        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("name", "zcx");
        emp2.put("salary", 8000);
        emp2.put("age", 21);
        emps.add(emp1);
        emps.add(emp2);
        return emps;
    }

    // cookie示例
    @RequestMapping("/cookie/set")
    @ResponseBody
    // 把cookie存入response
    public String setCookie(HttpServletResponse response) {
        // 创建cookie
        Cookie cookie = new Cookie("code", CommunityUtils.generateUUID());
        // 设置cookie生效的范围
        cookie.setPath("/community/alpha");
        // 设置cookie的生存时间（10 min）
        cookie.setMaxAge(60 * 10);
        // 发送cookie，把它放进response header中
        response.addCookie(cookie);
        return "set cookie";
    }

    @RequestMapping("/cookie/get")
    @ResponseBody
    // 拿到cookie中key位code的这个cookie
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }

    // session示例
    @RequestMapping("/session/set")
    @ResponseBody
    // HttpSession通过Spring MVC注入
    public String setSession(HttpSession session) {
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    @RequestMapping("/session/get")
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    // AJAX示例
    @RequestMapping(value = "/ajax", method = RequestMethod.POST)
    // 异步请求，给浏览器返回的不是网页，而是字符串
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtils.getJSONString(0, "操作成功！");
    }
}
