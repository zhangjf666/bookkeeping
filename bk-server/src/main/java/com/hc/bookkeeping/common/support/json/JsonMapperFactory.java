package com.hc.bookkeeping.common.support.json;

import com.hc.bookkeeping.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 15:31
 */
@Slf4j
public class JsonMapperFactory {
    public static final String JACKSON_CLASS_TYPE = "jackson";
    public static final String GSON_CLASS_TYPE = "gson";
    public static final String FASTJSON_CLASS_TYPE = "fastjson";
    private static final String CLASS_TYPE_OBJECT_MAPPER = "com.fasterxml.jackson.databind.ObjectMapper";
    private static final String CLASS_TYPE_GSON = "com.google.gson.Gson";
    private static final String CLASS_TYPE_FASTJSON = "com.alibaba.fastjson.JSON";
    public static String JSON_CLASS_TYPE = "json.class.type";

    public static JsonEnum jsonEnum;
    
    public static JsonMapper jsonMapper = null;

    public static JsonMapper createJsonMapper(){
        if(jsonMapper != null){
            return jsonMapper;
        }
        if(jsonEnum == null){
            jsonEnum = parseJsonLib();
        }
        switch (jsonEnum){
            case JACKSON:
                jsonMapper = new JacksonMapper();
                break;
            case GSON:
                jsonMapper = new GsonMapper();
                break;
            case FAST_JSON:
                jsonMapper = new FastJsonMapper();
                break;
        }
        return jsonMapper;
    }
    
    private static JsonEnum parseJsonLib(){
        String jsonType = null;
        try {
            Properties properties = new Properties();
            InputStream in = JsonUtil.class.getClassLoader().getResourceAsStream("application.properties");
            if (in != null) {
                properties.load(in);
                jsonType = properties.getProperty(JSON_CLASS_TYPE);
            } else {
                log.warn("未找到application.properties");
                in = JsonUtil.class.getClassLoader().getResourceAsStream("application.yml");
                if (in != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> propsMap = yaml.loadAs(in, LinkedHashMap.class);
                    propsMap = (Map<String, Object>) propsMap.get("json");
                    if (propsMap != null) {
                        jsonType = String.valueOf(propsMap.get("class-type"));
                    } else {
                        log.warn("application.yml中未配置json.class-type");
                    }
                } else {
                    log.warn("未找到application.yml");
                }
            }

            if (jsonType != null && jsonType.trim().length() > 0) {
                jsonType = jsonType.trim();
                if (Class.forName(CLASS_TYPE_OBJECT_MAPPER) != null && jsonType.equals(JACKSON_CLASS_TYPE)) {
                    log.info("use jackson lib");
                } else if (Class.forName(CLASS_TYPE_GSON) != null && jsonType.equals(GSON_CLASS_TYPE)) {
                    log.info("use gson lib");
                } else if (Class.forName(CLASS_TYPE_FASTJSON) != null && jsonType.equals(FASTJSON_CLASS_TYPE)) {
                    log.info("use fastjson lib");
                } else {
                    log.error("未找到jackson、gson或fastjson的依赖");
                    throw new RuntimeException("未找到jackson、gson或fastjson的依赖");
                }
            } else if (Class.forName(CLASS_TYPE_OBJECT_MAPPER) != null) {
                log.info("use jackson lib");
                jsonType = "jackson";
            } else if (Class.forName(CLASS_TYPE_GSON) != null) {
                log.info("use gson lib");
                jsonType = "gson";
            } else if (Class.forName(CLASS_TYPE_FASTJSON) != null) {
                log.info("use fastjson lib");
                jsonType = "fastjson";
            } else {
                log.error("未找到jackson、gson或fastjson的依赖");
                throw new RuntimeException("未找到jackson、gson或fastjson的依赖");
            }
        } catch (ClassNotFoundException | IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return JsonEnum.getJsonMappeEnum(jsonType);
    }
}
