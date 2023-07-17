package com.ferdousulhaque.logger.loki;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import responseObjs.SimpleResponse;

@RestController
@Slf4j
public class Controller {

    @Autowired
    Service $service;

    @GetMapping("/")
    public SimpleResponse index(){
        SimpleResponse interceptor = $service.getMessage();
        log.info(interceptor.getData().toString());
        return interceptor;
    }
}
