package com.redmath.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WelcomeController {

    @GetMapping("/welcome")
    public String welcomeWithQueryParams(@RequestParam(name = "name",defaultValue = "Guest") String name){
        return "Welcome " + name + " to Spring Boot";
    }

    @GetMapping("/welcome/{name}")
    public String welcomeByName(@PathVariable String name){
        return "Welcome " + name + " to Spring Boot";
    }

    @GetMapping("/info")
    public Map<String,String> version() {
        return Map.of(
                "status","success",
                "framework","Spring Boot 4.0.7",
                "runtime","Java 25"
        );
    }
}