package com.abs.crawler.commons.message;

/**
 * @author hao.wang
 * @since 2016/4/14 21:18
 */
public class CrawlerMessage {

    private static final int RETRY = 3;

    private static final int DELAY_SECONDS = 30;

    private Params params;

    private String  actorClassName;

    private int retry;

    private long retryDelaySeconds;

    private long createdAt;

    public CrawlerMessage(Params params, String  actorClassName, int retry, long retryDelaySeconds){
        this.params = params;
        this.retry = retry;
        this.retryDelaySeconds = retryDelaySeconds;
        this.actorClassName = actorClassName;
        this.createdAt = System.currentTimeMillis();
    }

    public CrawlerMessage(Params params, String  actorClassName, long retryDelaySeconds){
        this(params, actorClassName, RETRY, retryDelaySeconds);
    }

    public CrawlerMessage(Params params, String  actorClassName){
        this(params, actorClassName, RETRY, DELAY_SECONDS);
    }

    public CrawlerMessage(){
    }

    public String getActorClassName() {
        return actorClassName;
    }

    public void setActorClassName(String actorClassName) {
        this.actorClassName = actorClassName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public long getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(long retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "CrawlerMessage{" +
                "createdAt=" + createdAt +
                ", params=" + params +
                ", retry=" + retry +
                ", retryDelaySeconds=" + retryDelaySeconds +
                '}';
    }
}
