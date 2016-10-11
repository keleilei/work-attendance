import com.alibaba.fastjson.JSON;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.kelei.wa.entities.WaRecord;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

/**
 * Mongodb测试类
 * Created by kelei on 2016/10/10.
 */
public class MongoDBTest {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    @Before
    public void init(){
//        MongoClientURI uri = new MongoClientURI("mongodb://kelei:ketingjiang@localhost/?authSource=admin");
//        mongoClient = new MongoClient(uri);
        MongoCredential credential = MongoCredential.createCredential("kelei","admin","ketingjiang".toCharArray());
        mongoClient = new MongoClient(new ServerAddress(), Arrays.asList(credential));
        database = mongoClient.getDatabase("wa");
        collection = database.getCollection("waRecord");
    }

    @Test
    public void add(){
        WaRecord record = new WaRecord();
        record.setWaPid("1098");
        record.setWaDate(new Date());
        record.setWaDevice("aaaaa");
        record.setWaState("1");
        record.setWaPid("219");
        record.setWaValidateWay("bbbb");
        record.setWaWeek("星期二");
        Document document = Document.parse(JSON.toJSONString(record));
        collection.insertOne(document);
    }

    @Test
    public void find(){
        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                String json = document.toJson();
                System.out.println(json);
                WaRecord record = JSON.parseObject(json, WaRecord.class);
                System.out.println(record.getWaWeek());
            }
        });
    }

    @Test
    public void remove(){
        collection.deleteOne(new Document("first","aaa"));
    }

    @Test
    public void objectToDocument(){
        WaRecord record = new WaRecord();
        record.setWaPid("1098");
        record.setWaDate(new Date());
        record.setWaDevice("aaaaa");
        record.setWaState("1");
        record.setWaPid("219");
        record.setWaValidateWay("bbbb");
        record.setWaWeek("星期二");
        System.out.println(JSON.toJSONString(record));
        Document document = Document.parse(JSON.toJSONString(record));
        System.out.println(document.toJson());
    }

    @After
    public void destroy(){
        mongoClient.close();
    }
}
