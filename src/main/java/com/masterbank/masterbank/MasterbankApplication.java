package com.masterbank.masterbank;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class MasterbankApplication {


	public static void main(String[] args) {
		SpringApplication.run(MasterbankApplication.class, args);
	}

}
