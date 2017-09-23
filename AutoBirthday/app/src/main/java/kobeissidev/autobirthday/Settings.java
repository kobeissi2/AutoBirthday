package kobeissidev.autobirthday;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormatSymbols;

import static kobeissidev.autobirthday.MainActivity.Save;

public class Settings extends Activity {
    private File path;
    private File file;
    DBHandler dbHandler;
    Permissions permissions;
    Button contactsButton;
    Button resetButton;
    CheckBox timeCheckBox;
    CheckBox birthdayCheckBox;
    EditText birthdayEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        permissions = new Permissions(this, Settings.this);
        contactsButton = findViewById(R.id.contactsButton);
        resetButton = findViewById(R.id.resetButton);
        dbHandler = new DBHandler(this);
        timeCheckBox= findViewById(R.id.timeCheckBox);
        birthdayCheckBox= findViewById(R.id.birthdayCheckBox);
        birthdayEditText=findViewById(R.id.birthdayEditText);
        path = this.getFilesDir();
        file = new File(path, "message.txt");

        loadButton();
        reloadButton();
        birthdayCheck();
    }

    private void loadButton(){
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissions.getPermission()) {
                    loadContacts(getApplicationContext(),dbHandler);
                    Toast toast = Toast.makeText(getApplicationContext(), "Contacts are loaded!", Toast.LENGTH_SHORT);
                    toast.show();
                    setResult(0);
                    finish();
                }
            }
        });
    }

    private void reloadButton(){
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHandler.startOver();
                Toast toast = Toast.makeText(getApplicationContext(), "Table is now empty! Loading contacts!", Toast.LENGTH_SHORT);
                toast.show();
                loadContacts(getApplicationContext(),dbHandler);
                setResult(0);
                finish();
            }
        });
    }

    private void birthdayCheck(){
        if(birthdayCheckBox.isChecked()){
            birthdayEditText.setVisibility(View.VISIBLE);
        }
        Save(file,new String[]{birthdayEditText.getText().toString()});
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
}
