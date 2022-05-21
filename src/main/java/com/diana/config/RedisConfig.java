package com.diana.config;

import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类 主要是用来设置一下key序列化
 *  使用 redisTemplate 的默认序列器是 JdkSerializationRedisSerializer  不是字符串类型
 *  下面配置类的作用就是将key设置为String类型
 *  这里虽然不用，但是还是记录一下
 *
 *  我们使用模板对象  StringRedisTemplate  可以直接存储和读取String
 *
 */
//@Configuration
public class RedisConfig extends CachingConfigurerSupport {

//    @Bean
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<Object,Object> redisTemplate=new RedisTemplate<>();
        //默认Key的序列化器为  JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
