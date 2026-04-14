package com.puneetchhabra.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    private RedisTemplate redisTemplate;

    public RedisService(RedisTemplate redisTemplate, ObjectMapper objectMapper){
        this.redisTemplate = redisTemplate;
    }
    public void set(String templateName, int templatePriority){
        try{
            redisTemplate.opsForValue().set(templateName,Integer.toString(templatePriority),1, TimeUnit.DAYS); //refresh Daily
        } catch (Exception exception){
            log.error("Exception setting value to redis. Exception: "+exception);
        }
    }
    public int get(String templateName){
        try{
            Object o = redisTemplate.opsForValue().get(templateName);
            if(o == null){
                log.info("{} template not available in Redis",templateName);
            }
            return Integer.parseInt(o.toString());
        } catch (Exception exception){
            log.error("Exception getting value from redis. Exception: "+exception);
            return -1;
        }
    }
}
