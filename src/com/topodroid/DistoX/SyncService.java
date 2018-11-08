/* @file SyncService.java
 * 
 * --------------------------------------------------------
 * this class is made after the sample BluetoothChat by the 
 * The Android Open Source Project which is licenced under
 * the Apache License, Version 2.0 (the "License");
 * --------------------------------------------------------
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
// import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;

/* ---- IF_COSURVEY

class SyncService
{
    // Name for the SDP record when creating server socket
    private static final String NAME = "TopoDroidSync";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Member fields
    // private Context mContext;
    private final TopoDroidApp mApp;

    private final BluetoothAdapter mAdapter;
    private BluetoothDevice  mRemoteDevice;
    private final Handler    mHandler;
    private AcceptThread     mAcceptThread;
    private ConnectingThread mConnectingThread;
    private ConnectedThread  mConnectedThread;

    private volatile int mConnectState; // NONE --> CONNECTING --> CONNECTED --> NONE
    private volatile int mAcceptState;  // NONE --> LISTEN --> NONE

    private int mType;   // the service type. either server (LISTEN) or client (CONNECTING)
    private volatile boolean mConnectRun;
    private volatile boolean mAcceptRun;

    // Constants that indicate the current connection state
    static final int STATE_NONE = 0;       // we're doing nothing
    static final int STATE_LISTEN = 1;     // now listening for incoming connections
    static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    static final int STATE_CONNECTED = 3;  // now connected to a remote device

    static final private String mStateName[] = { "none", "listen", "connecting", "connected" };

    static final int MESSAGE_CONNECT_STATE  = 1;
    static final int MESSAGE_DEVICE = 2;
    static final int MESSAGE_READ   = 3;
    static final int MESSAGE_WRITE  = 4;
    static final int MESSAGE_LOST_CONN = 5;
    static final int MESSAGE_FAIL_CONN = 6;
    static final int MESSAGE_ACCEPT_STATE = 7;

    static final String DEVICE = "DEVICE";

  SyncService( // Context context, 
               TopoDroidApp app, Handler handler )
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
    TDLog.Log( TDLog.LOG_SYNC, "sync connect state: " 
      + mStateName[mConnectState] + " --> " + mStateName[state] );
    // if ( state == STATE_NONE ) mRemoteDevice = null;
    mConnectState = state;
    mHandler.obtainMessage( MESSAGE_CONNECT_STATE, state, -1).sendToTarget();
  }

  private synchronized void setAcceptState(int state)
  {
    TDLog.Log( TDLog.LOG_SYNC, "sync accept state "
      + mStateName[mAcceptState] + " --> " + mStateName[state] );
    mAcceptState = state;
    mHandler.obtainMessage( MESSAGE_ACCEPT_STATE, state, -1).sendToTarget();
  }

  synchronized int getConnectState() { return mConnectState; }
  synchronized int getAcceptState() { return mAcceptState; }

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

  int getType() { return mType; }

  synchronized void start()
  {
    TDLog.Log( TDLog.LOG_SYNC, "sync start()" );
    mAcceptRun = false;

    mConnectRun = false;
    if (mConnectingThread != null) { mConnectingThread.cancel(); mConnectingThread = null; }
    if (mConnectedThread != null)  { mConnectedThread.cancel(); mConnectedThread = null; }
    startAccept();
  }

  private synchronized void startAccept()
  {
    TDLog.Log( TDLog.LOG_SYNC, "sync startAccept()" );
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

  //
  // Start the ConnectingThread to initiate a connection to a remote device.
  // @param device  The BluetoothDevice to connect
  //
  synchronized void connect( BluetoothDevice device )
  {
    TDLog.Log( TDLog.LOG_SYNC, "sync connect to " + device.getName() );
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
    TDLog.Log( TDLog.LOG_SYNC, "sync reconnect to " + mRemoteDevice.getName() );
    mType = STATE_CONNECTING;
    mConnectRun = true;
    mConnectingThread = new ConnectingThread( mRemoteDevice );
    mConnectingThread.start();
    setConnectState(STATE_CONNECTING);
  }

  //
  // Start the ConnectedThread to begin managing a Bluetooth connection
  // @param socket  The BluetoothSocket on which the connection was made
  // @param device  The BluetoothDevice that has been connected
  //
  synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
  {
    TDLog.Log( TDLog.LOG_SYNC, "sync connected. remote device " + device.getName() );

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

  synchronized void disconnect()
  {
    TDLog.Log( TDLog.LOG_SYNC, "sync disconnect");
    if ( mConnectingThread != null ) { mConnectingThread.cancel();   mConnectingThread   = null; }

    if ( mConnectState == STATE_CONNECTED ) {
      byte shutdown[] = new byte[4];
      shutdown[0] = 0;
      shutdown[1] = DataListener.SHUTDOWN;
      shutdown[2] = 0;
      shutdown[3] = DataListener.EOL;
      writeBuffer( shutdown ); // FIXME if failure ? nothing: connectedThread already closed
    }
    if ( mConnectedThread != null ) { mConnectedThread.cancel(); mConnectedThread = null; }
    mRemoteDevice = null;
    setConnectState( STATE_NONE );
  }

  synchronized void stop()
  {
    TDLog.Log( TDLog.LOG_SYNC, "sync stop");
    if ( mAcceptThread    != null ) { mAcceptThread.cancel();    mAcceptThread    = null; }
    setAcceptState( STATE_NONE );
    mType = STATE_NONE;
  }

  boolean writeBuffer( byte[] buffer )
  {
    // Log.v("DistoX", "sync write (conn state " + mConnectState + " length " + buffer.length + ") " + buffer[0] + " " + buffer[1] + " ... "); 
    ConnectedThread r;    // Create temporary object
    synchronized (this) { // Synchronize a copy of the ConnectedThread
      if ( mConnectState != STATE_CONNECTED ) return false;
      r = mConnectedThread;
    }
    if ( r.doWriteBuffer( buffer ) ) {  // Perform the write unsynchronized
      return true;
    } // else {
    mConnectedThread.cancel();
    mConnectedThread = null;
    mRemoteDevice = null;
    setConnectState( STATE_NONE );
    return false;
  }

  // called by the Connect-Thread
  private void connectionFailed()  
  {
    TDLog.Error( "sync connection failed");
    mRemoteDevice = null;
    setConnectState(STATE_NONE);
    mType = STATE_NONE;
    Message msg = mHandler.obtainMessage( MESSAGE_FAIL_CONN );
    mHandler.sendMessage(msg);
  }

  // called by the connected-Thread
  private void connectionLost() 
  {
    TDLog.Error( "sync connection lost");
    mRemoteDevice = null;
    setConnectState(STATE_NONE);
    Message msg = mHandler.obtainMessage( MESSAGE_LOST_CONN );
    mHandler.sendMessage(msg);
  }

  //
  // This thread runs while listening for incoming connections. It behaves
  // like a server-side client. It runs until a connection is accepted (or until cancelled).
  //
  private class AcceptThread extends Thread 
  {
    private BluetoothServerSocket mmServerSocket;

    AcceptThread() {
      createServerSocket();
    }
 
    private void createServerSocket()
    {
      BluetoothServerSocket tmp = null;
      try { // Create a new listening server socket
        tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
      } catch (IOException e) {
        TDLog.Error( "listen() failed " + e);
      }
      mmServerSocket = tmp;
    }

    public void run()
    {
      TDLog.Log( TDLog.LOG_SYNC, "sync AcceptThread run");
      setName("AcceptThread");
      BluetoothSocket socket = null;

      while ( mAcceptRun && mAcceptState == STATE_LISTEN ) {
        if ( mmServerSocket == null ) { // FIXME should not happen
          TDLog.Error("null server socket");
          mConnectState = STATE_NONE;
          break;
        }
        try {
          // Log.v("DistoX", "sync accept listening ... ");
          socket = mmServerSocket.accept(); // blocking call
        } catch (IOException e) {
          TDLog.Error( "accept() failed " + e);
          break;
        }

        if (socket != null) { // If a connection was accepted
          TDLog.Log(TDLog.LOG_SYNC, "incoming connection request " + socket.getRemoteDevice().getName() );
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
                  TDLog.Error( "Could not close unwanted socket " + e);
                }
                break;
            }
          }
          // createServerSocket();
          socket = null;
        }
      }
      TDLog.Log( TDLog.LOG_SYNC, "sync AcceptThread done");
    }

    public void cancel()
    {
      try {
        mAcceptRun = false;
        mmServerSocket.close();
      } catch (IOException e) {
        TDLog.Error( "close() of server failed " + e);
      }
    }
  }


  //
  // This thread runs while attempting to make an outgoing connection
  // with a device. It runs straight through; the connection either succeeds or fails.
  // 
  private class ConnectingThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    ConnectingThread( BluetoothDevice device )
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
        TDLog.Error( "ConnectingThread cstr failed " + e);
      }
      mmSocket = tmp;
    }

    public void run()
    {
      TDLog.Log( TDLog.LOG_SYNC, "sync ConnectingThread run");
      setName("ConnectingThread");
      mAdapter.cancelDiscovery(); // Always cancel discovery because it will slow down a connection

      try { // Make a connection to the BluetoothSocket
        mmSocket.connect(); // blocking call
      } catch (IOException e) {
        connectionFailed();
        try { // Close the socket
          mmSocket.close();
        } catch (IOException e2) {
          TDLog.Error( "unable to close() socket during connection failure " + e2);
        }
        // SyncService.this.start(); // Start the service over to restart listening mode
        return;
      }

      synchronized ( SyncService.this ) { // Reset the ConnectingThread because we're done
        mConnectingThread = null;
      }

      connected( mmSocket, mmDevice ); // Start the connected thread
      TDLog.Log( TDLog.LOG_SYNC, "sync connecting thread done");
    }

    public void cancel()
    {
      try {
        mmSocket.close();
      } catch (IOException e) {
        TDLog.Error( "close() of connect socket failed " + e);
      }
    }
  }

  //
  // This thread runs during a connection with a remote device.
  // It handles all incoming and outgoing transmissions.
  //
  private class ConnectedThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    ConnectedThread(BluetoothSocket socket)
    {
      mmSocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      try { // Get the BluetoothSocket input and output streams
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) {
        TDLog.Error( "temp sockets not created " + e);
      }

      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    public void run() 
    {
      TDLog.Log( TDLog.LOG_SYNC, "sync connected thread run");

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
			  // for ( int j=0; j<pos; ++j) tmp[j] = data[j];
              System.arraycopy(data, 0, tmp, 0, pos);
              // special handle shutdown message 
              if ( data[0] == 0 && 
                   data[1] == DataListener.SHUTDOWN && 
                   data[2] == 0 ) {
                mConnectRun = false;
                mRemoteDevice = null;
                setConnectState( STATE_NONE );
              } else {
                // Log.v("DistoX", "read <" + data[0] + "|" + data[1] + ">" );
                mHandler.obtainMessage( MESSAGE_READ, pos, -1, tmp).sendToTarget();
                pos = 0;
              }
            } else {
              data[pos] = buffer[k];
              ++pos;
            }
          }
        } catch (IOException e) {
          TDLog.Error( "disconnected " + e);
          try { // Close the socket
            mmSocket.close();
          } catch (IOException e2) {
            TDLog.Error( "unable to close() socket during connection failure " + e2);
          }
          connectionLost();
          break;
        }
      }
      // TopoDroidLoLogog( TDLog.LOG_SYNC, "sync ConnectedThread done type " + mType );
      if ( mType == STATE_LISTEN ) {
        // ONE-TO-ONE
        // startAccept(); 
      } else if ( mType == STATE_CONNECTING ) {
        TopoDroidUtil.slowDown( 200 );
        reconnect();
      }
    }

    //
    // Write to the connected OutStream.
    // @param buffer  The bytes to write
    //
    boolean doWriteBuffer( byte[] buffer )
    {
      Log.v("DistoX", "sync connected write " + buffer.length + ": <" + buffer[0] + "|" + buffer[1] + ">" );

      try {
        mmOutStream.write( buffer );
        // Share the sent message back to the UI Activity: NOT USED .... FIXME
        // mHandler.obtainMessage( MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
      } catch (IOException e) {
        TDLog.Error( "Exception during write " + e );
        return false;
      }
      return true;
    }

    public void cancel() 
    {
      mConnectRun = false;
      try {
        mmInStream.close();
        mmSocket.close();
      } catch (IOException e) {
        TDLog.Error( "close() of connect socket failed " + e );
      }
    }
  }
}

*/
