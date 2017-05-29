package com.oomvelt.oomvelt.io

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
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
    } catch (e: InterruptedException) {
      e.printStackTrace()
    }

    return this
  }

  fun open() {
    mRunning = true
    Thread(this).start()
  }

  override fun run() {
    while (mRunning) {
      try {
        val line = mQueue.poll(100, TimeUnit.MICROSECONDS)

        if (line != null) {
          try {
            // info(line)
            mOut.write(line)
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
      } catch (e: InterruptedException) {
        e.printStackTrace()
      }
    }

    try {
      mOut.flush()
      mOut.close()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  fun close() {
    mRunning = false
  }
}