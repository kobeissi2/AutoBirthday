package kobeissidev.autobirthday;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permissions {
    private Context appContext;
    private Activity appActivity;
    private static final int MY_PERMISSIONS_REQUEST = 0;

    public Permissions(Context context, Activity activity) {
        appContext = context;
        appActivity = activity;
        checkPermissions();
    }

    private boolean checkPermissions() {
        int permissionCheckContacts = ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS);
        int permissionCheckSMS = ContextCompat.checkSelfPermission(appContext, Manifest.permission.SEND_SMS);

        if (permissionCheckContacts != PackageManager.PERMISSION_GRANTED || permissionCheckSMS != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(appActivity, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(appActivity, Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(appActivity, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, 1);
            } else {
                ActivityCompat.requestPermissions(appActivity, new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST);
            }
        }

        if (permissionCheckContacts == PackageManager.PERMISSION_GRANTED && permissionCheckSMS == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public boolean getPermission(){
        return checkPermissions();
    }
}
