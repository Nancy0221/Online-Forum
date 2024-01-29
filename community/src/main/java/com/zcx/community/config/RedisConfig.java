package com.zcx.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    // 把哪个对象装配到容器中，就返回哪个对象
    // RedisConnectionFactory自动被Spring装配
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory); // redisTemplate有了工厂后，就具备了访问数据库的能力
        // 我们得到的都是Java类型的数据，因此要指定每个类型的数据存到Redis数据库中 序列化的方式，也就是数据转换的方式
        // 设置key的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式，它可以为值，集合，列表，因此把它序列化成json，这样也好恢复
        redisTemplate.setValueSerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        // 昨晚设置后要触发一下，让它生效
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
