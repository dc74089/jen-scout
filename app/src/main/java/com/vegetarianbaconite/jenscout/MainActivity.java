package com.vegetarianbaconite.jenscout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.vegetarianbaconite.blueapi.SynchronousBlueAPI;

public class MainActivity extends AppCompatActivity {

    public static SynchronousBlueAPI api = new SynchronousBlueAPI("DominicCanora", "JenScout", "1");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent().setClass(this, SingleMatchFinder.class));
        //setContentView(R.layout.activity_main);
    }


}
