package com.example.ubitricitychallange.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/api/health")
    public String ping(){
        return "OK";
    }
}
