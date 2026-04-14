package com.puneetchhabra.PushNConsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puneetchhabra.PushNConsumer.models.PushNRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@Slf4j
public class MessageHandlerService {
    ObjectMapper mapper;
    PushNProcessingService pushNProcessingService;

    public MessageHandlerService(ObjectMapper mapper, PushNProcessingService pushNProcessingService){
        this.mapper = mapper;
        this.pushNProcessingService = pushNProcessingService;
    }
    private int sentRequests = 0;
    private LocalTime startTime = LocalTime.now();
    private LocalTime endTime = startTime.plusMinutes(1);

    public void handlePushNRequest(String pushNRequestString){
        log.info("Push Notification Request Received: "+pushNRequestString);

        if(sentRequests == 0){ //Rate limiting configuration as per third party limits - (this case, 600/min)
            startTime = LocalTime.now();
            endTime = startTime.plusMinutes(1);
        }
        try{
            JsonNode pushNRequestJson = mapper.readTree(pushNRequestString);
            PushNRequest pushNRequest = mapper.treeToValue(pushNRequestJson,PushNRequest.class);
            log.debug("Successfully parsed Consumed PushN Request: {}", pushNRequest.toString());
            try{
                pushNProcessingService.processPushN(pushNRequest);
                sentRequests++;
            } catch (Exception exception){
                log.error("Unexpected Exception in PushNProcessingService while processing PushN Request: {}", pushNRequest);
                log.error("Exception: {}", exception.toString());
            }
        } catch (JsonProcessingException jsonProcessingException){
            log.error("Error parsing kafka consumed message to JSON. Exception: \n {}", jsonProcessingException.toString());
        }

        //Achieving rate limiting for consumer
        if(LocalTime.now().isAfter(endTime)){
            sentRequests = 0;
        }
        if(sentRequests >= 600){
            try {
                log.debug("Rate Limit of thi minute reached. Forcing thread to sleep for remaining "+(endTime.getSecond() - LocalTime.now().getSecond())+" seconds");
                Thread.sleep((endTime.getSecond() - LocalTime.now().getSecond()));
                sentRequests = 0;
            } catch (InterruptedException e) {
                log.error("Unexpected error while thread sleeping w.r.t Rate limiting: "+e.toString());
            }
        }

    }
}
