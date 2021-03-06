package kobeissidev.autobirthday;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contacts.db";
    private static final String TABLE_CONTACTS = "contact";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CONTACT_NAME = "_contactName";
    private static final String COLUMN_BIRTHDAY = "_birthday";
    private static final String COLUMN_APPTOUSE = "_appToUse";
    private static final String COLUMN_PHONENUMBER = "_phoneNumber";

    public DBHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        String query = "CREATE TABLE " + TABLE_CONTACTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CONTACT_NAME + " TEXT," + COLUMN_BIRTHDAY + " TEXT,"
                + COLUMN_APPTOUSE + " TEXT," + COLUMN_PHONENUMBER + " TEXT" + ");";

        database.execSQL(query);

    }

    //TODO: Change this to a better way to upgrade.
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        onCreate(database);

    }

    public Contact getContact(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{COLUMN_ID, COLUMN_CONTACT_NAME,
                        COLUMN_BIRTHDAY, COLUMN_APPTOUSE, COLUMN_PHONENUMBER}, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null) {

            cursor.moveToFirst();

        }

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));

        cursor.close();

        return contact;

    }

    public List<Contact> getAllContacts() {

        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                Contact contact = new Contact();

                contact.set_id(Integer.parseInt(cursor.getString(0)));
                contact.set_contactName(cursor.getString(1));
                contact.set_birthday(cursor.getString(2));
                contact.set_appToUse(cursor.getString(3));
                contact.set_phoneNumber(cursor.getString(4));

                contactList.add(contact);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return contactList;

    }

    public int getContactCount() {

        String countQuery = "SELECT * FROM " + TABLE_CONTACTS;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(countQuery, null);
        int count = cursor.getCount();

        cursor.close();

        return count;

    }

    public boolean isDatabaseEmpty() {

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.close();

            return false;

        } else {

            cursor.close();

            return true;

        }

    }

    public void startOver() {

        SQLiteDatabase database = getWritableDatabase();

        database.execSQL("drop table " + TABLE_CONTACTS);

        onCreate(database);

    }

    public void updateContactAppToUse(int id, String appToUseUpdateID) {

        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_APPTOUSE, appToUseUpdateID);

        database.update(TABLE_CONTACTS, contentValues, COLUMN_APPTOUSE + "= ? AND " + COLUMN_ID + "= ?", new String[]{getContact(id).get_appToUse(), String.valueOf(id)});

    }

    public int getUniqueID(Contact contact) {

        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.query(TABLE_CONTACTS, new String[]{"_id"}, COLUMN_CONTACT_NAME
                + "= ? AND " + COLUMN_BIRTHDAY + "= ?", new String[]{contact.get_contactName(),
                contact.get_birthday()}, null, null, null, null);

        if (cursor.moveToFirst()) {

            int cursorValue = cursor.getInt(cursor.getColumnIndex("_id"));

            cursor.close();

            return cursorValue;

        }

        cursor.close();

        return -1;

    }

    public void insertOrUpdate(Contact contact) {

        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_CONTACT_NAME, contact.get_contactName());
        contentValues.put(COLUMN_BIRTHDAY, contact.get_birthday());
        contentValues.put(COLUMN_PHONENUMBER, contact.get_phoneNumber());

        int id = getUniqueID(contact);

        if (id == -1) {

            contentValues.put(COLUMN_APPTOUSE, contact.get_appToUse());
            database.insert(TABLE_CONTACTS, null, contentValues);

        } else {

            database.update(TABLE_CONTACTS, contentValues, "_id=?", new String[]{String.valueOf(id)});

        }

        database.close();

    }

}