/* @file ConnectionHandler.java
 *
 * @author marco corvi
 * @date dec 2014
 *
 * @brief TopoDroid lister interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.content.Intent;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

class ConnectionHandler extends Handler
                        implements DataListener
{
   SyncService mSyncService;
   
   long mSID; // survey id for this connection
   private byte mSendCounter;  // send counter
   private byte mRecvCounter;  // recv counter must be equal to the peer send counter
                       // it is increased after the ack
   byte mAck[]; // ACK buffer
   // ConcurrentLinkedQueue< byte[] > mBufferQueue;
   ConnectionQueue mBufferQueue;
   TopoDroidApp mApp;
   BluetoothDevice mDevice;
   boolean mClient;   // whether this TopoDroid initiated the connection
   boolean mRun;

   SendThread mSendThread;

   ConnectionHandler( TopoDroidApp app )
   {
     mApp = app;
     mSendCounter = (byte)0;
     mRecvCounter = (byte)0;
     mAck = new byte[3];
     // mBufferQueue = new ConcurrentLinkedQueue< byte[] >();
     mBufferQueue = new ConnectionQueue();

     mSyncService = new SyncService( mApp, this );
     mDevice = null;
     mClient = false;
     mRun = false;
     mSendThread = null;
   }

   int getType() { return mSyncService.getType(); }

  int getAcceptState()
  { 
    if ( mSyncService == null ) return SyncService.STATE_NONE;
    return mSyncService.getAcceptState();
  }

  int getConnectState()
  { 
    if ( mSyncService == null ) return SyncService.STATE_NONE;
    return mSyncService.getConnectState();
  }


  String getConnectStateStr()
  {
    if ( mSyncService == null ) return "UNKNOWN";
    return mSyncService.getConnectStateStr();
  }

  String getConnectedDeviceName()
  {
    if ( mSyncService == null ) return null;
    return mSyncService.getConnectedDeviceName();
  }


  String getConnectionStateTitleStr()
  {
    if ( mSyncService == null ) return "";

    String s1 = "";
    if ( mSyncService.getConnectState() == SyncService.STATE_CONNECTING ) {
      s1 = "<.>";
    } else if ( mSyncService.getConnectState() == SyncService.STATE_CONNECTED ) {
      s1 = "<->";
    }
    String s2 = ( mSyncService.getAcceptState() != SyncService.STATE_LISTEN )? "" : "(*)";
    return s2 + s1;
  }

   void start()
   {  
     // TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler start()");
     mClient = false;
     mDevice = null;
     mSyncService.start();
   }

   void stop() 
   { 
     // TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler stop()");
     stopSendThread();
     mSyncService.stop();
   }

   void connect( BluetoothDevice device )
   {
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler connect() " + device.getName() );
     if ( mDevice != device ) {
       mRun = false;
       if ( mSendThread != null ) {
         try {
           mSendThread.join();
         } catch ( InterruptedException e ) { }
         mSendThread = null;
         mBufferQueue.clear(); // flush the queue
       }
     }
     mDevice = device;
     if ( mDevice != null ) {
       mClient = true;
       mSyncService.connect( mDevice );
     }
   }

   // called when the connection has been lost
   private void reconnect()
   {
     if ( mDevice == null ) return;
     if ( ! mClient ) return;
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler reconnect() ");
     if ( mSendThread != null ) stopSendThread();
     while ( mSyncService.getConnectState() == SyncService.STATE_NONE ) {
       try {
         Thread.sleep( 200 );
       } catch ( InterruptedException e ) { }
       mSyncService.connect( mDevice );
     }
     if ( mSyncService.getConnectState() == SyncService.STATE_CONNECTED ) {
       doSyncCounter();
     }
   }
  
   void connectionFailed()
   {
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler connectionFailed() ");
     if ( mClient ) {
       mClient = false;
       mDevice = null;
       mApp.syncConnectionFailed();
     } else {
       // mSyncService.start();
     }
   }

   // device will be used when n-n (instead of 1-1)
   void disconnect( BluetoothDevice device )
   {
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler disconnect() ");
     // if ( device.getName().equals( mDevice.getName() ) {
       stopSendThread();
       mSyncService.disconnect();
       mDevice = null;
     // }
   }

   void syncDevice( BluetoothDevice device )
   {
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler syncDevice() ");
     // if ( device.getName().equals( mDevice.getName() ) {
       doSyncCounter();
     // }
   }

   void write( byte[] buffer ) 
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler write CNT " + buffer[0] + " key " + buffer[1] );
     mSyncService.write( buffer );
   }

   void startSendThread()
   {
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler startSendThread()");
     mRun = true;
     mSendThread = new SendThread( mBufferQueue );
     mSendThread.start();
   }

   void stopSendThread()
   {
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ConnectionHandler stopSendThread()");
     mRun = false;
     if ( mSendThread != null ) {
       try {
         mSendThread.join();
       } catch ( InterruptedException e ) { }
       mSendThread = null;
       // flush the queue
     }
   }


   // -----------------------------------------

   byte increaseCounter( byte cnt )
   {
     return ( cnt == (byte)0xfe )? (byte)0 : (byte)(cnt+1);
   }


   void doAcknowledge( int cnt )
   {
     // Log.v("DistoX", "ACK count " + cnt );
     mAck[0] = (byte)cnt;
     mAck[1] = DataListener.ACK; // 0
     mAck[2] = DataListener.EOL;
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "ACK write <" + cnt + ">" );
     write( mAck );
   }

   // tell the peer my send counter
   void doSyncCounter( )
   {
     mAck[0] = (byte)mSendCounter;
     mAck[1] = DataListener.SYNC;
     mAck[2] = DataListener.EOL;
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "SYNC write <" + mSendCounter + ">" );
     write( mAck );
   }


   // received command
   // the received command is terminated by 0xff
   //
   // ACK = 0xfe;
   // SURVEY_SET  = 1;
   // SURVEY_INFO = 2;
   // SURVEY_DATE = 3;
   // SURVEY_TEAM = 4;
   // SURVEY_DECL = 5;
   //
   void onRecv( int bytes, byte[] buffer ) 
   {
     // Log.v("DistoX", "ConnectionHandler onRecv() len " + buffer.length );
     if ( buffer.length < 2 ) {
       return;
     }
     byte cnt = buffer[0];
     byte key = buffer[1];

     if ( key == DataListener.SYNC ) { // sync request
       mRecvCounter = cnt;
       return;
     } else if ( key == DataListener.ACK ) {
       synchronized( mBufferQueue ) {
         ConnectionQueueItem item = mBufferQueue.find( cnt );
         if ( item != null ) {
           mBufferQueue.remove( item );
           TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "recv ACK <" + cnt + "> removed. queue size " + mBufferQueue.size() );
         } else {
           TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "recv ACK <" + cnt + "> not found" );
         }
       }
       return;
     }

     if ( mRecvCounter != cnt ) {
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "recv ERROR <" + cnt + "|" + key + "> expected " + mRecvCounter );
       // should ack again ?
       // doAcknowledge( cnt );
       return;
     }

     doAcknowledge( cnt );
     mRecvCounter = increaseCounter( mRecvCounter );

     String data_str = (new String( buffer )).substring( 2 );
     int kk = 0;
     for ( int k=0; k < data_str.length(); ++k ) if ( data_str.charAt(k) == '|' ) ++kk;
     // String[] data = data_str.split("\\|");
     String[] data = new String[kk];
     kk = 0;
     int k0 = 0;
     for ( int k=0; k < data_str.length(); ++k ) if ( data_str.charAt(k) == '|' ) {
       if ( k0 == k ) {
         data[kk] = "";
       } else {
         data[kk] = data_str.substring( k0, k );
       }
       k0 = k+1;
       ++kk;
     }
     TopoDroidLog.Log( TopoDroidLog.LOG_SYNC,
        "recv <" + cnt + "|" + key + "> len " + buffer.length + " data[" + data.length + "]: " + data_str );

     switch ( key ) {
       case DataListener.SURVEY_SET:
         if ( ! data[0].equals( mApp.mySurvey ) ) {
           mSID = mApp.setSurveyFromName( data[0], false );
         } else {
           mSID = mApp.mSID;
         }
         break; 
       case DataListener.SURVEY_INFO:
         mApp.mData.updateSurveyInfo( mSID, data[0], data[1], Double.parseDouble( data[2] ), data[3], false );
         break;
       case DataListener.SURVEY_DATE:
         mApp.mData.updateSurveyDayAndComment( mSID, data[0], data[1], false );
         break;
       case DataListener.SURVEY_TEAM:
         mApp.mData.updateSurveyTeam( mSID, data[0], false );
         break;
       case DataListener.SURVEY_DECL:
         mApp.mData.updateSurveyDeclination( mSID, Double.parseDouble( data[0] ), false );
         break;

       case DataListener.SHOT_UPDATE:
         mApp.mData.updateShot( Integer.parseInt(data[0]), mSID, data[2], data[3], 
                                Integer.parseInt(data[4]), Integer.parseInt(data[5]), Integer.parseInt(data[6]),
                                data[7], false );
         break;
       case DataListener.SHOT_NAME:
         mApp.mData.updateShotName( Integer.parseInt(data[0]), mSID, data[2], data[3], false );
         break;
       case DataListener.SHOT_LEG:
         mApp.mData.updateShotLeg( Integer.parseInt(data[0]), mSID, Integer.parseInt(data[2]), false );
         break;
       case DataListener.SHOT_EXTEND:
         mApp.mData.updateShotExtend( Integer.parseInt(data[0]), mSID, Integer.parseInt(data[2]), false );
         break;
       case DataListener.SHOT_FLAG:
         mApp.mData.updateShotFlag( Integer.parseInt(data[0]), mSID, Integer.parseInt(data[2]), false );
         break;
       case DataListener.SHOT_COMMENT:
         mApp.mData.updateShotComment( Integer.parseInt(data[0]), mSID, data[2], false );
         break;
       case DataListener.SHOT_DELETE:
         mApp.mData.deleteShot( Integer.parseInt(data[0]), mSID, false );
         break;
       case DataListener.SHOT_UNDELETE:
         mApp.mData.undeleteShot( Integer.parseInt(data[0]), mSID, false );
         break;
       case DataListener.SHOT_AMDR:
         mApp.mData.updateShotAMDR( Integer.parseInt(data[0]), mSID, 
           Double.parseDouble(data[2]), Double.parseDouble(data[3]), Double.parseDouble(data[4]), Double.parseDouble(data[5]),
           false );
         break;
       case DataListener.SHOT_DBC_UPDATE:
         mApp.mData.updateShotDistanceBearingClino( Integer.parseInt(data[0]), mSID, 
                                Float.parseFloat(data[2]), Float.parseFloat(data[3]), Float.parseFloat(data[4]), false );
         break;

       case DataListener.SHOT_INSERT:
         mApp.mData.insertShot( mSID, Integer.parseInt(data[1]), data[2], data[3],
           Double.parseDouble(data[4]), Double.parseDouble(data[5]), Double.parseDouble(data[6]), Double.parseDouble(data[7]),
           Integer.parseInt(data[8]), Integer.parseInt(data[9]), Integer.parseInt(data[10]), Integer.parseInt(data[11]),
           Integer.parseInt(data[12]),
           data[13], false );
         break;
       case DataListener.SHOT_INSERTAT:
         mApp.mData.insertShotAt( mSID, Integer.parseInt(data[1]), 
           Double.parseDouble(data[2]), Double.parseDouble(data[3]), Double.parseDouble(data[4]), Double.parseDouble(data[5]),
           Integer.parseInt(data[6]),
           false );
         break;

       case DataListener.PLOT_INSERT:
         mApp.mData.insertPlot( mSID, Integer.parseInt(data[1]), data[2], Integer.parseInt(data[3]), Integer.parseInt(data[4]),
           data[5], data[6], Double.parseDouble(data[7]), Double.parseDouble(data[8]),
           Double.parseDouble(data[9]), Double.parseDouble(data[10]), Double.parseDouble(data[11]), false ); 
         break;
       case DataListener.PLOT_UPDATE:
         break;
       case DataListener.PLOT_DROP:
         break;
       case DataListener.PLOT_DELETE:
         break;
       case DataListener.PLOT_UNDLEETE:
         break;
     }
     mApp.refreshUI();
  }

  // put a command onto the queue
  // the buffer has two header bytes, followed by the command string, terminated by 0xff
  //
  // data must be terminated by adding 0xff
  private void enqueue( byte key, String data )
  {
    int len = data.length();
    byte[] buf = new byte[ 3 + len ];
    buf[0] = mSendCounter;
    buf[1] = key;
    buf[2+len] = DataListener.EOL;
    for ( int k=0; k<len; ++k ) buf[2+k] = (byte)data.charAt(k);
    mBufferQueue.add( buf );
    TopoDroidLog.Log( TopoDroidLog.LOG_SYNC,
      "enqueue <" + mSendCounter + "|" + key + "> queue " + mBufferQueue.size() + " data[" + len + "]: " + data );
    mSendCounter = increaseCounter( mSendCounter );
  }

  // only the sync-layer need be notified of this
  public void onSetSurvey( long id, String name ) 
  {
    String data = name + "|";
    enqueue( DataListener.SURVEY_SET, data );
  }

  public void onUpdateSurveyInfo( long id, String date, String team, double decl, String comment ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%s|%s|%.2f|%s|", date, team, decl, comment );
    enqueue( DataListener.SURVEY_INFO, sw.getBuffer().toString() );
  }

  public void onUpdateSurveyDayAndComment( long id, String date, String comment )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%s|%s|", date, comment );
    enqueue( DataListener.SURVEY_DATE, sw.getBuffer().toString() );
  }

  public void onUpdateSurveyTeam( long id, String team )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%s|", team );
    enqueue( DataListener.SURVEY_TEAM, sw.getBuffer().toString() );
  }

  public void onUpdateSurveyDeclination( long id, double decl )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%.2f|", decl );
    enqueue( DataListener.SURVEY_DECL, sw.getBuffer().toString() );
  }

  // -------------------------------------------------------------------------
  // SHOTS

  public void onUpdateShotDBC( long id, long sid, float d, float b, float c )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|%.2f|%.1f|%.1f|", (int)id, (int)sid, d, b, c );
    enqueue( DataListener.SHOT_DBC_UPDATE, sw.getBuffer().toString() );
  }

  public void onUpdateShot( long id, long sid, String fStation, String tStation,
                            long extend, long flag, long leg, String comment ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|%s|%s|%d|%d|%d|%s|",
      (int)id, (int)sid, fStation, tStation, (int)extend, (int)flag, (int)leg, comment );
    enqueue( DataListener.SHOT_UPDATE, sw.getBuffer().toString() );
  }

  public void onUpdateShotName( long id, long sid, String fStation, String tStation )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|%s|%s|", (int)id, (int)sid, fStation, tStation );
    enqueue( DataListener.SHOT_NAME, sw.getBuffer().toString() );
  }

  public void onUpdateShotLeg( long id, long sid, long leg ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|%d|", (int)id, (int)sid, (int)leg );
    enqueue( DataListener.SHOT_LEG, sw.getBuffer().toString() );
  }

  public void onUpdateShotExtend( long id, long sid, long extend ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|%d|", (int)id, (int)sid, (int)extend );
    enqueue( DataListener.SHOT_EXTEND, sw.getBuffer().toString() );
  }

  public void onUpdateShotFlag( long id, long sid, long flag )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|%d|", (int)id, (int)sid, (int)flag );
    enqueue( DataListener.SHOT_FLAG, sw.getBuffer().toString() );
  }

  public void onUpdateShotComment( long id, long sid, String comment ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|%s|", (int)id, (int)sid, comment );
    enqueue( DataListener.SHOT_COMMENT, sw.getBuffer().toString() );
  }

  public void onUpdateShotAMDR( long sid, long id, double acc, double mag, double dip, double roll ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%d|%d|%.2f|%.2f|%.2f|%.2f|", (int)sid, (int)id, acc, mag, dip, roll );
    enqueue( DataListener.SHOT_AMDR, sw.getBuffer().toString() );
  }

  public void onDeleteShot( long id, long sid )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|", (int)id, (int)sid );
    enqueue( DataListener.SHOT_DELETE, sw.getBuffer().toString() );
  }

  public void onUndeleteShot( long id, long sid )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format( "%d|%d|", (int)id, (int)sid );
    enqueue( DataListener.SHOT_UNDELETE, sw.getBuffer().toString() );
  }

  public void onInsertShot( long sid, long id, String from, String to, 
                          double d, double b, double c, double r, 
                          long extend, long flag, long leg, long status, String comment ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%d|%d|%s|%s|%.2f|%.1f|%.1f|%.1f|%d|%d|%d|%d|%s|", 
      (int)sid, (int)id, from, to, d, b, c, r, (int)extend, (int)flag, (int)leg, (int)status, comment );
    enqueue( DataListener.SHOT_INSERT, sw.getBuffer().toString() );
  }

  public void onInsertShotAt( long sid, long at, double d, double b, double c, double r ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%d|%d||%.2f|%.1f|%.1f|%.1f|", (int)sid, (int)at, d, b, c, r );
    enqueue( DataListener.SHOT_INSERT, sw.getBuffer().toString() );
  }

  // public void transferShots( long sid, long old_sid, long old_id ) { }

  // public void doDeleteSurvey( long sid ) 

  // -------------------------------------------------------
  // PLOTS Aand SKETCHES


  public void onInsertPlot( long sid, long id, String name, long type, long status, String start, String view,
                            double xoffset, double yoffset, double zoom, double azimuth, double clino ) 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%d|%d|%s|%d|%d|%s|%s|%.2f|%.2f|%.2f|%.2f|%.2f|",
      (int)sid, (int)id, name, (int)type, (int)status, start, view, xoffset, yoffset, zoom, azimuth, clino );
    enqueue( DataListener.PLOT_INSERT, sw.getBuffer().toString() );
  }

  // public void updatePlot( long plot_id, long survey_id, double xoffset, double yoffset, double zoom ) { }

  // public void onNewSketch3d( long sid, long id, String name, long status, String start, String st1, String st2,
  //                         double xoffsettop, double yoffsettop, double zoomtop,
  //                         double xoffsetside, double yoffsetside, double zoomside,
  //                         double xoffset3d, double yoffset3d, double zoom3d,
  //                         double x, double y, double z, double azimuth, double clino ) { }

  // public void updateSketch( long sketch_id, long survey_id, 
  //                            String st1, String st2,
  //                            double xofftop, double yofftop, double zoomtop,
  //                            double xoffside, double yoffside, double zoomside,
  //                            double xoff3d, double yoff3d, double zoom3d,
  //                            double east, double south, double vert, double azimuth, double clino ) { }

  // public void dropPlot( long plot_id, long survey_id ) { }
  // public void deletePlot( long plot_id, long survey_id ) { }
  // public void undeletePlot( long plot_id, long survey_id ) { }
  // public void deleteSketch( long sketch_id, long survey_id ) { }

  // public void onNewPhoto( long sid, long id, long shotid, String title, String date, String comment ) { }
  // public void onUpdatePhoto( long sid, long id, String comment ) { }
  // public void onDeletePhoto( long sid, long id ) { }

  // -------------------------------------------------------------------------
  // public void onNewSensor( long sid, long id, long shotid, String title, String date, String comment, 
  //                            String type, String value ) { }
  // public void onDeleteSensor( long sid, long id ) { }
  // public void onUpdateSensor( long sid, long id, String comment ) { }

  // public void onNewFixed( long sid, long id, String station, double lng, double lat, double alt, double asl,
  //                         String comment, long status ) { }
  // public void onUpdateFixedStation( long id, long sid, String station ) { }
  // public void onUpdateFixedStatus( long id, long sid, long status ) { }
  // public void onDeletedFixed( long sid, String station ) { }

  // -------------------------------------------------------------------
  // need a thread to empty the queue and write to the SyncService connected thread
  // incoming messages are handled by this class directly

  static int SLEEP_DEQUE =  100; 
  static int SLEEP_EMPTY = 1000;

  private class SendThread extends Thread
  {
    // final ConcurrentLinkedQueue<byte[]> mQueue;
    final ConnectionQueue mQueue;

    // SendThread( ConcurrentLinkedQueue<byte[]> queue ) 
    SendThread( ConnectionQueue queue ) 
    {
      mQueue = queue;
    }
  
    @Override
    public void run()
    {
      int cnt = 0;
      byte lastByte = (byte)0xff;
      // Log.v( "DistoX", "SendThread running ...");
      while( mRun ) {
        try {
          while ( mRun && mQueue.isEmpty() ) Thread.sleep( SLEEP_EMPTY );
          if ( mRun ) {
            // byte buffer[] = mQueue.peek();
            // write( buffer );
            ConnectionQueueItem item = mQueue.peek();
            if ( item != null ) {
              byte[] buffer = item.mData;
              if ( buffer[0] == lastByte ) {
                ++ cnt;
              }
              TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "lastByte " + lastByte + " cnt " + cnt );
              if ( cnt > 10 ) {
                // bail-out
                disconnect( mDevice );
              } else {
                cnt = 0;
                lastByte = buffer[0];
                TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "data write <" + buffer[0] + "|" + buffer[1] + ">" );
                write( item.mData );
              }
            }
            Thread.sleep( SLEEP_DEQUE );   
          }
        } catch ( InterruptedException e ) {
        }
      }
      TopoDroidLog.Log( TopoDroidLog.LOG_SYNC, "SendThread exiting");
    }
  }

  @Override
  public void handleMessage( Message msg )
  {
    // Log.v("DistoX", "handle message: " + msg.arg1 );
    Bundle bundle; 
    switch (msg.what) {
      case SyncService.MESSAGE_LOST_CONN: // 5
        reconnect();
        break;
      case SyncService.MESSAGE_FAIL_CONN: // 6
        connectionFailed();
        break;
      case SyncService.MESSAGE_CONNECT_STATE: // 1
        mApp.connStateChanged();
        // Log.v("DistoX", "handle message: sync connect stat " + msg.arg1 );
        break;
      case SyncService.MESSAGE_ACCEPT_STATE: // 7
        mApp.connStateChanged();
        // Log.v("DistoX", "handle message: sync accept state " + msg.arg1 );
        break;
      case SyncService.MESSAGE_DEVICE: // 2
        bundle = msg.getData();
        String name = bundle.getString( SyncService.DEVICE );
        // Log.v("DistoX", "sync device " + name );
        mApp.syncConnectedDevice( name );
        startSendThread();
        break;
      case SyncService.MESSAGE_READ: // 3
        int bytes = msg.arg1;
        // Log.v("DistoX", "READ bytes " + bytes );
        byte[] buffer = ( byte[] ) msg.obj;
        onRecv( bytes, buffer );
        break;
      case SyncService.MESSAGE_WRITE: // 4
        // Log.v("DistoX", "WRITE " );
        // nothing
        break;
    }

  }

}

