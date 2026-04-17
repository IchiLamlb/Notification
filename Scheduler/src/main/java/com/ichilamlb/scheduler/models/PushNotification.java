package com.ichilamlb.scheduler.models;

import com.ichilamlb.scheduler.models.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class PushNotification {
    private String title;
    private Action action;
    private String body;
    }

