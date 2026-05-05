package org.example.multileanproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
public class MultiLeanProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiLeanProjectApplication.class, args);
        System.out.println("==============================================");
        System.out.println("✅ EduStar Marketplace Backend đã khởi động!");
        System.out.println("📖 Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("🔗 API Docs: http://localhost:8080/v3/api-docs");
        System.out.println("==============================================");
    }
}