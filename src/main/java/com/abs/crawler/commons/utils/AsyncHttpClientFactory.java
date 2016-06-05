package com.abs.crawler.commons.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

/**
 * @author hao.wang
 * @since 2016/1/8 23:26
 */
public class AsyncHttpClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClientFactory.class);

    private static final LoadingCache<String,AsyncHttpClient> ASYNC_HTTP_CLIENT_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<String, AsyncHttpClient>() {

        @Override
        public AsyncHttpClient load(String key) throws Exception {
            return create();
        }

    });

    private interface DefaultAsyncHttpClientParams {

        String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)";

        int DEFAULT_MAX_CONNECTIONS_PER_HOST = 1000;

        int DEFAULT_MAX_CONNECTIONS = 1000;

        int DEFAULT_MAX_CONNECTION_TIME_OUT = 60000;

        int DEFAULT_MAX_REQUEST_TIME_OUT = 60000;

        int DEFAULT_POOLING_CONNECTION_IDLE_TIME_OUT = 60000;
    }


    /**
     * return exist async http client if has no create one put into map and return
     */
    public static AsyncHttpClient get(String key) {
        try {
            return ASYNC_HTTP_CLIENT_CACHE.get(key);
        } catch (ExecutionException e) {
            throw  new RuntimeException("get async http client error,key=" + key);
        }
    }

    /**
     * create async http client
     */
    private static AsyncHttpClient create() {
        return create(DefaultAsyncHttpClientParams.DEFAULT_MAX_CONNECTIONS
                , DefaultAsyncHttpClientParams.DEFAULT_MAX_CONNECTIONS_PER_HOST
                , DefaultAsyncHttpClientParams.DEFAULT_MAX_CONNECTION_TIME_OUT
                , DefaultAsyncHttpClientParams.DEFAULT_MAX_REQUEST_TIME_OUT);
    }




    /**
     * create AsyncHttpClient and set config
     *
     * @param maxConnectionsTotal   this client max connections
     * @param maxConnectionsPerHost   the max connections count per host at the same time
     * @param connectionTimeOutInMs   wait when connecting to a remote host
     * @param requestTimeoutInMs   the maximum time in millisecond  waits until the response is completed
     * @return AsyncHttpClient
     */
    public static AsyncHttpClient create(int maxConnectionsTotal, int maxConnectionsPerHost, int connectionTimeOutInMs,
                                         int requestTimeoutInMs) {
        AsyncHttpClientConfig config = null;
        try {
            config = new AsyncHttpClientConfig.Builder()
                    .setMaxConnectionsPerHost(maxConnectionsPerHost)  //
                    .setMaxConnections(maxConnectionsTotal)  //
                    .setConnectTimeout(connectionTimeOutInMs)  //
                    .setRequestTimeout(requestTimeoutInMs) //
                    .setAllowPoolingConnections(true)  //
                    .setPooledConnectionIdleTimeout(DefaultAsyncHttpClientParams.DEFAULT_POOLING_CONNECTION_IDLE_TIME_OUT)  //
                    .setCompressionEnforced(true) //
                    .setUserAgent(DefaultAsyncHttpClientParams.DEFAULT_USER_AGENT) //
                    .setIOThreadMultiplier(2) // 注意：这个参数是用来说明nio worker的数量为cpu数乘以此数，你的processResponseThreadPool需要大于他们的乘积
                    .setSSLContext(createIgnoreVerifySSL())
                    .setAcceptAnyCertificate(true)
                    .build();
        } catch (Exception e) {
            LOGGER.warn("async http client initial error", e);
        }
        return create(config);
    }

    public static AsyncHttpClient create(AsyncHttpClientConfig config) {
        return new AsyncHttpClient(config);
    }

    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager easyTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);
        return sslcontext;
    }
}
