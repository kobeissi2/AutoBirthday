package kobeissidev.autobirthday;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static kobeissidev.autobirthday.MainActivity.runNotificationManager;
import static kobeissidev.autobirthday.MyJobService.getIntent;

public class Message extends Service {
    private String timeToSend;
    private String messageToSend;
    private ArrayList<String> contactName;
    private String contactNumber;
    private boolean timeBool;
    private boolean messageBool;
    private DBHandler dbHandler;

    private void setBirthdayPreferences() {
        SharedPreferences sharedBirthday = getSharedPreferences("birthdayPrefs", Context.MODE_PRIVATE);
        messageToSend = sharedBirthday.getString("birthdayText", "Happy Birthday!");
        messageBool = sharedBirthday.getBoolean("birthdayChecked", false);
    }

    private void setTimePreferences() {
        SharedPreferences sharedTime = getSharedPreferences("timePrefs", Context.MODE_PRIVATE);
        timeToSend = sharedTime.getString("timeText", "Time to send text: 00:00.");
        timeBool = sharedTime.getBoolean("timeChecked", false);
    }

    private void setEmptyTime() {
        final boolean isUser24Hour = DateFormat.is24HourFormat(getApplicationContext());
        if (!timeBool) {
            if (isUser24Hour) {
                timeToSend = "Time to send text: 00:00.";
            } else {
                timeToSend = "Time to send text: 12:00 AM.";
            }
        }
    }

    private void setEmptyMessage() {
        if (!messageBool) {
            messageToSend = "Happy Birthday!";
        }
    }

    private boolean isTimeToSendMessage() {
        String time = timeToSend.substring(19, timeToSend.length() - 1);
        String currentTime = android.text.format.DateFormat.getTimeFormat(this).format(new Date());
        return time.equals(currentTime);
    }

    private boolean isDayToSendMessage() {
        List<Contact> contacts = dbHandler.getAllContacts();
        String currentDate = android.text.format.DateFormat.getDateFormat(this).format(new Date());
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
                contactName.add(contact.get_contactName());
            }
        }
        if (!contactName.isEmpty()) {
            isDayToSend = true;
        }
        return isDayToSend;
    }

    private String getContactNumber(String contactName) {
        List<Contact> contacts = dbHandler.getAllContacts();
        String contactNumber = "";
        for (Contact contact : contacts) {
            if (contact.get_contactName().equals(contactName) && contact.get_appToUse().equals("SMS")) {
                contactNumber = contact.get_phoneNumber();
            }
        }
        return contactNumber;
    }

    private void sendSMS(String contactName) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contactNumber, null, messageToSend, null, null);
        Toast.makeText(this, "Sent Birthday Message To " + contactName + "!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        contactName = new ArrayList<>();
        dbHandler = new DBHandler(this);
        setBirthdayPreferences();
        setTimePreferences();
        setEmptyTime();
        setEmptyMessage();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Boolean messageSent = false;
        String message = "Tap to open AutoBirthday!";

        if (isDayToSendMessage() && isTimeToSendMessage()) {
            if (!contactName.isEmpty()) {
                for (String contact : contactName) {
                    contactNumber = getContactNumber(contact);
                    sendSMS(contact);
                    messageSent = true;
                }
            } else {
                Log.w("Contact", "Contact Empty!");
            }
        }
        if (messageSent) {
            message = "Birthday Message sent!";
        }

        NotificationManager notificationManager = runNotificationManager(getApplicationContext());
        notificationManager.cancelAll();
        notificationManager.notify(1, notificationSet(getIntent(this), message));

        return START_STICKY;
    }

    private Notification notificationSet(Intent intent, String text) {

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(),MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder builder = new Notification.Builder(this, "auto_birthday_01")
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_cake)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                    .setContentIntent(pendingIntent);

            Notification notification = builder.build();
            startForeground(1, notification);
            return notification;
        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_cake)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                    .setContentIntent(pendingIntent);

            Notification notification = builder.build();
            startForeground(1, notification);
            return notification;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
