package com.ichilamlb.scheduler.constants;

public class Constants {
    // Trạng thái của Job trong Database
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";

    // Tên các Topic Kafka tương ứng với độ ưu tiên (Khớp với Processor code)
    public static final String TOPIC_PRIORITY_1 = "priority-1";
    public static final String TOPIC_PRIORITY_2 = "priority-2";
    public static final String TOPIC_PRIORITY_3 = "priority-3";
}