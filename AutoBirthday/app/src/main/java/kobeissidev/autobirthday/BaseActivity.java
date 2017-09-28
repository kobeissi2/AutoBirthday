package kobeissidev.autobirthday;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedTheme = getSharedPreferences("themePrefs", Context.MODE_PRIVATE);

        final String theme = sharedTheme.getString("selectedTheme", "Material Dark");

        switch (theme) {

            case "Material Dark":

                setTheme(R.style.MaterialBlack);

                break;

            case "Material Red":

                setTheme(R.style.MaterialAutoBirthday);

                break;

            default:

                setTheme(R.style.MaterialBlue);

                break;

        }

        super.onCreate(savedInstanceState);

    }

}