package com.example.pierremichel.breathalyzerskeleton;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize database
        int userID = initializeDatabase();

        //if user is already logged in go to menu
        if(userID != -1)
        {
            Intent nextIntent = new Intent (MainActivity.this,MenuActivity.class);
            MainActivity.this.startActivity(nextIntent);
        }

        final Button bLogin = (Button) findViewById(R.id.bLogin);
        final TextView invalid = (TextView) findViewById(R.id.tvInvalid);

        invalid.setVisibility(View.INVISIBLE);

        bLogin.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v)  {

                final EditText username = (EditText) findViewById(R.id.etUsername);
                final EditText password = (EditText) findViewById(R.id.etPassword);

                Cursor dataRow = dataBase.rawQuery("SELECT * FROM User WHERE username = '"+username.getText().toString()+"' AND password = '"+password.getText().toString()+"' ", null);

                if(dataRow.moveToFirst()) {
                    dataBase.execSQL("UPDATE User SET logged = '1' WHERE _id = '"+dataRow.getInt(0)+"';");

                    Intent nextIntent = new Intent (MainActivity.this,MenuActivity.class);
                    MainActivity.this.startActivity(nextIntent);
                }
                else
                {
                    invalid.setVisibility(View.VISIBLE);
                }


            }
        });

    }

    int initializeDatabase()
    {

        dataBase = openOrCreateDatabase("courseDB", MODE_PRIVATE, null);

        //dataBase.execSQL("DROP TABLE User;");
        //dataBase.execSQL("INSERT INTO User (username, password, age) VALUES ('mike', 'test', '25');");
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS User(_id INTEGER PRIMARY KEY, username STRING, password STRING, age INTEGER, logged BOOLEAN DEFAULT '0');");
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS Session(_id INTEGER PRIMARY KEY, userID INTEGER, drinkingDateTime STRING, leavingDateTime STRING);");
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS Reading(_id INTEGER PRIMARY KEY, sessiongID INTEGER, drinkingTime STRING, BAC FLOAT);");

        Cursor dataRow = dataBase.rawQuery("SELECT * FROM User WHERE logged = '1'", null);

        //get the user ID for session use
        if(dataRow.moveToFirst()) {
            return dataRow.getInt(0);
        }

        return -1;
    }
}
