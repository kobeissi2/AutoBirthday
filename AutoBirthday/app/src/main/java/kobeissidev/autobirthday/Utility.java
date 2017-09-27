package kobeissidev.autobirthday;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

@SuppressWarnings("All")

public class Utility {
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, MyJobService.class);
        JobScheduler jobScheduler;
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(60000);
        builder.setOverrideDeadline(60000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        jobScheduler.schedule(builder.build());
    }
}
