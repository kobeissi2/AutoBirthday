package kobeissidev.autobirthday;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Permissions permissions = new Permissions(this, MainActivity.this);
        final Button contactsButton = findViewById(R.id.contactsButton);
        dbHandler = new DBHandler(this);

        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsButton.setVisibility(View.GONE);
                if (permissions.getPermission()) {
                    //CLEARS ALL SETTINGS
                    dbHandler.startOver();
                    loadContacts();

                    if (dbHandler.isDatabaseEmpty()) {
                        showNoContactDialog();
                    } else {
                        displayContacts();
                    }
                }
            }
        });
    }

    public void showNoContactDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.sym_contact_card)
                .setTitle("No Contacts!")
                .setMessage("You do not have any contacts. \nWant to go to settings to load contacts?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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

    private void displayContacts() {
        int count = dbHandler.getContactCount();
        TextView[] birthdayTextView = new TextView[count];
        TextView[] nameTextView = new TextView[count];
        RadioGroup[] typeRadioGroup = new RadioGroup[count];
        RadioButton[] typeRadioButton = new RadioButton[3];

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        for (int index = 0; index < count; index++) {

            nameTextView[index] = new TextView(this);
            nameTextView[index].setTextSize(16);
            nameTextView[index].setPadding(20, 20, 20, 20);
            nameTextView[index].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            nameTextView[index].setText(dbHandler.getContact(index + 1).get_contactName());

            String birthday = dbHandler.getContact(index + 1).get_birthday();
            int birthdayInt = Integer.parseInt(birthday.substring(0, 2));
            String birthdayMonth = new DateFormatSymbols().getMonths()[birthdayInt - 1];
            birthday = birthdayMonth + " " + birthday.substring(3, birthday.length());
            birthdayTextView[index] = new TextView(this);
            birthdayTextView[index].setTextSize(16);
            birthdayTextView[index].setPadding(0, 20, 0, 20);
            birthdayTextView[index].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            birthdayTextView[index].setText(birthday);

            typeRadioGroup[index] = new RadioGroup(this);
            typeRadioGroup[index].setOrientation(LinearLayout.HORIZONTAL);
            typeRadioButton[0] = new RadioButton(this);
            typeRadioButton[1] = new RadioButton(this);
            typeRadioButton[2] = new RadioButton(this);
            typeRadioButton[0].setText(R.string.SMS);
            typeRadioButton[1].setText(R.string.Whatsapp);
            typeRadioButton[2].setText(R.string.Off);
            typeRadioGroup[index].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            typeRadioGroup[index].addView(typeRadioButton[0]);
            typeRadioGroup[index].addView(typeRadioButton[1]);
            typeRadioGroup[index].addView(typeRadioButton[2]);

            gridLayout.addView(nameTextView[index]);
            gridLayout.addView(birthdayTextView[index]);
            gridLayout.addView(typeRadioGroup[index]);
        }
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
