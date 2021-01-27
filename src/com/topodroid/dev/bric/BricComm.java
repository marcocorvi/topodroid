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
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TopoDroidApp;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BricComm extends TopoDroidComm
                      implements BleComm
{
  private ConcurrentLinkedQueue< BleOperation > mOps;

  private Context mContext;
  BleCallback mCallback;
  private BricProto mProto;
  private String          mRemoteAddress;
  private BluetoothDevice mRemoteBtDevice;
  private int mDataType;   // packet datatype 

  public BricComm( Context ctx, TopoDroidApp app, String address, BluetoothDevice bt_device ) 
  {
    super( app );
    mContext  = ctx;
    mRemoteAddress = address;
    mRemoteBtDevice = bt_device;
  }

  public boolean readChrt( UUID srvUuid, UUID chrtUuid ) { return mCallback.readChrt( srvUuid, chrtUuid ); }

  public boolean writeChrt( UUID srvUuid, UUID chrtUuid, byte[] bytes ) { return mCallback.writeChrt( srvUuid, chrtUuid, bytes ); }

  public boolean enableNotify( BluetoothGattCharacteristic chrt ) { return mCallback.enableNotify( chrt ); }
  
  public void close()
  {
    if ( mBTConnected ) disconnectDevice();
  }

  // ---------------------------------------------------------------------------
  // send data to the application

  // --------------------------------------------------------------
  // private void addService( BluetoothGattService srv ) 
  // { 
  //   String srv_uuid = BleUtils.uuidToShortString( srv.getUuid() );
  //   // Log.v("BRIC", "comm add S: " + srv_uuid );
  // }

  private void addChrt( UUID src_uuid, BluetoothGattCharacteristic chrt ) 
  {
    int ret;
    String chrt_uuid = BleUtils.uuidToShortString( chrt.getUuid() );
    if ( chrt_uuid.equals( BricConst.MEAS_PRIM ) ) {
      ret = enqueueOp( new BleOpNotify( mContext, this, chrt, true ) );
      doNextOp();
      // Log.v("BRIC", "comm add   +C: " + chrt_uuid + " ops " + ret );
    } else if ( chrt_uuid.equals( BricConst.MEAS_META ) ) {
      ret = enqueueOp( new BleOpNotify( mContext, this, chrt, true ) );
      doNextOp();
      // Log.v("BRIC", "comm add   +C: " + chrt_uuid + " ops " + ret );
    } else if ( chrt_uuid.equals( BricConst.MEAS_ERR ) ) {
      ret = enqueueOp( new BleOpNotify( mContext, this, chrt, true ) );
      doNextOp();
      // Log.v("BRIC", "comm add   +C: " + chrt_uuid + " ops " + ret );
    } else {
      // Log.v("BRIC", "comm add   +C: " + chrt_uuid );
    }
  }

  // private void addDesc( UUID src_uuid, UUID chrt_uuid, BluetoothGattDescriptor desc ) 
  // {
  //   String desc_uuid = BleUtils.uuidToShortString( desc.getUuid() );
  //   // Log.v("BRIC", "comm add     +D: " + desc_uuid );
  // }

  // --------------------------------------------------

 public void servicesDiscovered( BluetoothGatt gatt )
  {
    (new Handler( Looper.getMainLooper() )).post( new Runnable() {
      public void run() {
        List< BluetoothGattService > services = gatt.getServices();
        for ( BluetoothGattService service : services ) {
          // addService() does not do anything
          // addService( service );
          UUID srv_uuid = service.getUuid();
          // Log.v("BRIC", "Srv  " + BleUtils.uuidToString( srv_uuid ) );
          List< BluetoothGattCharacteristic> chrts = service.getCharacteristics();
          for ( BluetoothGattCharacteristic chrt : chrts ) {
            addChrt( srv_uuid, chrt );
            // addDesc() does not do anything
            // UUID chrt_uuid = chrt.getUuid();
            // // Log.v("BRIC", "  Chrt " + BleUtils.uuidToString( chrt_uuid ) + BleUtils.chrtPermString(chrt) + BleUtils.chrtPropString(chrt) );
            // List< BluetoothGattDescriptor> descs = chrt.getDescriptors();
            // for ( BluetoothGattDescriptor desc : descs ) {
            //   addDesc( srv_uuid, chrt_uuid, desc );
            //   // Log.v("BRIC", "    Desc " + BleUtils.uuidToString( desc.getUuid() ) + BleUtils.descPermString( desc ) );
            // }
          }
        }
      }
    } );
    clearPending();
  }

  public void readedChrt( String uuid_str, byte[] bytes )
  {
    int ret;
    clearPending();
    if ( uuid_str.equals( BricConst.MEAS_PRIM ) ) { // this is not executed
      mProto.addMeasPrim( bytes );
      // appendString( BricDebug.measPrimToString( bytes ) );
      // BricDebug.logMeasPrim( bytes );
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_META_UUID ) );
    } else if ( uuid_str.equals( BricConst.MEAS_META ) ) {
      mProto.addMeasMeta( bytes );
      // appendBytes( uuid_str, bytes );
      // BricDebug.logMeasMeta( bytes );
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_ERR_UUID ) );
    } else if ( uuid_str.equals( BricConst.MEAS_ERR  ) ) {
      mProto.addMeasErr( bytes );
      // appendBytes( uuid_str, bytes );
      // BricDebug.logMeasErr( bytes );
      processData(); 
    }
    clearPending();
  }

  private void processData()
  {
    // TODO process
    mProto.processData();
  }

  public void writtenChrt( String uuid_str, byte[] bytes )
  {
    // BricDebug.log( "Pipe dialog WC " + uuid_str, bytes );
    clearPending();
  }

  public void readedDesc( String uuid_str, byte[] bytes )
  {
    // BricDebug.log( "Pipe dialog RD " + uuid_str, bytes );
    clearPending();
  }

  public void writtenDesc( String uuid_str, byte[] bytes )
  {
    // BricDebug.log( "Pipe dialog WD " + uuid_str, bytes );
    clearPending();
  }

  public void changedChrt( BluetoothGattCharacteristic chrt )
  {
    int ret;
    String chrt_uuid = BleUtils.uuidToShortString( chrt.getUuid() );
    if ( chrt_uuid.equals( BricConst.MEAS_PRIM ) ) {
      byte[] bytes = chrt.getValue();
      mProto.addMeasPrim( bytes );
      // appendString( BricDebug.measPrimToString( bytes ) );
      // BricDebug.logMeasPrim( bytes );
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_META_UUID ) );
      clearPending();
    } else {
      Log.v("BRIC", "Pipe dialog chrt changed " + chrt_uuid );
    }
  }

  public void notified( String uuid_str )
  {
    int ret;
    if ( uuid_str.equals( BricConst.MEAS_PRIM ) ) {
      ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_PRIM_UUID ) );
      // Log.v("BRIC", "Pipe dialog notified MEAS_PRIM" + uuid_str + " start reading ... ops " + ret );
    } else if ( uuid_str.equals( BricConst.MEAS_META ) ) {
      // ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_META_UUID ) );
      // Log.v("BRIC", "Pipe dialog notified MEAS_META" + uuid_str );
    } else if ( uuid_str.equals( BricConst.MEAS_ERR ) ) {
      // ret = enqueueOp( new BleOpChrtRead( mContext, this, BricConst.MEAS_SRV_UUID, BricConst.MEAS_ERR_UUID ) );
      // Log.v("BRIC", "Pipe dialog notified MEAS_ERR" + uuid_str );
    } else {
      // Log.v("BRIC", "Pipe dialog notified unknown" + uuid_str );
    }
    clearPending();
    // doNextOp();
  }

  public void disconnected()
  {
    Log.v("BRIC", "Pipe dialog: disconnected");
    mOps.clear(); 
    clearPending();
    mBTConnected = false;
  }

  public void error( int status )
  {
    Log.v("BRIC", "Error " + status + ": continuing ...");
    reconnectDevice();
  }

  public void failure( int status )
  {
    Log.v("BRIC", "Failure " + status + ": disconnecting ...");
    disconnectDevice();
  }
    
  // ----------------- CONNECT -------------------------------
  // private int mConnectionMode = -1;

  private void connectBricDevice( Device device, Handler lister, int data_type ) // FIXME BLEX_DATA_TYPE
  {
    if ( mRemoteBtDevice == null ) {
      // TDToast.makeBad( R.string.ble_no_remote );
      Log.v("DistoX-BLE-C", "ERROR null remote device");
    } else {
      mProto    = new BricProto( mContext, mApp, lister, device, this );
      mCallback = new BleCallback( this );
      mOps = new ConcurrentLinkedQueue< BleOperation >();
      clearPending();

      disconnectDevice();
      int ret = enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
      Log.v("BRIC", "Pipe dialog connects ... ops " + ret);
      doNextOp();
      mBTConnected = true;
    }
  }

  public void connectGatt( Context ctx, BluetoothDevice bt_device ) // called from BleOpConnect
  {
    mContext = ctx;
    mCallback.connectGatt( mContext, bt_device );
  }

  @Override
  public boolean connectDevice( String address, Handler /* ILister */ lister, int data_type )
  {
    // mLister = lister; 
    // mConnectionMode = 1;
    mNrPacketsRead = 0;
    mDataType      = data_type;
    connectBricDevice( TDInstance.deviceA, lister, data_type );
    return true;
  }

  // try to recover from an error ... 
  private void reconnectDevice()
  {
    mOps.clear();
    clearPending();
    disconnectDevice();
    int ret = enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
    doNextOp();
    mBTConnected = true;
  }


  // ----------------- DISCONNECT -------------------------------
  public void disconnectGatt()  // called from BleOpDisconnect
  {
    mCallback.disconnectGatt();
  }

  @Override
  public void disconnectDevice()
  {
    if ( mBTConnected ) {
      int ret = enqueueOp( new BleOpDisconnect( mContext, this ) ); // exec disconnectGatt
      Log.v("BRIC", "Pipe dialog: diconnect ... ops " + ret );
    }
  }

  // ----------------- SEND COMMAND -------------------------------
  public boolean sendCommand( byte[] cmd )
  {
    enqueueOp( new BleOpChrtWrite( mContext, this, BricConst.CTRL_SRV_UUID, BricConst.CTRL_CHRT_UUID, cmd ) );
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
    // Log.v("BRIC", "enqueued " + mOps.size() );
    // if ( mPendingOp == null ) doNextOp();
    return mOps.size();
  }

  private void doNextOp() 
  {
    if ( mPendingOp != null ) {
      Log.v("BRIC", "next op with pending not null " + mOps.size() ); 
      return;
    }
    mPendingOp = mOps.poll();
    // Log.v("BRIC", "polled " + mOps.size() );
    if ( mPendingOp == null ) return; // empty queue 
    mPendingOp.execute();
  }
    
  // ---------------------------------------------------------------------------------

  public void completedReliableWrite() { Log.v("BRIC", "COMM reliable write" ); }

}
