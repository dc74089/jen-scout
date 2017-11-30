package com.vegetarianbaconite.jenscout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.swagger.client.ApiClient;

public class MainActivity extends AppCompatActivity {

    public static ApiClient api;

    static {
        api = new ApiClient();
        api.setApiKey(Secret.apiKey);
    }

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
