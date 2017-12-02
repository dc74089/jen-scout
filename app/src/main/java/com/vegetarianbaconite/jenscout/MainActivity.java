package com.vegetarianbaconite.jenscout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button setTeam;
    TextView teamNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTeam = (Button) findViewById(R.id.homeSetTeam);
        teamNo = (TextView) findViewById(R.id.homeTeam);

        setTeam.setOnClickListener(this);

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


    @Override
    public void onClick(View v) {

    }
}
