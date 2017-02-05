package com.vegetarianbaconite.jenscout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.vegetarianbaconite.blueapi.SynchronousBlueAPI;

import org.apache.commons.math3.analysis.function.Power;

public class MainActivity extends AppCompatActivity {

    public static SynchronousBlueAPI api = new SynchronousBlueAPI("DominicCanora", "JenScout", "1");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.homeSingle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SingleMatchFinder.class));
            }
        });

        findViewById(R.id.homePower).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PowerRatings.class));
            }
        });
    }


}
