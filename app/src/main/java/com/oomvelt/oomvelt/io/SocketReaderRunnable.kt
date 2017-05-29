package com.oomvelt.oomvelt.io

import android.bluetooth.BluetoothSocket
import com.oomvelt.oomvelt.bluetooth.BluetoothService
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
import com.google.gson.Gson







class SocketReaderRunnable @Throws(IOException::class)

constructor(socket: BluetoothSocket, service: BluetoothService, file: File) : Runnable, AnkoLogger {

  private val mSocket = socket

  private val mInputStream = socket.inputStream
  private val mBufferedReader: BufferedReader
  private val mFileWriterRunnable: FileWriterRunnable

  private var lastTime = 0
  private val gson = Gson()

  @Volatile private var mRunning = false

  private val myCallback = DroidBehaviourCallback(service)

  init {
    info("Will write to file " + file.absolutePath)
    mBufferedReader = BufferedReader(InputStreamReader(this.mInputStream))

    mFileWriterRunnable = FileWriterRunnable(file)
  }

  fun marker(marker: DataNode) {
    marker.time = lastTime + 1

    var line = "0.2:" + gson.toJson(marker)
    info(line)
    mFileWriterRunnable.append(line)
  }

  override fun run() {
    val behaviour = SimpleBehaviour()
    behaviour.setup(this.myCallback)

    val lpp = vTwoParser()

    while (mRunning) {
      try {
        val line = mBufferedReader.readLine()

        // Log the line before processing it.
        mFileWriterRunnable.append(line)

        try {
          val dn = lpp.parseLine(line)
          lastTime = dn.time;

          behaviour.feedEntry(dn)
        } catch (e: IllegalArgumentException) {
          info ("There was an exception on that line - probably a bad / non v2 line?")
          info(e.toString())
          info("Socket status: " + this.mSocket.isConnected )
        }

      } catch (e: IOException) {

        // Whilst the following code looks good, isConnected doesn't seem to do what you think it does

        //if ( ! this.mSocket.isConnected){
        //  this.mSocket.connect()
        //} else {
        //  info ("IO Exception on the read.. not printing stack to reduce log files")
          //e.printStackTrace()
        //}

        if(mRunning) {
          try {
            info("Socket status: " + this.mSocket.isConnected )
            this.mSocket.connect()
            if (this.mSocket.isConnected){
              break
            }
          } catch(e: IOException) {
            info("Reconnect failed, try again!")
          }
        }
      }
    }

    try {
      mBufferedReader.close()
      mInputStream.close()
    } catch (e: IOException) {
      e.printStackTrace()
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
