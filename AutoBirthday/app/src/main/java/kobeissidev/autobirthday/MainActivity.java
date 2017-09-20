package kobeissidev.autobirthday;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
                setDefaultMessage();
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
        TextView[] birthdayTextView = new TextView[count];
        TextView[] nameTextView = new TextView[count];
        RadioGroup[] typeRadioGroup = new RadioGroup[count];
        RadioButton[] typeRadioButton = new RadioButton[3];

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        for (int index = 0; index < count; index++) {

            nameTextView[index] = new TextView(this);
            nameTextView[index].setTextSize(16);
            nameTextView[index].setPadding(20, 20, 20, 20);
            nameTextView[index].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            nameTextView[index].setText(dbHandler.getContact(index + 1).get_contactName());

            String birthday = dbHandler.getContact(index + 1).get_birthday();
            int birthdayInt = Integer.parseInt(birthday.substring(0, 2));
            String birthdayMonth = new DateFormatSymbols().getMonths()[birthdayInt - 1];
            birthday = birthdayMonth + " " + birthday.substring(3, birthday.length());
            birthdayTextView[index] = new TextView(this);
            birthdayTextView[index].setTextSize(16);
            birthdayTextView[index].setPadding(20, 20, 20, 20);
            birthdayTextView[index].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            birthdayTextView[index].setText(birthday);

            typeRadioGroup[index] = new RadioGroup(this);
            typeRadioGroup[index].setOrientation(LinearLayout.HORIZONTAL);
            typeRadioButton[0] = new RadioButton(this);
            typeRadioButton[1] = new RadioButton(this);
            typeRadioButton[2] = new RadioButton(this);
            typeRadioButton[0].setText(R.string.SMS);
            typeRadioButton[1].setText(R.string.WhatsApp);
            typeRadioButton[2].setText(R.string.Off);
            typeRadioGroup[index].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            typeRadioGroup[index].addView(typeRadioButton[0]);
            typeRadioGroup[index].addView(typeRadioButton[1]);
            typeRadioGroup[index].addView(typeRadioButton[2]);
            typeRadioGroup[index].setId(index + 1);

            gridLayout.addView(nameTextView[index]);
            gridLayout.addView(birthdayTextView[index]);
            gridLayout.addView(typeRadioGroup[index]);
        }
    }

    private void setDefaultMessage() {
        List<Contact> contactList = dbHandler.getAllContacts();
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        RadioGroup[] radioGroupList = new RadioGroup[gridLayout.getChildCount()];

        RadioGroup radioGroup = (RadioGroup) gridLayout.getChildAt(2);


        for (Contact contact : contactList) {
            RadioButton radioButtonSMS = (RadioButton) radioGroup.getChildAt(0);
            RadioButton radioButtonWhatsApp = (RadioButton) radioGroup.getChildAt(1);
            RadioButton radioButtonNone = (RadioButton) radioGroup.getChildAt(2);
            switch (contact.get_appToUse()) {
                case "SMS":
                    radioButtonSMS.setChecked(true);
                    radioButtonWhatsApp.setChecked(false);
                    radioButtonNone.setChecked(false);
                    break;
                case "WhatsApp":
                    radioButtonWhatsApp.setChecked(true);
                    radioButtonSMS.setChecked(false);
                    radioButtonNone.setChecked(false);
                    break;
                default:
                    radioButtonNone.setChecked(true);
                    radioButtonWhatsApp.setChecked(false);
                    radioButtonSMS.setChecked(false);
                    break;
            }
        }
    }

    public void navigateToSettings() {
        Intent intent = new Intent(getBaseContext(), Settings.class);
        startActivity(intent);

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
