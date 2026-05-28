package com.example.be_foodgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class BeFoodgoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeFoodgoApplication.class, args);
	}

}
