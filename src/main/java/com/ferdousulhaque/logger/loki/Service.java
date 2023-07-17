package com.ferdousulhaque.logger.loki;

import org.springframework.stereotype.Component;
import responseObjs.SimpleResponse;

import java.util.HashMap;

@org.springframework.stereotype.Service
@Component
public class Service {
    public SimpleResponse getMessage(){
        SimpleResponse response = new SimpleResponse();
        response.status = true;
        response.data = new HashMap<>();
        response.data.put("key", 100);
        return response;
    }
}
