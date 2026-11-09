package com.uco.ucopetapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan
public class UcopetapiApplication {


	public static void main(String[] args) {
		SpringApplication.run(UcopetapiApplication.class, args);
	}

}
