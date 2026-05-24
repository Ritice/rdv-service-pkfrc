package com.rdv_service_pkfrc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class RdvServicePkfrcApplication {

	public static void main(String[] args) {
		SpringApplication.run(RdvServicePkfrcApplication.class, args);
	}

}
