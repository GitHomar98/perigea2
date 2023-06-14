package com.ceruti.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.ceruti.mongodb.repository.ItemRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.ceruti.mongodb.repository")
@PropertySource(value = { "classpath:api.properties" })
public class MongodbApplication {

	@Autowired
	ItemRepository itemrepository;
	
	public static void main(String[] args) {
		SpringApplication.run(MongodbApplication.class, args);
	}
}
