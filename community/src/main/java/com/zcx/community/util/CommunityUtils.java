package com.zcx.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtils {

    // static，不初始化CommunityUtils这个object也能调用
    public static String generateUUID() {
        // 不要横线
        return UUID.randomUUID().toString().replace("-", "");
    }

    // MD5加密，key = password + salt
    public static String md5(String key) {
        // 利用StringUtils判空，如果是空格也会被判为空
        if (StringUtils.isBlank(key)) {
            return null;
        }
        // Spring中的工具：把密码加密为16进制的字符串
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    // map：存放了业务数据
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

}
