package com.abs.crawler.tripadvisor.repository;

import com.abs.crawler.commons.repository.MongoRepository;
import com.abs.crawler.tripadvisor.domain.Review;
import org.springframework.stereotype.Repository;

/**
 * @author hao.wang
 * @since 2016/4/17 20:55
 */
@Repository
public class ReviewRepository extends MongoRepository<Review> {
}
