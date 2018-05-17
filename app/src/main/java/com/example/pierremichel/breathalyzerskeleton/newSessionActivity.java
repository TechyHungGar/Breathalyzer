package com.example.pierremichel.breathalyzerskeleton;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class newSessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_session);

        Calendar c = Calendar.getInstance();

        final Button bEnter = (Button) findViewById(R.id.bEnter);
        final NumberPicker timePicker = (NumberPicker) findViewById(R.id.numPicker);

        timePicker.setMinValue(0);
        timePicker.setMaxValue(11);

        String[] values = {"0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5", "6"};
        final double[] floatValues = {0.5,1,1.5,2,2.5,3,3.5,4,4.5,5,5.5,6};

        timePicker.setDisplayedValues(values);

        final  AlertDialog.Builder builder = new AlertDialog.Builder(newSessionActivity.this, R.style.MyDialogTheme);



        bEnter.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v)  {
               String s;
                Format formatter;
                Calendar calendar = Calendar.getInstance();
                int minutes = 0;
                if(floatValues[timePicker.getValue()] %1 != 0)
                {
                    minutes = 30;
                }
                calendar.add(Calendar.MINUTE, minutes);
                calendar.add(Calendar.HOUR, (int) floatValues[timePicker.getValue()]);
                formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                s = formatter.format(calendar.getTime()); // 08:00:00



                builder.setMessage("Set leaving time to: " + s + "?")
                        .setCancelable(false)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();





            }
        });
    }
}
