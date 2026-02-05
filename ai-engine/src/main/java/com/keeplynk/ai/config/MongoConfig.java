package com.keeplynk.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final Environment env;

    public MongoConfig(Environment env) {
        this.env = env;
    }

    @Override
    protected String getDatabaseName() {
        String mongoUrl = getMongoUrl();
        System.out.println("=== MongoConfig: Using MongoDB URL: " + mongoUrl);
        
        ConnectionString connString = new ConnectionString(mongoUrl);
        return connString.getDatabase() != null ? connString.getDatabase() : "keeplynk_ai";
    }

    @Override
    public MongoClient mongoClient() {
        String mongoUrl = getMongoUrl();
        ConnectionString connString = new ConnectionString(mongoUrl);
        
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();
        
        return MongoClients.create(settings);
    }
    
    private String getMongoUrl() {
        return env.getProperty("MONGO_URL", 
               env.getProperty("MONGODB_URI", 
               "mongodb://localhost:27017/keeplynk_ai"));
    }
}
