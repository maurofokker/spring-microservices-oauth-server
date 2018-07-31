package com.maurofokker.poc.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableResourceServer
@RestController
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringMicroservicesOauthServerApplication {

    @RequestMapping("/resource/endpoint")
    @PreAuthorize("hasRole('ADMIN')")
    public String endpoint() {
        return "resource protected by the resource server";
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringMicroservicesOauthServerApplication.class, args);
    }
}
