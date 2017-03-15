package com.oomvelt.oomvelt.io

import org.jetbrains.anko.AnkoLogger
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.Writer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

internal class FileWriterRunnable @Throws(IOException::class) constructor(
    private val mFile: File) : Runnable, AnkoLogger {
  private val mOut: Writer
  private val mQueue = LinkedBlockingQueue<String>()
  @Volatile private var mRunning = false

  init {
    mOut = BufferedWriter(java.io.FileWriter(mFile))
  }

  fun append(s: String): FileWriterRunnable {
    if (!mRunning) {
      throw IllegalStateException("open() call expected before append()")
    }

    try {
      mQueue.put(s)
    } catch (ignored: InterruptedException) {
    }

    return this
  }

  fun open() {
    mRunning = true
    Thread(this).start()
  }

  override fun run() {
    while (!mRunning) {
      try {
        val line = mQueue.poll(100, TimeUnit.MICROSECONDS)

        if (line != null) {
          try {
            mOut.write(line)
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
      } catch (e: InterruptedException) {
      }

    }

    try {
      mOut.close()
    } catch (ignore: IOException) {
    }
  }

  fun close() {
    mRunning = false
  }
}