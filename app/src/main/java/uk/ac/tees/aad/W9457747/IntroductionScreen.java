package uk.ac.tees.aad.W9457747;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class IntroductionScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction_screen);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        },2000);
    }

    @Override
    public void onBackPressed() {
        finish();
        finishAffinity();
    }
}
