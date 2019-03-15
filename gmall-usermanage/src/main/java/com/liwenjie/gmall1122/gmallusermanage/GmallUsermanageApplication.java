package com.liwenjie.gmall1122.gmallusermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.liwenjie.gmall1122.gmallusermanage.mapper")
@ComponentScan(basePackages = "com.liwenjie.gmall1122")
public class GmallUsermanageApplication {
	public static void main(String[] args) {
		SpringApplication.run(GmallUsermanageApplication.class, args);
	}
}
