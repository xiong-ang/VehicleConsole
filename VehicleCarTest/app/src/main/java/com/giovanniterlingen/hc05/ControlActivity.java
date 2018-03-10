package com.giovanniterlingen.hc05;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * An easy and good implementation for the HC-05 bluetooth component
 *
 * @author Giovanni Terlingen
 */
public class ControlActivity extends AppCompatActivity implements BluetoothStateCallback {

    private static final String device = "HC-05";
    private static final UUID BT_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");

    private TextView connectionState;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothSocket bluetoothSocket;
    private static ConnectTask connectTask;

    public EditText inputET;
    public String sendValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // assign the widgets specified in the layout xml file
        connectionState = (TextView) findViewById(R.id.connection_state);
        inputET= (EditText) findViewById(R.id.et_Input);
        Button sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendValue = inputET.getText().toString();
                send();
            }
        });
        ControlActivity.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // register these receivers, we need them for setting up connections automatically
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        // try to connect
        ControlActivity.connectTask = new ConnectTask();
        ControlActivity.connectTask.execute();
    }

    private void send() {

            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                //outputStream.write(sendValue.getBytes("US-ASCII"));
                outputStream.write(String.valueOf(sendValue).getBytes("UTF-8"));
                //outputStream.write(String.valueOf("555").getBytes());
                outputStream.flush();

                onWriteSuccess(new String(String.valueOf(sendValue)));

            } catch (IOException e) {
                // something went wrong, so notify the activity
                onWriteFailure("thread caught IOException: " + e.getMessage());
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
            }
        });
    }


    /**
     * This class is the main logic for setting up a connection and opening a socket
     */
    class ConnectTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectionState.setText("trying to connect to " + ControlActivity.device + "...");
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
            connectionState.setText(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            connectionState.setText(values[0]);
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
                    connectionState.setText("bluetooth was turned off, restarting...");
                    // enable the bluetooth again, and wait till it is turned on
                    bluetoothAdapter.enable();
                    while (!bluetoothAdapter.isEnabled()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                    connectionState.setText("bluetooth turned on");
                    // try to connect again with the device
                    ControlActivity.connectTask = new ConnectTask();
                    ControlActivity.connectTask.execute();
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                // this event is useful if the user has paired the device for the first time
                if (ControlActivity.connectTask == null || ControlActivity.connectTask.getStatus() == AsyncTask.Status.FINISHED) {
                    connectionState.setText("connected with bluetooth device, reconnecting...");
                    // reconnect since the app is doing nothing at this moment
                    ControlActivity.connectTask = new ConnectTask();
                    ControlActivity.connectTask.execute();
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // if the connection gets lost, we have to reconnect again
                connectionState.setText("connection lost, reconnecting...");
                if (ControlActivity.connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                    ControlActivity.connectTask.cancel(true);
                }
                ControlActivity.connectTask = new ConnectTask();
                ControlActivity.connectTask.execute();
            }
        }
    };
}
