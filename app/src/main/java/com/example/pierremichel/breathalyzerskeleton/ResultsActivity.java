package com.example.pierremichel.breathalyzerskeleton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);


        final Button bUber = (Button) findViewById(R.id.bUber);
        final Button bBack = (Button) findViewById(R.id.bBack);

        bBack.setOnClickListener(new Button.OnClickListener() {


            public void onClick(View v) {
                Intent nextIntent = new Intent (ResultsActivity.this,MainActivity.class);
                ResultsActivity.this.startActivity(nextIntent);
            }
        });

        bUber.setOnClickListener(new Button.OnClickListener() {


            public void onClick(View v)  {

                PackageManager pm = getPackageManager();
                try {
                    pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
                    String uri = "uber://?action=setPickup&pickup=my_location";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                } catch (PackageManager.NameNotFoundException e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ubercab")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.ubercab")));
                    }
                }

            }
        });
    }
}
