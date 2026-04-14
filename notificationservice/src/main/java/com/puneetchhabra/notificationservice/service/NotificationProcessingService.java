package com.puneetchhabra.notificationservice.service;

import com.puneetchhabra.notificationservice.models.Content;
import com.puneetchhabra.notificationservice.models.NotificationRequest;
import com.puneetchhabra.notificationservice.models.Recipient;
import com.puneetchhabra.notificationservice.models.Template;
import com.puneetchhabra.notificationservice.repo.TemplateRepository;
import com.puneetchhabra.notificationservice.service.exceptions.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class NotificationProcessingService {
    RedisService redisService;
    TemplateRepository templateRepository;

    public NotificationProcessingService(RedisService redisService, TemplateRepository templateRepository){
        this.redisService = redisService;
        this.templateRepository = templateRepository;
    }

    /*
    The method does these checks:
        - Have valid priority -1,1,2 or 3
        - Contains valid channels - sms, email or push
        - Non-empty recipient id
        - Should contain a message body or using a template
     */
    public void validateRequest(NotificationRequest notificationRequest) {
        int priority = notificationRequest.getNotificationPriority();
        if ( priority < -1 || priority == 0 || priority > 3){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Bad priority request.");
        }

        List<String> channels = Arrays.asList(notificationRequest.getChannels());

        boolean valid = channels.stream().anyMatch(c ->
                c.equalsIgnoreCase("sms") ||
                        c.equalsIgnoreCase("push") ||
                        c.equalsIgnoreCase("email")
        );

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad channels request");
        }

        Recipient recipient = notificationRequest.getRecipient();
        if (recipient.getUserId().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Bad request recipient.");
        }

        Content content = notificationRequest.getContent();
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request content.");
        }

        // FIX QUAN TRỌNG NHẤT
        if (content.isUsingTemplates()) {

            if (content.getTemplateName() == null || content.getTemplateName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template required.");
            }

        } else {

            String message = content.getMessage();

            if (message == null || message.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message required.");
            }
        }
    }
    /*
        If not using any template, notification is given medium priority i.e. 2
     */
    public void assignPriority(NotificationRequest notificationRequest) {
        if(!notificationRequest.getContent().isUsingTemplates()){
            notificationRequest.setNotificationPriority(2);
        }else{
            assignPriorityWithTemplate(notificationRequest);
        }
    }

    /*
        Search in redis,
            if found, assign
            else check in DB for template
        If not found appropriate priority through DB as well, notification is given medium priority i.e. 2
     */
    private void assignPriorityWithTemplate(NotificationRequest notificationRequest) {
        String templateName = notificationRequest.getContent().getTemplateName();
        int priority = redisService.get(templateName);
        if(priority == -1){ //not got through redis, try db
            try{
                Template usedTemplate = templateRepository.findByName(templateName)
                        .orElseThrow(() -> new TemplateNotFoundException("Template with name: " + templateName + " Not found"));

                priority = usedTemplate.getTemplatePriority();
                if(priority == 1 || priority == 2 || priority == 3){
                    redisService.set(templateName,priority);
                }
            } catch (TemplateNotFoundException e){
                log.error("{} For notificationRequest: {}", e, notificationRequest);
            } catch (Exception e){
                log.error("Unexpected Exception: {} For notificationRequest: {}", e, notificationRequest);
            }
        }

        if(priority != 1 && priority != 2 && priority != 3){
            notificationRequest.setNotificationPriority(2);
            //If not found appropriate priority, notification is given medium priority i.e. 2
        } else {
            notificationRequest.setNotificationPriority(priority);
        }
    }
}
