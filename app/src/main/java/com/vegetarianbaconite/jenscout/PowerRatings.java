package com.vegetarianbaconite.jenscout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dominicc.me.PowerCalc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.swagger.client.ApiCallback;
import io.swagger.client.ApiException;
import io.swagger.client.api.EventApi;
import io.swagger.client.model.Event;
import io.swagger.client.model.Team;

public class PowerRatings extends AppCompatActivity implements View.OnClickListener,
        Dialog.OnClickListener, ApiCallback<List<Event>> {

    PowerCalc powerCalc;
    EventApi api = new EventApi(Utils.api);
    TreeMap<String, Event> nameEventMap;
    Map<Integer, Team> teamMap;

    EditText year, comp, stat;
    Button go;
    TableLayout table;

    ArrayList<String> stats;
    Dialog compDialog, statDialog;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_ratings);

        year = findViewById(R.id.prYear);
        comp = findViewById(R.id.prComp);
        stat = findViewById(R.id.prStat);
        go = findViewById(R.id.prGo);
        table = findViewById(R.id.prTable);

        year.setOnClickListener(this);
        comp.setOnClickListener(this);
        stat.setOnClickListener(this);
        go.setOnClickListener(this);
    }

    public void handleResults(final Map<Integer, Double> results) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<Integer> teamNums = new ArrayList<>(results.keySet());

                Collections.sort(teamNums, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer val1, Integer val2) {
                        return (int) Math.round(1000 * (results.get(val2) - results.get(val1)));
                    }
                });

                LayoutInflater inflater = getLayoutInflater();
                table.removeAllViews();

                int i = 1;

                for (Integer teamNo : teamNums) {
                    View row = inflater.inflate(R.layout.power_rating_table_row, table, false);

                    ((TextView) row.findViewById(R.id.prtrRank)).setText(i + ":");
                    ((TextView) row.findViewById(R.id.prtrTeam)).setText("" + teamNo);
                    ((TextView) row.findViewById(R.id.prtrName)).setText(teamMap.get(teamNo).getNickname());
                    ((TextView) row.findViewById(R.id.prtrVal)).setText(String.format("%.3f", results.get(teamNo)));

                    table.addView(row);

                    i++;
                }

                pd.dismiss();
            }
        });


        /*
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

                    rankTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    teamTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    powerTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

                    rankTV.setText(""+i+":");
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
        */
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.prComp) { //Set Competition
            try {
                Integer.parseInt(year.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please make sure you've typed in a valid year.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                api.getEventsByYearAsync(new BigDecimal(year.getText().toString()), null,
                        this);
            } catch (ApiException e) {
                e.printStackTrace();
            }

            pd = new ProgressDialog(this);
            pd.setMessage("Downloading Events");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();

        } else if (view.equals(stat)) { //Set Stat
            stat.setText("");
            statDialog = new AlertDialog.Builder(this)
                    .setTitle("Select Stat for Power Rating")
                    .setItems(stats.toArray(new String[0]), this)
                    .create();
            statDialog.show();
        } else if (view.equals(go)) { //Compute Power Ratings
            pd = new ProgressDialog(PowerRatings.this);
            pd.setMessage("Please wait, doing math. ");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<Integer, Double> results = powerCalc.getForKey(stat.getText().toString());
                        handleResults(results);
                    } catch (Exception e) {
                        e.printStackTrace();
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
            final Event e = (Event) nameEventMap.values().toArray()[i];
            comp.setText(e.getShortName());

            pd = new ProgressDialog(this);
            pd.setMessage("Please wait, creating match matrix. This involves a lot of math, and can take up to 90 seconds. ");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Team> teams = api.getEventTeams(e.getKey(), null);
                        teamMap = new HashMap<>();
                        for (Team t : teams)
                            teamMap.put(t.getTeamNumber(), t);

                        Log.d("JenScout", "Done fetching teams. Creating Cholesky...");

                        powerCalc = new PowerCalc(Secret.apiKey, e.getKey(), true);

                        stats = new ArrayList<>();
                        stats.addAll(powerCalc.getStats());

                        PowerRatings.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                            }
                        });
                    } catch (ApiException e) {
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        pd.dismiss();
                        Toast.makeText(PowerRatings.this, "It looks like that event doesn't have any match data yet", Toast.LENGTH_SHORT).show();
                    }

                }
            }).start();
        } else if (dialogInterface.equals(statDialog)) {
            stat.setText(stats.get(i));
        }
    }

    @Override
    public void onSuccess(List<Event> events, int i, Map<String, List<String>> map) {
        nameEventMap = new TreeMap<>();

        for (Event e : events) {
            nameEventMap.put(e.getShortName(), e);
        }

        pd.dismiss();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                compDialog = new AlertDialog.Builder(PowerRatings.this)
                        .setTitle("Select Event")
                        .setItems(nameEventMap.keySet().toArray(new String[0]), PowerRatings.this)
                        .show();
            }
        });
    }

    @Override
    public void onFailure(ApiException e, int i, Map<String, List<String>> map) {
        e.printStackTrace();

        try {
            pd.dismiss();
        } catch (Exception ignored) {

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PowerRatings.this, "It looks like there was an error fetching some information.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUploadProgress(long l, long l1, boolean b) {

    }

    @Override
    public void onDownloadProgress(long l, long l1, boolean b) {

    }
}
