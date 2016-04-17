package com.abs.crawler.commons.retry;

import com.abs.crawler.commons.constants.MongoCollectionNames;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.repository.MongoCollection;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author hao.wang
 * @since 2016/4/15 00:35
 */
@Document(collection = MongoCollectionNames.DELAY_ITEMS)
public class DelayItem extends MongoCollection implements Delayed {

    private long time;

    private CrawlerMessage message;

    public DelayItem(CrawlerMessage message) {
        this.message = message;
        this.time = (System.currentTimeMillis() / 1000) + message.getRetryDelaySeconds();
        this.message.setRetry(message.getRetry() - 1);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public CrawlerMessage getMessage() {
        return message;
    }

    public void setMessage(CrawlerMessage message) {
        this.message = message;
    }

    public long getDelay(TimeUnit timeUnit) {
        return timeUnit.convert(time - (System.currentTimeMillis() / 1000),TimeUnit.SECONDS);
    }

    public int compareTo(Delayed other) {
        long otherTime = ((DelayItem)other).time;
        if(this.time < otherTime) {
            return -1;
        } else if(this.time > otherTime) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "DelayItem{" +
                "message=" + message +
                ", time=" + time +
                '}';
    }
}
