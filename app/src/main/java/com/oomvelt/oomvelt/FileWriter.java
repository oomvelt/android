package com.oomvelt.oomvelt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class FileWriter implements Runnable {
    private final File file;
    private final Writer out;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    private volatile boolean started = false;
    private volatile boolean stopped = false;

    public FileWriter(File file) throws IOException {
        this.file = file;
        this.out = new BufferedWriter(new java.io.FileWriter(file));
    }

    public FileWriter append(String s) {
        if (!started) {
            throw new IllegalStateException("open() call expected before append()");
        }

        try {
            queue.put(s);
        }
        catch (InterruptedException ignored) {
        }
        return this;
    }

    public void open() {
        this.started = true;
        new Thread(this).start();
    }

    public void run() {
        while (!stopped) {
            try {
                String line = queue.poll(100, TimeUnit.MICROSECONDS);
                if (line != null) {
                    try {
                        this.out.write(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (InterruptedException e) {
            }
        }
        try {
            out.close();
        }
        catch (IOException ignore) {
        }
    }

    public void close() {
        this.stopped = true;
    }
}