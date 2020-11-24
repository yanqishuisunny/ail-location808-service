package com.ail.location;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author sujuxnuan
 */
@SpringBootApplication(scanBasePackages = {"com.ail.location"})
@MapperScan(value = {"com.ail.location.dao.gps"})
@EnableMongoRepositories
@EnableScheduling
public class LocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }

}
