package com.back.catchmate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    @GetMapping
    public String home() {
        return "HELLO WORLD!";
    }
}
