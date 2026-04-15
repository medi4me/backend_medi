package com.mediforme.mediforme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.mediforme.mediforme",  // 기존 main 모듈
		"com.mediforme.lib"         // redis 모듈 추가
})
@EnableJpaAuditing
@EnableRedisRepositories(basePackages = "com.mediforme.lib.redis.repository")
public class MediformeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediformeApplication.class, args);
	}

}
