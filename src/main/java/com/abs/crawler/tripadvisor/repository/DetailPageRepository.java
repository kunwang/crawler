package com.abs.crawler.tripadvisor.repository;

import com.abs.crawler.commons.repository.MongoRepository;
import com.abs.crawler.tripadvisor.domain.DetailPage;
import org.springframework.stereotype.Repository;

/**
 * @author hao.wang
 * @since 2016/4/16 22:20
 */
@Repository
public class DetailPageRepository extends MongoRepository<DetailPage> {
}
