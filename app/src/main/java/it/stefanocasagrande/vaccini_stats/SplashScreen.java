package it.stefanocasagrande.vaccini_stats;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    ProgressBar splashProgress;
    int SPLASH_TIME = 3000; //This is 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //This is additional feature, used to run a progress bar
        splashProgress = findViewById(R.id.splashProgress);
        TextView tv_versione = findViewById(R.id.tv_versione);
        String version="";

        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tv_versione.setText(String.format("Version: %s", version));

        playProgress();

        //Code to start timer and take action after the timer ends
        new Handler().postDelayed(() -> {
            //Do any action here. Now we are moving to next page
            Intent mySuperIntent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(mySuperIntent);

            //This 'finish()' is for exiting the app when back button pressed from Home page which is ActivityHome
            finish();

        }, SPLASH_TIME);
    }

    //Method to run progress bar for 5 seconds
    private void playProgress() {
        ObjectAnimator.ofInt(splashProgress, "progress", 100)
                .setDuration(5000)
                .start();
    }
}