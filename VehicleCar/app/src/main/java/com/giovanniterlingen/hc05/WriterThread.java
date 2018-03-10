package com.giovanniterlingen.hc05;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An easy and good implementation for the HC-05 bluetooth component
 *
 * @author Giovanni Terlingen
 */
class WriterThread extends Thread {
    private ControlActivity controlActivity;
    private BluetoothSocket bluetoothSocket;
    private volatile boolean isRunning = true;

    WriterThread(ControlActivity controlActivity, BluetoothSocket bluetoothSocket) {
        this.controlActivity = controlActivity;
        this.bluetoothSocket = bluetoothSocket;
    }

    void setRunning(boolean running) {
        isRunning = running;
    }

    private boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        try {
            // get the output stream from the bluetooth device, so we can send data on it
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            // this while is looping, but don't worry, we use a nice queue
            // if there is no data to send, the thread will be blocked
            while (isRunning()) {
                try {

                    if(ControlActivity.mCarState.isChangeDirection)
                    {
                        outputStream.write(ControlActivity.mCarState.speedSignal.getBytes());
                        outputStream.flush();
                        sleep(20);

                        outputStream.write(ControlActivity.mCarState.steerSignal.getBytes());
                        outputStream.flush();
                        sleep(20);

                        this.controlActivity.onWriteSuccess(CarState.GetState(ControlActivity.mCarState.speedSignal)+" "+ CarState.GetState(ControlActivity.mCarState.steerSignal));
                    }else
                    {
                        outputStream.write(ControlActivity.mCarState.speedSignal.getBytes());
                        outputStream.flush();
                        sleep(20);
                        this.controlActivity.onWriteSuccess(CarState.GetState(ControlActivity.mCarState.speedSignal));
                    }
                    this.controlActivity.updateUI();
                }catch(InterruptedException e)
                {
                    this.controlActivity.onWriteFailure("thread caught InterruptedException: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // something went wrong, so notify the activity
            this.controlActivity.onWriteFailure("thread caught IOException: " + e.getMessage());
        } finally {
            // the thread stopped, let's try to close the socket
            try {
                bluetoothSocket.close();
            } catch (IOException e1) {
                this.controlActivity.onWriteFailure("could not close socket");
            }
        }
    }
}
