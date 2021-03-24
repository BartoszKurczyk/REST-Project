package com.example.demo.web;

import com.example.demo.model.SetNameRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping(path = "/name")
    public String getName()
    {
        return "Bartosz";
    }
    @PostMapping(value = "/name")
    public String setName(@Validated @RequestBody(required = true) SetNameRequest setNameRequest)
    {
        return setNameRequest.getName();
    }
}
