package com.oomvelt.oomvelt.io

import android.bluetooth.BluetoothSocket
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import raticator.SimpleBehaviour

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import raticator.vTwoParser
import raticator.LineProtocolParser
import raticator.DataNode





class SocketReaderRunnable @Throws(IOException::class)

constructor(socket: BluetoothSocket, file: File) : Runnable, AnkoLogger {
  private val mInputStream = socket.inputStream
  private val mBufferedReader: BufferedReader
  private val mFileWriterRunnable: FileWriterRunnable
  @Volatile private var mRunning = false

  private val myCallback = DroidBehaviourCallback()

  init {
    info("Will write to file " + file.absolutePath)
    mBufferedReader = BufferedReader(InputStreamReader(this.mInputStream))

    mFileWriterRunnable = FileWriterRunnable(file)
  }

  override fun run() {

    val behaviour = SimpleBehaviour();
    behaviour.setup(this.myCallback);

    val lpp = vTwoParser()

    while (mRunning) {
      try {
        val line = mBufferedReader.readLine()
        info(line)

        try {
          val dn = lpp.parseLine(line)
          behaviour.feedEntry(dn);
        } catch (e: IllegalArgumentException) {
          info ("There was an exception on that line - probably a bad / non v2 line?")
          info(e.toString())
        }


        mFileWriterRunnable.append(line)
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }

    try {
      mBufferedReader.close()
      mInputStream.close()
    } catch (ignore: IOException) {
    }

    mFileWriterRunnable.close()
  }

  fun start() {
    info("Starting")

    mFileWriterRunnable.open()

    mRunning = true
    Thread(this).start()
  }

  @Throws(IOException::class)
  fun stop() {
    info("Stopping")

    mRunning = false
  }
}
