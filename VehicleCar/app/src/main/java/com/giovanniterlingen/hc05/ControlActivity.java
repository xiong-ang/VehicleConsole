package com.giovanniterlingen.hc05;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;
import java.io.IOException;
import java.util.UUID;

/**
 * An easy and good implementation for the HC-05 bluetooth component
 *
 * @author Giovanni Terlingen
 */
public class ControlActivity extends AppCompatActivity implements BluetoothStateCallback {

    private static final String device = "HC-05";
    private static final UUID BT_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");

    private static TextView connectionState;
    private volatile boolean bSuccess=false;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothSocket bluetoothSocket;

    private static ConnectTask connectTask;
    private static WriterThread writerThread;

    public static CarState mCarState=new CarState();

    //public SeekBar speedSeekBar;
    //public SeekBar steerSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //去掉虚拟按键全屏显示
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // assign the widgets specified in the layout xml file
        ControlActivity.connectionState = (TextView) findViewById(R.id.connection_state);

        Button highBtn = (Button) findViewById(R.id.highSpeed);
        highBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.speedSignal=CarState.highSpeedSignal;
                ControlActivity.this.sendCommand(CarState.GetState(mCarState.speedSignal));
                updateUI();
            }
        });
        Button midBtn = (Button) findViewById(R.id.midSpeed);
        midBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.speedSignal=CarState.midSpeedSignal;
                ControlActivity.this.sendCommand(CarState.GetState(mCarState.speedSignal));
                updateUI();
            }
        });
        Button lowBtn = (Button) findViewById(R.id.lowSpeed);
        lowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.speedSignal=CarState.lowSpeedSignal;
                ControlActivity.this.sendCommand(CarState.GetState(mCarState.speedSignal));
                updateUI();
            }
        });
        Button stopBtn = (Button) findViewById(R.id.stopSpeed);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.speedSignal=CarState.stopSpeedSignal;
                ControlActivity.this.sendCommand(CarState.GetState(mCarState.speedSignal));
                updateUI();
            }
        });
        Button backBtn = (Button) findViewById(R.id.backSpeed);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.speedSignal=CarState.reverseSpeedSignal;
                ControlActivity.this.sendCommand(CarState.GetState(mCarState.speedSignal));
                updateUI();
            }
        });
