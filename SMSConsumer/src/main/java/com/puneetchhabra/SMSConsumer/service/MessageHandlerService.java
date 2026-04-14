package com.puneetchhabra.SMSConsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puneetchhabra.SMSConsumer.models.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@Slf4j
public class MessageHandlerService {
    ObjectMapper mapper;
    SmsProcessingService smsProcessingService;

    public MessageHandlerService(ObjectMapper mapper, SmsProcessingService smsProcessingService){
        this.mapper = mapper;
        this.smsProcessingService = smsProcessingService;
    }
    private int sentRequests = 0;
    private LocalTime startTime = LocalTime.now();
    private LocalTime endTime = startTime.plusMinutes(1);

    public void handleSmsRequest(String smsRequestString){
        log.info("SMS Request Received: "+smsRequestString);

        if(sentRequests == 0){ //Rate limiting configuration as per third party limits - (this case, 600/min)
            startTime = LocalTime.now();
            endTime = startTime.plusMinutes(1);
        }
        try{
            JsonNode smsRequestJson = mapper.readTree(smsRequestString);
            SmsRequest smsRequest = mapper.treeToValue(smsRequestJson,SmsRequest.class);
            log.debug("Successfully parsed Consumed Sms Request: {}", smsRequest.toString());
            try{
                smsProcessingService.processSms(smsRequest);
                sentRequests++;
            } catch (Exception exception){
                log.error("Unexpected Exception in SmsProcessingService while processing Sms Request: {}", smsRequest);
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
                log.debug("Rate Limit of this minute reached. Forcing thread to sleep for remaining "+(endTime.getSecond() - LocalTime.now().getSecond())+" seconds");
                Thread.sleep((endTime.getSecond() - LocalTime.now().getSecond()));
                sentRequests = 0;
            } catch (InterruptedException e) {
                log.error("Unexpected error while thread sleeping w.r.t Rate limiting: "+e.toString());
            }
        }

    }
}
