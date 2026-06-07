package com.example.persona.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog")
@Tag(name = "Catalog API", description = "APIs for managing catalog data")
public class CatalogController {

    @GetMapping
    public String getCatalog() {
        return "Hello World";
    }
}
