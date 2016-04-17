package com.abs.crawler.commons.exporter;

import com.abs.crawler.commons.repository.MongoCollection;
import org.springframework.stereotype.Service;

/**
 * @author hao.wang
 * @since 2016/4/14 22:18
 */
@Service
public class MongoExporter implements IExporter {

//    @Resource
//    private MongoRepository<MongoCollection> mongoRepository;

    @Override
    public void save(Object object) {
        MongoCollection document = (MongoCollection) object;
//        mongoRepository.saveOrUpdate(document);
    }
}
