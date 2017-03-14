package com.oomvelt.oomvelt;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SocketReader implements Runnable {
    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final BufferedReader bufferedReader;
    private final FileWriter fileWriter;
    private volatile boolean started = false;

    SocketReader(BluetoothSocket socket, File file) throws IOException {
        this.socket = socket;

        this.inputStream = socket.getInputStream();
        this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));

        Log.v("SocketReader", "Will write to file " + file.getAbsolutePath());
        this.fileWriter = new FileWriter(file);
    }

    @Override
    public void run() {
        while (this.started) {
            try {
                String line = this.bufferedReader.readLine();
                Log.v("SocketReader", "Read line");
                Log.v("SocketReader", line);

                this.fileWriter.append(line);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void start() {
        Log.v("SocketReader", "Starting");
        this.fileWriter.open();

        this.started = true;
        new Thread(this).start();
    }

    public void stop() throws IOException {
        Log.v("SocketReader", "Stopping");
        this.fileWriter.close();

        this.started = false;

        this.bufferedReader.close();
        this.inputStream.close();
    }
}
