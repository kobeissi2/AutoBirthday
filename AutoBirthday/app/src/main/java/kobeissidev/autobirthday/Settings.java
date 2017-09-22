package kobeissidev.autobirthday;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;

public class Settings extends Activity {
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final Permissions permissions = new Permissions(this, Settings.this);
        final Button contactsButton = findViewById(R.id.contactsButton);
        final Button resetButton = findViewById(R.id.resetButton);
        dbHandler = new DBHandler(this);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissions.getPermission()) {
                    loadContacts();
                    Toast toast = Toast.makeText(getApplicationContext(), "Contacts are loaded!", Toast.LENGTH_SHORT);
                    toast.show();
                    setResult(0);
                    finish();
                }
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHandler.startOver();
                Toast toast = Toast.makeText(getApplicationContext(), "Table is now empty!", Toast.LENGTH_SHORT);
                toast.show();
                loadContacts();
                setResult(0);
                finish();
            }
        });
    }

    private void loadContacts() {
        ContentResolver contentResolver = getContentResolver();
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
                        dbHandler.addContact(contact);
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
}
