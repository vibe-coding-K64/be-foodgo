package com.example.be_foodgo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FoodGo API")
                        .description("API cho he thong giao do an FoodGo - Phan he Khach hang")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FoodGo Dev Team")
                                .email("dev@foodgo.com")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhap token JWT nhan duoc sau khi dang nhap/dang ky thanh cong. VD: Bearer eyJhbGciOiJIUzI1NiJ9...")))
                .tags(List.of(
                        new Tag().name("Xac thuc (Auth)").description("API xac thuc tai khoan, dang nhap, dang ky, va khoi phuc mat khau"),
                        new Tag().name("Cart").description("API quan ly gio hang cho phan he Khach hang")
                ))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Moi truong phat trien"),
                        new Server().url("https://api.foodgo.com").description("Moi truong san xuat")
                ));
    }
}
