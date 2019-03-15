package com.liwenjie.gmall1122.gmallcartweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.liwenjie.gmall1122")
public class GmallCartWebApplication {
	public static void main(String[] args) {
		SpringApplication.run(GmallCartWebApplication.class, args);
	}
}
