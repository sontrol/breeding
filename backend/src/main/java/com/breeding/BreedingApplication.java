package com.breeding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.breeding.mapper")
@EnableTransactionManagement
@EnableScheduling // 开启定时任务支持
public class BreedingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BreedingApplication.class, args);
    }
}
