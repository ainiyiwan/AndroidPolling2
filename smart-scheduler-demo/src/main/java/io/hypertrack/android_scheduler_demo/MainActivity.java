package io.hypertrack.android_scheduler_demo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import io.hypertrack.smart_scheduler.Job;
import io.hypertrack.smart_scheduler.SmartScheduler;

public class MainActivity extends AppCompatActivity implements SmartScheduler.JobScheduledCallback {

    private static final int JOB_ID = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String JOB_PERIODIC_TASK_TAG = "io.hypertrack.android_scheduler_demo.JobPeriodicTask";

    private Spinner jobTypeSpinner, networkTypeSpinner;
    private Switch requiresChargingSwitch, isPeriodicSwitch;
    private EditText intervalInMillisEditText;
    private Button smartJobButton;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> data;

    //have seen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Views
        jobTypeSpinner = (Spinner) findViewById(R.id.spinnerJobType);
        networkTypeSpinner = (Spinner) findViewById(R.id.spinnerNetworkType);
        requiresChargingSwitch = (Switch) findViewById(R.id.switchRequiresCharging);
        isPeriodicSwitch = (Switch) findViewById(R.id.switchPeriodicJob);
        intervalInMillisEditText = (EditText) findViewById(R.id.jobInterval);
        smartJobButton = (Button) findViewById(R.id.smartJobButton);
        mRecyclerView = findViewById(R.id.recyclerView);

        init();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(data);
        mRecyclerView.setAdapter(mAdapter);
    }

    /*
     * 初始化
     */
    private void init() {
        if (data != null){
            data.clear();
        }else {
            data = new ArrayList<>();
        }

        data.add("default data"+DateUtil.getDateToString());
    }

    public void onSmartJobBtnClick(View view) {
        SmartScheduler jobScheduler = SmartScheduler.getInstance(this);

        // Check if any periodic job is currently scheduled
        if (jobScheduler.contains(JOB_ID)) {
            removePeriodicJob();
            return;
        }

        // Create a new job with specified params
        Job job = createJob();
        if (job == null) {
            Toast.makeText(MainActivity.this, "Invalid paramteres specified. " +
                    "Please try again with correct job params.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Schedule current created job
        if (jobScheduler.addJob(job)) {
            Toast.makeText(MainActivity.this, "Job successfully added!", Toast.LENGTH_SHORT).show();
            Logger.t("jobType").d("任务添加成功==========="+"\n"+"时间："+DateUtil.getDateToString());
            if (job.isPeriodic()) {
                smartJobButton.setText(getString(R.string.remove_job_btn));
            } else {
                smartJobButton.setAlpha(0.5f);
                smartJobButton.setEnabled(false);
            }
        }
    }

    //have seen
    private Job createJob() {
        int jobType = getJobType();
        Logger.t("jobType").d("jobType==="+jobType+"\n"+"时间："+DateUtil.getDateToString());
        int networkType = getNetworkTypeForJob();
        Logger.d("networkType==="+networkType+"\n"+"时间："+DateUtil.getDateToString());
        boolean requiresCharging = requiresChargingSwitch.isChecked();
        Logger.d("requiresCharging==="+requiresCharging+"\n"+"时间："+DateUtil.getDateToString());
        boolean isPeriodic = isPeriodicSwitch.isChecked();
        Logger.d("isPeriodic==="+isPeriodic+"\n"+"时间："+DateUtil.getDateToString());

        String intervalInMillisString = intervalInMillisEditText.getText().toString();
        Logger.d("intervalInMillisString==="+intervalInMillisString+"\n"+"时间："+DateUtil.getDateToString());
        if (TextUtils.isEmpty(intervalInMillisString)) {
            return null;
        }

        Long intervalInMillis = Long.parseLong(intervalInMillisString);
        Job.Builder builder = new Job.Builder(JOB_ID, this, jobType, JOB_PERIODIC_TASK_TAG)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(requiresCharging)
                .setIntervalMillis(intervalInMillis);

        if (isPeriodic) {
            builder.setPeriodic(intervalInMillis);
            Logger.t("schedule").d("工作开始喽---------------===-----------"+"\n"+"时间："+DateUtil.getDateToString());
        }

        return builder.build();
    }

    //have seen
    private int getJobType() {
        int jobTypeSelectedPos = jobTypeSpinner.getSelectedItemPosition();
        switch (jobTypeSelectedPos) {
            default:
                return Job.Type.JOB_TYPE_NONE;
            case 1:
                return Job.Type.JOB_TYPE_HANDLER;
            case 2:
                return Job.Type.JOB_TYPE_ALARM;
            case 3:
                return Job.Type.JOB_TYPE_PERIODIC_TASK;
        }
    }

    //have seen
    private int getNetworkTypeForJob() {
        int networkTypeSelectedPos = networkTypeSpinner.getSelectedItemPosition();
        switch (networkTypeSelectedPos) {
            default:
            case 0:
                return Job.NetworkType.NETWORK_TYPE_ANY;
            case 1:
                return Job.NetworkType.NETWORK_TYPE_CONNECTED;
            case 2:
                return Job.NetworkType.NETWORK_TYPE_UNMETERED;
        }
    }

    //have seen
    private void removePeriodicJob() {
        smartJobButton.setText(getString(R.string.schedule_job_btn));

        SmartScheduler jobScheduler = SmartScheduler.getInstance(this);
        if (!jobScheduler.contains(JOB_ID)) {
            Toast.makeText(MainActivity.this, "No job exists with JobID: " + JOB_ID, Toast.LENGTH_SHORT).show();
            return;
        }

        if (jobScheduler.removeJob(JOB_ID)) {
            Toast.makeText(MainActivity.this, "Job successfully removed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onJobScheduled(Context context, final Job job) {
        Logger.t("schedule").d("onJobScheduled 另一个线程---------------进行中-----------"+"\n"+"时间："+DateUtil.getDateToString());
        if (job != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Logger.t("schedule").d("onJobScheduled 切换到工作线程---------------工作结束吐司吧-----------"+"\n"+"时间："+DateUtil.getDateToString());
//                    Toast.makeText(MainActivity.this, "Job: " + job.getJobId() + " scheduled!", Toast.LENGTH_SHORT).show();
                    int count = data.size();
                    data.add("成功次数= "+count+" 成功时间= "+DateUtil.getDateToString());
//                    mAdapter.notifyDataSetChanged();
                    mAdapter.notifyItemInserted(data.size());
                }
            });
//            Log.d(TAG, "Job: " + job.getJobId() + " scheduled!");
            Logger.t("schedule").d("新一轮的工作开始喽---------------===-----------"+"\n"+"时间："+DateUtil.getDateToString());
            if (!job.isPeriodic()) {
                smartJobButton.setAlpha(1.0f);
                smartJobButton.setEnabled(true);
            }
        }
    }

    //have seen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //have seen
    public void onResetSchedulerClick(MenuItem item) {
        SmartScheduler smartScheduler = SmartScheduler.getInstance(getApplicationContext());
        smartScheduler.removeJob(JOB_ID);

        smartJobButton.setText(getString(R.string.schedule_job_btn));
        smartJobButton.setEnabled(true);
        smartJobButton.setAlpha(1.0f);

        init();
        mAdapter.notifyDataSetChanged();
    }
}

