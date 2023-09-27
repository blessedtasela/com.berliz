package com.berliz.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping(path = "/social")
public interface SocialRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addProduct(@RequestBody Map<String, String> requestMap);
}
