package com.puc.realTimeUpdateService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RealTimeUpdateServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealTimeUpdateServiceApplication.class, args);
	}

}
