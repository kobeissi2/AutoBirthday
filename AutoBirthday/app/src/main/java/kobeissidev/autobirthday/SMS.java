package kobeissidev.autobirthday;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

public class SMS extends Activity {
    private static String timeToSend = "";
    private static boolean timeBool;
    private static String messageToSend = "";
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

    //TEMPORARY: TODO: Make SMS service and add timer to send on set time.
    public static void test(Context context) {
        isUser24Hour = DateFormat.is24HourFormat(context.getApplicationContext());
        setBirthdayPreferences(context.getApplicationContext());
        setTimePreferences(context.getApplicationContext());
        if (!messageBool) {
            messageToSend = "Happy Birthday!";
        }
        if (!timeBool) {
            if (isUser24Hour) {
                timeToSend = "Time to send text: 00:00";
            }
            else{
                timeToSend = "Time to send text: 12:00 AM";
            }
        }
        Log.e("TTS", timeToSend);
        Log.e("MTS", messageToSend);
    }
}
