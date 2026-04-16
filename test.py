import requests
import json
import time

API_URL = "http://localhost:8083/api/send-notification"

# Cấu hình 2 email test
EMAIL_USER_2 = "lelam7c10tp@gmail.com"
EMAIL_USER_3 = "iganomus@gmail.com"

def trigger_test_emails():
    emails_to_send = 5
    print(f"--- Bắt đầu bắn {emails_to_send} request với Template theo Độ ưu tiên ---")

    for i in range(1, emails_to_send + 1):
        # 1. Xác định User và Email (xoay vòng)
        if i % 2 != 0:
            test_user_id = "2"
            test_email = EMAIL_USER_2
        else:
            test_user_id = "3"
            test_email = EMAIL_USER_3

        # 2. Xác định Độ ưu tiên (1, 2, 3)
        priority = (i % 3) + 1

        # 3. Xác định Template theo yêu cầu:
        # Prio 1 -> Welcome Greeting | Prio 2 -> Birthday Wish | Prio 3 -> Hello
        if priority == 1:
            current_template = "Welcome Greeting"
            subject_prefix = "Chào mừng"
        elif priority == 2:
            current_template = "Birthday Wish"
            subject_prefix = "Chúc mừng sinh nhật"
        else:
            current_template = "Hello"
            subject_prefix = "Xin chào"

        notification_id = 6000 + i  # Dùng dải ID mới 6000+

        payload = {
            "notificationId": notification_id,
            "notificationPriority": priority,
            "recipient": {
                "userId": test_user_id,
                "userEmail": test_email
            },
            "content": {
                "usingTemplates": True,
                "templateName": current_template,
                "placeholders": {
                    "name": f"User {test_user_id}",
                    "otp": str(123000 + i)
                },
                "emailSubject": f"{subject_prefix} - Gửi tới {test_email} (P{priority})"
            },
            "channels": ["email"]
        }

        try:
            response = requests.post(
                API_URL,
                data=json.dumps(payload),
                headers={'Content-Type': 'application/json'}
            )
            print(f"[SENT] ID: {notification_id} | Prio: {priority} | Template: {current_template} -> {test_email}")
        except Exception as e:
            print(f"[ERROR] Kết nối thất bại: {e}")

        time.sleep(0.05)

if __name__ == "__main__":
    trigger_test_emails()