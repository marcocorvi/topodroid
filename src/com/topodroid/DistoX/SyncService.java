/** @file SyncService.java
 * 
 * this class is mae after the sample BluetoothChat by the 
 * The Android Open Source Project which is licenced under
 * the Apache License, Version 2.0 (the "License");
 */
package com.topodroid.DistoX;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;

public class SyncService 
{
    // Name for the SDP record when creating server socket
    private static final String NAME = "TopoDroidSync";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Member fields
    // private Context mContext;
    private TopoDroidApp mApp;

    private final BluetoothAdapter mAdapter;
    private BluetoothDevice mRemoteDevice;
    private final Handler   mHandler;
    private AcceptThread    mAcceptThread;
    private ConnectingThread   mConnectingThread;
    private ConnectedThread mConnectedThread;

    private int mConnectState; // NONE --> CONNECTING --> CONNECTED --> NONE
    private int mAcceptState;  // NONE --> LISTEN --> NONE

    private int mType;   // the service type. either server (LISTEN) or client (CONNECTING)
    private boolean mConnectRun;
    private boolean mAcceptRun;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    static final int MESSAGE_CONNECT_STATE  = 1;
    static final int MESSAGE_DEVICE = 2;
    static final int MESSAGE_READ   = 3;
    static final int MESSAGE_WRITE  = 4;
    static final int MESSAGE_LOST_CONN = 5;
    static final int MESSAGE_FAIL_CONN = 6;
    static final int MESSAGE_ACCEPT_STATE = 7;

    static final String DEVICE = "DEVICE";

  public SyncService( /* Context context, */ TopoDroidApp app, Handler handler )
  {
    // mContext = context;
    mApp     = app;
    mAdapter = BluetoothAdapter.getDefaultAdapter();
    mRemoteDevice = null;
    mConnectState = STATE_NONE;
    mAcceptState = STATE_NONE;

    mType  = STATE_NONE;
    mHandler = handler;
    mConnectRun = false;
    mAcceptRun  = false;
  }

  private synchronized void setConnectState(int state)
  {
    // Log.v("DistoX", "sync connect state " + mConnectState + " --> " + state );
    // if ( state == STATE_NONE ) mRemoteDevice = null;
    mConnectState = state;
    mHandler.obtainMessage( MESSAGE_CONNECT_STATE, state, -1).sendToTarget();
  }

  private synchronized void setAcceptState(int state)
  {
    // Log.v("DistoX", "sync accept state " + mAcceptState + " --> " + state );
    mAcceptState = state;
    mHandler.obtainMessage( MESSAGE_ACCEPT_STATE, state, -1).sendToTarget();
  }

  public synchronized int getConnectState() { return mConnectState; }
  public synchronized int getAcceptState() { return mAcceptState; }

  String getConnectStateStr()
  {
    switch ( mConnectState ) {
      case STATE_NONE: return "NONE";
      // case STATE_LISTEN: return "LISTEN";
      case STATE_CONNECTING: return "CONNECTING";
      case STATE_CONNECTED: return "CONNECTED " + mRemoteDevice.getName();
    }
    return "UNKNOWN";
  }

  String getConnectedDeviceName()
  {
    return ( mRemoteDevice != null )? mRemoteDevice.getName() : null;
  }

  public int getType() { return mType; }

  public synchronized void start() 
  {
    // Log.v("DistoX", "sync start() ");
    mAcceptRun = false;

    mConnectRun = false;
    if (mConnectingThread != null) { mConnectingThread.cancel(); mConnectingThread = null; }
    if (mConnectedThread != null)  { mConnectedThread.cancel(); mConnectedThread = null; }
    startAccept();
  }

  private synchronized void startAccept()
  {
    // Log.v("DistoX", "sync startAccept() ");
    if (mAcceptThread != null) {
      mAcceptRun = false;
      setAcceptState(STATE_NONE);
      try {
        mAcceptThread.join();
      } catch ( InterruptedException e ) { }
    }

    mAcceptRun = true;
    mType = STATE_LISTEN;
    mAcceptThread = new AcceptThread();
    mAcceptThread.start();
    setAcceptState(STATE_LISTEN);
  }

