/* @file CavwayComm.java
 *
 * @author siwei tian
 * @date july 2024
 *
 * @brief TopoDroid Cavway BLE comm class
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
// import android.os.Handler;

import com.topodroid.TDX.R;
import com.topodroid.TDX.TDandroid;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.ListerHandler;
import com.topodroid.TDX.TopoDroidApp;
// import com.topodroid.TDX.TopoDroidAlertDialog;
import com.topodroid.dev.ConnectionState;
// import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.dev.ble.BleCallback;
import com.topodroid.dev.ble.BleOpsQueue;
import com.topodroid.dev.ble.BleComm;
import com.topodroid.dev.ble.BleOpChrtWrite;
import com.topodroid.dev.ble.BleOpConnect;
import com.topodroid.dev.ble.BleOpDisconnect;
import com.topodroid.dev.ble.BleOpNotify;
import com.topodroid.dev.ble.BleOpRequestMtu;
import com.topodroid.dev.ble.BleOperation;
import com.topodroid.dev.ble.BleBuffer;
import com.topodroid.dev.ble.BleQueue;
import com.topodroid.dev.ble.BleUtils;
import com.topodroid.dev.distox.DistoX;
import com.topodroid.dev.distox.IMemoryDialog;
import com.topodroid.packetX.CavwayData;
import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.TDProgress;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
// import java.util.Timer;
// import java.util.TimerTask;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Handler;
// import android.os.Message;
import android.os.Looper;
import android.content.res.Resources;

public class CavwayComm extends BleComm
{
  private final static String TAG = "CAVWAY comm ";
  private final static boolean LOG = true;
  private final static boolean USE_MTU = false; // max MTU 250

  private final static int READ_NONE   = 0;
  private final static int READ_MEMORY = 1;
  // private final static int READ_COEFFS = 2;

  private static final int BYTE_PER_DATA  = CavwayData.SIZE;

  // private ConcurrentLinkedQueue< BleOperation > mOps;
  // private BleOpsQueue mQps;
  // BleCallback mCallback;
  // private Context mContext;
  // private BleQueue mQueue;

  private ListerHandler mLister = null;
  private String mAddress;
  private int    mTimeout;
  public boolean mHasInfo = false;
  public boolean mHasWritten = false;

  BluetoothGattCharacteristic mReadChrt  = null;
  BluetoothGattCharacteristic mWriteChrt = null;
  //private boolean mReadInitialized  = false;
  //private boolean mWriteInitialized = false;
  private boolean mReconnect = false;
  private boolean mSkipNotify = false;
  private int mReadingMemory = READ_NONE; // 0 none; 1 memory; 2 coeffs

  private int mDataType;
  private int mPacketType;  // type of the last incoming packet that has been read

  // private Timer mTimer;

  // private int mPacketToRead = 0; // number of packet to read with laser-commands
  Thread mConsumer = null;

  // final Object mNewDataFlag = new Object();

  boolean mThreadConsumerWorking = false;

  /** cstr
   * @param ctx       context
   * @param app       application
   * @param address   device address (not used: TODO drop)
   * @param bt_device remote device
   */
  public CavwayComm(Context ctx,TopoDroidApp app, String address, BluetoothDevice bt_device )
  {
    super( app, USE_MTU, CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_READ_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID );
    if ( LOG ) TDLog.v( TAG + "cstr" );
    // mRemoteAddress  = address;
    mRemoteBtDevice = bt_device;
    mContext = ctx;
    // mNewDataFlag = new Object();
    // mQueue = new BleQueue(); done by super
    startConsumerThread();
    if ( LOG ) TDLog.v( TAG + "cstr: addr " + address );
    // mOps = new ConcurrentLinkedQueue<BleOperation>();
    // clearPending();
  }

  // class CavwayTimerTask extends TimerTask
  // {
  //   private CavwayComm mComm;
  //   
  //   CavwayTimerTask( CavwayComm comm ) { mComm = comm; }

  //   public void run()
  //   {
  //     if ( LOG ) TDLog.v("Timer Task reconnect device");
  //     mComm.closeDevice( true );
  //     mComm.connectDevice( mAddress, mLister, mDataType, mTimeout );
  //   }
  // }

  // private void resetTimer()
  // {
  //   if ( ! TDSetting.isConnectionModeContinuous() ) return;
  //   TDLog.v("Timer reset ");
  //   if ( mTimer != null ) {
  //     mTimer.cancel();
  //     mTimer.purge();
  //   }
  //   mTimer = new Timer();
  //   mTimer.schedule( new CavwayTimerTask( this ), 5000 );
  // }

  // private void stopTimer()
  // {
  //   if ( mTimer == null ) return;
  //   TDLog.v("Timer stop ");
  //   mTimer.cancel();
  //   mTimer.purge();
  //   mTimer = null;
  // }


  private void startConsumerThread()
  {
    if ( mConsumer != null ) {
      TDLog.e("CONSUMER THREAD start with non-null consumer");
      return;
    }
    if ( LOG ) TDLog.v("start consumer thread");
    mConsumer = new Thread() { // this is the thread that consumes data on the queue
      @Override public void run() {
        //mThreadConsumerWorking = true;
        if ( LOG ) TDLog.v("CONSUMER THREAD start");
        while ( true ) {
          if ( LOG ) TDLog.v( "CONSUMER THREAD: Queue size " + mQueue.size );
          BleBuffer buffer = mQueue.get();
          if ( buffer == null ) continue;
          if ( buffer.type == BleQueue.DATA_PRIM ) {
            if ( buffer.data == null) {
              TDLog.t( "CONSUMER THREAD: buffer with null data");
              continue;
            }
            // ++mNrReadPackets; this is incremented once for DATA and once for VECTOR by TopoDroidComm
            if ( LOG ) TDLog.v( "CONSUMER THREAD: buffer read " + mNrReadPackets );
            int res = ((CavwayProtocol)mProtocol).packetProcess( buffer.data );
            if ( res == CavwayProtocol.PACKET_FLASH_BYTES_1 ) continue;   // first-half of firmware block received
            synchronized (mNewDataFlag) {
              // if ( mPacketToRead > 0 ) mPacketToRead --; // only for shot data from laser task
              mPacketType = res;
              mNewDataFlag.notifyAll(); // wake sleeping threads
            }
          } else if ( buffer.type == BleQueue.DATA_QUIT ) {
            if ( LOG ) TDLog.v( "CONSUMER THREAD - buffer QUIT");
            break;
          }
          TDUtil.slowDown(300);
        }
        if ( LOG ) TDLog.v( "CONSUMER THREAD exit");
      }
    };
    mConsumer.start();
  }

  private void stopConsumerThread()
  {
    if ( mConsumer != null ) {
      // put a BleQueue.DATA_QUIT buffer on the queue
      if ( LOG ) TDLog.v( TAG + "stop consumer thread");
      mQueue.put( BleQueue.DATA_QUIT, new byte[0] );
    }
    mConsumer = null;
  }
    

  /** @return true if it is downloading or in skip-notify mode
   */
  boolean isDownloading() 
  { 
    if ( LOG ) TDLog.v( TAG + "is downloading - skip notyfy " + mSkipNotify );
    return mApp.isDownloading() || mSkipNotify;
  }

  /** @return true if the communication is reading from memory
   */
  boolean isReadingMemory() { return mReadingMemory == READ_MEMORY; }

  // boolean isReadingCoeffs() { return mReadingMemory == READ_COEFFS; }

  // void clearReadingMemory() { mReadingMemory = READ_NONE; }

  /* terminate the consumer thread - put a "quit" buffer on the queue
   * @note this method has still to be used
   */
  @Override
  public void terminate()
  {
    if ( LOG ) TDLog.v( TAG + "terminate");
    stopConsumerThread();
  }
  // -------------------------------------------------------------
  /**
   * connection and data handling must run on a separate thread
   */

  /** connect to the remote Cavway device
   * @param device    device (info)
   * @param lister    data lister
   // * @param data_type expected type of data (unused)
   * @return true if success
   * @note Device has mAddress, mModel, mName, mNickname, mType
   * the only thing that coincide with the remote_device is the address
   */
  private boolean connectCavwayDevice( Device device, ListerHandler lister /*, int data_type */ )
  {
    mLister = lister;
    if ( mRemoteBtDevice == null ) {
      TDToast.makeBad( R.string.ble_no_remote );
      // TDLog.t(" comm ERROR null remote cavway");
      if ( LOG ) TDLog.v( TAG + "- connect cavway: null => status DISCONNECTED" );
      notifyStatus( ConnectionState.CONN_DISCONNECTED );
      return false;
    }
    if ( LOG ) TDLog.v( TAG + "- connect cavway => status WAITING");
    notifyStatus( ConnectionState.CONN_WAITING );
    mReconnect   = true;

    mProtocol    = new CavwayProtocol( mContext, mApp, lister, device, this );
    mCallback    = new BleCallback( this, false ); // auto_connect false

    // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    // mQps.clearPending();

    if ( LOG ) TDLog.v( TAG + "connect: enqueue connect" );
    // mOps         = new ConcurrentLinkedQueue< BleOperation >();
    mQps = new BleOpsQueue();
    int ret = mQps.enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
    // if ( LOG ) TDLog.v( TAG + "connect ... " + ret);
    mQps.clearPending();
    return true;
  }

  // /** open connection to the GATT
  //  * @param ctx       context
  //  * @param bt_device (remote) bluetooth device
  //  */
  // public void connectGatt( Context ctx, BluetoothDevice bt_device ) // called from BleOpConnect
  // {
  //   if ( LOG ) TDLog.v( TAG + "connect GATT");
  //   mContext = ctx;
  //   mCallback.connectGatt( mContext, bt_device );
  //   // setupNotifications(); // FIXME_Cavway
  //   // if ( LOG ) TDLog.v( TAG + "after connect GATT: bond state " + bt_device.getBondState() );
  // }

  /** connect to the remote Cavway device
   * @param address   device address (unused)
   * @param lister    data lister
   * @param data_type expected type of data (unused)
   * @param timeout   ...
   * @return true if success
   */
  @Override
  public boolean connectDevice(String address, ListerHandler lister, int data_type, int timeout ) // FIXME XBLE_DATA_TYPE ?
  {
    if ( LOG ) TDLog.v( TAG + "\"connect Device\" data type " + data_type );
    mAddress       = address; // saved
    mNrReadPackets = 0;
    mDataType      = data_type;
    mLister        = lister;  // saved
    mTimeout       = timeout; // saved
    return connectCavwayDevice( TDInstance.getDeviceA(), lister /*, data_type */ );
  }


  // ----------------- DISCONNECT -------------------------------

  /** notified that the device has disconnected
   * @note from onConnectionStateChange STATE_DISCONNECTED
   */
  public void disconnected()
  {
    // clearPending();
    // mOps.clear();
    mQps.clear( true );

    // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    mBTConnected = false;
    if ( LOG ) TDLog.v( TAG + "\"disconnected\": status DISCONNECTED");
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    // mCallback.closeGatt();
  }

  public void connected()
  {
    if ( LOG ) TDLog.v( TAG + "\"connected\": bond state " + mRemoteBtDevice.getBondState() );
    mQps.clearPending();
  }

  // public void disconnectGatt()  // called from BleOpDisconnect
  // {
  //   if ( LOG ) TDLog.v( TAG + "\"disconnect GATT\": status DISCONNECTED");
  //   notifyStatus( ConnectionState.CONN_DISCONNECTED );
  //   mCallback.closeGatt();
  // }

  /** disconnect from the remote device
   */
  @Override
  public boolean disconnectDevice()
  {
    if ( LOG ) TDLog.v( TAG + "\"disconnect device\": connected " + mBTConnected );
    mReconnect = false;  // was stopTimer();
    return closeDevice( false );
  }

  // this is called only on a GATT failure, or the user disconnects
  private boolean closeDevice( boolean force )
  {
    mReconnect = false;
    if ( mBTConnected || force ) {
      if ( LOG ) TDLog.v( TAG + "\"close device\": connected " + mBTConnected + " force " + force );
      //mThreadConsumerWorking = false;
      mBTConnected = false;
      notifyStatus( ConnectionState.CONN_DISCONNECTED ); // not necessary
      // TDLog.v( TAG + "close device: enqueue disconnect => status ISCONNECTED");
      int ret = mQps.enqueueOp( new BleOpDisconnect( mContext, this ) ); // exec disconnectGatt
      mQps.doNextOp();
      // TDLog.v( TAG + "close Device - disconnect ... ops " + ret );
    }
    return true;
  }

  // --------------------------------------------------------------------------
  // private BleOperation mPendingOp = null;

  // /** clear the pending op and do the next if the queue is not empty
  //  */
  // private void clearPending()
  // {
  //   mPendingOp = null;
  //   // if ( ! mOps.isEmpty() || mPendingCommands > 0 ) doNextOp();
  //   if ( ! mOps.isEmpty() ) {
  //     doNextOp();
  //   } else {
  //     if ( LOG ) TDLog.v( TAG + "\"clear pending\": no more ops" );
  //   }
  // }

  // /** add a BLE op to the queue
  //  * @param op   BLE op
  //  * @return the length of the ops queue
  //  */
  // private int enqueueOp( BleOperation op )
  // {
  //   if ( LOG ) {
  //     if ( mRemoteBtDevice != null ) {
  //       // TDLog.v( TAG + "enqueue " + op.name() + " bond state " + mRemoteBtDevice.getBondState() );
  //     } else {
  //       TDLog.v( TAG + "enqueue " + op.name() + " null remote" );
  //     }
  //   }
  //   mOps.add( op );
  //   // printOps(); // DEBUG
  //   return mOps.size();
  // }

  // /** do the next op on the queue
  //  * @note access by BricChrtChanged
  //  */
  // private void doNextOp()
  // {
  //   if ( mPendingOp != null ) {
  //     if ( LOG ) TDLog.v( TAG + "next op with pending " + mPendingOp.name() + " not null, ops " + mOps.size() );
  //     return;
  //   }
  //   mPendingOp = mOps.poll();
  //   if ( mPendingOp != null ) {
  //     if ( LOG ) TDLog.v( TAG + "polled, ops " + mOps.size() + " exec " + mPendingOp.name() );
  //     mPendingOp.execute();
  //   } else {
  //     if ( LOG ) TDLog.v( TAG + "do next op - no op");
  //   }
  //   // else if ( mPendingCommands > 0 ) {
  //   //   enqueueShot( this );
  //   //   -- mPendingCommands;
  //   // }
  // }

  // BleComm interface

  // /** notified that the MTU (max transmit unit) has changed
  //  * @param mtu    max transmit unit
  //  */
  // public void changedMtu( int mtu )
  // {
  //   if ( USE_MTU ) {
  //     mQps.enqueueOp( new BleOpNotify( mContext, this, CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_READ_UUID, true ) );
  //   }
  //   mQps.clearPending();
  // }

  // /** notified that the remote RSSI has been read (Received Signal Strength Indicator)
  //  * @param rssi   remote rssi
  //  */
  // public void readedRemoteRssi( int rssi ) // from BleComm
  // {
  //   mQps.clearPending();
  // }

  /** notifies that a characteristics has changed
   * @param chrt    changed characteristics
   * @note queue.put() stores a buffer with a copy of the byte values in the characteristic
   */
  public void changedChrt( BluetoothGattCharacteristic chrt )
  {
    if ( LOG ) TDLog.v( TAG + "changed chrt" );
    assert( mChrtReadUuid == CavwayConst.CAVWAY_CHRT_READ_UUID );
    assert( mChrtWriteUuid == CavwayConst.CAVWAY_CHRT_WRITE_UUID );
    UUID uuid = chrt.getUuid();
    String uuid_str = uuid.toString();
    // if ( uuid.compareTo( mChrtReadUuid ) == 0 ) {
    if ( uuid_str.equals( CavwayConst.CAVWAY_CHRT_READ_UUID_STR ) ) {
      if ( LOG ) TDLog.v( TAG + "changed read chrt" );
      // TODO set buffer type according to the read value[]
      mQueue.put( BleQueue.DATA_PRIM, chrt.getValue() );
      // TDLog.v( TAG + "changed read chrt " + byteArray2String( chrt.getValue() ) );
      CavwayData cw = new CavwayData( 0 );
      cw.setData( chrt.getValue() );
      // TDLog.v( TAG + cw.toString() );
      // resetTimer(); // resetTimer was empty
    // } else if ( uuid.compareTo( mChrtWriteUuid ) == 0 ) {
    } else if ( uuid_str.equals( CavwayConst.CAVWAY_CHRT_WRITE_UUID_STR ) ) {
      if ( LOG ) TDLog.v( TAG + "changed write chrt");
      // TDLog.v( TAG + "changed write chrt " + byteArray2String( chrt.getValue() ) );
    } else {
      TDLog.t( TAG + "changed unknown chrt" );
    }
  }

  /** notified that bytes have been read from the read characteristics
   * @param uuid_str  service UUID string
   * @param bytes    array of read bytes 
   */
  public void readedChrt( String uuid_str, byte[] bytes )
  {
    if ( LOG ) TDLog.v( TAG + "readed chrt " + bytes.length );
    mQueue.put( BleQueue.DATA_PRIM, bytes );
    // resetTimer();  // resetTimer was empty
  }

  // /** notified that bytes have been written to the write characteristics
  //  * @param uuid_str  service UUID string
  //  * @param bytes     array of written bytes
  //  */
  // public void writtenChrt( String uuid_str, byte[] bytes )
  // {
  //   // if ( LOG ) TDLog.v( TAG + "written chrt " + bytes.length );
  //   mQps.clearPending();
  // }

  // FIXME BleComm has mQps.clearPending();
  /** notified that bytes have been read
   * @param uuid_str  service UUID string
   * @param uuid_chrt_str characteristics UUID string
   * @param bytes    array of read bytes 
   */
  @Override
  public void readedDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    /* if ( LOG ) */ TDLog.v( TAG + "readed desc - bytes " + bytes.length );
  }

  /** notified that bytes have been written
   * @param uuid_str  descriptor UUID string
   * @param uuid_chrt_str characteristics UUID string
   * @param bytes    array of written bytes 
   *
   * TODO distinguish CCC descriptor (BleUtils.CCCD_UUID) from other descriptors
   */
  public void writtenDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    if ( LOG ) TDLog.v( TAG + "written descr, bytes " + bytes.length );
    if ( uuid_str.equals( BleUtils.CCCD_UUID_STR ) ) { // a notify op - 202301818 using CCCD_UUID_STR
      if ( bytes != null ) {
        if ( bytes[0] != 0 ) { // set notify/indicate
          if ( LOG ) TDLog.v( TAG + "CCCD set notify " + bytes[0] + " chrt " + uuid_chrt_str );
          // here we may save the UUID of notifying characteristics
        } else { 
          if ( LOG ) TDLog.v( TAG + "CCCD clear notify chrt " + uuid_chrt_str );
          // here we may clear the UUID of the notifying characteristics
        }
      } else {
        TDLog.t( TAG + "written null-bytes CCCD chrt " + uuid_chrt_str );
      }
    } else {
      if ( LOG ) TDLog.v( TAG + "written normal desc - bytes " + bytes.length + " UUID " + uuid_str + " chrt " + uuid_chrt_str );
    }
    mQps.clearPending();
  }

  // FIXME BleComm has clearPending
  /** notified that a reliable write was completed
   */
  @Override
  public void completedReliableWrite()
  {
    if ( LOG ) TDLog.v( TAG + "completed reliable write" );
  }

  // /** read a characteristics
  //  * @param srvUuid  service UUID
  //  * @param chrtUuid characteristics UUID
  //  * @return true if successful
  //  * @note this is run by BleOpChrtRead
  //  */
  // public boolean readChrt(UUID srvUuid, UUID chrtUuid )
  // {
  //   if ( LOG ) TDLog.v( TAG + "\"read chrt\" " + chrtUuid.toString() );
  //   return mCallback.readChrt( srvUuid, chrtUuid );
  // }

  // /** write a characteristics
  //  * @param srvUuid  service UUID
  //  * @param chrtUuid characteristics UUID
  //  * @param bytes    array of bytes to write
  //  * @return true if successful
  //  * @note this is run by BleOpChrtWrite
  //  */
  // public boolean writeChrt( UUID srvUuid, UUID chrtUuid, byte[] bytes )
  // {
  //   // if ( LOG ) TDLog.v( TAG + "write chrt " + chrtUuid.toString() );
  //   return mCallback.writeChrt( srvUuid, chrtUuid, bytes );
  // }

  /** react to service discovery
   * @param gatt   bluetooth GATT
   * @note from onServicesDiscovered
   *
   * What actually takes time is the service discovery which is always done when the
   * device has connected (even if you don't call discoverServices). Any command is delayed
   * until that is complete. To speed it up, you should try to shrink the peripheral's GATT db.
   * You could also try to send an MTU request from the peripheral directly after connection
   * to make the service discovery use less packets.
   */
  public int servicesDiscovered( BluetoothGatt gatt )
  {
    if ( LOG ) TDLog.v( TAG + "services discovered");
    assert( mServiceUuid == CavwayConst.CAVWAY_SERVICE_UUID );
    assert( mChrtReadUuid == CavwayConst.CAVWAY_CHRT_READ_UUID );
    if ( USE_MTU ) {
      mQps.enqueueOp( new BleOpRequestMtu( mContext, this, 250 ) ); // exec requestMtu
    } else {
      mQps.enqueueOp( new BleOpNotify( mContext, this, mServiceUuid, mChrtReadUuid, true ) );
    }
    mQps.doNextOp();


    // 20221026 MOVED TO enablePNotify -- 202211XX
    // mBTConnected  = true;
    // notifyStatus( ConnectionState.CONN_CONNECTED );
    // // TODO write a resend-interrupt to the Cavway
    return 0;
  }

  /** enable P-notify
   * @param srvUuid  service UUID
   * @param chrtUuid characteristics UUID
   * @return true if success, false if failure
   */
  @Override
  public boolean enablePNotify( UUID srvUuid, UUID chrtUuid )
  {
    boolean ret = mCallback.enablePNotify( srvUuid, chrtUuid );
    if ( ! ret ) {
      TDLog.t( TAG + "enable PNotify failed ");
      // closeDevice( true );
    } else {
      if ( LOG ) TDLog.v( TAG + "enable PNotify success");
      // 202211XX
      mBTConnected  = true;
      notifyStatus( ConnectionState.CONN_CONNECTED );
      // resetTimer();  // resetTimer was empty
      // TODO write a resend-interrupt to the DistoXBLE
    }
    return ret;
  }

  // /** enable P-indicate
  //  * @param srvUuid  service UUID
  //  * @param chrtUuid characteristics UUID
  //  * @return true if success, false if failure
  //  */
  // public boolean enablePIndicate( UUID srvUuid, UUID chrtUuid )
  // {
  //   // if ( LOG ) TDLog.v( TAG + "enable P indicate");
  //   return mCallback.enablePIndicate( srvUuid, chrtUuid );
  // }

  /** react to an error
   * @param status   GATT error status
   * @param extra    error extra message
   * @param what     error source
   */
  public void error( int status, String extra, String what )
  {
    switch ( status ) {
      case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
        TDLog.t( TAG + "invalid attr length " + extra );
        break;
      case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
        TDLog.t( TAG + "write not permitted " + extra );
        break;
      case BluetoothGatt.GATT_READ_NOT_PERMITTED:
        TDLog.t( TAG + "read not permitted " + extra );
        break;
      // case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION: // 20221026 moved to failure()
      //   TDLog.t( TAG + "insufficient encrypt " + extra );
      //   break;
      // case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
      //   TDLog.t( TAG + "insufficient auth " + extra );
      //   break;
      case BleCallback.CONNECTION_TIMEOUT:
        if ( LOG ) TDLog.v( TAG + "connection timeout reconnect ...");
        reconnectDevice();
        break;
      case BleCallback.CONNECTION_133: // unfortunately this happens
        if ( LOG ) TDLog.v( TAG + "connection " + status + " - disconnect");
        TDUtil.slowDown( 111 ); // wait at least 500 msec
        reconnectDevice( );
        break;
      case BleCallback.CONNECTION_19: // unfortunately this too happens (when device is slow respond?)
        if ( LOG ) TDLog.v( TAG + "connection " + status + " - reconnect");
        // mCallback.clearServiceCache(); // TODO not sure there is a need to clear the service cache
        TDUtil.slowDown( 112 ); // wait at least 500 msec (let xble BT initialize)
        reconnectDevice();
        break;
      default:
        TDLog.t( TAG + "***** ERROR " + status + ": reconnecting ...");
        reconnectDevice();
    }
    mQps.clearPending();
  }

  /** try to recover from an error ... and reconnect
   */
  private void reconnectDevice()
  {
    if ( LOG ) TDLog.v( TAG + "reconnect device - close GATT" );
    // mOps.clear();
    // // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    // clearPending();
    mQps.clear( false ); // after_clear_pending = false

    mCallback.closeGatt();
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    if ( mReconnect ) {
      if ( LOG ) TDLog.v( TAG + "reconnect device - reconnecting ... ");
      notifyStatus( ConnectionState.CONN_WAITING );
      mQps.enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
      mQps.doNextOp();
      mBTConnected = true;
    } else {
      if ( LOG ) TDLog.v( TAG + "reconnect device - disconnected" );
      notifyStatus( ConnectionState.CONN_DISCONNECTED );
    }
  }

  /** react to a failure (unrecoverable error):
   *    clear pending op 
   *    close the connection to the remote device
   *    close the GATT
   * @param status   GATT error status (unused)
   * @param extra    failure extra message (unused)
   * @param what     failure source (unused)
   */
  public void failure( int status, String extra, String what )
  {
    if ( LOG ) TDLog.v( TAG + "Failure (" + status + "): disconnect and close GATT ...");
    // notifyStatus( ConnectionState.CONN_DISCONNECTED ); // this will be called by disconnected
    mQps.clearPending();
    closeDevice( false );
    mCallback.closeGatt();
  }

  /** forward status notification to the application
   * @param status   new status
   * @note the notification is delivered to this class' lister
   */
  @Override
  public void notifyStatus( int status )
  {
    if ( LOG ) TDLog.v( TAG + "notify status " + status + " skip " + mSkipNotify );
    if ( mSkipNotify ) return;
    mApp.notifyListerStatus( mLister, status );
  }

  /** prepare a write op and put it on the queue - call the next op
   * @param srvUuid   service UUID
   * @param chrtUuid  characteristic UUID
   * @param bytes     data array byte
   * @param addHeader whether to add a (6-byte) header "data:#" and 2-byte footer "\r\n"
   * @return ...
   */
  private boolean enlistWrite( UUID srvUuid, UUID chrtUuid, byte[] bytes, boolean addHeader )
  {
    if ( LOG ) TDLog.v( TAG + "enlist write, bytes " + bytes.length + " add address " + addHeader );
    BluetoothGattCharacteristic chrt = null;
    for ( int repeat = 3; repeat > 0; --repeat ) {
      if ( (chrt = mCallback.getWriteChrt( srvUuid, chrtUuid )) != null ) break;
      if ( LOG ) TDLog.v( TAG + "could not get write chrt ... repeat " + repeat );
      TDUtil.slowDown( 101 ); // 300
    }
    if ( chrt == null ) {
      TDLog.e( TAG + "enlist write: null write chrt");
      return false;
    }
    //Chrt.getPermission() always returns 0, I don't know why. Siwei Tian deleted
    // if ( ! BleUtils.isChrtWrite( chrt ) ) {
    //   TDLog.t( TAG + "enlist write: cannot write chrt");
    //   return false;
    // }
    // TDLog.v( TAG + "comm: enlist chrt write " + chrtUuid.toString() );
    if ( addHeader ) {
      byte[] framebytes = new byte[bytes.length + 8];
      framebytes[0] = 'd';
      framebytes[1] = 'a';
      framebytes[2] = 't';
      framebytes[3] = 'a';
      framebytes[4] = ':';
      framebytes[5] = (byte)(bytes.length);
      int i = 0;
      for ( i = 0;i < bytes.length; i++ ) {
        framebytes[i+6] = bytes[i];
      }
      framebytes[i+6] = '\r';
      framebytes[i+7] = '\n';
      mQps.enqueueOp( new BleOpChrtWrite( mContext, this, srvUuid, chrtUuid, framebytes ) );
    } else {
      mQps.enqueueOp( new BleOpChrtWrite( mContext, this, srvUuid, chrtUuid, bytes ) );
    }
    mQps.doNextOp();
    //wait 100ms to let the MCU to receive correct frame, Siwei Tian added
    return true;
  }

  /** get DistoX-BLE hw/fw info, and display that on the Info dialog
   * @param info     Cavway info dialog
   * @return true on full success
   * was read-set read-set read-set
   */
  public boolean getCavwayInfo( CavwayInfoDialog info )
  {
    TDLog.v( TAG + "get Cavway info");
    if ( info == null ) return false;
    // TDLog.v( TAG + "get Cavway info");
    mHasInfo = false;
    if ( ! readMemoryNoWait( CavwayDetails.FIRMWARE_ADDRESS, 4 ) ) return false;
    syncWait(1000, "read fw");
    if ( ! readMemoryNoWait( CavwayDetails.HARDWARE_ADDRESS, 4 ) ) return false;
    syncWait(1000, "read hw");
    if ( ! readMemoryNoWait( CavwayDetails.TIMESTAMP_ADDRESS, 4 ) ) return false;
    syncWait(1000, "read time");
    int cnt = 0;
    while ( mHasInfo == false ) {
      syncWait(1000, "read info");
      if ( ++cnt >= 10 ) break;
    }
    TDLog.v("Info fw " +  ((CavwayProtocol)mProtocol).mFirmVer 
            + " hw " + ((CavwayProtocol)mProtocol).mHardVer
            + " sync " + ((CavwayProtocol)mProtocol).mTimeStamp );
    if ( ! mHasInfo ) return false;
    info.setVal( CavwayProtocol.PACKET_INFO_FIRMWARE, ((CavwayProtocol)mProtocol).mFirmVer);
    info.setVal( CavwayProtocol.PACKET_INFO_HARDWARE, ((CavwayProtocol)mProtocol).mHardVer);
    info.setVal( CavwayProtocol.PACKET_INFO_TIMESTAMP, ((CavwayProtocol)mProtocol).mTimeStamp);
    return true;
  }

  // public Date readDateTime()
  // {
  //   byte[] tmp = readMemory( CavwayDetails.TIMESTAMP_ADDRESS, 4 );
  //   long seconds = tmp[3]; // seconds since the epoch
  //   seconds = ( seconds << 8 ) | tmp[2];
  //   seconds = ( seconds << 8 ) | tmp[1];
  //   seconds = ( seconds << 8 ) | tmp[0];
  //   return new Date( seconds * 1000L );
  // }

  public boolean syncDateTime( String address )
  {
    if ( LOG ) TDLog.v( TAG + " set datetime address " + address );
    if ( ! tryConnectDevice( address, null, 0 ) ) {
      if ( LOG ) TDLog.v(" failed connect ");
      closeDevice( true );
      return false;
    }
    // no need to startConsumerThread(); - already running
    Date date = new Date();
    Calendar cal = new GregorianCalendar();
    int timezoneseconds = ( cal.get( Calendar.ZONE_OFFSET ) + cal.get( Calendar.DST_OFFSET ) )/1000;
    long seconds = date.getTime() / 1000L + timezoneseconds + 1; // round up: add 1 for the millis fraction // FIXME Cavway time is off by 1 hour
    if ( LOG ) TDLog.v( TAG + " set timestamp " + seconds + " timezone " + timezoneseconds );
    byte[] tmp = new byte[4];
    tmp[0] = (byte)( seconds       & 0xff ); // LSB
    tmp[1] = (byte)( (seconds>> 8) & 0xff );
    tmp[2] = (byte)( (seconds>>16) & 0xff );
    tmp[3] = (byte)( (seconds>>24) & 0xff ); // MSB
    boolean ret = writeMemory( CavwayDetails.TIMESTAMP_ADDRESS, tmp, 4 );
    disconnectDevice();
    return ret;
  }

  // --------------------------------------------------------
  /**
   * nothing to read (only write) --> no AsyncTask
   * @param address   remote device address
   * @param what      command to send to the remote device
   * @param to_read   number of data to read (unused)
   * @param lister    callback handler
   * @param data_type packet datatype
   * @param closeBT   whether to close the connection at the end
   * @return true on success
   */
  public boolean setCavwayLaser( String address, int what, int to_read, ListerHandler lister, int data_type, boolean closeBT )
  {
    mSkipNotify = true;
    // FIXME no need to run on a thread
    // Thread laserThread = new Thread() {
    //   @Override public void run() {
        boolean connected = isConnected();
        if ( ! connected ) {
          if ( ! tryConnectDevice( address, lister, 0 ) ) {
            TDLog.t( TAG + "set laser - failed connect device");
            closeDevice( true );
            mSkipNotify = false;
            return false; 
          }
        }
        // mNrReadPackets = 0;
        // mPacketToRead = to_read; // set the number of packet to read
        if ( LOG ) TDLog.v( TAG + "set laser: " + what + " packet " + to_read );
        switch ( what ) {
          case Device.DEVICE_OFF:
            mSkipNotify = false;
            // stopTimer();
            sendCommand( (byte)Device.DEVICE_OFF );
            disconnectDevice( );
            break;
          case Device.LASER_ON:
            sendCommand( (byte)Device.LASER_ON );
            break;
          case Device.LASER_OFF:
            sendCommand( (byte)Device.LASER_OFF );
            break;
          case Device.MEASURE:
            // sendCommand( (byte)Device.MEASURE );
            // break;
          case Device.MEASURE_DOWNLOAD:
            sendCommand( (byte)Device.MEASURE );
            break;
          case Device.CALIB_OFF:
            sendCommand( (byte)Device.CALIB_OFF );
            break;
          case Device.CALIB_ON:
            sendCommand( (byte)Device.CALIB_ON );
            break;
        }
        if ( LOG ) TDLog.v( TAG + "set laser - slow down after send command");
        TDUtil.slowDown(601);
        if ( closeBT ) {
          // synchronized ( mNewDataFlag ) {
          //   try {
          //     if ( LOG ) TDLog.v( TAG + "to read " + to_read + " read " + mNrReadPackets );
          //     while ( 2 * to_read > mNrReadPackets ) mNewDataFlag.wait( 500 ); // 0.5 seconds
          //   } catch ( InterruptedException e ) { 
          //     if ( LOG ) TDLog.v( TAG + "interrupted setXBLELaser");
          //     // e.printStackTrace();
          //   }
          // }
          if ( LOG ) TDLog.v( TAG + "set laser - wait 2 sec before closing device");
          TDUtil.slowDown( 2001 );
          // syncWait(2000, "laser close device");
          // TDLog.v( TAG + "laser close device");
          closeDevice( true );
          mSkipNotify = false;
        }
    //   }
    // };
    // laserThread.start();
    return true;
  }

  // ----------------- SEND COMMAND -------------------------------

  /** send a command to the DistoXBLE
   * @param cmd    command code
   * @return true if the command was scheduled
   * @note used by the protocol to send ack
   */
  @Override
  public boolean sendCommand( int cmd )
  {
    if ( ! isConnected() ) return false;
    if ( cmd == 0 ) return false;
    // TDLog.v( String.format( TAG + "send cmd 0x%02x", cmd ) );
    return enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, new byte[] {(byte)cmd}, true);
  }

  @Override
  public byte[] readMemory( String address, int addr )
  {
    TDLog.t( TAG + "readMemory( String address, int addr ) not implemented");
    return null;
  }

  /** read bytes from memory, asynchronously
   * @param addr memory address
   * @param len  number of bytes to read (between 0 and 124)
   * @return true if successfully send the read command
   * @note used to read FW HW time
   */
  private boolean readMemoryNoWait( int addr, int len )
  {
    // TDLog.v( TAG + "read memory " + addr + " " + len  );
    if ( len < 0 || len > 124 ) return false;
    byte[] cmd = new byte[4];
    cmd[0] = 0x3d;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = (byte)(len);
    mPacketType = CavwayProtocol.PACKET_NONE;
    if ( ! enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, cmd, true ) ) {
      TDLog.e( TAG + "failed enlist memory-write [1]" );
      return false;
    }
    return true;
  }

  /** read bytes from memory, synchronously
   * @param addr memory address
   * @param len  number of bytes to read (between 0 and 124)
   * @return array of read bytes, or null on failure
   */
  public byte[] readMemory( int addr, int len )
  {
    // TDLog.v( TAG + "read memory " + addr + " " + len  );
    if ( len < 0 || len > 124 ) return null;
    byte[] cmd = new byte[4];
    cmd[0] = 0x3d;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = (byte)(len);
    mPacketType = CavwayProtocol.PACKET_NONE;
    if ( ! enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, cmd, true ) ) {
      TDLog.e( TAG + "failed enlist memory-write [1]" );
      return null;
    }
    syncWait( 2000, "read memory" );
    // synchronized ( mNewDataFlag ) {
    //   try {
    //     long start = System.currentTimeMillis();
    //     mNewDataFlag.wait( 2000 );
    //     // here if the thread gets notified
    //     long millis = System.currentTimeMillis() - start;
    //     if ( LOG ) TDLog.v( TAG + "read-memory (len " + len + ") waited " + millis + " msec, packet type " + mPacketType );
    //   } catch ( InterruptedException e ) {
    //     e.printStackTrace();
    //   }
    // }
    if ( (mPacketType & CavwayProtocol.PACKET_REPLY) == CavwayProtocol.PACKET_REPLY ) {
      int length = ((CavwayProtocol) mProtocol).mRepliedData.length;
      if ( length != len ) {
        TDLog.e( TAG + " READ meomry expected length " + len + " got " + length );
        return null;
      }
      byte[] replydata = new byte[len];
      System.arraycopy( ((CavwayProtocol) mProtocol).mRepliedData, 0, replydata, 0, len );
      return replydata;
    }
    return null;
  }

  /** write 4 bytes to memory and wait synchronously for the reply packet 0x39
   * @param addr   memory address
   * @param data   4-byte array
   * @return true if the bytes have been written to memory 
   */
  public boolean writeMemory( int addr, byte[] data )
  {
    if ( LOG ) TDLog.v( TAG + "write memory " + addr + " bytes " + data.length );
    if ( data.length < 4 ) return false;
    byte[] cmd = new byte[7];
    cmd[0] = CavwayData.BYTE_PACKET_REQST; // 0x39;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = data[0];
    cmd[4] = data[1];
    cmd[5] = data[2];
    cmd[6] = data[3];
    mHasWritten = false;
    ((CavwayProtocol) mProtocol).resetRepliedData();
    mPacketType = CavwayProtocol.PACKET_NONE;
    if ( ! enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, cmd, true ) ) {
      if ( LOG ) TDLog.v( TAG + "failed enlist memory-write [2]" );
      return false;
    }
    syncWait( 2000, "write memory" );
    int cnt = 0;
    while ( ! mHasWritten ) {
      syncWait( 1000, "write memory" );
      if ( cnt++ >= 10 ) break;
    }
    if ( ! mHasWritten ) {
      TDLog.e("failed to write 4-bytes to memory");
      // return false;
    } else {
      if ( LOG ) TDLog.v("write 0x39 OK");
    }
    if ( (mPacketType & CavwayProtocol.PACKET_REPLY) == CavwayProtocol.PACKET_REPLY ) {
      byte[] repliedbytes = ((CavwayProtocol) mProtocol).mRepliedData;
      if ( LOG ) for ( int i=0; i<4; ++i) TDLog.v("Byte " + i + " : " + data[i] + " " + repliedbytes[i] );
      return Arrays.equals(data,repliedbytes);
    }
    return false;
  }

  /** write an array of bytes to memory, synchronously 0x3E
   * @param addr   memory address
   * @param data   byte array (length must be at least len)
   * @param len    number of bytes to write (between 0 and 124)
   * @return true if the bytes have been written to memory
   */
  public boolean writeMemory( int addr, byte[] data, int len)
  {
    // TDLog.v( TAG + "write memory " + addr + " bytes " + data.length + " len " + len);
    if ( data.length < len ) return false;
    if ( len < 0 || len > 124 ) return false;
    byte[] cmd = new byte[len+4];
    cmd[0] = 0x3e;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = (byte)len;
    for ( int i = 0; i < len; i++) cmd[i+4] = data[i];
    ((CavwayProtocol) mProtocol).resetRepliedData();
    mHasWritten = false;
    // mPacketType = CavwayProtocol.PACKET_NONE;
    if ( ! enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, cmd, true ) ) {
      // TDLog.v( TAG + "failed enlist memory-write [3]" );
      return false;
    }
    syncWait( 2000, "write memory" );
    int cnt = 0;
    while ( ! mHasWritten ) {
      syncWait( 1000, "write memory" );
      if ( cnt++ >= 3 ) break;
    }
    if ( ! mHasWritten ) { // somehow the protocol does not get a feedback
      TDLog.e("failed to write 0x3e len " + len + " bytes to memory");
      // return false;
    } else {
      if ( LOG ) TDLog.v("write 0x3E OK");
    }
    // if ( (mPacketType & CavwayProtocol.PACKET_REPLY) == CavwayProtocol.PACKET_REPLY )
    if ( len == 4 && CavwayProtocol.PACKET_REPLY == CavwayProtocol.PACKET_REPLY ) {
      byte[] repliedbytes = ((CavwayProtocol) mProtocol).mRepliedData;
      for ( int i = 0; i < 4; i++) TDLog.v( String.format(Locale.US, "%d %02x %02x", i, data[i], repliedbytes[i] ) );;
      return Arrays.equals( data, repliedbytes );
    }
    return true; // false;
  }

  /** 0x38: start reading one data from cavway memory
   * @param addr memory address
   * @param what what type of memory is read: data, coeff
   * @return true if success
   * @note when this is entered mReadingMemory is READ_NONE
   */
  private boolean readOneMemory( int addr, int what )
  {
    // TDLog.v( TAG + "read one memory. index " + addr + " set READING flag" );
    syncSetReadingMemory( what );
    byte[] cmd = new byte[4];
    cmd[0] = 0x3d; // command 0x3d is used for data reading
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = 64;         //one data frame, 64 bytes
    mPacketType = CavwayProtocol.PACKET_NONE;
    return enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, cmd, true );
    // if ( (mPacketType & CavwayProtocol.PACKET_REPLY) == CavwayProtocol.PACKET_REPLY ) {
    //   int length = ((CavwayProtocol) mProtocol).mRepliedData.length;
    //   if ( LOG ) TDLog.v( TAG + " READ meomry expected got length " + length );
    //   return ( (CavwayProtocol) mProtocol).mRepliedData;
    // } else {
    //   TDLog.t( TAG + "read memory: no reply");
    // }
  }

  /** handle the reading of one memory data
   * @param res_buf  buffer result of the reading
   * @return true if success
   */
  boolean handleOneMemory( byte[] res_buf )
  {
    boolean ret = false;
    if ( res_buf == null || res_buf.length != BYTE_PER_DATA ) {
      TDLog.e( TAG + "handle memory: FAILURE - index " + mMemoryIndex );
      syncClearReadingMemory();
    } else {
      // TDLog.v( TAG + "handle memory: result size " + res_buf.length );
      if ( mReadingMemory == READ_MEMORY ) {
        // TDLog.v( TAG + "handle memory - index " + mMemoryIndex );
        final CavwayData result = new CavwayData( mMemoryIndex );
        result.setData( res_buf );
        if ( mMemory != null ) {
          // TDLog.v( TAG + "  add to MEMORY" );
          mMemory.add( result );
        }
        if ( mMemoryDialog != null ) {
          final int k1 = mMemoryIndex + 1;
          (new Handler( Looper.getMainLooper() )).post( new Runnable() {
            public void run() {
              // TDLog.v( TAG + "handle memory run " + k1 );
              mMemoryDialog.setIndex( k1 );
              mMemoryDialog.appendToList( result );
              // TDLog.v( TAG + "handle memory: clear FLAG");
              syncClearReadingMemory();
            }
          } );
        } else {
          // TDLog.v( TAG + "handle memory: null MEMORY dialog - clear FLAG");
          syncClearReadingMemory();
        }
      // } else if (mReadingMemory == READ_COEFFS ) { // calib coeffs
      //   // TDLog.v( TAG + "handle memory coeff: ");
      //   final CavwayData result = new CavwayData( -1 );
      //   result.setData( res_buf );
      //   if ( mMemory != null ) {
      //     mMemory.add( result );
      //   }
      //   syncClearReadingMemory();
      }
      ret = true;
    }
    return ret;
  }

  /** set reading-flag to false and notify waiting threads
   */
  void syncClearReadingMemory()
  {
    synchronized (mNewDataFlag) {
      // TDLog.v( TAG + "clear READING flag");
      mReadingMemory = READ_NONE;
      mNewDataFlag.notifyAll();
    }
  }

  /** set reading-flag to true
   * @param what what memory is read
   */
  void syncSetReadingMemory( int what )
  {
    // if ( what == READ_NONE ) return;
    synchronized (mNewDataFlag) {
      // TDLog.v( TAG + "set READING flag");
      mReadingMemory = what;
      // mNewDataFlag.notifyAll(); // nobody waits on flag == false
    }
  }

  /** wait while reading-flag is true
   */
  void syncWaitOnReadingMemory( )
  {
    synchronized (mNewDataFlag) {
      while ( mReadingMemory > READ_NONE ) {
        syncWait( 1000, "waiting to read mmory" );
      }
    }
  }

  // memory reading struct and pointers
  private ArrayList< CavwayData > mMemory = null;
  private CavwayMemoryDialog mMemoryDialog = null;
  private int mMemoryIndex = 0;

  /** read the Cavway memory
   * @param address   device address
   * @param number    number of data to read
   * @param data      array of octets to be filled by the memory-read
   * @return number of octets that have been read (-1 on error)
   */
  public int readCavwayMemory( String address, int number, ArrayList< CavwayData > data, CavwayMemoryDialog dialog )
  { 
    TDLog.t( TAG + "read Cavway memory ... " + number);
    if ( ! tryConnectDevice( address, null, 0 ) ) return -1;
    TDLog.t( TAG + "  set MEMORY and dialog");
    mMemory       = data;
    mMemoryDialog = dialog;
    int cnt = 0; // number of memory location that have been read
    for ( int idx = 0; idx < number; ++idx ) {
      // TDLog.v( TAG + "set memory index " + idx );
      mMemoryIndex = idx;
      if ( ! readOneMemory( idx, READ_MEMORY ) ) { // write failure
        break;
      }
      syncWaitOnReadingMemory();
      ++ cnt;
    }
    // syncWaitOnReadingMemory();
    disconnectDevice();
    TDLog.t( TAG + "  clear MEMORY and dialog");
    mMemoryDialog = null;
    mMemory = null;
    return cnt;
  }

  /** toggle calibration mode
   * @param address  device address
   * @param type     ...
   * @return true on success
   */
  @Override
  public boolean toggleCalibMode( String address, int type )
  {
    // TDLog.v( TAG + "toggle calib");
    boolean ret = false;
    if ( ! tryConnectDevice( address, null, 0 ) ) return false;
    ret = setCalibMode( 2 );  //convert cali mode
    if ( LOG ) TDLog.v( TAG + "toggle calib - wait 700 msec before closing device");
    TDUtil.slowDown( 701 );
    closeDevice( false );
    return ret;
  }


  /** send the set/unset calib-mode command
   *
   * @param mode   0: quit 1: enter 2: convert
   * @return true if success
   * @note commands: 0x31 calib-ON 0x30 calib-OFF
   */
  private boolean setCalibMode( int mode )
  {
    switch (mode)
    {
      case 0:
        return sendCommand( Device.CALIB_OFF );
      case 1:
        return sendCommand( Device.CALIB_ON );
      case 2:
        return sendCommand( Device.CALIB_CONVERT );
      default:
        return false;
    }
  }

  /** batch data download
   * @param address    device address
   * @param lister     data lister
   * @param data_type  expected type of data (not really used)
   * @param timeout    (unused)
   * @return number of downloaded data (neg on error)
   *   -1 failed connect
   */
  @Override
  public int downloadData( String address, ListerHandler lister, int data_type, int timeout ) // FIXME_LISTER
  {
    if ( LOG ) TDLog.v( TAG + "cavway batch download, address " + address + " data_type " + data_type );
    // mConnectionMode = 0;
    mDataType = data_type;
    if ( ! tryConnectDevice( address, lister, 0 ) ) return -1; 
    mNrReadPackets = 0;

    //sendCommand( 0x40 );     // start send measure packet ???
    TDUtil.yieldDown( 500 );
    // ??? start a thread that keeps track of read packets - when read done stop it and return
    mPacketType = CavwayProtocol.PACKET_NONE;
    synchronized ( mNewDataFlag ) {
      while( true ) {
        if ( syncWait( 2000, "data download" ) ) {
          if ( mPacketType == CavwayProtocol.PACKET_MEASURE_DATA || mPacketType == CavwayProtocol.PACKET_CALIB_DATA ) {
            mPacketType = CavwayProtocol.PACKET_NONE; // reset
            if ( LOG ) TDLog.v( TAG + "got packet type " + mPacketType + " nr packets " + mNrReadPackets );
          } else {
            if ( LOG ) TDLog.v( TAG + "no packet, packet type " + mPacketType );
            break;
          }
        }
        // long start = System.currentTimeMillis();
        // try {
        //   mNewDataFlag.wait(2000); // was 5000
        //   if ( mPacketType == DistoXBLEProtocol.PACKET_MEASURE_DATA ) {
        //     mPacketType = DistoXBLEProtocol.PACKET_NONE; // reset
        //     ret++; // increment counter
        //     if ( LOG ) TDLog.v( TAG + "got packet " + ret );
        //   } else {
        //     if ( LOG ) TDLog.v( TAG + "no packet " );
        //     break;
        //   }
        // } catch (InterruptedException e) {
        //   if ( LOG ) TDLog.v( TAG + "interrupted");
        //   // e.printStackTrace();
        // }
        // long millis = System.currentTimeMillis() - start;
        // TDLog.v( TAG + "download one data took " + millis + " msec" );
      }
    }
    disconnectDevice();
    return mNrReadPackets; // each data has two packets: DATA and VECTOR
  }

  /** try to connect to the Cavway device
   * @param address   device address
   * @param lister    data lister
   * @param data_type expected type of data
   * @return ...
   */
  public boolean tryConnectDevice( String address, ListerHandler lister, int data_type )
  {
    if ( LOG ) TDLog.v( TAG + "try connect " + address + " conneted: " + mBTConnected );
    if ( ! mBTConnected ) {
      int timeout = 10;
      if ( ! connectDevice( address, lister, data_type, timeout ) ) {
        return false;
      }
    }
    int loop_cnt = 50; // 20230118 local var "loop_cnt"
    while( ! mBTConnected ) {
      if ( LOG ) TDLog.v( TAG + "try connect - not connected ... " + loop_cnt );
      TDUtil.slowDown( 105 );
      if ( loop_cnt-- == 0 ) {
        disconnectGatt();
        return false;
      }
    }
    return true;
  }

  /** read the calibration coeff from the device
   * @param address   device address
   * @param coeff     array of CavwayDetails.COEFF_SIZE calibration coeffs (filled by the read)
   * @return true if success
   * 
   * Cavway has 48 2-byte coeffiients stored as
   * BG1 BM1 AG1x AG1y AG1z AM1x AM1y AM1z
   * BG2 BM2 AG2x AG2y AG2z AM2x AM2y AM2z
   */
  @Override
  public boolean readCoeff( String address, byte[] coeff ) // 20250123 dropped second
  {
    // TDLog.v( TAG + "read coeff " + address );
    if ( coeff == null ) return false;
    int  len  = coeff.length;
    if ( len > CavwayDetails.COEFF_SIZE ) len = CavwayDetails.COEFF_SIZE; // FIXME force max length of calib coeffs
    if ( ! tryConnectDevice( address, null, 0 ) ) return false;
    boolean ret = true;
    for ( int k = 0; k < 2; ++ k ) {
      int addr = CavwayDetails.COEFF_ADDRESS + k * 64;
      byte[] coeff_tmp = readMemory( addr, 64 /* CavwayDetails.COEFF_SIZE */ ); // FIXME it can fail 
      if ( coeff_tmp != null && coeff_tmp.length >= 52 ) {
        // TDLog.v( TAG + "memory size " + mMemory.size() + " copying coeff-set " + k );
        System.arraycopy( coeff_tmp, 0, coeff, k*52, 52 /* CavwayDetails.COEFF_LEN */ );
      } else {
        if ( coeff_tmp == null ) {
          TDLog.e("null coeff " + k );
        } else {
          TDLog.e("coeff " + k + " length " +  coeff_tmp.length + " too short" );
        }
        ret = false;
        break;
      }
    }
    disconnectDevice();
    // mMemory = null;
    return ret;
  }

  /** write the calibration coeff to the device
   * @param address   device address
   * @param coeff     array of CavwayDetails.COEFF_SIZE calibration coeffs
   * @return true if success
   */
  @Override
  public boolean writeCoeff( String address, byte[] coeff ) // 20250123 dropped second
  {
    if ( coeff == null ) {
      // TDLog.v( TAG + "write coeff: null coeff" );
      return false;
    }
    int  len  = coeff.length;
    // TDLog.v( TAG + "write coeff: length " + len );
    if ( len != 104 ) return false;
    if( ! tryConnectDevice( address, null, 0 )) {
      if ( LOG ) TDLog.v( TAG + "write coeff: failed connect address " + address );
      return false;
    }
    boolean ret = true;
    for ( int k = 0; k < 2; ++k ) {
      byte[] buf = new byte[64];
      for ( int j=52; j<64; ++j ) buf[j] = 0;
      System.arraycopy( coeff, 0+k*52, buf, 0, 52 );
      if ( ! writeMemory( CavwayDetails.COEFF_ADDRESS + k * 64, buf, 64 /* CavwayDetails.COEFF_SIZE */ ) ) {
        TDLog.e( TAG + "fail write coeff set " + k );
        ret = false;
        break;
      }
      // TDLog.v( TAG + "OK write coeff set " + k );
    }
    disconnectDevice();
    return ret;
  }

  // /**
  //  * calculate CRC-16 (polynomial 1 + x^2 + x^15 + (x^16))
  //  *
  //  * @param bytes  array of bytes containing the data
  //  * @param length length of the data (in the array, starting at index 0)
  //  * @return crc of the byte array
  //  */
  // private int calCRC16( byte[] bytes, int length )
  // {
  //   int CRC = 0x0000ffff;
  //   int POLYNOMIAL = 0x0000a001;
  //   for ( int i = 0; i < length; i++) {
  //     CRC ^= ((int) bytes[i] & 0x000000ff);
  //     for ( int j = 0; j < 8; j++) {
  //       if ((CRC & 0x00000001) != 0) {
  //         CRC >>= 1;
  //         CRC ^= POLYNOMIAL;
  //       } else {
  //         CRC >>= 1;
  //       }
  //     }
  //   }
  //   return CRC;
  // }

  // /** upload a firmware to the device
  //  * @param address   device address
  //  * @param file      firmware file
  //  * @param progress  progress dialog
  //  */
  // public void uploadFirmware( String address, File file, TDProgress progress )
  // {
  //   final boolean DRY_RUN = false; // DEBUG
  //   if ( LOG ) TDLog.v( TAG + "upload firmware " + file.getPath() );
  //   // boolean is_log_file = TDLog.isStreamFile();
  //   // if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_FILE ); // set log to file if necessary
  //   // int ret = 0;
  //   long len        = file.length();
  //   String filename = file.getName();
  //   Resources res   = mContext.getResources();
  //   Handler handler = new Handler();
  //   new Thread( new Runnable() {
  //     public void run() {
  //       boolean ok = true;
  //       int cnt = 0;
  //       String msg;
  //       byte[] buf = new byte[259];
  //       buf[0] = CavwayData.BYTE_PACKET_FW_WRITE; // (byte)0x3b;
  //       buf[1] = (byte)0;
  //       buf[2] = (byte)0;
  //       try {
  //         // File fp = new File( filepath );
  //         if ( ! tryConnectDevice( address, null, 0 ) ) {
  //           TDLog.t( TAG + "fw upload - failed connect");
  //           ok = false; // return 0;
  //         }
  //         FileInputStream fis = new FileInputStream(file);
  //         DataInputStream dis = new DataInputStream(fis);
  //         try{
  //           for ( int addr = 0; ok /* && addr < end_addr */ ; /*++addr*/) {
  //             for (int k = 0; k < 256; ++k) buf[k] = (byte) 0xff;
  //             int nr = dis.read( buf, 0, 256 );
  //             int crc16 = calCRC16( buf, 256 );
  //             //for (int k = 0; k < 256; ++k) buf[k] = (byte) 0xff;  //for simulating the bug
  //             if (nr <= 0) { // EOF ?
  //               if ( LOG ) TDLog.v( TAG + "fw upload: file read " + nr + " - break");
  //               break;
  //             }
  //             if ( LOG ) TDLog.v( TAG + "fw upload: addr " + addr + " count " + cnt + " crc16 " + crc16 );
  //             //if(addr < 8) continue;
  //             int flashaddr = addr + 8;
  //             cnt += nr;
  //             addr++;
  //             int repeat = 3; // THIS IS A repeat TEST
  //             for ( ; repeat > 0; -- repeat ) { // repeat-for: exit with repeat == 0 (error) or -1 (success)
  //               byte[] separated_buf1 = new byte[131]; // 131 = 3 (cmd, addr, index) + 128 (payload) // 20230118 corrected "separated"
  //               separated_buf1[0] = CavwayData.BYTE_PACKET_FW_WRITE; // (byte) 0x3b;
  //               separated_buf1[1] = (byte) (flashaddr & 0xff);
  //               separated_buf1[2] = 0; //packet index
  //               System.arraycopy(buf, 0, separated_buf1, 3, 128);
  //               if ( DRY_RUN ) {
  //                 TDUtil.slowDown( 103 );
  //               } else {
  //                 enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, separated_buf1, true);
  //               }
  //               byte[] separated_buf2 = new byte[133]; // 20230118 corrected "separated"
  //               separated_buf2[0] = CavwayData.BYTE_PACKET_FW_WRITE; // (byte) 0x3b;
  //               separated_buf2[1] = (byte) (flashaddr & 0xff);
  //               separated_buf2[2] = 1;
  //               System.arraycopy(buf, 128, separated_buf2, 3, 128);
  //               separated_buf2[131] = (byte) (crc16 & 0x00FF);
  //               separated_buf2[132] = (byte) ((crc16 >> 8) & 0x00FF);
  //               if ( DRY_RUN ) {
  //                 TDUtil.slowDown( 104 );
  //                 mPacketType = CavwayProtocol.PACKET_FLASH_CHECKSUM;
  //                 ((CavwayProtocol) mProtocol).mCheckCRC = crc16;
  //               } else {
  //                 enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, separated_buf2, true);
  //                 //TDUtil.yieldDown(1000);
  //                 mPacketType = CavwayProtocol.PACKET_NONE;
  //                 syncWait( 5000, "write firmware block" );
  //                 // synchronized ( mNewDataFlag ) {
  //                 //   try {
  //                 //     long start = System.currentTimeMillis();
  //                 //     mNewDataFlag.wait(5000); // allow long wait for firmware
  //                 //     long millis = System.currentTimeMillis() - start;
  //                 //     if ( LOG ) TDLog.v( TAG + "write firmware block waited " + millis + " msec" );
  //                 //   } catch (InterruptedException e) {
  //                 //     e.printStackTrace();
  //                 //   }
  //                 // }
  //               }
  //               if ( mPacketType == CavwayProtocol.PACKET_FLASH_CHECKSUM ) {
  //                 //int checksum = 0;
  //                 //for (int i = 0; i < 256; i++) checksum += (buf[i] & 0xff);
  //                 int ret_crc16 = ((CavwayProtocol) mProtocol).mCheckCRC;
  //                 int ret_code  = ((CavwayProtocol) mProtocol).mFwOpReturnCode;
  //                 if ( ret_crc16 != crc16 || ret_code != 0 ) {
  //                   if ( LOG ) TDLog.v( TAG + "fw upload: fail at " + cnt + " buf[0]: " + buf[0] + " reply addr " + addr + " code " + ret_code + " CRC " + ret_crc16 );
  //                   // ok = false; // without repeat-for uncomment these
  //                   // break;
  //                 } else {
  //                   String msg1 = String.format( mContext.getResources().getString( R.string.firmware_uploaded ), "Cavway", cnt );
  //                   int cnt1 = cnt;
  //                   if ( LOG ) TDLog.v( TAG + msg1 );
  //                   if ( progress != null ) {
  //                     handler.post( new Runnable() {
  //                       public void run() {
  //                         progress.setProgress( cnt1 );
  //                         progress.setText( msg1 );
  //                       }
  //                     } );
  //                   }
  //                   repeat = 0; // then the for-loop breaks with repeat = -1 (ie. success)
  //                 }
  //               } else {
  //                 TDLog.t( TAG + "fw upload: fail at " + cnt + " repeat " + repeat + " packet " + mPacketType );
  //                 // ok = false; // without repeat-for uncomment these two lines
  //                 // break;
  //               }
  //             }
  //             if ( repeat == 0 ) {
  //               TDLog.t( TAG + "fw upload: fail after 3 repeats at " + cnt );
  //               ok = false;
  //               break;
  //             }
  //           }
  //           fis.close();
  //         } catch ( EOFException e ) { // OK
  //           if ( LOG ) TDLog.v( TAG + "fw update: EOF " + e.getMessage());
  //         } catch ( FileNotFoundException e ) {
  //           TDLog.t( TAG + "fw update: Not Found error " + e.getMessage() );
  //           ok = false;
  //         }
  //       } catch ( IOException e ) {
  //         TDLog.t( TAG + "fw update: IO error " + e.getMessage() );
  //         ok = false;
  //       }
  //       closeDevice( false );     //close ble here
  //       msg = TAG + "Firmware update: result is " + (ok? "OK" : "FAIL") + " count " + cnt;
  //       if ( LOG ) TDLog.v( TAG + msg );
  //       int ret = ( ok ? cnt : -cnt );
  //       if ( LOG ) TDLog.v( TAG + "Dialog Firmware upload result: written " + ret + " bytes of " + len );
  //       boolean ok2 = ok;
  //       String msg2 = ( ret > 0 )? String.format( res.getString(R.string.firmware_file_uploaded), filename, ret, len )
  //                                : res.getString(R.string.firmware_file_upload_fail);
  //       if ( progress != null ) {
  //         handler.post( new Runnable() {
  //           public void run() {
  //             progress.setDone( ok2, msg2  );
  //           }
  //         } );
  //       } else { // run on UI thread
  //         handler.post( new Runnable() { 
  //           public void run () { TDToast.makeLong( msg2 ); }
  //         } );
  //       }
  //     }
  //   } ).start();
  //   // if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
  // }

  // /** read a 256-byte firmware block
  //  * @param addr   block address
  //  * @return 256-byte array, block of firmware
  //  */
  // private byte[] readFirmwareBlock(int addr)
  // {
  //   if ( LOG ) TDLog.v( TAG + "fw read block at addr " + addr );
  //   try {
  //     for (int repeat = 3; repeat > 0; --repeat ) {
  //       byte[] req_buf = new byte[3]; // request buffer
  //       req_buf[0] = CavwayData.BYTE_PACKET_FW_READ; // (byte)0x3a;
  //       req_buf[1] = (byte)( addr & 0xff );
  //       req_buf[2] = 0; // not necessary
  //       enlistWrite(CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, req_buf, true);
  //       mPacketType = CavwayProtocol.PACKET_NONE;
  //       syncWait( 5000, "read firmware block" );
  //       // synchronized (mNewDataFlag) {
  //       //   try {
  //       //     long start = System.currentTimeMillis();
  //       //     mNewDataFlag.wait(5000); // allow long wait for firmware
  //       //     long millis = System.currentTimeMillis() - start;
  //       //     if ( LOG ) TDLog.v( TAG + "read firmware block waited " + millis + " msec" );
  //       //   } catch (InterruptedException e) {
  //       //     e.printStackTrace();
  //       //   }
  //       // }
  //       if ( mPacketType == CavwayProtocol.PACKET_FLASH_BYTES_2 ) {
  //         byte[] ret_buf = ((CavwayProtocol) mProtocol).mFlashBytes; // return buffer
  //         int crc16 = calCRC16( ret_buf, 256 );
  //         if ( crc16 == ((CavwayProtocol) mProtocol).mCheckCRC ) {
  //           TDLog.t( TAG + "read fw (" + repeat +") OK");
  //           return ret_buf; // success
  //         }
  //         TDLog.t( TAG + "read fw (" + repeat +") CRC-16 mismatch: got " + crc16 + " expected " + ((CavwayProtocol) mProtocol).mCheckCRC );
  //       } else {
  //         TDLog.t( TAG + "read fw (" + repeat +") bad packet type " + mPacketType );
  //       }
  //     }
  //     TDLog.t( TAG + "read fw: repeatedly failed packet addr " + addr );
  //   } catch (Exception e) {
  //     TDLog.t( TAG + "error " + e.getMessage() );
  //   }
  //   return null;
  // }

  // /** read the firmware from the device and save it to file
  //  * @param address   device address (passed to tryConnect)
  //  * @param file      output file
  //  */
  // public void dumpFirmware( String address, File file, TDProgress progress )
  // {
  //   if ( LOG ) TDLog.v( TAG + "fw dump: output filepath " + file.getPath() );
  //   Resources res   = mContext.getResources();
  //   Handler handler = new Handler();
  //   new Thread( new Runnable() {
  //     public void run() {
  //       String filename = file.getName();
  //       byte[] buf = new byte[256];
  //       boolean ok = true;
  //       int cnt = 0;
  //       try {
  //         // TDPath.checkPath( filepath );
  //         // File fp = new File( filepath );
  //         FileOutputStream fos = new FileOutputStream(file);
  //         DataOutputStream dos = new DataOutputStream(fos);
  //         if ( tryConnectDevice( address, null, 0 ) ) {
  //           try {
  //             for ( int addr = 8; ; addr++ ) {
  //               buf = readFirmwareBlock(addr);
  //               if ( buf == null || buf.length < 256 ) {
  //                 TDLog.t( TAG + "fw read - failed at addr " + addr + " cnt " + cnt );
  //                 ok = false;
  //                 break;
  //               }
  //               dos.write( buf, 0, 256 );
  //               cnt += 256;
  //               int k = 0; // check if the block is fully 0xFF
  //               for ( ; k<256; ++k ) {
  //                 if ( buf[k] != (byte)0xff ) break;
  //               }
  //               if ( k == 256 ) break;
  //               String msg1 = String.format( mContext.getResources().getString( R.string.firmware_downloaded ), "Cavway", cnt );
  //               int cnt1 = cnt;
  //               if ( LOG ) TDLog.v( TAG + msg1 );
  //               if ( progress != null ) {
  //                 handler.post( new Runnable() {
  //                   public void run() {
  //                     progress.setProgress( cnt1 );
  //                     progress.setText( msg1 );
  //                   }
  //                 } );
  //               }
  //             }
  //             fos.close();
  //           } catch (EOFException e) {
  //             //OK
  //           } catch (IOException e) {
  //             ok = false;
  //           } finally {
  //             closeDevice( false );
  //           }
  //         } else {
  //           ok = false;
  //         }
  //       } catch ( FileNotFoundException e ) {
  //         ok = false;
  //       }
  //       String msg = "Cavway Firmware dump: result is " + (ok? "OK" : "FAIL") + " count " + cnt;
  //       if ( LOG ) TDLog.v( TAG + msg );
  //       int ret = ( ok ? cnt : -cnt );
  //       boolean ok2 = ok;
  //       String msg2 = ( ret > 0 )? String.format( res.getString(R.string.firmware_file_downloaded), filename, ret )
  //                                : res.getString(R.string.firmware_file_download_fail);
  //       if ( progress != null ) {
  //         handler.post( new Runnable() {
  //           public void run() {
  //             progress.setDone( ok2, msg2  );
  //           }
  //         } );
  //       } else { // run on UI thread
  //         handler.post( new Runnable() { 
  //           public void run () { TDToast.makeLong( msg2 ); }
  //         } );
  //       }
  //     }
  //   } ).start();
  //   // if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
  // }

  // /** 0x3c: read the hardware code
  //  * @param address device address
  //  * @param hw      (unused)
  //  * @return 2-byte hw code
  //  */
  // public byte[] readFirmwareSignature( String address, int hw ) 
  // {
  //   if ( ! tryConnectDevice( address, null, 0 ) ) return null;
  //   byte[] buf = new byte[1];
  //   buf[0] = (byte)0x3c;
  //   enlistWrite( CavwayConst.CAVWAY_SERVICE_UUID, CavwayConst.CAVWAY_CHRT_WRITE_UUID, buf, true);
  //   mPacketType = CavwayProtocol.PACKET_NONE;
  //   syncWait( 5000, "read firmware signature" );
  //   // synchronized (mNewDataFlag) {
  //   //   try {
  //   //     long start = System.currentTimeMillis();
  //   //     mNewDataFlag.wait(5000); // allow long wait for firmware
  //   //     long millis = System.currentTimeMillis() - start;
  //   //     if ( LOG ) TDLog.v( TAG + "read firmware signature waited " + millis + " msec" );
  //   //   } catch (InterruptedException e) {
  //   //     e.printStackTrace();
  //   //   }
  //   // }
  //   boolean bisSuccess = false;
  //   if ( mPacketType == CavwayProtocol.PACKET_SIGNATURE ) {
  //     buf = ((CavwayProtocol) mProtocol).mRepliedData;
  //     bisSuccess = true;
  //   }
  //   closeDevice( false );
  //   return (bisSuccess)?  buf : null;
  // }

  // /** synchronized wait
  //  * @param msec  wait timeout [msec]
  //  * @param msg   log message
  //  * @return true if ok, false if interrupted
  //  */
  // private boolean syncWait( long msec, String msg )
  // {
  //   // TDLog.v( TAG + "sync wait " + msec );
  //   synchronized ( mNewDataFlag ) {
  //     try {
  //       long start = System.currentTimeMillis();
  //       mNewDataFlag.wait( msec );
  //       long millis = System.currentTimeMillis() - start;
  //       if ( LOG ) TDLog.v( TAG + "" + msg + " msec " + millis );
  //       return true;
  //     } catch ( InterruptedException e ) {
  //       if ( LOG ) TDLog.v( TAG + "interrupted wait " + msg );
  //       // e.printStackTrace();
  //       return false;
  //     }
  //   }
  // }


  // /** request a new MTU
  //  * @param mtu   new value
  //  * @return true if success
  //  */
  // public boolean requestMtu( int mtu )
  // {
  //   return mCallback.requestMtu( mtu );
  // }

  /** log
   * @return bond state,or -1 if no permission
   */
  private int getBondState( BluetoothDevice bt_dev )
  {
    if ( ! TDandroid.checkBluetooth( mContext ) ) {
      return -1;
    }
    return bt_dev.getBondState();
  }

  // /** debug 
  //  */
  // private String byteArray2String( byte[] b )
  // {
  //   StringBuilder sb = new StringBuilder();
  //   for (int k=0; k < b.length; ++k ) sb.append( String.format("%02x ", b[k] ) );
  //   return sb.toString();
  // }
}


