package com.analyio.analyiobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnalyiobackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyiobackendApplication.class, args);
	}

}
