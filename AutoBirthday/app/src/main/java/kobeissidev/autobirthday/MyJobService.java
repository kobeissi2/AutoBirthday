package kobeissidev.autobirthday;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MyJobService extends JobService {

    Intent service;

    @Override
    public void onCreate() {

        service = new Intent(getApplicationContext(), Message.class);

    }

    public static Intent getIntent(Context context) {

        return new Intent(context.getApplicationContext(), Message.class);

    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForegroundService(service);

        } else {

            startService(service);

        }

        Utility.scheduleJob(getApplicationContext());

        return false;

    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return false;

    }

}