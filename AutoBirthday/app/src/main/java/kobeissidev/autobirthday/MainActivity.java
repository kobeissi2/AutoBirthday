package kobeissidev.autobirthday;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private List<List<String>> contactInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button contactsButton = findViewById(R.id.contactsButton);
        contactInformation = new ArrayList<>();

        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });
    }

    private void checkPermissions() {
        int permissionCheckContacts = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS);
        int permissionCheckSMS = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS);

        if (permissionCheckContacts != PackageManager.PERMISSION_GRANTED || permissionCheckSMS != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }

        if (permissionCheckContacts == PackageManager.PERMISSION_GRANTED && permissionCheckSMS == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        }
    }

    private void loadContacts() {
        ContentResolver contentResolver = getContentResolver();
        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

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

                        List<String> contactRow = new ArrayList<>();
                        contactRow.add(displayName);
                        contactRow.add(birthday);
                        contactInformation.add(contactRow);
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
        loadNames();
        loadBirthdays();
        loadEnableDisable();
    }

    private void loadNames() {
        TextView[] nameTextView = new TextView[contactInformation.size()];
        LinearLayout linearLayout = findViewById(R.id.contactLayout);

        for (int index = 0; index < contactInformation.size(); index++) {
            nameTextView[index] = new TextView(this);
            nameTextView[index].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            nameTextView[index].setText(contactInformation.get(index).get(0));
            linearLayout.addView(nameTextView[index]);
        }
    }

    private void loadBirthdays() {
        TextView[] birthdayTextView = new TextView[contactInformation.size()];
        LinearLayout linearLayout = findViewById(R.id.birthdayLayout);

        for (int index = 0; index < contactInformation.size(); index++) {
            String birthday = contactInformation.get(index).get(1);
            int birthdayInt = Integer.parseInt(birthday.substring(0, 2));
            String birthdayMonth = new DateFormatSymbols().getMonths()[birthdayInt - 1];

            birthday = birthdayMonth + " " + birthday.substring(3, birthday.length());
            birthdayTextView[index] = new TextView(this);
            birthdayTextView[index].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            birthdayTextView[index].setText(birthday);
            linearLayout.addView(birthdayTextView[index]);
        }
    }

    private void loadEnableDisable() {
        LinearLayout linearLayout = findViewById(R.id.enableDisableLayout);
        RadioGroup[] enableDisableRadioGroup = new RadioGroup[contactInformation.size()];
        RadioButton[] enableDisableRadioButton = new RadioButton[2];
        Button[] enableDisableSettingButton = new Button[contactInformation.size()];


        for (int index = 0; index < contactInformation.size(); index++) {
            enableDisableRadioGroup[index] = new RadioGroup(this);
            enableDisableRadioGroup[index].setOrientation(LinearLayout.HORIZONTAL);

            enableDisableRadioButton[0]= new RadioButton(this);
            enableDisableRadioButton[1]= new RadioButton(this);

/*
            //TODO: Write settings to file and change this to reflect that
            enableDisableRadioButton[0].setPressed(true);
*/

            enableDisableRadioGroup[index].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            enableDisableRadioGroup[index].addView(enableDisableRadioButton[0]);
            enableDisableRadioGroup[index].addView(enableDisableRadioButton[1]);
            linearLayout.addView(enableDisableRadioGroup[index]);
        }
    }
}