/*
        speedSeekBar = (SeekBar) findViewById(R.id.speed_seekbar);
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Speed:15-29
                int speed=15+progress;
                mCarState.speed=speed;
                ControlActivity.this.sendCommand("Speed: "+String.valueOf(speed)+" Steer: "+mCarState.steer);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        steerSeekBar = (SeekBar) findViewById(R.id.direction_seekbar);
        steerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Steer:31-49
                int steer=progress+31;
                mCarState.steer=steer;
                ControlActivity.this.sendCommand("Speed: "+mCarState.speed+" Steer: "+String.valueOf(steer));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
*/
/*
        Button midBtn = (Button) findViewById(R.id.middle);
        midBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.steer=40;
                ControlActivity.this.sendCommand("Speed: "+mCarState.speed+" Steer:"+mCarState.steer);
                updateUI();
            }
        });
        Button midBtn = (Button) findViewById(R.id.middle);
        midBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.steer=40;
                ControlActivity.this.sendCommand("Speed: "+mCarState.speed+" Steer:"+mCarState.steer);
                updateUI();
            }
        });
        Button midBtn = (Button) findViewById(R.id.middle);
        midBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.steer=40;
                ControlActivity.this.sendCommand("Speed: "+mCarState.speed+" Steer:"+mCarState.steer);
                updateUI();
            }
        });
        Button midBtn = (Button) findViewById(R.id.middle);
        midBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarState.steer=40;
                ControlActivity.this.sendCommand("Speed: "+mCarState.speed+" Steer:"+mCarState.steer);
                updateUI();
            }
        });
        */
        ButtonListener b = new ButtonListener();

        Button leftLargeSteerBtn = (Button) findViewById(R.id.leftLargeSteer);
        leftLargeSteerBtn.setOnClickListener(b);
        leftLargeSteerBtn.setOnTouchListener(b);

        Button leftSmallSteerBtn = (Button) findViewById(R.id.leftSmallSteer);
        leftSmallSteerBtn.setOnClickListener(b);
        leftSmallSteerBtn.setOnTouchListener(b);

        Button rightSmallSteerBtn = (Button) findViewById(R.id.rightSmallSteer);
        rightSmallSteerBtn.setOnClickListener(b);
        rightSmallSteerBtn.setOnTouchListener(b);

        Button rightLargeSteerBtn = (Button) findViewById(R.id.rightLargeSteer);
        rightLargeSteerBtn.setOnClickListener(b);
        rightLargeSteerBtn.setOnTouchListener(b);

        if(ControlActivity.bluetoothAdapter == null)
            ControlActivity.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // register these receivers, we need them for setting up connections automatically
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        // try to connect
        if(ControlActivity.connectTask ==null)
        {
            ControlActivity.connectTask = new ConnectTask();
            ControlActivity.connectTask.execute();
        }

        updateUI();
    }

    public void updateUI() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //speedSeekBar.setProgress(mCarState.speed-15);
                //steerSeekBar.setProgress(mCarState.steer-31);

                if(bSuccess) {
                    ControlActivity.connectionState.setBackgroundColor(android.graphics.Color.GREEN);
                    ControlActivity.connectionState.setTextColor(android.graphics.Color.BLACK);
                } else {
                    ControlActivity.connectionState.setBackgroundColor(android.graphics.Color.RED);
                    ControlActivity.connectionState.setTextColor(android.graphics.Color.WHITE);
                }
            }
        });
    }

    /**
     * Send a command to the writerThread, and the command will be processed there.
     * Note that the thread must be running, otherwise the message is not sent, nor saved.
     *
     * @param command the command we want to send
     */
    private void sendCommand(String command) {
        if (ControlActivity.writerThread != null) {
            ControlActivity.connectionState.setText("trying to send command \"" + command + "\"");
        } else {
            ControlActivity.connectionState.setText("could not send command \"" + command + "\" because there is no socket connection");
        }
    }

    /**
     * When the writerThread failed writing, this method is called.
     *
     * @param e the exception message
     */
    @Override
    public void onWriteFailure(final String e) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText("failure: " + e + ", reconnecting...");
                if (ControlActivity.connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                    ControlActivity.connectTask.cancel(true);
                }
                ControlActivity.connectTask = new ConnectTask();
                ControlActivity.connectTask.execute();
                bSuccess=false;
            }
        });
    }

    /**
     * When the writerThread writes successful on the socket, this method is called.
     *
     * @param command the already sent command
     */
    @Override
    public void onWriteSuccess(final String command) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText("successfully sent command: \"" + command + "\"");
                bSuccess=true;
            }
        });
    }

    /**
     * Closes any running writerThread, and starts a new one
     */
    private void restartWriterThread() {
        if (ControlActivity.writerThread != null) {
            ControlActivity.writerThread.interrupt();
            ControlActivity.writerThread.setRunning(false);
            ControlActivity.writerThread = null;
        }
        ControlActivity.writerThread = new WriterThread(ControlActivity.this, ControlActivity.bluetoothSocket);
        ControlActivity.writerThread.start();
    }

    /**
     * This class is the main logic for setting up a connection and opening a socket
     */
    class ConnectTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ControlActivity.connectionState.setText("trying to connect to " + ControlActivity.device + "...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // we need to enable the bluetooth first in order to make this app working
                if (!bluetoothAdapter.isEnabled()) {
                    publishProgress("bluetooth was not enabled, enabling...");
                    bluetoothAdapter.enable();
                    // turning on bluetooth takes some time
                    while (!bluetoothAdapter.isEnabled()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                    publishProgress("bluetooth turned on");
                }
                // here we are going to check if the device was paired in android, if not the user will be prompt to do so.
                String address = null;
                for (BluetoothDevice d : bluetoothAdapter.getBondedDevices()) {
                    if (ControlActivity.device.equals(d.getName())) {
                        address = d.getAddress();
                        break;
                    }
                }
                if (address == null) {
                    return ControlActivity.device + " was never paired. Please pair first using Android.";
                }
                // we have a mac address, now we try to open a socket
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                publishProgress("creating socket...");
                ControlActivity.bluetoothSocket = device.createRfcommSocketToServiceRecord(ControlActivity.BT_UUID);
                publishProgress("canceling discovery...");
                // we cancel discovery for other devices, since it will speed up the connection
                ControlActivity.bluetoothAdapter.cancelDiscovery();
                publishProgress("trying to connect to " + device + " with address " + address);
                // try to connect to the bluetooth device, if unsuccessful, an exception will be thrown
                ControlActivity.bluetoothSocket.connect();
                // start the writerThread
                restartWriterThread();
                return "connected, writer thread is running";
            } catch (IOException e) {
                try {
                    // try to close the socket, since we can have only one
                    ControlActivity.bluetoothSocket.close();
                } catch (IOException e1) {
                    return "failure due " + e.getMessage() + ", closing socket did not work.";
                }
                return "failure due " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ControlActivity.connectionState.setText(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            ControlActivity.connectionState.setText(values[0]);
        }
    }

    /**
     * This class will process some events which are called from android itself, we use it to
     * establish connections automatically.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // the bluetooth was turned off, so we stop any running connection tasks
                    if (ControlActivity.connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                        ControlActivity.connectTask.cancel(true);
                    }
                    ControlActivity.connectionState.setText("bluetooth was turned off, restarting...");
                    // enable the bluetooth again, and wait till it is turned on
                    bluetoothAdapter.enable();
                    while (!bluetoothAdapter.isEnabled()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                    ControlActivity.connectionState.setText("bluetooth turned on");
                    // try to connect again with the device
                    ControlActivity.connectTask = new ConnectTask();
                    ControlActivity.connectTask.execute();
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                // this event is useful if the user has paired the device for the first time
                if (ControlActivity.connectTask == null || ControlActivity.connectTask.getStatus() == AsyncTask.Status.FINISHED) {
                    ControlActivity.connectionState.setText("connected with bluetooth device, reconnecting...");
                    // reconnect since the app is doing nothing at this moment
                    ControlActivity.connectTask = new ConnectTask();
                    ControlActivity.connectTask.execute();
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // if the connection gets lost, we have to reconnect again
                ControlActivity.connectionState.setText("connection lost, reconnecting...");
                if (ControlActivity.connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                    ControlActivity.connectTask.cancel(true);
                }
                ControlActivity.connectTask = new ConnectTask();
                ControlActivity.connectTask.execute();
            }
        }
    };

    class ButtonListener implements View.OnClickListener, View.OnTouchListener {
        @Override
        public void onClick(View view) {

        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(view.getId() == R.id.leftLargeSteer){
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mCarState.isChangeDirection=false;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    mCarState.steerSignal=CarState.leftLargeSteerSignal;
                    mCarState.isChangeDirection=true;
                }
            }else if(view.getId() == R.id.leftSmallSteer){
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mCarState.isChangeDirection=false;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    mCarState.steerSignal=CarState.leftSmallSteerSignal;
                    mCarState.isChangeDirection=true;
                }
            }else if(view.getId() == R.id.rightSmallSteer){
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mCarState.isChangeDirection=false;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    mCarState.steerSignal=CarState.rightSmallSteerSignal;
                    mCarState.isChangeDirection=true;
                }
            }else if(view.getId() == R.id.rightLargeSteer){
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mCarState.isChangeDirection=false;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    mCarState.steerSignal=CarState.rightLargeSteerSignal;
                    mCarState.isChangeDirection=true;
                }
            }
            return false;
        }
    }
}
