package com.itgu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class RockApplication {

    public static void main(String[] args) {
        SpringApplication.run(RockApplication.class, args);
    }

}
