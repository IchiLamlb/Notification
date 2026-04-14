package com.puneetchhabra.NotificationProcessorPriority3.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puneetchhabra.NotificationProcessorPriority3.models.db.Preference;
import com.puneetchhabra.NotificationProcessorPriority3.models.enums.Channel;
import com.puneetchhabra.NotificationProcessorPriority3.models.requests.EmailRequest;
import com.puneetchhabra.NotificationProcessorPriority3.models.requests.PushNRequest;
import com.puneetchhabra.NotificationProcessorPriority3.models.requests.SmsRequest;
import com.puneetchhabra.NotificationProcessorPriority3.repo.PreferenceRepository;
import com.puneetchhabra.NotificationProcessorPriority3.service.exceptions.PreferenceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import static com.puneetchhabra.NotificationProcessorPriority3.constants.Constants.PRIORITY;

@Service
@Slf4j
public class NotificationHelperService {
    PreferenceRepository preferenceRepository;
    ObjectMapper objectMapper;
    
    public NotificationHelperService(PreferenceRepository preferenceRepository, ObjectMapper objectMapper){
        this.preferenceRepository = preferenceRepository;
        this.objectMapper = objectMapper;
    }
    public boolean isNotificationAllowed_PreferenceCheck(Long userId, Channel channel) {
        Preference channelPreference = preferenceRepository.findByUserIdAndChannel(userId, channel)
                .orElseThrow(() -> {
                    log.error(channel+" preference not found for userId: {}",userId);
                    return new PreferenceNotFoundException(channel+" preference not found for userId: " + userId);
                });
        
        log.info("Preference for userId {} and channel {} is: {}",userId,channel,channelPreference);
        
        if(!channelPreference.isEnabled()) {
            log.info("Preference: Channel is disabled. Preference for userId {} and channel {} is: {}",userId,channel,channelPreference);
            return false;
        }

        //Channel enabled, check priority enabled
        try {
            ArrayList<Integer> allowedPriority = objectMapper.readValue(channelPreference.getAllowedMessagesPriority(),ArrayList.class);
            if(!allowedPriority.contains(PRIORITY)) {
                log.info("Preference: Priority {} is disabled for channel {}. Preference for userId {} and channel {} is: {}",PRIORITY,channel,userId,channel,channelPreference);
                return false;
            }

            //Priority also enabled, check quiet hours
            try {
                JsonNode quietHours = objectMapper.readTree(channelPreference.getQuietHours());
                if(quietHours.get("quietHoursEnabled").asBoolean() && quietHoursActive(quietHours)){
                    log.info("Preference: Quiet hours is active. Preference for userId {} and channel {} is: {}",userId,channel,channelPreference);
                    return false;
                } else {
                    return true;
                }
            } catch (JsonProcessingException e){
                log.error("Error parsing quietHours from String to Json. Channel preference: {}",channelPreference);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing allowedPriority from String to Json. Channel preference: {}",channelPreference);
        }
        return true;
    }



    private boolean quietHoursActive(JsonNode quietHours) {
        String startTimeString = quietHours.get("start").asText();
        String endTimeString = quietHours.get("end").asText();

        LocalTime startTime = LocalTime.parse(startTimeString);
        LocalTime endTime = LocalTime.parse(endTimeString);

        if (startTime.isAfter(endTime)) { //midnight interval
            return LocalTime.now().isAfter(startTime) || LocalTime.now().isBefore(endTime);
        } else {
            return LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime);
        }
    }

    public String getSmsHash(SmsRequest smsRequest, Long userId) {
        String text = PRIORITY+"&"+smsRequest.getMessage()+"&"+smsRequest.getMobileNumber()+"&"+userId.toString();
        return DigestUtils.sha256Hex(text);
    }

    public String getPushNHash(PushNRequest pushNRequest, Long userId) {
        String text = PRIORITY+"&"+pushNRequest.getTitle()+"&"+pushNRequest.getMessage()+"&"+pushNRequest.getAction()+"&"+userId.toString();
        return DigestUtils.sha256Hex(text);
    }

    public String getEmailHash(EmailRequest emailRequest, Long userId) {
        String text = PRIORITY+"&"+emailRequest.getEmailSubject()+"&"+emailRequest.getMessage()+"&"+ Arrays.toString(emailRequest.getEmailAttachments())+"&"+userId.toString();
        return DigestUtils.sha256Hex(text);
    }
}
