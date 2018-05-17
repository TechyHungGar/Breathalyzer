package com.example.pierremichel.breathalyzerskeleton;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;


public class MenuActivity extends AppCompatActivity{

    BarChart chart;
    ArrayList<String> lables;
    ArrayList<BarEntry> BARENTRY;
    BarDataSet Bardataset ;
    BarData BARDATA ;
    SQLiteDatabase dataBase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        int userID = initializeDatabase();

        final Button bStart = (Button) findViewById(R.id.bStart);
        final Button bNext = (Button) findViewById(R.id.bNext);
        final Button bEraseData = (Button) findViewById(R.id.bEraseData);

        chart = (BarChart) findViewById(R.id.lineChart);

        bNext.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v)  {

                Intent nextIntent = new Intent (MenuActivity.this,ReadActivity.class);
                MenuActivity.this.startActivity(nextIntent);
            }
        });

        bStart.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v)  {

                Intent nextIntent = new Intent (MenuActivity.this,newSessionActivity.class);
                MenuActivity.this.startActivity(nextIntent);
            }
        });

        bEraseData.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v)  {
                SQLiteDatabase readingsDB = openOrCreateDatabase("courseDB", MODE_PRIVATE, null);
                readingsDB.execSQL("DROP TABLE IF EXISTS Reading;");

                Intent nextIntent = new Intent (MenuActivity.this,MenuActivity.class);
                MenuActivity.this.startActivity(nextIntent);
            }
        });

        lables = new ArrayList<>();
        BARENTRY = new ArrayList<>();

        Cursor dataRow = dataBase.rawQuery("SELECT * FROM Session WHERE drinkingDate = " + getCurrentDate(), null);

        int j = 0;

        if(dataRow.moveToFirst())
        {
            do {
                BARENTRY.add(new BarEntry(j, dataRow.getFloat(3)));
                lables.add(dataRow.getString(2));
                j++;
                Log.i("MainActivity", "value: " + dataRow.getFloat(3));
            } while (dataRow.moveToNext());

        }

        bStart.setVisibility(View.GONE);
        bEraseData.setVisibility(View.GONE);

        if(dataRow.getCount() == 0)
        {
            bStart.setVisibility(View.VISIBLE);
            chart.setVisibility(View.INVISIBLE);
        }
        else
        {
            bEraseData.setVisibility(View.VISIBLE);
            Bardataset = new BarDataSet(BARENTRY, "BAC per reading");
            BARDATA = new BarData(Bardataset);
            XAxis xAxis = chart.getXAxis();
            YAxis yAxis = chart.getAxisLeft();
            xAxis.setValueFormatter(new MyYAxisValueFormatter(lables));
            Bardataset.setColors(Color.BLUE);

            chart.setData(BARDATA);
            chart.setDescription(null);
            xAxis.setGranularity(1f);
            yAxis.setGranularity(0.01f);
            chart.animateY(3000);

        }
    }

    String getCurrentDate()
    {
        //get current date
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int day = c.get(Calendar.DATE);

        String out = Integer.toString(year);
        out += Integer.toString(month);
        out += Integer.toString(day);

        return out;

    }

    int initializeDatabase()
    {

        dataBase = openOrCreateDatabase("courseDB", MODE_PRIVATE, null);

        Cursor dataRow = dataBase.rawQuery("SELECT * FROM User WHERE logged = '1'", null);

        //get the user ID for session use
        if(dataRow.moveToFirst()) {
            return dataRow.getInt(0);
        }

        return -1;
    }


}
