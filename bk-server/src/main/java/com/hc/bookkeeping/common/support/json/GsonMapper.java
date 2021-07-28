package com.hc.bookkeeping.common.support.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 15:34
 */
public class GsonMapper implements JsonMapper {

    private Gson gson;

    public GsonMapper(){
        gson = new GsonBuilder()
                .setLenient()
                // 解决gson序列化时出现整型变为浮点型的问题
                .registerTypeAdapter(new TypeToken<Map<Object, Object>>() {
                        }.getType(), (JsonDeserializer<Map<Object, Object>>) (jsonElement, type, jsonDeserializationContext) -> {
                            Map<Object, Object> map = new LinkedHashMap<>();
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                Object obj = entry.getValue();
                                if (obj instanceof JsonPrimitive) {
                                    map.put(entry.getKey(), ((JsonPrimitive) obj).getAsString());
                                } else {
                                    map.put(entry.getKey(), obj);
                                }
                            }
                            return map;
                        }
                )
                .registerTypeAdapter(new TypeToken<List<Object>>() {
                        }.getType(), (JsonDeserializer<List<Object>>) (jsonElement, type, jsonDeserializationContext) -> {
                            List<Object> list = new LinkedList<>();
                            JsonArray jsonArray = jsonElement.getAsJsonArray();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                if (jsonArray.get(i).isJsonObject()) {
                                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                                    list.addAll(entrySet);
                                } else if (jsonArray.get(i).isJsonPrimitive()) {
                                    list.add(jsonArray.get(i));
                                }
                            }
                            return list;
                        }
                )
                .create();
    }

    @Override
    public Map toMap(String json) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<Map<Object, Object>>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public List toList(String json) {
        TypeToken<List<Object>> typeToken = new TypeToken<List<Object>>() {
        };
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public <T> List<T> toList(String json, Type type) {
        return gson.fromJson(json, type);
    }

    @Override
    public String toJsonString(Object object) {
        return gson.toJson(object);
    }

    @Override
    public String toJsonWithDateFormat(Object object, String dateFormatPattern) {
        gson = new GsonBuilder().setDateFormat(dateFormatPattern).create();
        return gson.toJson(object);
    }

    @Override
    public <T> T toPojo(String json, Class<T> valueType) {
        return gson.fromJson(json, valueType);
    }

    @Override
    public Map convertToMap(Object fromValue) {
        TypeToken<Map<Object, Object>> typeToken = new TypeToken<Map<Object, Object>>() {
        };
        String json = gson.toJson(fromValue);
        return gson.fromJson(json, typeToken.getType());
    }

    @Override
    public <T> T convertFromMap(Map fromMap, Class<T> toValueType) {
        String json = gson.toJson(fromMap);
        return gson.fromJson(json, toValueType);    }
}
