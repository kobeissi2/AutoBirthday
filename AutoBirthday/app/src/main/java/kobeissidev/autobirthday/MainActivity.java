package kobeissidev.autobirthday;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;

import static kobeissidev.autobirthday.Settings.getLoadChecked;
import static kobeissidev.autobirthday.Settings.getNotificationChecked;
import static kobeissidev.autobirthday.Settings.loadContacts;

public class MainActivity extends Activity {
    DBHandler dbHandler;
    String id = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Permissions permissions = new Permissions(this, MainActivity.this);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        dbHandler = new DBHandler(this);
        boolean isFirst = MyPreferences.isFirst(MainActivity.this);

        if (permissions.getPermission()) {
            if (getLoadChecked(this)) {
                loadContacts(getApplicationContext(), dbHandler);
                Toast.makeText(this, "New Contacts Loaded!", Toast.LENGTH_SHORT).show();
            }
            run();
        }
        if (isFirst) {
            run();
        }

        if (getNotificationChecked(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                runNotificationManager();
            }
            runNotification(notificationManager);
        } else {
            notificationManager.cancelAll();
        }
        runInBackground();
    }

    @TargetApi(26)
    private void runNotificationManager() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        id = "auto_birthday_01";
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.WHITE);
        mChannel.enableVibration(false);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void runNotification(NotificationManager notificationManager) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);
        builder.setSmallIcon(R.drawable.ic_stat_cake);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
        builder.setContentTitle("AutoBirthday is Running!");
        builder.setContentText("Tap to open AutoBirthday.");
        builder.setOngoing(true);
        notificationManager.notify(1, builder.build());

    }

    private void run() {
        if (dbHandler.isDatabaseEmpty()) {
            showNoContactDialog();
        } else {
            displayContacts();
        }
    }

    private void runInBackground(){
        Intent intent = new Intent(MainActivity.this, Message.class);
        PendingIntent smsPendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, smsPendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contact_load_menu:
                menuLoadContacts("Loading Contacts!");
                return true;
            case R.id.contact_reload_menu:
                dbHandler.startOver();
                menuLoadContacts("Table Is Now Empty! Loading Contacts!");
                return true;
            case R.id.action_settings:
                startActivityForResult(new Intent(getApplicationContext(), Settings.class), 0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void menuLoadContacts(String toastText) {
        loadContacts(getApplicationContext(), dbHandler);
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        finish();
        startActivity(getIntent());
    }

    private static class MyPreferences {
        private static final String MY_PREFERENCES = "my_preferences";

        public static boolean isFirst(Context context) {
            final SharedPreferences sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            final boolean first = sharedPreferences.getBoolean("is_first", true);
            if (first) {
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_first", false);
                editor.commit();
            }
            return first;
        }
    }

    private void displayContacts() {
        int count = dbHandler.getContactCount();
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        //Creates the text views and radio buttons programmatically.
        for (int index = 0; index < count; index++) {
            gridLayout.addView(setName(index));
            gridLayout.addView(setBirthday(index));
            gridLayout.addView(setAppToUse(index));
        }
    }

    private TextView setName(int index) {
        TextView nameTextView;
        nameTextView = new TextView(this);
        nameTextView.setTextSize(16);
        nameTextView.setPadding(20, 20, 20, 20);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        nameTextView.setText(dbHandler.getContact(index + 1).get_contactName());
        return nameTextView;
    }

    private TextView setBirthday(int index) {
        TextView birthdayTextView;
        //Contact ID's start at one.
        String birthday = dbHandler.getContact(index + 1).get_birthday();
        //Get the month and make it an integer.
        int birthdayInt = Integer.parseInt(birthday.substring(0, 2));
        //Convert it to a string with the full text name of the month.
        String birthdayMonth = new DateFormatSymbols().getMonths()[birthdayInt - 1];
        //This shows the month by text then the remaining part of the birthday which is the day.
        birthday = birthdayMonth + " " + birthday.substring(3, birthday.length());
        birthdayTextView = new TextView(this);
        birthdayTextView.setTextSize(16);
        birthdayTextView.setPadding(20, 20, 20, 20);
        birthdayTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        birthdayTextView.setText(birthday);
        return birthdayTextView;
    }

    private RadioGroup setAppToUse(int index) {
        RadioGroup typeRadioGroup;
        //SMS and Off buttons.
        RadioButton[] typeRadioButton = new RadioButton[3];
        //Contact ID's start at one.
        String appToUse = dbHandler.getContact(index + 1).get_appToUse();
        typeRadioGroup = new RadioGroup(this);
        //Have the buttons appear horizontally.
        typeRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
        typeRadioButton[0] = new RadioButton(this);
        typeRadioButton[1] = new RadioButton(this);
        typeRadioButton[0].setText(R.string.SMS);
        typeRadioButton[1].setText(R.string.Off);
        typeRadioGroup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //Add the radio buttons to the radio group.
        typeRadioGroup.addView(typeRadioButton[0]);
        typeRadioGroup.addView(typeRadioButton[1]);
        //Set the radio button to the one defaulted at.
        switch (appToUse) {
            case "SMS":
                typeRadioButton[0].setChecked(true);
                typeRadioButton[1].setChecked(false);
                break;
            default:
                typeRadioButton[0].setChecked(false);
                typeRadioButton[1].setChecked(true);
                break;
        }
        //Save the radio button default to the database.
        saveRadioGroup(dbHandler.getContact(index + 1), typeRadioGroup);
        return typeRadioGroup;
    }

    public void saveRadioGroup(final Contact contact, RadioGroup radioGroup) {
        //Check whenever a radio button is selected.
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //The index of the selected radio button.
                int index = group.indexOfChild(findViewById(group.getCheckedRadioButtonId()));
                String appToUseUpdateID = "";
                if (index == 0) {
                    appToUseUpdateID = "SMS";
                } else {
                    appToUseUpdateID = "Off";
                }
                //Updates the database through the ID and the new app to use.
                dbHandler.updateContactAppToUse(contact.get_id(), appToUseUpdateID);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Runs when returns from settings to refresh itself.
        super.onActivityResult(requestCode, resultCode, data);
        finish();
        startActivity(getIntent());
    }

    public void showNoContactDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.sym_contact_card)
                .setTitle("No Contacts!")
                .setMessage("You do not have any contacts. \nWant to go to load contacts?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadContacts(getApplicationContext(), dbHandler);
                        finish();
                        startActivity(getIntent());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Application")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
