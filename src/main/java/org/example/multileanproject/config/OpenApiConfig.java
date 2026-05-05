package org.example.multileanproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()

                // Thông tin dự án
                .info(new Info()
                        .title("EduStar E-Learning Marketplace API")
                        .version("1.0.0")
                        .description(
                                "API Documentation cho hệ thống bán khóa học trực tuyến EduStar. " +
                                        "Bao gồm các endpoint quản lý khóa học, danh mục, thanh toán và người dùng."
                        )

                        // Thông tin liên hệ
                        .contact(new Contact()
                                .name("EduStar Development Team")
                                .email("support@edustar.vn")
                                .url("https://edustar.vn")
                        )

                        // License
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                )

                // Danh sách server
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),

                        new Server()
                                .url("https://api.edustar.vn")
                                .description("Production Server")
                ));
    }
}