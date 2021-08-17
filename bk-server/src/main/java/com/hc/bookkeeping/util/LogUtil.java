package com.swsc.microservices.common.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.swsc.microservices.common.base.SwscBase;
import com.swsc.microservices.common.config.MyGsonTypeAdapter;


@Component
@SuppressWarnings(value={"unchecked", "rawtypes"})
public class LogUtil extends SwscBase{
	
	private Gson myGson = new GsonBuilder()
            .registerTypeAdapter(new TypeToken<Map>() {
            }.getType(), new MyGsonTypeAdapter()).create();
	
    public String logFilter(String json){
    	if(StringUtils.isBlank(json)) return json;
    	Map<String, Object> result = myGson.fromJson(json, Map.class);
    	return mapFilter(result);
    }
    
    public String mapFilter(Object object){
    	StringBuilder sb = new StringBuilder();
    	sb.append("{");
    	if(object != null){
    		Map<String, Object> result = (Map<String, Object>)object;
	    	Iterator entries = result.entrySet().iterator();
			Map.Entry entry;
			String key ;
			Object value ;
			while (entries.hasNext()) {
				entry = (Map.Entry)entries.next();
				key = (String) entry.getKey();
				value = entry.getValue();
				sb.append("\"" + key + "\"").append(":");
				if(value instanceof List){
					sb.append(listFilter((List)value));
				}else if(value instanceof Map){
					sb.append(mapFilter(value));
				}else{
					sb.append("\"" + replace(key, value) + "\"");
				}
				sb.append(",");
			}
    	}
    	if(sb.lastIndexOf(",") == sb.length() - 1) sb.deleteCharAt(sb.length() - 1);
    	sb.append("}");
    	return sb.toString();
    }
    
    private String listFilter(Object object){
    	StringBuilder sb = new StringBuilder();
    	sb.append("[");
    	if(object != null){
    		List<Object> list = (List)object;
    		if(!list.isEmpty()){
    			for(Object value : list){
    				if(value instanceof Map){
    					sb.append(mapFilter(value));
    				}else if(value instanceof String){
    					sb.append("\"" + replace("", value) + "\"");
    				}
    				sb.append(",");
    			}
        	}
    	}
    	if(sb.lastIndexOf(",") == sb.length() - 1) sb.deleteCharAt(sb.length() - 1);
    	sb.append("]");
    	return sb.toString();
    }
    
    private Object replace(String key, Object value){
    	String logFilterKey = env.getProperty("online.logfilterkey", "");
    	if(StringUtils.isNotBlank(logFilterKey)){
			for(String k : logFilterKey.split(",")){
				if(k.equalsIgnoreCase(key)){
					return "******";
				}
			}
		}
    	if(value != null){
    		String str = value.toString();
			if(StringUtils.isNotBlank(str)){
				str = str.replaceAll("=", "%3D");
				str = str.replaceAll("\\+", "%2B");
				str = str.replaceAll("&", "%26");
				str = str.replaceAll("\\?", "%3F");
			}
			value = str.length() > 1000 ? "大数据过滤" : str;
    	}
    	return value;
    }
}
