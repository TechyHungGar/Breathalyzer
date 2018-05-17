package com.example.pierremichel.breathalyzerskeleton;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.ForcedDataProducer;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.Gpio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;


public class ReadActivity extends AppCompatActivity implements ServiceConnection {
    private BtleService.LocalBinder serviceBinder;
    MetaWearBoard board;
    private Button button1;
    ArrayList<Float> bData = new ArrayList();
    private Handler mHandler;
    private Handler hReadADC;
    private TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        //set up the dialog that will be used when reading the BAC (this is the little
        //menu with progress that pops up when you are reading BAC)
        final ProgressDialog readingDialog = new ProgressDialog(this);

        readingDialog.setTitle("Reading BAC");
        readingDialog.setMessage("Please Blow Into Sensor for 10 seconds");
        readingDialog.setCancelable(false);
        readingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        readingDialog.setCanceledOnTouchOutside(false);
        readingDialog.setIndeterminate(false);
        readingDialog.setProgress(0);
        readingDialog.setMax(100);

        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);

        //create button to be used on main screen
        final Button bStart = (Button) findViewById(R.id.bStart);

        //create a poller to test for click
        bStart.setOnClickListener(new Button.OnClickListener() {

            //when button is click go to grade activity
            public void onClick(View v) {

                //show the reading BAC dialog
                readingDialog.show();

                //this runnable will cancel the dialog after 11 seconds to allow for the
                //10 seconds of reading
                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        readingDialog.setProgress(0);
                        readingDialog.cancel();

                    }
                };

                //this is a runnable the will read the adc values
                Runnable readADC = new Runnable() {
                    public void run() {
                        final Gpio gpio = board.getModule(Gpio.class);  //create a pin object for module
                        ForcedDataProducer adc = gpio.pin((byte) 0).analogAbsRef();  //set the pin module as a adc input
                        readingDialog.incrementProgressBy(1);   //increment the dialog progess by 1%
                        adc.read(); //read the adc

                    }
                };



                //create a handler so that we can call the runnable
                mHandler = new Handler();
                hReadADC = new Handler();

                //erase arraylist with all the data to allow for more data
                bData.clear();

                //run the read ADC runnable 100 times for 100 readings
                //the delay between each will be incremented so that the run
                //at different times
                for (int x = 0; x < 100; x++)
                    hReadADC.postDelayed(readADC, 100 * x);

                //create a handler for the progress dialog canceling
                Handler pdCanceller = new Handler();

                //close the progress dialog after 11 seconds
                pdCanceller.postDelayed(progressRunnable, 11000);

                //update the screen with data after 11 seconds
                //must be done outside the OnCreate function to actually update
                mHandler.postDelayed(mUpdate, 11000);

            }
        });

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) service;

        //mac address of bt module
        final String MW_MAC_ADDRESS= "C4:71:24:1A:ED:F6";

        //create bluetooth pairing for android
        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        //create connected! toast (for bottom of screen)
        Context context = getApplicationContext();
        CharSequence text = "Connected!";
        int duration = Toast.LENGTH_SHORT;
        final Toast toast = Toast.makeText(context, text, duration);

        //create a dialog for connecting

        final ProgressDialog connectDialog = new ProgressDialog(this);
        connectDialog.setTitle("Connecting");
        connectDialog.setMessage("Please Wait");
        connectDialog.setCancelable(false);
        connectDialog.setCanceledOnTouchOutside(false);
        connectDialog.setIndeterminate(true);

        connectDialog.show();

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);


        //connect to the board and run a task (function) once it is done
        board.connectAsync().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                //make sure its connected
                if(board.isConnected()) {
                    Log.i("MainActivity", "Connected"); //this message will appear in the android debug logcat
                    connectDialog.cancel(); //close the connecting dialog
                    toast.show(); //show the connected! icon
                    btConnected(); //run pin setups after it is connected!
                }
                //it is not connected
                else {
                    Log.i("MainActivity", "Could Not connect");
                    Intent nextIntent = new Intent (ReadActivity.this,MenuActivity.class);
                    ReadActivity.this.startActivity(nextIntent);
                }

                return null;
            }
        });

        //create a message in LogCat when the board disconnects unexpectedly
        board.onUnexpectedDisconnect(new MetaWearBoard.UnexpectedDisconnectHandler() {
            @Override
            public void disconnected(int status) {
                Log.i("MainActivity", "Unexpectedly lost connection: " + status);
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                Log.i("MainActivity", "Disconnected");
                return null;
            }
        });

    }

    void btConnected()
    {
        //this function will override a function that reroutes
        //the adc values to Logcat and the arraylist
        //->everytime we call adc.read() it will save to the arraylist..
        final Gpio gpio = board.getModule(Gpio.class);
        // Get producer for analog adc data
        ForcedDataProducer adc = gpio.pin((byte) 0).analogAbsRef();

        adc.addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object ... env) {
                        Log.i("MainActivity", "adc = " + data.value(Float.class));
                        bData.add(data.value(Float.class));
                    }
                });
            }
        });

    }

    //this module will update the textview and show the user
    //if they are higher or lower from 0.08%
    private Runnable mUpdate = new Runnable() {
        public void run() {

            //get the lowest value
            float lowest = bData.get(0);
            for(int x = 0; x < bData.size(); x++)
                if(lowest > bData.get(x))
                    lowest = bData.get(x);

            SQLiteDatabase readingsDB = openOrCreateDatabase("courseDB", MODE_PRIVATE, null);
            String sql;

            double temp = 0.272941 - (0.0941176*lowest);
            if(temp > 0.02)
                temp = 0.2;

            float BAC = (float) temp;

            BAC = round(BAC,2);

            sql = "INSERT INTO Reading (drinkingDate, drinkingTime, BAC) " +
                    "VALUES ('" + getCurrentDate() + "', '"+ getCurrentTime() +"' , '"+ BAC +"');";
            readingsDB.execSQL(sql);

            if(BAC < 0.08)
            {
                Intent nextIntent = new Intent (ReadActivity.this,MainActivity.class);
                ReadActivity.this.startActivity(nextIntent);
            }
            else
            {
                Intent nextIntent = new Intent (ReadActivity.this,ResultsActivity.class);
                ReadActivity.this.startActivity(nextIntent);
            }


        }
    };

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue(); }

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

    String getCurrentTime()
    {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int dayHour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int AmPm = c.get(Calendar.AM_PM);



        String currentTime = Integer.toString(hour);
        currentTime += ":";
        currentTime += Integer.toString(minute);

        if(AmPm == 0)
            currentTime += "am";
        else
            currentTime += "pm";

        return currentTime;
    }

}
