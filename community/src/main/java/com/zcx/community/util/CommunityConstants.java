package com.zcx.community.util;

public interface CommunityConstants {

    // 激活成功
    int ACTIVATION_SUCCESS = 0;
    // 重复激活
    int ACTIVATION_REPEAT = 1;
    // 激活失败
    int ACTIVATION_FAILURE = 2;
    // 激活超时
    int ACTIVATION_TIMEOUT = 3;
    // 默认状态的登陆凭证的超时时间
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    // 记住状态的登陆凭证超时时间
    int REMEMBER_EXPIRED_SECONDS =  3600 * 24 * 100;
    // 实体类型：帖子
    int ENTITY_TYPE_POST = 1;
    // 实体类型：评论
    int ENTITY_TYPE_COMMENT = 2;
    // 实体类型：用户
    int ENTITY_TYPE_USER = 3;
    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";
    String TOPIC_PUBLISH = "publish";
    int SYSTEM_USER_ID = 1;

}
