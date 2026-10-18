package com.uco.ucopetapi;

import com.uco.ucopetapi.config.ProviderQueueProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProviderQueueProperties.class)
public class UcopetapiApplication {


	public static void main(String[] args) {
		SpringApplication.run(UcopetapiApplication.class, args);
	}

}