  /**
   * Start the ConnectingThread to initiate a connection to a remote device.
   * @param device  The BluetoothDevice to connect
   */
  public synchronized void connect( BluetoothDevice device )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "sync connect to " + device.getName() );
    mRemoteDevice = device;

    mConnectRun = false;
    if ( mConnectState == STATE_CONNECTING && mConnectingThread != null ) {
      mConnectingThread.cancel();
      mConnectingThread = null;
    }
    if ( mConnectedThread != null ) { mConnectedThread.cancel(); mConnectedThread = null; }

    reconnect();
  }

  private synchronized void reconnect()
  {
    if ( mRemoteDevice == null ) return;
    TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "sync reconnect to " + mRemoteDevice.getName() );
    mType = STATE_CONNECTING;
    mConnectRun = true;
    mConnectingThread = new ConnectingThread( mRemoteDevice );
    mConnectingThread.start();
    setConnectState(STATE_CONNECTING);
  }

  /**
   * Start the ConnectedThread to begin managing a Bluetooth connection
   * @param socket  The BluetoothSocket on which the connection was made
   * @param device  The BluetoothDevice that has been connected
   */
  public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "sync connected. remote device " + device.getName() );

    mRemoteDevice = device;
    mConnectRun = true;

    if ( mConnectingThread   != null ) { mConnectingThread.cancel();   mConnectingThread   = null; }
    if ( mConnectedThread != null ) { mConnectedThread.cancel(); mConnectedThread = null; }
    // ONE-TO-ONE
    // if ( mAcceptThread    != null ) { mAcceptThread.cancel();    mAcceptThread    = null; }

    mConnectedThread = new ConnectedThread(socket);
    mConnectedThread.start();

    Message msg = mHandler.obtainMessage( MESSAGE_DEVICE );
    Bundle bundle = new Bundle();
    bundle.putString( DEVICE, mRemoteDevice.getName() );
    msg.setData(bundle);
    mHandler.sendMessage(msg);

    setConnectState(STATE_CONNECTED);
  }

  public synchronized void disconnect() 
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "sync disconnect");
    if ( mConnectingThread != null ) { mConnectingThread.cancel();   mConnectingThread   = null; }

    if ( mConnectState == STATE_CONNECTED ) {
      byte shutdown[] = new byte[4];
      shutdown[0] = 0;
      shutdown[1] = DataListener.SHUTDOWN;
      shutdown[2] = 0;
      shutdown[3] = DataListener.EOL;
      write( shutdown );
    }
    if ( mConnectedThread != null ) { mConnectedThread.cancel(); mConnectedThread = null; }

    mRemoteDevice = null;
    setConnectState( STATE_NONE );
  }

  public synchronized void stop() 
  {
    // Log.v("DistoX", "sync stop");
    if ( mAcceptThread    != null ) { mAcceptThread.cancel();    mAcceptThread    = null; }
    setAcceptState( STATE_NONE );
    mType = STATE_NONE;
  }

  public void write( byte[] buffer ) 
  {
    // Log.v("DistoX", "sync write (conn state " + mConnectState + " length " + buffer.length + ") " + buffer[0] + " " + buffer[1] + " ... "); 
    ConnectedThread r;    // Create temporary object
    synchronized (this) { // Synchronize a copy of the ConnectedThread
      if ( mConnectState != STATE_CONNECTED ) return;
      r = mConnectedThread;
    }
    r.write( buffer );         // Perform the write unsynchronized
  }

  // called by the Connect-Thread
  private void connectionFailed()  
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "sync connection failed");
    mRemoteDevice = null;
    setConnectState(STATE_NONE);
    mType = STATE_NONE;
    Message msg = mHandler.obtainMessage( MESSAGE_FAIL_CONN );
    mHandler.sendMessage(msg);
  }

  // called by the connected-Thread
  private void connectionLost() 
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "sync connection lost");
    mRemoteDevice = null;
    setConnectState(STATE_NONE);
    Message msg = mHandler.obtainMessage( MESSAGE_LOST_CONN );
    mHandler.sendMessage(msg);
  }

  /**
   * This thread runs while listening for incoming connections. It behaves
   * like a server-side client. It runs until a connection is accepted
   * (or until cancelled).
   */
  private class AcceptThread extends Thread 
  {
    private BluetoothServerSocket mmServerSocket;

    public AcceptThread() {
      createServerSocket();
    }
 
    private void createServerSocket()
    {
      BluetoothServerSocket tmp = null;
      try { // Create a new listening server socket
        tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
      } catch (IOException e) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "listen() failed " + e);
      }
      mmServerSocket = tmp;
    }

    public void run()
    {
      setName("AcceptThread");
      BluetoothSocket socket = null;

      while ( mAcceptRun && mAcceptState == STATE_LISTEN ) {
        try {
          // Log.v("DistoX", "sync accept listening ... ");
          socket = mmServerSocket.accept(); // blocking call
        } catch (IOException e) {
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "accept() failed " + e);
          break;
        }

        if (socket != null) { // If a connection was accepted
          TopoDroidLog.Log(TopoDroidLog.LOG_SYNC, "incoming connection request " + socket.getRemoteDevice().getName() );
          synchronized ( SyncService.this ) {
            switch ( mConnectState ) {
              case STATE_NONE:
              case STATE_CONNECTING: // Situation normal. Start the connected thread.
                connected( socket, socket.getRemoteDevice() );
                break;
              case STATE_CONNECTED: // Either not ready or already connected. Terminate new socket.
                try {
                  socket.close();
                } catch (IOException e) {
                  TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Could not close unwanted socket " + e);
                }
                break;
            }
          }
          // createServerSocket();
          socket = null;
        }
      }
      // TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "sync AcceptThread done");
    }

    public void cancel()
    {
      try {
        mAcceptRun = false;
        mmServerSocket.close();
      } catch (IOException e) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "close() of server failed " + e);
      }
    }
  }


  /**
   * This thread runs while attempting to make an outgoing connection
   * with a device. It runs straight through; the connection either
   * succeeds or fails.
   */
  private class ConnectingThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectingThread( BluetoothDevice device )
    {
      mmDevice = device;
      BluetoothSocket tmp = null;

      // Get a BluetoothSocket for a connection with the given BluetoothDevice
      try {
        // Class[] classes1 = new Class[ 1 ];
        // classes1[0] = int.class;
        // Method m = mmDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
        // tmp = (BluetoothSocket) m.invoke( mBTDevice, 1 );
        //
        tmp = mmDevice.createRfcommSocketToServiceRecord( MY_UUID );
      } catch (IOException e) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ConnectingThread cstr failed " + e);
      }
      mmSocket = tmp;
    }

    public void run()
    {
      // Log.v("DistoX", "sync ConnectingThread run");
      setName("ConnectingThread");
      mAdapter.cancelDiscovery(); // Always cancel discovery because it will slow down a connection

      try { // Make a connection to the BluetoothSocket
        mmSocket.connect(); // blocking call
      } catch (IOException e) {
        connectionFailed();
        try { // Close the socket
          mmSocket.close();
        } catch (IOException e2) {
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "unable to close() socket during connection failure " + e2);
        }
        // SyncService.this.start(); // Start the service over to restart listening mode
        return;
      }

      synchronized ( SyncService.this ) { // Reset the ConnectingThread because we're done
        mConnectingThread = null;
      }

      connected( mmSocket, mmDevice ); // Start the connected thread
      // TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "sync connecting thread done");
    }

    public void cancel()
    {
      try {
        mmSocket.close();
      } catch (IOException e) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "close() of connect socket failed " + e);
      }
    }
  }

  /**
   * This thread runs during a connection with a remote device.
   * It handles all incoming and outgoing transmissions.
   */
  private class ConnectedThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket)
    {
      mmSocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      try { // Get the BluetoothSocket input and output streams
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "temp sockets not created " + e);
      }

      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    public void run() 
    {
      // Log.v("DistoX", "sync ConnectedThread run() " + mConnectRun );

      byte[] buffer = new byte[512];
      byte[] data = new byte[4096];
      int bytes;
      int pos = 0; // data pos

      while ( mConnectRun ) { // Keep listening to the InputStream while connected
        try {
          bytes = mmInStream.read(buffer); // Read from the InputStream
          for ( int k=0; k<bytes; ++k ) {
            // add buffer to the data 
            if ( buffer[k] == DataListener.EOL ) {
              // end of message: send to upper layer
              byte[] tmp = new byte[pos];  
              for ( int j=0; j<pos; ++j) tmp[j] = data[j];
              // special handle shutdown message 
              if ( data[0] == 0 && 
                   data[1] == DataListener.SHUTDOWN && 
                   data[2] == 0 ) {
                mConnectRun = false;
                mRemoteDevice = null;
                setConnectState( STATE_NONE );
              } else {
                Log.v("DistoX", "read <" + data[0] + "|" + data[1] + ">" );
                mHandler.obtainMessage( MESSAGE_READ, pos, -1, tmp).sendToTarget();
                pos = 0;
              }
            } else {
              data[pos] = buffer[k];
              ++pos;
            }
          }
        } catch (IOException e) {
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "disconnected " + e);
          try { // Close the socket
            mmSocket.close();
          } catch (IOException e2) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "unable to close() socket during connection failure " + e2);
          }
          connectionLost();
          break;
        }
      }
      // TopoDroidLoLogog( TopoDroidLog.LOG_SYNC, "sync ConnectedThread done type " + mType );
      if ( mType == STATE_LISTEN ) {
        // ONE-TO-ONE
        // startAccept(); 
      } else if ( mType == STATE_CONNECTING ) {
        try {
          Thread.sleep( 200 );
        } catch ( InterruptedException e ) { }
        reconnect();
      }
    }

    /**
     * Write to the connected OutStream.
     * @param buffer  The bytes to write
     */
    public void write( byte[] buffer ) 
    {
      Log.v("DistoX", "sync connected write <" + buffer[0] + "|" + buffer[1] + ">" );
      try {
        mmOutStream.write( buffer );
        // Share the sent message back to the UI Activity: NOT USED .... FIXME
        // mHandler.obtainMessage( MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
      } catch (IOException e) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Exception during write " + e );
      }
    }

    public void cancel() 
    {
      mConnectRun = false;
      try {
        mmInStream.close();
        mmSocket.close();
      } catch (IOException e) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "close() of connect socket failed " + e );
      }
    }
  }
}
