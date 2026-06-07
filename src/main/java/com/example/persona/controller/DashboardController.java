package com.example.persona.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard API", description = "APIs for managing dashboard data")
public class DashboardController {

    @GetMapping
    public String getDashboard() {
        return "Hello World";
    }
}
