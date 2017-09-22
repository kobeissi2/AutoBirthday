package kobeissidev.autobirthday;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.List;

public class MainActivity extends Activity {
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Permissions permissions = new Permissions(this, MainActivity.this);
        dbHandler = new DBHandler(this);
        if (permissions.getPermission()) {
            if (dbHandler.isDatabaseEmpty()) {
                showNoContactDialog();
            } else {
                displayContacts();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                navigateToSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        //SMS, WhatsApp and Off buttons.
        RadioButton[] typeRadioButton = new RadioButton[3];
        //Contact ID's start at one.
        String appToUse = dbHandler.getContact(index + 1).get_appToUse();
        typeRadioGroup = new RadioGroup(this);
        //Have the buttons appear horizontally.
        typeRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
        typeRadioButton[0] = new RadioButton(this);
        typeRadioButton[1] = new RadioButton(this);
        typeRadioButton[2] = new RadioButton(this);
        typeRadioButton[0].setText(R.string.SMS);
        typeRadioButton[1].setText(R.string.WhatsApp);
        typeRadioButton[2].setText(R.string.Off);
        typeRadioGroup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //Add the radio buttons to the radio group.
        typeRadioGroup.addView(typeRadioButton[0]);
        typeRadioGroup.addView(typeRadioButton[1]);
        typeRadioGroup.addView(typeRadioButton[2]);
        //Set the radio button to the one defaulted at.
        switch (appToUse) {
            case "SMS":
                typeRadioButton[0].setChecked(true);
                typeRadioButton[1].setChecked(false);
                typeRadioButton[2].setChecked(false);
                break;
            case "WhatsApp":
                typeRadioButton[0].setChecked(false);
                typeRadioButton[1].setChecked(true);
                typeRadioButton[2].setChecked(false);
                break;
            default:
                typeRadioButton[0].setChecked(false);
                typeRadioButton[1].setChecked(false);
                typeRadioButton[2].setChecked(true);
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
                } else if (index == 1) {
                    appToUseUpdateID = "WhatsApp";
                } else if (index == 2) {
                    appToUseUpdateID = "Off";
                }
                //Updates the database through the ID and the new app to use.
                dbHandler.updateContactAppToUse(contact.get_id(), appToUseUpdateID);
            }
        });
    }

    public void navigateToSettings() {
        Intent intent = new Intent(getBaseContext(), Settings.class);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //Runs when returns from settings to refresh itself.
        super.onActivityResult(requestCode,resultCode,data);
        finish();
        startActivity(getIntent());
    }

    public void showNoContactDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.sym_contact_card)
                .setTitle("No Contacts!")
                .setMessage("You do not have any contacts. \nWant to go to settings to load contacts?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        navigateToSettings();
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
