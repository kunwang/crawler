package com.abs.crawler.commons.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/1/10 16:06
 */
public abstract class MongoRepository<T extends MongoCollection> implements BaseRepository<T> {

    private static final String ID = "_id";

    @Resource
    protected MongoTemplate mongoTemplate;

    private Class<T> clz;

    public Class<T> getClz() {
        if(clz == null) {
            synchronized (this) {
                if (clz == null) {
                    ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
                    clz = (Class<T>) type.getActualTypeArguments()[0];
                }
            }
        }
        return clz;
    }

    public T queryOne(Query query) {
        return mongoTemplate.findOne(query, this.getClz());
    }

    public List<T> query(Query query) {
        return mongoTemplate.find(query, this.getClz());
    }


    public T query(String id) {
        return mongoTemplate.findById(id, this.getClz());
    }

    public List<T> query(Collection<String> ids) {
        return mongoTemplate.find(new Query(Criteria.where(ID).in(ids)), this.getClz());
    }

    public List<T> query() {
        return mongoTemplate.findAll(this.getClz());
    }

    public void update(final Collection<T> objects) {
        mongoTemplate.execute(this.getClz(), new CollectionCallback<Void>() {

            public Void doInCollection(DBCollection collection) throws MongoException, DataAccessException {
                BulkWriteOperation bulk = collection.initializeUnorderedBulkOperation();
                MongoConverter mongoConverter = mongoTemplate.getConverter();
                DBObject dbDoc;
                for (T object : objects) {
                    if (!StringUtils.isBlank(object.getId())) {
                        dbDoc = new BasicDBObject();
                        mongoConverter.write(object, dbDoc);
                        bulk.find(new BasicDBObject(ID, object.getId())).replaceOne(dbDoc);
                    }
                }
                bulk.execute();
                return null;
            }
        });
    }

    public void update(T object) {
        mongoTemplate.save(object);
    }

    @Override
    public void saveOrUpdate(T object) {
        mongoTemplate.save(object);
    }

    public void insert(T object) {
        try {
            mongoTemplate.insert(object);
        } catch (Exception ignore) {}
    }

    public void insert(Collection<T> objects) {
        mongoTemplate.insertAll(objects);
    }

    public void remove(String id) {
        mongoTemplate.remove(new Query(Criteria.where(ID).is(id)), this.getClz());
    }
}
