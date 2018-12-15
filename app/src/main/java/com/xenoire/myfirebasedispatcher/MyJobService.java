package com.xenoire.myfirebasedispatcher;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class MyJobService extends JobService {

    public static final String TAG = MyJobService.class.getSimpleName();
    final String WEATHER_API_KEY = "34b85c4702d2ef57c5cab3bb805f52c3";
    public static String EXTRA_CITY = "city";
    public final String NOTIFICATION_ID = "firebaseDispatcher";

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d(TAG, "ONSTARTJOB : Executed");
        getCurrentWeather(job);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d(TAG, "ONSTOPJOB : EXECUTED");
        return true;
    }

    private void getCurrentWeather(final JobParameters job){

        Bundle extras = job.getExtras();
        // Lakukan pengecekan terlebih dahulu terhadap parameter job
        if (extras == null) {
            jobFinished(job, false);
            return;
        } else if (extras.isEmpty()) {
            jobFinished(job, false);
            return;
        }
        String city = extras.getString(EXTRA_CITY);
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+city+"&APPID="+WEATHER_API_KEY;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject obj = new JSONObject(result);
                    String currentWeather = obj.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = obj.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = obj.getJSONObject("main").getDouble("temp");
                    double tempInCelcius = tempInKelvin - 273;
                    String temperature = new DecimalFormat("##.##").format(tempInCelcius);
                    String city = obj.getString("name");
                    String title = city + " Current Weather";
                    String message = currentWeather + ", " + description + " with "+temperature+" C";
                    int notifId = 105;
                    showNotification(getApplicationContext(), title, message, notifId);
                    jobFinished(job, false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    jobFinished(job, true);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                jobFinished(job, true);
            }
        });
    }
     private void showNotification(Context context, String title, String msg, int notifId){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID, "Weather Firebase Notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{0, 1000});
            channel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
         NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_ID)
                 .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                 .setSmallIcon(R.drawable.ic_replay_30_black_24dp)
                 .setContentTitle(title)
                 .setContentText(msg)
                 .setColor(ContextCompat.getColor(context, android.R.color.black));
         if (notificationManager != null) {
             notificationManager.notify(notifId, builder.build());
         }
     }
}
