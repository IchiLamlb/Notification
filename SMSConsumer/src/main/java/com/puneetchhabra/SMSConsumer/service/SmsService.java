package com.puneetchhabra.SMSConsumer.service;

import com.puneetchhabra.SMSConsumer.models.SendSmsResponse;
import com.puneetchhabra.SMSConsumer.models.SmsRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    public final String ACCOUNT_SID = "";
    public final String AUTH_TOKEN = "";

    public SendSmsResponse sendSms(SmsRequest smsRequest){

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        try{
            Message message = Message
                    .creator(
                            new PhoneNumber("+1" + smsRequest.getMobileNumber()),
                            new PhoneNumber("+6419343470"),
                            smsRequest.getMessage()
                    )
                    .create();

            log.info("Sms Request (Notification Id: {}). Response from Twilio: \n Status: {}, Body: {}, Twilio_Message: {}",smsRequest.getNotificationId(),message.getStatus(),message.getBody(), message.toString());
            return new SendSmsResponse(200,"Sid: "+message.getSid()+" Body: "+message.getBody());
        }catch (Exception exception){
            log.error("Something went wrong with Twilio. Exception: {}",exception.toString());
            return new SendSmsResponse(500, "Exception occurred in Sms Service-Twilio");
        }
    }
}
