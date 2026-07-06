package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Java Maven App is running successfully on AWS EKS with Jenkins and AWS ECR!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is healthy";
    }
}
