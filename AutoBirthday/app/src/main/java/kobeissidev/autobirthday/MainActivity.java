package kobeissidev.autobirthday;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private Button contactsButton;
    private TextView contactsText;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactsButton = findViewById(R.id.contactsButton);
        contactsText = findViewById(R.id.contactsText);

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
        StringBuilder stringBuilder = new StringBuilder();

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

        while (cursor.moveToNext()) {

            Map<String, String> contactInfoMap = new HashMap<String, String>();
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
            String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

            String columns[] = {
                    ContactsContract.CommonDataKinds.Event.START_DATE,
                    ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.CommonDataKinds.Event.MIMETYPE,
            };

            String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                    " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId;

            String[] selectionArgs = null;
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;

            Cursor birthdayCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
            if (birthdayCur.getCount() > 0) {
                while (birthdayCur.moveToNext()) {
                    String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                    birthday=birthday.substring(2,birthday.length());
                    contactInfoMap.put(displayName,birthday);
                    stringBuilder.append("Name: ").append(displayName).append(". Birthday: ").append(birthday).append("\n\n");
                }
            }
            birthdayCur.close();

        }
        cursor.close();
        contactsText.setText(stringBuilder.toString());
    }
}
