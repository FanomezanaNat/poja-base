package com.company.base.endpoint.rest.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/hello")
  public ResponseEntity<Map<String, String>> hello(
      @RequestParam(defaultValue = "Nathan") String name) {
    return ResponseEntity.ok(Map.of("hello", name));
  }
}
