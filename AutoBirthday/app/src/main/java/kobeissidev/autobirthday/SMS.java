package kobeissidev.autobirthday;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Date;
import java.util.List;

public class SMS extends Activity {
    private static String timeToSend;
    private static boolean timeBool;
    private static String messageToSend;
    private static boolean messageBool;
    private static boolean isUser24Hour;
    private static DBHandler dbHandler;

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

    public static void SMSService(Context context) {
        dbHandler = new DBHandler(context);

        setBirthdayPreferences(context.getApplicationContext());
        setTimePreferences(context.getApplicationContext());
        setEmptyTime(context);
        setEmptyMessage();
        if (isTimeToSendMessage(context) && isDayToSendMessage(context)) {
            sendSMS();
        }
    }

    private static void setEmptyTime(Context context) {
        isUser24Hour = DateFormat.is24HourFormat(context.getApplicationContext());
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
            String[] contactDaySplit = contact.get_birthday().split("-");
            String contactDay;
            if (contactDaySplit.length != 2) {
                contactDay = contactDaySplit[1];
            } else {
                contactDay = contactDaySplit[0];
            }
            String currentMonth = currentDateSplit[0];

            if (currentMonth.length() == 1) {
                currentMonth = "0" + currentMonth;
            }

            String currentDay = currentDateSplit[1];

            if (currentDay.length() == 1) {
                currentDay = "0" + currentDay;
            }

            if (contactMonth.equals(currentMonth) && contactDay.equals(currentDay)) {
                isDayToSend = true;
            }
        }
        return isDayToSend;
    }

    private static void sendSMS() {

    }
}
