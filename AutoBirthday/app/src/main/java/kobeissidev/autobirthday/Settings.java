package kobeissidev.autobirthday;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class Settings extends Activity {

    private CheckBox timeCheckBox;
    private CheckBox birthdayCheckBox;
    private CheckBox defaultLoadCheckBox;
    private EditText birthdayEditText;
    private TextView timeTextView;
    private boolean birthdayChecked;
    private boolean timeChecked;
    private boolean loadChecked;
    private String birthdayText;
    private String timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getActionBar() != null) {

            getActionBar().setTitle(" Settings");
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setLogo(getDrawable(R.drawable.ic_stat_cake));
            getActionBar().setDisplayUseLogoEnabled(true);

        }

        birthdayCheckBox = findViewById(R.id.birthdayCheckBox);
        defaultLoadCheckBox = findViewById(R.id.defaultLoadCheckBox);
        timeCheckBox = findViewById(R.id.timeCheckBox);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        timeTextView = findViewById(R.id.timeTextView);

        setBirthdayPreferences();
        setTimePreferences();
        setLoadPreferences();

        birthdayCheck(findViewById(android.R.id.content));
        setCheckBox(birthdayChecked, birthdayCheckBox);

        timeCheck(findViewById(android.R.id.content));
        setCheckBox(timeChecked, timeCheckBox);

        defaultLoad(findViewById(android.R.id.content));
        setCheckBox(loadChecked, defaultLoadCheckBox);

        birthdayEditText.setTextSize(16);
        timeTextView.setTextSize(16);

    }

    public static boolean getLoadChecked(Context context) {

        SharedPreferences sharedTime = context.getSharedPreferences("loadPrefs", Context.MODE_PRIVATE);

        return sharedTime.getBoolean("loadChecked", false);

    }

    private void setCheckBox(boolean isChecked, CheckBox checkBox) {

        if (isChecked) {

            checkBox.setChecked(true);

        } else {

            checkBox.setChecked(false);

        }

    }

    private void setBirthdayPreferences() {

        SharedPreferences sharedBirthday = getSharedPreferences("birthdayPrefs", Context.MODE_PRIVATE);

        birthdayChecked = sharedBirthday.getBoolean("birthdayChecked", false);
        birthdayText = sharedBirthday.getString("birthdayText", "Happy Birthday!");

        if (birthdayChecked) {

            birthdayCheckBox.setChecked(true);

        } else {

            birthdayCheckBox.setChecked(false);

        }

        setVisibility(birthdayChecked, birthdayEditText);

    }

    private void saveBirthdayPreferences() {

        SharedPreferences sharedBirthday = getSharedPreferences("birthdayPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor birthdayEditor = sharedBirthday.edit();

        birthdayEditor.putBoolean("birthdayChecked", birthdayChecked);
        birthdayEditor.putString("birthdayText", birthdayText);
        birthdayEditor.apply();

        birthdayEditText.setText(birthdayText);

    }

    public void birthdayCheck(View view) {

        if (birthdayCheckBox.isChecked()) {

            birthdayChecked = true;

        } else {

            birthdayChecked = false;

            birthdayText = "Happy Birthday!";

        }

        setVisibility(birthdayChecked, birthdayEditText);

        saveBirthdayPreferences();

    }

    private void setTimePreferences() {

        SharedPreferences sharedTime = getSharedPreferences("timePrefs", Context.MODE_PRIVATE);

        timeChecked = sharedTime.getBoolean("timeChecked", false);
        timeText = sharedTime.getString("timeText", "Time to send text: 00:00.");

        if (timeChecked) {

            timeCheckBox.setChecked(true);

        } else {

            timeCheckBox.setChecked(false);

        }

        setVisibility(timeChecked, timeTextView);

    }

    private void saveTimePreferences() {

        SharedPreferences sharedTime = getSharedPreferences("timePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor timeEditor = sharedTime.edit();

        timeEditor.putBoolean("timeChecked", timeChecked);
        timeEditor.putString("timeText", timeText);
        timeEditor.apply();

        timeTextView.setText(timeText);

    }

    private void showTimePicker() {

        final boolean isUser24Hour = DateFormat.is24HourFormat(this);
        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int minute = Calendar.getInstance().get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    String amPM;

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        String hourString;
                        String minuteString;

                        if (isUser24Hour) {

                            hourString = String.valueOf(hourOfDay);
                            minuteString = String.valueOf(minute);

                            if (hourOfDay < 10 && hourOfDay != 0) {

                                hourString = "0" + String.valueOf(hourOfDay);

                            } else if (hourOfDay == 0) {

                                hourString = "00";

                            }

                            if (minute < 10) {

                                minuteString = "0" + String.valueOf(minute);

                            }

                        } else {

                            if (hourOfDay >= 12) {

                                amPM = " PM";

                                if (hourOfDay != 12) {

                                    hourOfDay -= 12;

                                }

                                hourString = String.valueOf(hourOfDay);

                            } else {

                                amPM = " AM";

                                if (hourOfDay == 0) {

                                    hourOfDay = 12;

                                }

                                hourString = String.valueOf(hourOfDay);

                            }

                            if (minute < 10) {

                                minuteString = "0" + minute;

                            } else {

                                minuteString = String.valueOf(minute);

                            }

                        }

                        if (isUser24Hour) {

                            timeTextView.setText("Time to send text: " + hourString + ":" + minuteString + ".");

                        } else {

                            timeTextView.setText("Time to send text: " + hourString + ":" + minuteString + amPM + ".");

                        }

                    }

                }, hour, minute, isUser24Hour);

        timePickerDialog.show();

    }

    public void timeCheck(View view) {

        final boolean isUser24Hour = DateFormat.is24HourFormat(getApplicationContext());

        if (timeCheckBox.isChecked()) {

            timeChecked = true;

            if (timeTextView.getText().toString().equals(timeText)) {

                showTimePicker();

            }

        } else {

            timeChecked = false;

            if (!isUser24Hour) {

                timeText = "Time to send text: 12:00 AM.";

            } else {

                timeText = "Time to send text: 00:00.";

            }

        }

        setVisibility(timeChecked, timeTextView);

        saveTimePreferences();

    }

    private void setLoadPreferences() {

        SharedPreferences sharedTime = getSharedPreferences("loadPrefs", Context.MODE_PRIVATE);

        loadChecked = sharedTime.getBoolean("loadChecked", false);

        if (loadChecked) {

            defaultLoadCheckBox.setChecked(true);

        } else {

            defaultLoadCheckBox.setChecked(false);

        }

    }

    private void saveLoadPreferences() {

        SharedPreferences sharedTime = getSharedPreferences("loadPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor loadEditor = sharedTime.edit();

        loadEditor.putBoolean("loadChecked", loadChecked);
        loadEditor.apply();

    }

    public void defaultLoad(View view) {

        loadChecked = defaultLoadCheckBox.isChecked();

        saveLoadPreferences();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER) && birthdayChecked)) {

            birthdayText = birthdayEditText.getText().toString();

        }

        if (((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER) && timeChecked)) {

            timeText = timeTextView.getText().toString();

        }

        return super.onKeyDown(keyCode, event);

    }

    private void setVisibility(boolean checked, EditText editText) {

        if (checked) {

            editText.setVisibility(View.VISIBLE);

        } else {

            editText.setVisibility(View.GONE);

        }

    }

    private void setVisibility(boolean checked, TextView textView) {

        if (checked) {

            textView.setVisibility(View.VISIBLE);

        } else {

            textView.setVisibility(View.GONE);

        }

    }

    public static void loadContacts(Context context, DBHandler dbHandler, Boolean granted) {

        if(granted){

            ContentResolver contentResolver = context.getContentResolver();
            String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                    projection, null, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

            if (cursor != null) {

                while (cursor.moveToNext()) {

                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String phoneNumber = "";
                    String columns[] = {
                            ContactsContract.CommonDataKinds.Event.START_DATE,
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.MIMETYPE,
                    };

                    String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                            " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" +
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                            + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId;
                    String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;

                    Cursor birthdayCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                            columns, where, null, sortOrder);

                    if (birthdayCur != null && birthdayCur.getCount() > 0) {

                        while (birthdayCur.moveToNext()) {

                            String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Event.START_DATE));

                            birthday = birthday.substring(2, birthday.length());

                            phoneNumber = getPhoneNumber(context, displayName);

                            Contact contact = new Contact(displayName, birthday, "SMS", phoneNumber);

                            dbHandler.insertOrUpdate(contact);

                        }

                    }

                    if (birthdayCur != null) {

                        birthdayCur.close();

                    }

                }

            }

            if (cursor != null) {

                cursor.close();

            }

        }else{

            Toast.makeText(context.getApplicationContext(),"ERROR! Permissions Not Granted!",Toast.LENGTH_SHORT).show();

        }

    }

    private static String getPhoneNumber(Context context, String displayName) {

        String number = "";
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                "DISPLAY_NAME = '" + displayName + "'", null, null);

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                String contactId =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.
                        CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                        + " = " + contactId, null, null);

                if (phones != null) {

                    while (phones.moveToNext()) {

                        number = phones.getString(phones.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.NUMBER));

                    }

                    phones.close();

                }

            }

            cursor.close();

        }

        return number;

    }

    private void setText() {

        if (birthdayChecked) {

            birthdayText = birthdayEditText.getText().toString();

        }

        if (timeChecked) {

            timeText = timeTextView.getText().toString();

        }

    }

    private void saveAll() {

        saveBirthdayPreferences();

        saveTimePreferences();

        saveLoadPreferences();

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        setText();

        saveAll();

        setVisibility(birthdayChecked, birthdayEditText);

        setVisibility(timeChecked, timeTextView);

        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {

        super.onPause();

        setText();

        saveAll();

        setVisibility(birthdayChecked, birthdayEditText);

        setVisibility(timeChecked, timeTextView);

    }

    @Override
    public void onStop() {

        super.onStop();

        setText();

        saveAll();

        setVisibility(birthdayChecked, birthdayEditText);

        setVisibility(timeChecked, timeTextView);


    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        setText();

        saveAll();

        setVisibility(birthdayChecked, birthdayEditText);

        setVisibility(timeChecked, timeTextView);

    }

}