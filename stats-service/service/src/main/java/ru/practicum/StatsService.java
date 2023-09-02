package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StatsService {
    public static void main(String[] args) {
        System.setProperty("server.port", "9091");
        SpringApplication.run(StatsService.class, args);
    }
}