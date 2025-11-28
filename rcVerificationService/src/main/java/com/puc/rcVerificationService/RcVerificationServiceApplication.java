package com.puc.rcVerificationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class RcVerificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RcVerificationServiceApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
