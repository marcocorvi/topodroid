/* @file BricComm.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BRIC4 communication 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.dev.Device;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.R;
import com.topodroid.utils.TDLog;

import android.os.Looper;
import android.os.Handler;
import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCallback;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BricComm extends TopoDroidComm
                      implements BleComm
{
  private ConcurrentLinkedQueue< BleOperation > mOps;
  final static int DATA_PRIM = 1;
  final static int DATA_META = 2;
  final static int DATA_ERR  = 3;
  final static int DATA_TIME = 4;

  private Context mContext;
  BleCallback mCallback;
  private String          mRemoteAddress;
  private BluetoothDevice mRemoteBtDevice;
  private int mDataType;   // packet datatype 
  private BricQueue mQueue;
  private boolean mReconnect = false;

  public BricComm( Context ctx, TopoDroidApp app, String address, BluetoothDevice bt_device ) 
  {
    super( app );
    mContext  = ctx;
    mRemoteAddress = address;
    mRemoteBtDevice = bt_device;
    mQueue = new BricQueue();
    Thread consumer = new Thread(){
      public void run()
      {
        for ( ; ; ) {
          // Log.v("DistoX-BLE-B", "Queue size " + mQueue.size );
          BricBuffer buffer = mQueue.get();
          // Log.v("DistoX-BLE-B", "Queue buffer type " + buffer.type );
          switch ( buffer.type ) {
            case DATA_PRIM:
              BricDebug.logMeasPrim( buffer.data );
              ((BricProto)mProtocol).addMeasPrim( buffer.data );
              // ((BricProto)mProtocol).addMeasPrimAndProcess( buffer.data );
              break;
            case DATA_META:
              BricDebug.logMeasMeta( buffer.data );
              ((BricProto)mProtocol).addMeasMeta( buffer.data );
              break;
            case DATA_ERR:
              BricDebug.logMeasErr( buffer.data );
              ((BricProto)mProtocol).addMeasErr( buffer.data );
              ((BricProto)mProtocol).processData(); 
              break;
            default:
          }
        }
      } 
    };
    consumer.start();
  }

  public boolean readChrt( UUID srvUuid, UUID chrtUuid ) { return mCallback.readChrt( srvUuid, chrtUuid ); }

  public boolean writeChrt( UUID srvUuid, UUID chrtUuid, byte[] bytes ) { return mCallback.writeChrt( srvUuid, chrtUuid, bytes ); }

  // public boolean enablePNotify( UUID srvUuid, BluetoothGattCharacteristic chrt ) { return mCallback.enablePNotify( srvUuid, chrt ); }
  public boolean enablePNotify( UUID srvUuid, UUID chrtUuid ) { return mCallback.enablePNotify( srvUuid, chrtUuid ); }
  
  // ---------------------------------------------------------------------------
  // send data to the application

  // --------------------------------------------------------------
  /*
  private void addService( BluetoothGattService srv ) 
  { 
    String srv_uuid = BleUtils.uuidToShortString( srv.getUuid() );
    // Log.v("DistoX-BLE-B", "Bric comm add S: " + srv_uuid );
  }
  */

  /*
  // register characteristics for notification
  // doNextOp() is done by serviceDiscovered when it completes
  private void addChrt( UUID srvUuid, BluetoothGattCharacteristic chrt ) 
  {
    int ret;
    UUID chrtUuid = chrt.getUuid();
    String chrt_uuid = BleUtils.uuidToShortString( chrtUuid );
    // Log.v("DistoX-BLE-B", "Bric comm ***** add chrt " + chrtUuid );
    if ( chrt_uuid.equals( BricConst.MEAS_PRIM ) ) {
      ret = enqueueOp( new BleOpNotify( mContext, this, srvUuid, chrt, true ) );
    } else if ( chrt_uuid.equals( BricConst.MEAS_META ) ) {
      // ret = enqueueOp( new BleOpNotify( mContext, this, srvUuid, chrt, true ) );
    } else if ( chrt_uuid.equals( BricConst.MEAS_ERR ) ) {
      // ret = enqueueOp( new BleOpNotify( mContext, this, srvUuid, chrt, true ) );
    } else if ( chrt_uuid.equals( BricConst.LAST_TIME ) ) { // LAST_TIME is not notified
      ret = enqueueOp( new BleOpNotify( mContext, this, srvUuid, chrt, true ) );
    } else {
      // Log.v("DistoX-BLE-B", "Bric comm add: unknown chrt " + chrt_uuid );
    }
  }
  */

  /*
  private void addDesc( UUID srv_uuid, UUID chrt_uuid, BluetoothGattDescriptor desc ) 
  {
    String desc_uuid = BleUtils.uuidToShortString( desc.getUuid() );
    // Log.v("DistoX-BLE-B", "Bric comm add     +D: " + desc_uuid );
  }
  */

  // ---------------------------------------------------------------------------
  // callback action completions - these methods must clear the pending action by calling
  // clearPending() which starts a new action if there is one waiting

  // from onServicesDiscovered
  public void servicesDiscovered( BluetoothGatt gatt )
  {
    // Log.v("DistoX-BLE-B", "BRIC comm service discovered");
    /*
    // (new Handler( Looper.getMainLooper() )).post( new Runnable() {
    //   public void run() {
        List< BluetoothGattService > services = gatt.getServices();
        for ( BluetoothGattService service : services ) {
          // addService() does not do anything
          // addService( service );
          UUID srv_uuid = service.getUuid();
          // Log.v("DistoX-BLE-B", "BRIC comm Srv  " + BleUtils.uuidToString( srv_uuid ) );
          List< BluetoothGattCharacteristic> chrts = service.getCharacteristics();
          for ( BluetoothGattCharacteristic chrt : chrts ) {
            addChrt( srv_uuid, chrt );

            // addDesc() does not do anything
            // UUID chrt_uuid = chrt.getUuid();
            // // Log.v("DistoX-BLE-B", "BRIC comm Chrt " + BleUtils.uuidToString( chrt_uuid ) + BleUtils.chrtPermString(chrt) + BleUtils.chrtPropString(chrt) );
            // List< BluetoothGattDescriptor> descs = chrt.getDescriptors();
            // for ( BluetoothGattDescriptor desc : descs ) {
            //   addDesc( srv_uuid, chrt_uuid, desc );
            //   // Log.v("DistoX-BLE-B", "BRIC comm Desc " + BleUtils.uuidToString( desc.getUuid() ) + BleUtils.descPermString( desc ) );
            // }
          }
        }
    //   }
    // } );
    */

    enqueueOp( new BleOpNotify( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_PRIM_UUID, true ) );
    enqueueOp( new BleOpNotify( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.LAST_TIME_UUID, true ) );
   
    mBTConnected = true;
    // Log.v("DistoX-BLE", "[1] status CONNECTED" );
    notifyStatus( ConnectionState.CONN_CONNECTED ); 

    clearPending();

  }

  // from onCharacteristicRead
  public void readedChrt( String uuid_str, byte[] bytes )
  {
    // Log.v("DistoX-BLE-B", "BRIC comm chrt readed " + uuid_str );
    int ret;
    if ( uuid_str.equals( BricConst.MEAS_PRIM ) ) { // this is not executed: PRIM is read from onCharcateristicChanged
      mQueue.put( DATA_PRIM, bytes ); 
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_META_UUID ) );
    } else if ( uuid_str.equals( BricConst.MEAS_META ) ) {
      mQueue.put( DATA_META, bytes );
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_ERR_UUID ) );
    } else if ( uuid_str.equals( BricConst.MEAS_ERR  ) ) {
      mQueue.put( DATA_ERR, bytes ); 
      /* LAST_TIME could be read, but it is zero-filled
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.LAST_TIME_UUID ) );
      */
    } else if ( uuid_str.equals( BricConst.LAST_TIME  ) ) {
      mQueue.put( DATA_TIME, bytes );
    }
    clearPending();
  }

  // from onCharacteristicWrite
  public void writtenChrt( String uuid_str, byte[] bytes )
  {
    // Log.v("DistoX-BLE-B", "BRIC comm chrt written " + uuid_str + " " + BleUtils.bytesToString( bytes ) );
    // BricDebug.log( "BRIC comm WC " + uuid_str, bytes );
    clearPending();
  }

  // from onDescriptorRead
  public void readedDesc( String uuid_str, byte[] bytes )
  {
    // BricDebug.log( "BRIC comm RD " + uuid_str, bytes );
    clearPending();
  }

  // from onDescriptorWrite
  public void writtenDesc( String uuid_str, byte[] bytes )
  {
    // BricDebug.log( "BRIC comm WD " + uuid_str, bytes );
    clearPending();
  }

  // from onMtuChanged
  public void changedMtu( int mtu )
  {
    Log.v("DistoX-BLE", "BRIC comm changed MTU " + mtu );
    clearPending();
  }

  // from onReadRemoteRssi
  public void readedRemoteRssi( int rssi )
  {
    Log.v("DistoX-BLE", "BRIC comm readed RSSI " + rssi );
    clearPending();
  }

  // from onCharacteristicChanged - this is called when the BRIC4 signals
  // MEAS_META, MEAS_ERR, and LAST_TIME are not change-notified 
  public void changedChrt( BluetoothGattCharacteristic chrt )
  {
    // Log.v("DistoX-BLE-B", "BRIC comm changed char ======> " + chrt.getUuid() );
    int ret;
    String chrt_uuid = BleUtils.uuidToShortString( chrt.getUuid() );
    if ( chrt_uuid.equals( BricConst.MEAS_PRIM ) ) {
      mQueue.put( DATA_PRIM, chrt.getValue() );
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_META_UUID ) );
      // ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_ERR_UUID ) );
    } else if ( chrt_uuid.equals( BricConst.MEAS_META ) ) { 
      // mQueue.put( DATA_META, chrt.getValue() );
    } else if ( chrt_uuid.equals( BricConst.MEAS_ERR  ) ) {
      // mQueue.put( DATA_ERR, chrt.getValue() );
    } else if ( chrt_uuid.equals( BricConst.LAST_TIME  ) ) {
      // mQueue.put( DATA_TIME, chrt.getValue() ); 
      // // Log.v("DistoX-BLE-B", "BRIC comm last time " + BleUtils.bytesToString( chrt.getValue() ) );
    } else {
      TDLog.Error("Bric comm chrt changed " + chrt_uuid );
    }
    clearPending();
  }

  // from onReliableWriteCompleted
  public void completedReliableWrite() 
  { 
    Log.v("DistoX-BLE-B", "BRUC comm: reliable write" );
    clearPending();
  }

  // general error condition
  // the action may depend on the error status TODO
  public void error( int status )
  {
    switch ( status ) {
      case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH: 
        TDLog.Error("BRIC COMM: invalid attr lengt");
        break;
      case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
        TDLog.Error("BRIC COMM: write not permitted");
        break;
      case BluetoothGatt.GATT_READ_NOT_PERMITTED:
        TDLog.Error("BRIC COMM: read not permitted");
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
        TDLog.Error("BRIC COMM: insufficient encrypt");
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
        TDLog.Error("BRIC COMM: insufficient auth");
        break;
      case BleCallback.CONNECTION_TIMEOUT:
      case BleCallback.CONNECTION_133:
        Log.v("DistoX-BLE-B", "BRIC comm: connection timeout or 133");
        notifyStatus( ConnectionState.CONN_WAITING );
        break;
      default:
        Log.v("DistoX-BLE-B", "BRIC comm ***** ERROR " + status + ": reconnecting ...");
        reconnectDevice();
    }
    clearPending();
  }

  public void failure( int status )
  {
    // notifyStatus( ConnectionState.CONN_DISCONNECTED ); // this will be called by disconnected
    clearPending();
    Log.v("DistoX-BLE-B", "BRIC comm Failure: disconnecting ...");
    // Log.v("DistoX-BLE-B", "BRIC comm Failure " + status + ": [2] status DISCONNECTED ...");
    disconnectDevice();
  }
    
  // ----------------- CONNECT -------------------------------

  private boolean connectBricDevice( Device device, Handler lister, int data_type ) // FIXME BLEX_DATA_TYPE
  {
    if ( mRemoteBtDevice == null ) {
      TDToast.makeBad( R.string.ble_no_remote );
      // TDLog.Error("BRIC comm ERROR null remote device");
      // Log.v("DistoX-BLE-B", "BRIC comm ***** connect Device: null = [3b] status DISCONNECTED" );
      notifyStatus( ConnectionState.CONN_DISCONNECTED );
      return false;
    } 
    mReconnect = true;
    mProtocol = new BricProto( mContext, mApp, lister, device, this );
    mCallback = new BleCallback( this );
    mOps = new ConcurrentLinkedQueue< BleOperation >();
    clearPending();

    // Log.v("DistoX-BLE-B", "BRIC comm ***** connect Device = [3a] status WAITING" );
    notifyStatus( ConnectionState.CONN_WAITING );
    int ret = enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
    // Log.v("DistoX-BLE-B", "BRIC comm connects ... " + ret);
    doNextOp();
    return true;
  }

  public void connectGatt( Context ctx, BluetoothDevice bt_device ) // called from BleOpConnect
  {
    // Log.v("DistoX-BLE-B", "BRIC comm ***** connect GATT");
    mContext = ctx;
    mCallback.connectGatt( mContext, bt_device );
  }

  @Override
  public boolean connectDevice( String address, Handler /* ILister */ lister, int data_type )
  {
    // Log.v("DistoX-BLE-B", "BRIC comm ***** connect Device");
    mNrPacketsRead = 0;
    mDataType      = data_type;
    return connectBricDevice( TDInstance.deviceA, lister, data_type );
  }

  // try to recover from an error ... 
  private void reconnectDevice()
  {
    mOps.clear();
    clearPending();
    // disconnectDevice();
    mCallback.disconnectAndCloseGatt();
    if ( mReconnect ) {
      // Log.v("DistoX-BLE", "BRIC comm ***** reconnect Device = [4a] status WAITING" );
      notifyStatus( ConnectionState.CONN_WAITING );
      int ret = enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
      doNextOp();
      mBTConnected = true;
    } else {
      // Log.v("DistoX-BLE", "BRIC comm ***** reconnect Device = [4b] status DISCONNECTED" );
      notifyStatus( ConnectionState.CONN_DISCONNECTED );
    }
  }


  // ----------------- DISCONNECT -------------------------------

  // from onConnectionStateChange STATE_DISCONNECTED
  public void disconnected()
  {
    // Log.v("DistoX-BLE-B", "BRIC comm ***** disconnected = [5] status DISCONNECTED" );
    clearPending();
    mOps.clear(); 
    mBTConnected = false;
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
  }

  public void disconnectGatt()  // called from BleOpDisconnect
  {
    // Log.v("DistoX-BLE-B", "BRIC comm ***** disconnect GATT = [6] status DISCONNECTED" );
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    mCallback.disconnectAndCloseGatt();
  }

  @Override
  public void disconnectDevice()
  {
    // Log.v("DistoX-BLE-B", "BRIC comm ***** disconnect device = connected:" + mBTConnected );
    mReconnect = false;
    if ( mBTConnected ) {
      // Log.v("DistoX-BLE-B", "BRIC comm ***** disconnect connected Device");
      int ret = enqueueOp( new BleOpDisconnect( mContext, this ) ); // exec disconnectGatt
      doNextOp();
      // Log.v("DistoX-BLE-B", "BRIC comm: disconnect ... ops " + ret );
    }
  }

  // ----------------- SEND COMMAND -------------------------------
  public boolean sendCommand( int cmd )
  {
    byte[] command = null;
    switch ( cmd ) {
      case BricConst.CMD_SCAN:  command = Arrays.copyOfRange( BricConst.COMMAND_SCAN,  0, 4 );  break;
      case BricConst.CMD_SHOT:  command = Arrays.copyOfRange( BricConst.COMMAND_SHOT,  0, 4 );  break;
      case BricConst.CMD_LASER: command = Arrays.copyOfRange( BricConst.COMMAND_LASER, 0, 5 ); break;
      case BricConst.CMD_OFF:   command = Arrays.copyOfRange( BricConst.COMMAND_OFF,   0, 9 );   break;
    }
    if ( command == null ) return false;
    // StringBuilder sb = new StringBuilder();
    // for ( int k=0; k<command.length; ++k ) sb.append( (char)command[k] );
    // Log.v("DistoX-BLE-B", "BRIC comm send command " + cmd + " " + sb.toString() );
    enqueueOp( new BleOpChrtWrite( mContext, this, BricConst.CTRL_SRV_UUID, BricConst.CTRL_CHRT_UUID, command ) );
    doNextOp();
    return true;
  }
 
  private boolean sendLastTime( )
  {
    byte[] last_time = ((BricProto)mProtocol).getLastTime();
    // Log.v("DistoX-BLE-B", "BRIC comm send last time: " + BleUtils.bytesToString( last_time ) );
    if ( last_time == null ) return false;
    enqueueOp( new BleOpChrtWrite( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.LAST_TIME_UUID, last_time ) );
    doNextOp();
    return true;
  } 

  // --------------------------------------------------------------------------
  private BleOperation mPendingOp = null;

  private void clearPending() 
  { 
    mPendingOp = null; 
    if ( ! mOps.isEmpty() ) doNextOp();
  }

  // @return the length of the ops queue
  private int enqueueOp( BleOperation op ) 
  {
    mOps.add( op );
    // printOps(); // DEBUG
    return mOps.size();
  }

  private void doNextOp() 
  {
    if ( mPendingOp != null ) {
      // Log.v("DistoX-BLE-B", "BRIC comm: next op with pending not null, ops " + mOps.size() ); 
      return;
    }
    mPendingOp = mOps.poll();
    // Log.v("DistoX-BLE-B", "BRIC comm: polled, ops " + mOps.size() );
    if ( mPendingOp != null ) mPendingOp.execute();
  }

  /* DEBUG
  private void printOps()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Ops: ");
    for ( BleOperation op : mOps ) sb.append( op.name() ).append(" ");
    Log.v("DistoX-BLE", sb.toString() );
  }
  */
    
  // ---------------------------------------------------------------------------------

  public void notifyStatus( int status )
  {
    mApp.notifyStatus( status );
  }


}
