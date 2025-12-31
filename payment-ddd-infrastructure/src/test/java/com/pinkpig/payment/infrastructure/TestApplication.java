package com.pinkpig.payment.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 这是一个专门给单元测试用的“临时启动类”
 * 它的作用就是让 @SpringBootTest 能找到一个加载入口
 */
@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}