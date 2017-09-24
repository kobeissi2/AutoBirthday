package kobeissidev.autobirthday;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import java.util.Date;

public class SMS extends Activity {
    private static String timeToSend;
    private static boolean timeBool;
    private static String messageToSend;
    private static boolean messageBool;
    private static boolean isUser24Hour;

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

    public static void SMS(Context context) {
        setBirthdayPreferences(context.getApplicationContext());
        setTimePreferences(context.getApplicationContext());
        setEmptyTime(context);
        setEmptyMessage();

        isTimeToSendMessage(context);
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
        isUser24Hour = DateFormat.is24HourFormat(context.getApplicationContext());
        String time = timeToSend.substring(19, timeToSend.length() - 1);
        String newTimeString = android.text.format.DateFormat.getTimeFormat(context).format(new Date());
        return time.equals(newTimeString);
    }
}
