package com.piixl.media_service.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;


@Repository
public class PostRepository {
    private final MongoCollection<Document> posts;
    private MongoDatabase database;

    public PostRepository(MongoDatabase database) {
        this.database = database;
        this.posts = this.database.getCollection("posts");
        this.posts.createIndex(new Document("userId", 1));
    }

    public Document savePost(String userId, String username, String description, String fileName) {
        Document post = new Document()
                .append("userId", userId)
                .append("username", username)
                .append("description", description)
                .append("createdAt", new java.util.Date())
                .append("fileName", fileName);
        posts.insertOne(post);

        return post;
    }

}
