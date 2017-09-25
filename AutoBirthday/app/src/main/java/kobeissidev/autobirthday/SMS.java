package kobeissidev.autobirthday;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SMS extends Activity {
    private static String timeToSend;
    private static String messageToSend;
    private static String contactName;
    private static boolean timeBool;
    private static boolean messageBool;
    private static DBHandler dbHandler;

    public static void SMSService(Context context) {
        dbHandler = new DBHandler(context);

        setBirthdayPreferences(context.getApplicationContext());
        setTimePreferences(context.getApplicationContext());
        setEmptyTime(context);
        setEmptyMessage();

        if (isDayToSendMessage(context) && isTimeToSendMessage(context)) {
            final String contactNumber = getContactNumber(contactName);
            sendSMS(context, contactNumber);
        }
    }

    private static void setBirthdayPreferences(Context context) {
        SharedPreferences sharedBirthday = context.getSharedPreferences("birthdayPrefs", Context.MODE_PRIVATE);
        messageToSend = sharedBirthday.getString("birthdayText", "Happy Birthday!");
        messageBool = sharedBirthday.getBoolean("birthdayChecked", false);
    }

    private static void setTimePreferences(Context context) {
        SharedPreferences sharedTime = context.getSharedPreferences("timePrefs", Context.MODE_PRIVATE);
        timeToSend = sharedTime.getString("timeText", "Time to send text: 00:00.");
        timeBool = sharedTime.getBoolean("timeChecked", false);
    }

    private static void setEmptyTime(Context context) {
        final boolean isUser24Hour = DateFormat.is24HourFormat(context.getApplicationContext());
        if (!timeBool) {
            if (isUser24Hour) {
                timeToSend = "Time to send text: 00:00.";
            } else {
                timeToSend = "Time to send text: 12:00 AM.";
            }
        }
    }

    private static void setEmptyMessage() {
        if (!messageBool) {
            messageToSend = "Happy Birthday!";
        }
    }

    private static boolean isTimeToSendMessage(Context context) {
        String time = timeToSend.substring(19, timeToSend.length() - 1);
        String currentTime = android.text.format.DateFormat.getTimeFormat(context).format(new Date());
        return time.equals(currentTime);
    }

    private static boolean isDayToSendMessage(Context context) {
        List<Contact> contacts = dbHandler.getAllContacts();
        String currentDate = android.text.format.DateFormat.getDateFormat(context).format(new Date());
        String[] currentDateSplit = currentDate.split("/");
        boolean isDayToSend = false;

        for (Contact contact : contacts) {
            String contactMonth = contact.get_birthday().substring(0, 2);
            String contactDay = contact.get_birthday().substring(3, contact.get_birthday().length());
            String currentMonth = currentDateSplit[0];
            String currentDay = currentDateSplit[1];

            if (currentMonth.length() == 1) {
                currentMonth = "0" + currentMonth;
            }
            if (currentDay.length() == 1) {
                currentDay = "0" + currentDay;
            }
            if (contactMonth.equals(currentMonth) && contactDay.equals(currentDay)) {
                contactName = contact.get_contactName();
                isDayToSend = true;
            }
        }
        return isDayToSend;
    }

    private static String getContactNumber(String contactName) {
        List<Contact> contacts = dbHandler.getAllContacts();
        String contactNumber = "";
        for (Contact contact : contacts) {
            if (contact.get_contactName().equals(contactName) && contact.get_appToUse().equals("SMS")) {
                contactNumber = contact.get_phoneNumber();
            }
        }
        return contactNumber;
    }

    private static void sendSMS(Context context, String phoneNumber) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, messageToSend, null, null);
    }


}
