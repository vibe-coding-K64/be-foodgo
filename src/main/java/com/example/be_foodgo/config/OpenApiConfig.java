package com.example.be_foodgo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FoodGo API")
                        .description("API cho hệ thống giao đồ ăn FoodGo - Phân hệ Khách hàng")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FoodGo Dev Team")
                                .email("dev@foodgo.com")))
                .tags(List.of(
                        new Tag().name("Cart").description("API quản lý giỏ hàng cho phân hệ Khách hàng")
                ))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Môi trường phát triển"),
                        new Server().url("https://api.foodgo.com").description("Môi trường sản xuất")
                ));
    }
}
