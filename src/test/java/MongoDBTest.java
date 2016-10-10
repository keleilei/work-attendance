import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Mongodb测试类
 * Created by kelei on 2016/10/10.
 */
public class MongoDBTest {
    private MongoClient mongoClient;

    @Before
    public void init(){
//        MongoClientURI uri = new MongoClientURI("mongodb://kelei:ketingjiang@localhost/?authSource=admin");
//        mongoClient = new MongoClient(uri);
        MongoCredential credential = MongoCredential.createCredential("kelei","admin","ketingjiang".toCharArray());
        mongoClient = new MongoClient(new ServerAddress(), Arrays.asList(credential));

    }

    @Test
    public void test(){
        MongoDatabase database = mongoClient.getDatabase("wa");
        MongoCollection<Document> collection = database.getCollection("waRecord");
//        collection.insertOne(new Document("first","aaa").append("second","bbb"));
        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                System.out.println(document.toJson());
            }
        });

    }

    @After
    public void destroy(){
        mongoClient.close();
    }
}
