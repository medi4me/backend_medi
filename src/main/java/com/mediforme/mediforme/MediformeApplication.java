package com.mediforme.mediforme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class MediformeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediformeApplication.class, args);
	}

}
