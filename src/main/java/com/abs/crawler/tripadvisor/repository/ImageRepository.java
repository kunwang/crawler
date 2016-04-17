package com.abs.crawler.tripadvisor.repository;

import com.abs.crawler.commons.repository.MongoRepository;
import com.abs.crawler.tripadvisor.domain.Image;
import org.springframework.stereotype.Repository;

/**
 * @author hao.wang
 * @since 2016/4/16 23:47
 */
@Repository
public class ImageRepository extends MongoRepository<Image> {
}
