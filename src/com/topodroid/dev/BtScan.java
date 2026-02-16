/* @file BtScan.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid classic BT device discovery
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.Timer;
import com.topodroid.TDX.DeviceActivity;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import android.content.Intent;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;


public class BtScan
{ 
  private Context mContext;
  private DeviceActivity mParent;
  private Timer mTimer = null;

  BtScan( Context ctx, DeviceActivity parent )
  {
    mContext = ctx;
    mParent = parent;
  }

  private BroadcastReceiver mBTReceiver = null;

  private final static String[] mDiscoveryError = { "ok",
    "no adapter", "BT not enabled", "BT state off", "already discovering", "unknown error", "security exception" };

  /** start a BT scan
   */
  public boolean scanBtDevices()
  {
    // TDLog.v( "BT scan start" );
    mBTReceiver = new BroadcastReceiver() 
    {
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        TDLog.v( "BT scan on receive: action " + action );
        if ( DeviceUtil.ACTION_DISCOVERY_STARTED.equals( action ) ) {
          TDLog.v( "onReceive BT DISCOVERY STARTED" );
        } else if ( DeviceUtil.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
          resetReceiver();
        } else if ( DeviceUtil.ACTION_FOUND.equals( action ) ) {
          BluetoothDevice device = data.getParcelableExtra( DeviceUtil.EXTRA_DEVICE );
          String device_addr = device.getAddress();
          String model = device.getName();
          if ( device.getBondState() != DeviceUtil.BOND_BONDED ) {
            TDLog.v( "onReceive BT device <" + model + "> not bonded, address " + device_addr);
            if ( model != null && Device.isKnownDevice( model ) ) { // DistoX and DistoX2
              String name = Device.btnameToName( model );
              // TDLog.v( "BT scan receiver add <" + name + "> " + device_addr ); 
              // TopoDroidApp.mDData.insertDevice( device_addr, model, name );
              // mArrayAdapter.add( Device.btnameToModel(model) + " " + name + " " + device_addr );
              mParent.addBluetoothDevice( device, device_addr, model );
            }
          } else {
            TDLog.v( "onReceive BT device <" + model + "> bonded, address " + device_addr);
          }
        } else if ( DeviceUtil.ACTION_ACL_CONNECTED.equals( action ) ) {
          TDLog.v( "ACL_CONNECTED");
        } else if ( DeviceUtil.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
          TDLog.v( "ACL_DISCONNECT_REQUESTED");
        } else if ( DeviceUtil.ACTION_ACL_DISCONNECTED.equals( action ) ) {
          TDLog.v( "ACL_DISCONNECTED");
        }
      }
    };
    IntentFilter foundFilter = new IntentFilter( DeviceUtil.ACTION_FOUND );
    IntentFilter startFilter = new IntentFilter( DeviceUtil.ACTION_DISCOVERY_STARTED );
    IntentFilter finishFilter = new IntentFilter( DeviceUtil.ACTION_DISCOVERY_FINISHED );

    // IntentFilter connectedFilter = new IntentFilter( DeviceUtil.ACTION_ACL_CONNECTED );
    // IntentFilter disconnectRequestFilter = new IntentFilter( DeviceUtil.ACTION_ACL_DISCONNECT_REQUESTED );
    // IntentFilter disconnectedFilter = new IntentFilter( DeviceUtil.ACTION_ACL_DISCONNECTED );

    // IntentFilter uuidFilter  = new IntentFilter( myUUIDaction );
    // IntentFilter bondFilter  = new IntentFilter( DeviceUtil.ACTION_BOND_STATE_CHANGED );

    mParent.registerReceiver( mBTReceiver, foundFilter );
    mParent.registerReceiver( mBTReceiver, startFilter );
    mParent.registerReceiver( mBTReceiver, finishFilter );

    int err = DeviceUtil.startDiscovery();
    if ( err != 0 ) { // 1 no_adapter, 2 not_enabled, 3 state_off, 4 already_discovering, 5 error, 6 security_exception
      resetReceiver();
      TDToast.make( String.format( mContext.getResources().getString( R.string.discovery_error ), mDiscoveryError[err] ) );
      return false;
    } else {
      mTimer = new Timer( 5000, new Runnable() {
        @Override public void run() {
          resetReceiver();
        }
      } );
    }
    return true;
  }

  /** unregister the BT receiver and discart it
   */
  public void resetReceiver()
  {
    if ( mBTReceiver != null ) {
      mParent.unregisterReceiver( mBTReceiver );
      mBTReceiver = null;
    }
    if ( mTimer != null ) mTimer = null;
    mParent.runOnUiThread( new Runnable() { public void run() { 
      mParent.setBtScanning( false );
    } } );
  }

  // /** pair the android and the current device
  //  * @param address   device BT address
  //  */
  // private void pairDevice( String address )
  // {
  //   BluetoothDevice device = DeviceUtil.getRemoteDevice( address );
  //   if ( device == null ) {
  //     // TDLog.v( "no BT device for address " + address );
  //     return;
  //   }
  //   // TDLog.v( "onReceive BT DEVICES FOUND, name " + device.getName() );
  //   if ( device.getBondState() == DeviceUtil.BOND_BONDED ) {
  //     TDLog.v( "BT device " + address + " already bonded" );
  //     return;
  //   }
  //   int res = DeviceUtil.pairDevice( device );
  //   TDLog.v( "BT device " + address + " try to bond: result " + res );
  //   switch ( res ) {
  //     case -1: // failure
  //       // TDToast.makeBad( R.string.pairing_failed ); // TODO
  //       break;
  //     case 2: // already paired
  //       // TDToast.make( R.string.device_paired ); 
  //       break;
  //     default: // 0: null device
  //              // 1: paired ok
  //   }
  // }

}

