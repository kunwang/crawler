package com.abs.crawler.commons.message;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hao.wang
 * @since 2016/4/14 21:18
 */
public class Params {

    private final Map<String, Object> parameters = new HashMap<String, Object>();

    public Params param(String key, Object value) {
        if (value != null) {
            parameters.put(key, value);
        }
        return this;
    }

    public <T> T get(String key, Class<T> expectedClass) {
        return get(key, expectedClass, null);
    }

    public <T> T get(String key, Class<T> expectedClass, T defaultValue) {
        Object param = parameters.get(key);
        if (param != null && expectedClass != null) {
            if (expectedClass.isAssignableFrom(param.getClass())) {
                return expectedClass.cast(param);
            }
        }
        return defaultValue;
    }

    public boolean remove(String key) {
        return parameters.remove(key) != null;
    }

    public <T> T get(Param<T> param) {
        return this.get(param.getKey(), param.getType());
    }

    public <T> T get(Param<T> param, T defaultValue) {
        return this.get(param.getKey(), param.getType(), defaultValue);
    }

    public Object get(String key) {
        return parameters.get(key);
    }

    public Map<String, Object> toMap() {
        return new HashMap<String, Object>(this.parameters);
    }

    @Override
    public String toString() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.putAll(this.parameters);
        parameters.remove("COOKIES");
        return "Params [parameters=" + parameters + "]";
    }

    public static class Param<T> {
        private final String key;

        private final Class<T> type;

        public Param(String key, Class<T> type) {
            this.key = key;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public Class<T> getType() {
            return type;
        }
    }

}
