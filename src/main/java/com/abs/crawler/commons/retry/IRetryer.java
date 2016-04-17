package com.abs.crawler.commons.retry;

import com.abs.crawler.commons.message.CrawlerMessage;

/**
 * @author hao.wang
 * @since 2016/4/14 22:35
 */
public interface IRetryer {

    void retry(CrawlerMessage message);

}
