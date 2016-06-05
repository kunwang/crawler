package com.abs.crawler.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hao.wang
 * @since 2016/1/8 23:58
 */
public class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
    public static final ObjectMapper OM = new ObjectMapper();
    static {
        OM.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }

    public static Map<String, Object> readValue(String jsonStr) {
        return readValue(jsonStr, StringUtils.EMPTY);
    }

    public static <T> T readValue(String jsonStr, TypeReference<T> typeReference) {
        return readValue(jsonStr, typeReference, StringUtils.EMPTY);
    }

    public static <T> T readValue(String jsonStr, TypeReference<T> typeReference, String additionalReadErrLog) {
        try {
            //noinspection RedundantTypeArguments
            return OM.<T> readValue(jsonStr, typeReference);
        } catch (IOException e) {
            LOGGER.warn("{}, {}", e.getMessage(), additionalReadErrLog, e);
            return null;
        }
    }

    public static <T> T readValue(String jsonStr, Class<T> clazz) {
        return readValue(jsonStr, clazz, StringUtils.EMPTY);
    }

    public static <T> T readValue(String jsonStr, Class<T> clazz, String additionalReadErrLog) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }
        T t = null;
        try {
            t = OM.readValue(jsonStr, clazz);
        } catch (IOException e) {
        }
        return t;
    }

    public static Map<String, Object> readValue(String jsonStr, String additionalReadErrLog) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (StringUtils.isEmpty(jsonStr)) {
            return map;
        }

        try {
            map = OM.readValue(jsonStr, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            LOGGER.warn("{}, {}", e.getMessage(), additionalReadErrLog, e);
        }
        return map;
    }

    public static String writeValueAsString(Object obj) {
        return writeValueAsString(obj, StringUtils.EMPTY);
    }

    public static String writeValueAsString(Object obj, String additionalReadErrLog) {
        String str = StringUtils.EMPTY;

        if (obj == null) {
            return str;
        }

        try {
            str = OM.writeValueAsString(obj);
        } catch (IOException e) {
            LOGGER.warn("{}, {}", e.getMessage(), additionalReadErrLog, e);
        }
        return str;
    }

}
