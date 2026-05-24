package com.interniq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InternIqApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternIqApplication.class, args);
		System.out.println("Hello Bachcho!!!");
	}

}
