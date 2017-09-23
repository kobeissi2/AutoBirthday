package kobeissidev.autobirthday;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class Settings extends Activity {
    private File file;
    DBHandler dbHandler;
    Permissions permissions;
    Button contactsButton;
    Button resetButton;
    CheckBox timeCheckBox;
    CheckBox birthdayCheckBox;
    EditText birthdayEditText;
    boolean birthdayChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getActionBar().setTitle("Settings");

        permissions = new Permissions(this, Settings.this);
        contactsButton = findViewById(R.id.contactsButton);
        resetButton = findViewById(R.id.resetButton);
        dbHandler = new DBHandler(this);
        timeCheckBox = findViewById(R.id.timeCheckBox);
        birthdayCheckBox = findViewById(R.id.birthdayCheckBox);
        birthdayEditText = findViewById(R.id.birthdayEditText);

        loadButton();
        reloadButton();
        setPreferences();
        if (birthdayChecked) {
            birthdayCheckBox.setChecked(true);
        } else {
            birthdayCheckBox.setChecked(false);
        }

        birthdayCheck();
    }

    public void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("birthdayChecked", birthdayChecked);
        editor.apply();
    }

    public void setPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        birthdayChecked = sharedPreferences.getBoolean("birthdayChecked", false);
        setBirthdayVisibility();
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

    private void birthdayCheck() {

        birthdayCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (birthdayCheckBox.isChecked()) {
                    birthdayChecked = true;
                    if (birthdayEditText.length() == 0) {
                        birthdayEditText.setText(R.string.happy_birthday);
                    }
                } else {
                    birthdayChecked = false;
                }
                setBirthdayVisibility();
                savePreferences();
            }
        });
    }

    private void setBirthdayVisibility() {
        if (birthdayChecked) {
            birthdayEditText.setVisibility(View.VISIBLE);
        } else {
            birthdayEditText.setVisibility(View.GONE);
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
        savePreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    public void onStop() {
        super.onStop();
        savePreferences();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        savePreferences();
    }
}
