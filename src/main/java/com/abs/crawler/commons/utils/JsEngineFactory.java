package com.abs.crawler.commons.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.util.concurrent.ExecutionException;

/**
 * @author hao.wang
 * @since 2016/4/16 18:01
 */
public class JsEngineFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsEngineFactory.class);

    private static final LoadingCache<String,ScriptEngine> ENGINE_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<String, ScriptEngine>() {

        @Override
        public ScriptEngine load(String key) throws Exception {
            return create(key);
        }

    });

    private static ScriptEngine create(String jsCodePath) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        FileReader reader = new FileReader(jsCodePath);
        try {
            engine.eval(reader);
        } finally {
            reader.close();
        }
        return engine;
    }

    public static ScriptEngine get(String jsCodePath) {
        try {
            return ENGINE_CACHE.get(jsCodePath);
        } catch (ExecutionException e) {
            LOGGER.warn("script engine get error, path = {}", jsCodePath, e);
        }
        return null;
    }

}
