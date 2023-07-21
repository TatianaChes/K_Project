package com.example.qr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGHT = 3000;

    // картинки заставки
    ImageView del;    ImageView kr;    ImageView eta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        // инициализация
        del = findViewById(R.id.del);
        kr = findViewById(R.id.kr);
        eta = findViewById(R.id.eta);

        // добавление анимации на картинки
        Animation anim;
        Animation anim2;
        anim = AnimationUtils.loadAnimation(this, R.anim.myrotate);
        anim2 = AnimationUtils.loadAnimation(this, R.anim.mytrans);
        // запуск анимации
        del.startAnimation(anim);
        eta.startAnimation(anim2);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent mainIntent = new Intent(SplashScreen.this, Menu.class);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }

        }, SPLASH_DISPLAY_LENGHT);

    }


    @Override

    public void onBackPressed() {

        super.onBackPressed();

    }

}



