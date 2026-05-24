package com.questack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuestackApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuestackApplication.class, args);
	}

}
