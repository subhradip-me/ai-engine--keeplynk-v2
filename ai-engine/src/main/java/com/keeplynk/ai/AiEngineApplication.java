package com.keeplynk.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiEngineApplication {

	public static void main(String[] args) {
		// Debug: Print MongoDB environment variables
		System.out.println("=== MONGODB ENVIRONMENT VARIABLES DEBUG ===");
		System.out.println("MONGO_URL: " + System.getenv("MONGO_URL"));
		System.out.println("MONGODB_URI: " + System.getenv("MONGODB_URI"));
		System.out.println("===========================================");
		
		SpringApplication.run(AiEngineApplication.class, args);
	}

}
