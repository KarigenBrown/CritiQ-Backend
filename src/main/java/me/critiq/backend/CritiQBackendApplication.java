package me.critiq.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class CritiQBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CritiQBackendApplication.class, args);
    }

}
