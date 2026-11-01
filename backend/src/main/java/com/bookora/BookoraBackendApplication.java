package com.bookora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookoraBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookoraBackendApplication.class, args);
	}

}
