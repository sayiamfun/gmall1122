package com.liwenjie.gmall1122.gmallcartserviceone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.liwenjie.gmall1122")
@MapperScan(basePackages = "com.liwenjie.gmall1122.gmallcartserviceone.mapper")
public class GmallCartServiceOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallCartServiceOneApplication.class, args);
	}
}
