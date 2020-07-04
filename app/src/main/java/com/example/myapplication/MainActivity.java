package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        // tv=(TextView)findViewById(R.id.tv);
        iv=(ImageView)findViewById(R.id.imageView);
        Animation animation= AnimationUtils.loadAnimation(this,R.anim.mytransition);
        // tv.startAnimation(animation);
        iv.startAnimation(animation);
       final Intent intent= new Intent(this,login.class);
        Thread timer=new Thread(){
            @Override
            public void run() {
                try
                {
                    sleep(4000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally {
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();


    }
}
