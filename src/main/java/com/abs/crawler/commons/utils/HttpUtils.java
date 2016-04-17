package com.abs.crawler.commons.utils;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Param;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.cookie.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/1/8 23:43
 */
public class HttpUtils {

    /**
     * default async http request headers
     */
    public static  FluentCaseInsensitiveStringsMap defaultHeaders() {
        FluentCaseInsensitiveStringsMap headers = new FluentCaseInsensitiveStringsMap();
        headers.add("Accept", "*/*");
        headers.add("Accept-Language", "zh-cn");
        headers.add("Content-type","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36");
        headers.add("UA-CPU", "x86");
        headers.add("Accept-Encoding", "gzip, deflate");
        headers.add("Connection", "close");
        return headers;
    }

    public static Request buildRequest(String url) {
        return buildRequest(HttpRequestType.GET, url, StringUtils.EMPTY);
    }

    public static Request buildRequest(HttpRequestType type, String url, String body) {
        FluentCaseInsensitiveStringsMap headers = defaultHeaders();
        return buildRequest(headers, type, url, body, new ArrayList<Cookie>());
    }

    public static Request buildRequest(HttpRequestType type, String url, List<Param> params, Collection<Cookie> cookies) {
        FluentCaseInsensitiveStringsMap headers = defaultHeaders();
        return buildRequest(headers, type, url, params, cookies);
    }

    public static Request buildRequest(String url, Collection<Cookie> cookies) {
        FluentCaseInsensitiveStringsMap headers = defaultHeaders();
        if (CollectionUtils.isEmpty(cookies)) {
            return buildRequest(url);
        }
        return buildRequest(headers, HttpRequestType.GET, url, StringUtils.EMPTY, cookies);
    }

    /**
     * build default async http request
     */
    public static Request buildRequest(HttpRequestType type, String url, String body,Collection<Cookie> cookies) {
        FluentCaseInsensitiveStringsMap headers = defaultHeaders();
        return buildRequest(headers, type, url, body, cookies);
    }

    /**
     * build async http request
     */
    public static Request buildRequest(FluentCaseInsensitiveStringsMap headers, HttpRequestType type, String url, String body,Collection<Cookie> cookies) {
        RequestBuilder requestBuilder = new RequestBuilder();
        //common attribute
        requestBuilder.setHeaders(headers).setMethod(type.name()).setUrl(url).setBody(body).setCookies(cookies);
        return requestBuilder.build();
    }

    /**
     * build async http request
     */
    public static Request buildRequest(FluentCaseInsensitiveStringsMap headers, HttpRequestType type, String url, List<Param> params, Collection<Cookie> cookies) {
        RequestBuilder requestBuilder = new RequestBuilder();
        //common attribute
        requestBuilder.setHeaders(headers).setMethod(type.name()).setUrl(url).setFormParams(params).setCookies(cookies);
        return requestBuilder.build();
    }

}
