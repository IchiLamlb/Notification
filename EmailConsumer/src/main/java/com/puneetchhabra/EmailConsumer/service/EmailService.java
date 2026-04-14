package com.puneetchhabra.EmailConsumer.service;

import com.puneetchhabra.EmailConsumer.models.EmailRequest;
import com.puneetchhabra.EmailConsumer.models.SendEmailResponse;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {
    private String SENDGRID_API_KEY = "";


    public SendEmailResponse sendEmail(EmailRequest emailRequest){

        Email from = new Email("sender_email");
        String subject = emailRequest.getEmailSubject()+" | Scalable Notification System";
        Email to = new Email(emailRequest.getEmailId());
        Content content = new Content("text/plain", emailRequest.getMessage());
        Mail mail = new Mail(from, subject, to, content);

        if(emailRequest.getEmailAttachments().length != 0){
            //Add attachments from respective links
            // OUT OF SCOPE OF THIS PROJECT

            //Sample attachment here
            Attachments attachments2 = new Attachments();
            attachments2.setContent("BwdW");
            attachments2.setType("image/png");
            attachments2.setFilename("banner.png");
            attachments2.setDisposition("inline");
            attachments2.setContentId("Banner");
            mail.addAttachments(attachments2);
        }

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info("Email Request (Notification Id: {}). Response from SendGrid: \n Status Code: {}, Body: {}, Headers: {}",emailRequest.getNotificationId(),response.getStatusCode(),response.getBody(),response.getHeaders());
            return new SendEmailResponse(response.getStatusCode(), response.getBody());
        } catch (IOException ex) {
            log.error("Something went wrong with SendGrid. Exception: {}",ex.toString());
            return new SendEmailResponse(500, "IO Exception occurred in Email Service-SendGrid");
        }

    }
}
