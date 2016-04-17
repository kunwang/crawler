package com.abs.crawler.commons.repository;

import com.abs.crawler.commons.retry.DelayItem;
import org.springframework.stereotype.Repository;

/**
 * @author hao.wang
 * @since 2016/4/15 00:30
 */
@Repository
public class DelayRepository extends MongoRepository<DelayItem> {
}
