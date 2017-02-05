package com.vegetarianbaconite.jenscout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicc.me.PowerCalc;
import com.vegetarianbaconite.blueapi.BlueRequester;
import com.vegetarianbaconite.blueapi.SynchronousBlueAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PowerRatings extends AppCompatActivity implements BlueRequester, View.OnClickListener, Dialog.OnClickListener {
    PowerCalc powerCalc;
    SynchronousBlueAPI sApi;
    EditText comp, stat;
    Button go;
    TableLayout table;

    ArrayList<String> stats = new ArrayList<>();
    Dialog compDialog, statDialog;
    ProgressDialog pd;

    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_ratings);

        sApi = new SynchronousBlueAPI("DominicCanora", "JenScout", "1");

        comp = (EditText) findViewById(R.id.prComp);
        stat = (EditText) findViewById(R.id.prStat);
        go = (Button) findViewById(R.id.prGo);
        table = (TableLayout) findViewById(R.id.prTable);

        comp.setOnClickListener(this);
        stat.setOnClickListener(this);
        go.setOnClickListener(this);
    }

    @Override
    public void onResponse(String s) {

    }

    @Override
    public void onError(Exception e) {

    }

    public void handleResults(final Map<Integer, Double> results) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                table.removeAllViews();

                List<Integer> teamNums = new ArrayList<>(results.keySet());
                Collections.sort(teamNums, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer val1, Integer val2) {
                        return (int) Math.round(1000 *(results.get(val2) - results.get(val1)));
                    }
                });
                int i = 1;
                for (Integer teamNo : teamNums) {
                    TableRow tr = new TableRow(PowerRatings.this);
                    TextView rankTV = new TextView(PowerRatings.this);
                    TextView teamTV = new TextView(PowerRatings.this);
                    TextView powerTV = new TextView(PowerRatings.this);

                    rankTV.setLayoutParams(layoutParams);
                    teamTV.setLayoutParams(layoutParams);
                    powerTV.setLayoutParams(layoutParams);

                    rankTV.setText(""+i);
                    rankTV.setTypeface(Typeface.DEFAULT_BOLD);
                    teamTV.setText(""+teamNo);
                    powerTV.setText(""+Math.round(results.get(teamNo)*1000d)/1000d);

                    tr.addView(rankTV);
                    tr.addView(teamTV);
                    tr.addView(powerTV);

                    table.addView(tr);

                    i++;
                }

                pd.dismiss();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.equals(comp)) {
            comp.setText("");
            compDialog = new AlertDialog.Builder(this)
                    .setTitle("Select Event")
                    .setItems(R.array.events, this)
                    .create();
            compDialog.show();
        } else if (view.equals(stat)) {
            stat.setText("");
            statDialog = new AlertDialog.Builder(this)
                    .setTitle("Select Stat for Power Rating")
                    .setItems(stats.toArray(new String[stats.size()]), this)
                    .create();
            statDialog.show();
        } else if (view.equals(go)) {
            pd = new ProgressDialog(PowerRatings.this);
            pd.setIndeterminate(true);
            pd.setMessage("Please wait, doing math. ");
            pd.setCancelable(false);
            pd.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<Integer, Double> results = powerCalc.getForKey(stat.getText().toString());
                        handleResults(results);
                    } catch (Exception e) {
                        PowerRatings.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PowerRatings.this, "Please select a numeric field. ",
                                        Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(dialogInterface.equals(compDialog)) {
            comp.setText(getResources().getStringArray(R.array.eventKeys)[i]);

            pd = new ProgressDialog(this);
            pd.setIndeterminate(false);
            pd.setMessage("Please wait, loading event.");
            pd.setCancelable(false);
            pd.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    powerCalc = new PowerCalc(comp.getText().toString(), true);
                    stats.addAll(sApi.getMatch(comp.getText().toString(), "qm1").getScoreBreakdown().getRed().keySet());
                    PowerRatings.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                        }
                    });
                }
            }).start();
        } else if (dialogInterface.equals(statDialog)) {
            stat.setText(stats.get(i));
        }
    }
}
