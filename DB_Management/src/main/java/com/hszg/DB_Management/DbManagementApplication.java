package com.hszg.DB_Management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //to be able to run background tasks
@EnableMongoRepositories(basePackages = "com.hszg.DB_Management")
public class DbManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbManagementApplication.class, args);
	}

}
