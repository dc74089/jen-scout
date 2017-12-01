package com.vegetarianbaconite.jenscout;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import io.swagger.client.ApiCallback;
import io.swagger.client.ApiException;
import io.swagger.client.api.MatchApi;
import io.swagger.client.model.Match;

public class SingleMatchFinder extends AppCompatActivity implements View.OnClickListener,
        DialogInterface.OnClickListener, ApiCallback<Match> {
    EditText comp, match;
    TextView red1, red2, red3, blue1, blue2, blue3;
    Button go;
    TableLayout t;

    MatchApi api = new MatchApi(Utils.api);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_match);

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
    public void onFailure(ApiException e, int i, Map<String, List<String>> map) {

    }

    @Override
    public void onSuccess(final Match m, int i, Map<String, List<String>> map) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    red1.setText(m.getAlliances().getRed().getTeamKeys().get(0).substring(3));
                    red1.setText(m.getAlliances().getRed().getTeamKeys().get(1).substring(3));
                    red1.setText(m.getAlliances().getRed().getTeamKeys().get(2).substring(3));
                    blue1.setText(m.getAlliances().getRed().getTeamKeys().get(0).substring(3));
                    blue2.setText(m.getAlliances().getRed().getTeamKeys().get(1).substring(3));
                    blue3.setText(m.getAlliances().getRed().getTeamKeys().get(2).substring(3));

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
    public void onClick(View view) {
        if (view.equals(go)) try {
            api.getMatchAsync(comp.getText().toString().trim().toLowerCase(),
                    "qm" + match.getText().toString().trim().toLowerCase(), this);
        } catch (ApiException e) {
            e.printStackTrace();
        }

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
    public void onClick(DialogInterface dialogInterface, int i) {
        comp.setText(getResources().getStringArray(R.array.eventKeys)[i]);
    }

    @Override
    public void onUploadProgress(long l, long l1, boolean b) {

    }

    @Override
    public void onDownloadProgress(long l, long l1, boolean b) {

    }
}
