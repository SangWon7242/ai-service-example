package com.backend.domain.home.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController // @Controller + @ResponseBody
@RequestMapping("/api/home")
public class HomeController {

  @GetMapping
  public String home() {
    return "안녕";
  }
}
