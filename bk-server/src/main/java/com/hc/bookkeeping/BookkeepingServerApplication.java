package com.hc.bookkeeping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@MapperScan({"com.hc.bookkeeping.modules.*.mapper"})
public class BookkeepingServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookkeepingServerApplication.class, args);
    }
}
