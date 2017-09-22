package kobeissidev.autobirthday;

public class Contact {
    private int _id;
    private String _contactName;
    private String _birthday;
    private String _appToUse;
    private static final int BIRTHDAY_SIZE = 5;

    public Contact() {}

    public Contact(int id, String contactName, String birthday, String appToUse) {
        _id = id;
        _contactName = contactName;
        /*If the contact has a year in their birth date, it would be YY-MM-DD. If not, the birth date would show up as MM-DD.
        So if the birthday is not the same size as MM-DD, make it the substring that only carries the MM-DD.*/
        if (birthday.length() > BIRTHDAY_SIZE) {
            birthday = birthday.substring(3, birthday.length());
        }
        _birthday = birthday;
        _appToUse = appToUse;
    }

    public Contact(String contactName, String birthday, String appToUse) {
        _contactName = contactName;
        _birthday = birthday;
        _appToUse = appToUse;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_contactName() {
        return _contactName;
    }

    public void set_contactName(String _contactName) {
        this._contactName = _contactName;
    }

    public String get_birthday() {
        return _birthday;
    }

    public void set_birthday(String _birthday) {
        this._birthday = _birthday;
    }

    public String get_appToUse() {
        return _appToUse;
    }

    public void set_appToUse(String _appToUse) {
        this._appToUse = _appToUse;
    }
}
