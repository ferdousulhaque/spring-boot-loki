package com.ferdousulhaque.logger.loki;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import responseObjs.SimpleResponse;

@RestController
@Slf4j
public class Controller {
    @Autowired
    Service $service;

    @PostMapping("/")
    public SimpleResponse index() throws InterruptedException {
        SimpleResponse interceptor = $service.getMessage();
        try{
            Thread.sleep(1000);
        }catch (InterruptedException iex){
            throw new InterruptedException("Thread interrupted");
        }
        return interceptor;
    }

    @GetMapping("/1")
    public SimpleResponse one() throws InterruptedException {
        SimpleResponse interceptor = $service.getMessage();
        try{
            Thread.sleep(1000);
        }catch (InterruptedException iex){
            throw new InterruptedException("Thread interrupted");
        }
        return interceptor;
    }
}
