package com.abs.crawler.commons.crawler;

import akka.actor.UntypedActor;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.retry.Retryer;
import com.abs.crawler.commons.utils.AsyncHttpClientFactory;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.slf4j.Logger;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author hao.wang
 * @since 2016/4/14 21:17
 */
public abstract class AbstractCrawler extends UntypedActor {

    @Resource
    protected Retryer retryer;

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof CrawlerMessage) {
            CrawlerMessage message = (CrawlerMessage) o;
            Request request = this.buildRequest(message.getParams());
            if (request == null) {
                this.getLogger().info("request is null");
                return;
            }
            AsyncHttpClient asyncHttpClient = AsyncHttpClientFactory.get(this.getHttpClientKey());
            asyncHttpClient.executeRequest(request, new CompletionHandler(this, message));
        } else {
            unhandled(o);
        }
    }

    public abstract Request buildRequest(Params params);

    public abstract String getHttpClientKey();

    public abstract void onSuccess(CrawlerMessage message, Response response) throws IOException;

    public void onException(CrawlerMessage message, Throwable t) {
        this.getLogger().warn("crawler exception, message = {}", message, t);
        retryer.retry(message);
    }

    public abstract Logger getLogger();

    private class CompletionHandler extends AsyncCompletionHandler<Void> {

        private AbstractCrawler crawler;

        private CrawlerMessage message;

        public CompletionHandler(AbstractCrawler crawler, CrawlerMessage message) {
            this.crawler = crawler;
            this.message = message;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            try {
                crawler.onSuccess(this.message, response);
            } catch (Exception e) {
                crawler.getLogger().warn("process crawler response error, message = {}", this.message, e);
            }
            return null;
        }

        @Override
        public void onThrowable(Throwable t) {
            try{
                crawler.onException(this.message, t);
            } catch (Exception e) {
                crawler.getLogger().warn("process crawler exception error, message = {}", this.message, e);
            }
        }
    }


}
