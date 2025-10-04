package com.sqli.medwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class MedworkAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedworkAppApplication.class, args);
	}

}
