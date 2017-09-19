package kobeissidev.autobirthday;

public class Contact {
    private int _id;
    private String _contactName;
    private String _birthday;
    private String _appToUse;

    public Contact() {
    }

    public Contact(int id, String contactName, String birthday, String appToUse) {
        _id = id;
        _contactName = contactName;
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
