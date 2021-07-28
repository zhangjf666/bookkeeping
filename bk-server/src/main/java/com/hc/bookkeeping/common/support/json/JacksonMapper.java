package com.hc.bookkeeping.common.support.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hc.bookkeeping.common.utils.SpringContextUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 15:34
 */
public class JacksonMapper implements JsonMapper {

    private ObjectMapper objectMapper;

    public JacksonMapper() {
        this.objectMapper = SpringContextUtil.getBean(ObjectMapper.class);
        if(this.objectMapper == null){
            this.objectMapper = new ObjectMapper()
                    .findAndRegisterModules()
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }

    @Override
    public Map toMap(String json) {
        try {
            return objectMapper.readValue(json, LinkedHashMap.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List toList(String json) {
        try {
            return objectMapper.readValue(json, LinkedList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> toList(String json, Type type) {
        TypeReference<T> typeReference = new TypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        };
        try {
            return (List<T>) objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJsonWithDateFormat(Object object, String dateFormatPattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
        try {
            return objectMapper.writer(sdf).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T toPojo(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public Map convertToMap(Object fromValue) {
        return objectMapper.convertValue(fromValue, LinkedHashMap.class);
    }

    @Override
    public <T> T convertFromMap(Map fromMap, Class<T> toValueType) {
        return objectMapper.convertValue(fromMap, toValueType);
    }
}