package com.vegetarianbaconite.jenscout;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vegetarianbaconite.blueapi.AsyncBlueAPI;
import com.vegetarianbaconite.blueapi.BlueRequester;
import com.vegetarianbaconite.blueapi.beans.Match;

import java.util.SortedSet;
import java.util.TreeSet;

public class SingleMatchFinder extends AppCompatActivity implements View.OnClickListener, BlueRequester,
        DialogInterface.OnClickListener {
    AsyncBlueAPI api;
    EditText comp, match;
    TextView red1, red2, red3, blue1, blue2, blue3;
    Button go;
    TableLayout t;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_match);
        api = new AsyncBlueAPI(this, "DominicCanora", "JenScout", "1");

        comp = (EditText) findViewById(R.id.smComp);
        match = (EditText) findViewById(R.id.smMatch);
        go = (Button) findViewById(R.id.smGo);
        t = (TableLayout) findViewById(R.id.smTable);

        red1 = (TextView) findViewById(R.id.smRedOne);
        red2 = (TextView) findViewById(R.id.smRedTwo);
        red3 = (TextView) findViewById(R.id.smRedThree);
        blue1 = (TextView) findViewById(R.id.smBlueOne);
        blue2 = (TextView) findViewById(R.id.smBlueTwo);
        blue3 = (TextView) findViewById(R.id.smBlueThree);

        go.setOnClickListener(this);
        comp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.equals(go)) api.getMatch(comp.getText().toString().trim().toLowerCase(),
                "qm" + match.getText().toString().trim().toLowerCase());

        if(view.equals(comp)) {
            comp.setText("");
            AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle("Select Event")
                    .setItems(R.array.events, this)
                    .create();
            d.show();
        }
    }

    @Override
    public void onResponse(String s) {
        final Match m = new Gson().fromJson(s, Match.class);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    red1.setText(m.getAlliances().getRed().getTeams()[0].substring(3));
                    red2.setText(m.getAlliances().getRed().getTeams()[1].substring(3));
                    red3.setText(m.getAlliances().getRed().getTeams()[2].substring(3));
                    blue1.setText(m.getAlliances().getBlue().getTeams()[0].substring(3));
                    blue2.setText(m.getAlliances().getBlue().getTeams()[1].substring(3));
                    blue3.setText(m.getAlliances().getBlue().getTeams()[2].substring(3));

                    SortedSet<String> keys = new TreeSet<>(m.getScoreBreakdown().getRed().keySet());
                    for (String key : keys) {
                        TableRow tr = new TableRow(SingleMatchFinder.this);

                        TextView kv = new TextView(SingleMatchFinder.this);
                        TextView rv = new TextView(SingleMatchFinder.this);
                        TextView bv = new TextView(SingleMatchFinder.this);

                        kv.setText(key);

                        rv.setText(m.getScoreBreakdown().getRed().get(key));
                        rv.setTextColor(Color.RED);

                        bv.setText(m.getScoreBreakdown().getBlue().get(key));
                        bv.setTextColor(Color.BLUE);

                        tr.addView(kv);
                        tr.addView(rv);
                        tr.addView(bv);

                        t.addView(tr);
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(SingleMatchFinder.this, "Invalid Match Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onError(Exception e) {
        Log.w("Network Error", e);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        comp.setText(getResources().getStringArray(R.array.eventKeys)[i]);
    }
}
