package edu.cit.resuera.eventhive;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class EventhiveApplication {


	@PostConstruct
    public void init() {
        // Setting the default timezone for the JVM
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
    }
	
	public static void main(String[] args) {
		SpringApplication.run(EventhiveApplication.class, args);
	}

}
