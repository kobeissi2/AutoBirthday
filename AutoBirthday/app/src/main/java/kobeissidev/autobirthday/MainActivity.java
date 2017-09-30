package kobeissidev.autobirthday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;

import static kobeissidev.autobirthday.Settings.getLoadChecked;
import static kobeissidev.autobirthday.Settings.getThemeSelected;
import static kobeissidev.autobirthday.Settings.loadContacts;


public class MainActivity extends BaseActivity {

    DBHandler dbHandler;
    boolean granted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Permissions permissions = new Permissions(this, MainActivity.this);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        dbHandler = new DBHandler(this);
        final boolean isFirst = MyPreferences.isFirst(MainActivity.this);
        granted = permissions.getPermission();

        if (granted) {

            if (getLoadChecked(this)) {

                loadContacts(getApplicationContext(), dbHandler);

                Toast.makeText(this, "New Contacts Loaded!", Toast.LENGTH_SHORT).show();

            }

            run();

        }

        if (isFirst) {

            run();

        }

        runNotificationManager(getApplicationContext());

        runNotification(getApplicationContext(), notificationManager);

        runInBackground();

        setThemes();

    }

    private void setThemes() {

        if (getActionBar() != null) {

            getActionBar().setTitle(" AutoBirthday");
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setLogo(getDrawable(R.drawable.ic_stat_cake));
            getActionBar().setDisplayUseLogoEnabled(true);

        }

        String theme = getThemeSelected(getApplicationContext());

        switch (theme) {

            case "Material Dark":

                setTheme(R.style.MaterialBlack);

                break;

            case "Material Red":

                setTheme(R.style.MaterialAutoBirthday);

                break;

            default:

                setTheme(R.style.MaterialBlue);

                break;

        }

    }

    public static NotificationManager runNotificationManager(Context context) {

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "auto_birthday_01";
        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            int importance = NotificationManager.IMPORTANCE_MIN;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NotificationChannel mChannel = new NotificationChannel(id, name, importance);

                mChannel.setDescription(description);
                mChannel.enableVibration(false);
                mNotificationManager.createNotificationChannel(mChannel);

            }

        }

        return mNotificationManager;

    }

    public static void runNotification(Context context, NotificationManager notificationManager) {

        Notification notification;
        String message = "Tap to open AutoBirthday!";
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder builder = new Notification.Builder(context, "auto_birthday_01")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_cake)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                    .setContentIntent(pendingIntent);

            notification = builder.build();

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_cake)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                    .setContentIntent(pendingIntent);

            notification = builder.build();

        }

        notificationManager.notify(1, notification);

    }

    private void run() {

        if (dbHandler.isDatabaseEmpty()) {

            showNoContactDialog();

        } else {

            displayContacts();

        }

    }

    private void runInBackground() {

        Utility.scheduleJob(this);

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
                menuLoadContacts("Table Is Now Empty!\nLoading Contacts!");
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

        private static boolean isFirst(Context context) {

            final SharedPreferences sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            final boolean first = sharedPreferences.getBoolean("is_first", true);

            if (first) {

                final SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putBoolean("is_first", false);
                editor.apply();

            }

            return first;

        }

    }

    private void displayContacts() {

        int count = dbHandler.getContactCount();
        GridLayout gridLayout = findViewById(R.id.gridLayout);

        gridLayout.setPadding(0,20,0,0);

        //Creates the text views and radio buttons programmatically.

        for (int index = 0; index < count; index++) {

            gridLayout.addView(setName(index));
            gridLayout.addView(setBirthday(index));
            gridLayout.addView(setAppToUse(index));

        }

    }

    private TextView setName(int index) {

        TextView nameTextView = new TextView(this);

        nameTextView.setTextSize(16);
        nameTextView.setPadding(20, 20, 20, 20);
        nameTextView.setTextColor(Color.WHITE);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        nameTextView.setText(dbHandler.getContact(index + 1).get_contactName());

        return nameTextView;

    }

    private TextView setBirthday(int index) {

        //Contact ID's start at one.

        String birthday = dbHandler.getContact(index + 1).get_birthday();

        //Get the month and make it an integer.

        int birthdayInt = Integer.parseInt(birthday.substring(0, 2));

        //Convert it to a string with the full text name of the month.

        String birthdayMonth = new DateFormatSymbols().getMonths()[birthdayInt - 1];

        //This shows the month by text then the remaining part of the birthday which is the day.

        birthday = birthdayMonth + " " + birthday.substring(3, birthday.length());

        TextView birthdayTextView = new TextView(this);

        birthdayTextView.setTextSize(16);
        birthdayTextView.setPadding(20, 20, 20, 20);
        birthdayTextView.setTextColor(Color.WHITE);
        birthdayTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        birthdayTextView.setText(birthday);

        return birthdayTextView;

    }

    private RadioGroup setAppToUse(int index) {

        //SMS and Off buttons.

        RadioButton[] typeRadioButton = new RadioButton[2];

        //Contact ID's start at one.

        String appToUse = dbHandler.getContact(index + 1).get_appToUse();

        RadioGroup typeRadioGroup = new RadioGroup(this);

        //Have the buttons appear horizontally.

        typeRadioGroup.setOrientation(LinearLayout.HORIZONTAL);

        typeRadioButton[0] = new RadioButton(this);
        typeRadioButton[1] = new RadioButton(this);

        typeRadioButton[0].setText(R.string.SMS);
        typeRadioButton[0].setTextSize(16);
        typeRadioButton[1].setText(R.string.Off);
        typeRadioButton[1].setTextSize(16);

        typeRadioGroup.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

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