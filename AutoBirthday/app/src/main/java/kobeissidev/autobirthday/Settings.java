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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class Settings extends Activity {
    private DBHandler dbHandler;
    private Button contactsButton;
    private Button resetButton;
    private CheckBox timeCheckBox;
    private CheckBox birthdayCheckBox;
    private EditText birthdayEditText;
    private TextView timeTextView;
    private boolean birthdayChecked;
    private boolean timeChecked;
    private String birthdayText;
    private String timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getActionBar() != null) {
            getActionBar().setTitle("Settings");
        }

        contactsButton = findViewById(R.id.contactsButton);
        resetButton = findViewById(R.id.resetButton);
        dbHandler = new DBHandler(this);
        timeCheckBox = findViewById(R.id.timeCheckBox);
        birthdayCheckBox = findViewById(R.id.birthdayCheckBox);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        timeTextView = findViewById(R.id.timeTextView);

        loadButton();
        reloadButton();
        setBirthdayPreferences();
        setTimePreferences();

        birthdayCheck(findViewById(android.R.id.content));
        setCheckBox(birthdayChecked, birthdayCheckBox);
        timeCheck(findViewById(android.R.id.content));
        setCheckBox(timeChecked, timeCheckBox);
    }

    private void setCheckBox(boolean isChecked, CheckBox checkBox) {
        if (isChecked) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
    }

    private void saveBirthdayPreferences() {
        SharedPreferences sharedBirthday = getSharedPreferences("birthdayPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor birthdayEditor = sharedBirthday.edit();
        birthdayEditor.putBoolean("birthdayChecked", birthdayChecked);
        birthdayEditor.putString("birthdayText", birthdayText);
        birthdayEditText.setText(birthdayText);
        birthdayEditor.apply();
    }

    private void saveTimePreferences() {
        SharedPreferences sharedTime = getSharedPreferences("timePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor timeEditor = sharedTime.edit();
        timeEditor.putBoolean("timeChecked", timeChecked);
        timeEditor.putString("timeText", timeText);
        timeTextView.setText(timeText);
        timeEditor.apply();
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

    private void setTimePreferences() {
        SharedPreferences sharedTime = getSharedPreferences("timePrefs", Context.MODE_PRIVATE);
        timeChecked = sharedTime.getBoolean("timeChecked", false);
        timeText = sharedTime.getString("timeText", "Time to send text: 00:00.");

        if (timeChecked) {
            timeCheckBox.setChecked(true);
        } else {
            timeCheckBox.setChecked(false);
        }
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
        if (timeCheckBox.isChecked()) {
            timeChecked = true;
            if (timeTextView.getText().toString().equals(timeText)) {
                showTimePicker();
            }
        } else {
            timeChecked = false;
        }
        setVisibility(timeChecked, timeTextView);
        saveTimePreferences();
    }

    private void loadButton() {
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadContacts(getApplicationContext(), dbHandler);
                Toast toast = Toast.makeText(getApplicationContext(), "Contacts are loaded!", Toast.LENGTH_SHORT);
                toast.show();
                setResult(0);
                finish();
            }
        });
    }

    private void reloadButton() {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHandler.startOver();
                Toast toast = Toast.makeText(getApplicationContext(), "Table is now empty! Loading contacts!", Toast.LENGTH_SHORT);
                toast.show();
                loadContacts(getApplicationContext(), dbHandler);
                setResult(0);
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER && birthdayChecked)) {
            birthdayText = birthdayEditText.getText().toString();
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER && timeChecked)) {
            timeText = timeTextView.getText().toString();
        }
        return super.onKeyDown(keyCode, event);
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

    public static void loadContacts(Context context, DBHandler dbHandler) {
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                String columns[] = {
                        ContactsContract.CommonDataKinds.Event.START_DATE,
                        ContactsContract.CommonDataKinds.Event.TYPE,
                        ContactsContract.CommonDataKinds.Event.MIMETYPE,
                };
                String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                        " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId;
                String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
                Cursor birthdayCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder);
                if (birthdayCur != null && birthdayCur.getCount() > 0) {
                    while (birthdayCur.moveToNext()) {
                        String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                        birthday = birthday.substring(2, birthday.length());
                        Contact contact = new Contact(displayName, birthday, "SMS");
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveBirthdayPreferences();
        saveTimePreferences();
        setVisibility(birthdayChecked, birthdayEditText);
        setVisibility(timeChecked, timeTextView);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveBirthdayPreferences();
        saveTimePreferences();
        setVisibility(birthdayChecked, birthdayEditText);
        setVisibility(timeChecked, timeTextView);
    }

    @Override
    public void onStop() {
        super.onStop();
        saveBirthdayPreferences();
        saveTimePreferences();
        setVisibility(birthdayChecked, birthdayEditText);
        setVisibility(timeChecked, timeTextView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveBirthdayPreferences();
        saveTimePreferences();
        setVisibility(birthdayChecked, birthdayEditText);
        setVisibility(timeChecked, timeTextView);
    }
}
