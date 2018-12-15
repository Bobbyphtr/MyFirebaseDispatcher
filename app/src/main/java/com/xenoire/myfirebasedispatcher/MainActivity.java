package com.xenoire.myfirebasedispatcher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnSetScheduler, btnCancelScheduler;
    final String CITY = "Denpasar";
    private String DISPATCHER_TAG = "dispatcher";
    private FirebaseJobDispatcher jobDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCancelScheduler = findViewById(R.id.btn_cancel_scheduler);
        btnSetScheduler = findViewById(R.id.btn_set_scheduler);

        btnCancelScheduler.setOnClickListener(this);
        btnSetScheduler.setOnClickListener(this);

        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_scheduler:
                Toast.makeText(this, "Dispatcher Created", Toast.LENGTH_SHORT).show();
                startDispatcher();
                break;
            case R.id.btn_cancel_scheduler:
                Toast.makeText(this, "Dispatcher Cancelled", Toast.LENGTH_SHORT).show();
                cancelDispatcher(DISPATCHER_TAG);
                break;
        }
    }

    public  void startDispatcher(){
        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString(MyJobService.EXTRA_CITY, CITY);

        Job job = jobDispatcher.newJobBuilder()
                .setService(MyJobService.class) // kelas service
                .setTag(DISPATCHER_TAG)         // Identifier unique untuk identifikasi
                .setRecurring(true)             // TRUE jika job tersebut akan diulang, FALSE jika tidak di ulang
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT) //UNTIL_NEXT_BOOT => sampai boot selanjutnya; FOREVER => Berjalan meski setelah reboot
                .setTrigger(Trigger.executionWindow(0, 60)) //waktu trigger
                .setReplaceCurrent(true)        //TRUE untuk overwrite job dengan tag sama
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL) //waktu kapan akan dijalankan lagi jika gagal
                .setConstraints(
                        //Constraint.ON_UNMETERED_NETWORK, //Berjalan untuk koneksi yang unmetered (Wifi)
                        Constraint.DEVICE_CHARGING,     //Hanya berkalan ketika device di charge
                        Constraint.ON_ANY_NETWORK      // Berjalan saat ada koneksi internet
                        //Constraint.DEVICE_IDLE          //Berjalan saat device dalam kondisi idle
                )
                .setExtras(myExtrasBundle)
                .build();
        jobDispatcher.mustSchedule(job);
        Log.e("Log e", "startDispatcher");
    }

    public void cancelDispatcher(String tag){
        jobDispatcher.cancel(tag);
        Log.e("Log e", "CancelDispatcher");
    }
}
