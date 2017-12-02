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

import com.crashlytics.android.Crashlytics;
import com.vegetarianbaconite.powercalc.PowerCalc;
import com.vegetarianbaconite.powercalc.exceptions.NoMatchesException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.fabric.sdk.android.Fabric;
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
    Dialog yearDialog, compDialog, statDialog;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
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
                LayoutInflater inflater = getLayoutInflater();
                table.removeAllViews();

                int i = 1;

                for (Integer teamNo : results.keySet()) {
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
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.prYear) { //Set Year
            yearDialog = new AlertDialog.Builder(this)
                    .setTitle("Select Year")
                    .setItems(R.array.years, this)
                    .show();
        } else if (view.getId() == R.id.prComp) { //Set Competition
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
            if (comp.getText().toString().isEmpty()) {
                Toast.makeText(this, "Make sure you've selected a competition and year",
                        Toast.LENGTH_SHORT).show();
            }

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
                        Map<Integer, Double> results = powerCalc.getForKeySorted(stat.getText().toString());
                        handleResults(results);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
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
            pd.setMessage("Please wait, creating match matrix. This involves a lot of math, and can take up to 30 seconds. ");
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

                        try {
                            powerCalc = new PowerCalc(Secret.apiKey, e.getKey(), true);
                        } catch (NoMatchesException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PowerRatings.this, "It looks like that event hasn't had any qualifying matches yet. ",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        stats = new ArrayList<>();
                        stats.add("opr");
                        stats.addAll(powerCalc.getStats());

                        PowerRatings.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                            }
                        });
                    } catch (ApiException e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        pd.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PowerRatings.this,
                                        "It looks like that event doesn't have any match data yet",
                                        Toast.LENGTH_SHORT).show();
                                comp.setText("");
                            }
                        });
                    }

                }
            }).start();
        } else if (dialogInterface.equals(statDialog)) {
            stat.setText(stats.get(i));
        } else if (dialogInterface.equals(yearDialog)) {
            year.setText("" + (2016 + i));
        }
    }

    @Override
    public void onSuccess(List<Event> events, int i, Map<String, List<String>> map) {
        nameEventMap = new TreeMap<>();

        for (Event e : events) {
            //IDK why all these checks are necessary but they are.
            if (e != null && e.getShortName() != null && !e.getShortName().trim().isEmpty())
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
