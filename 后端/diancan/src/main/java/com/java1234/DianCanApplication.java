package com.java1234;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.java1234.mapper")
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public class DianCanApplication {

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        System.setProperty("spring.devtools.livereload.enabled", "false");
        SpringApplication.run(DianCanApplication.class, args);
    }

}
