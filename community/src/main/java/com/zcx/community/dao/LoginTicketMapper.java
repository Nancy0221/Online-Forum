package com.zcx.community.dao;

import com.zcx.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;


@Mapper
@Deprecated
public interface LoginTicketMapper {
    // 用注解拼成sql。values中引用的都是loginTicket中的属性
    @Insert({
            "insert into login_ticket (user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    // 让id属性自动生成
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(@Param("ticket") String ticket);

    /*
     * 演示动态sql
     * @Update({
     *          "<script>", 
     *          "update login_ticket set status = #{status} where ticket = #{ticket} ",
     *          "<if test = \"ticket != null\">",
     *          "and 1 = 1 ",
     *          "</if>",
     *          "</script>"
     * })
     * 
     */
    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);

    //将除本次登录以外的历史登录凭证都置为无效
    //作用：永远只能使用一个浏览器登录账号进行操作
    //事实证明功能冗余，多个浏览器可以登录同一个账号进行操作
    @Update({
            "update login_ticket set status = 1 where user_id = #{userId} and ticket != #{ticket}"
    })
    int updatePreviousLoginTicket(@Param("ticket") String ticket, @Param("userId") int userId);
}
