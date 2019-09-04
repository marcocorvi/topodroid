/* @file DistoXA3Comm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.nio.ByteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
// import java.util.ArrayList;
// import java.util.concurrent.atomic.AtomicInteger;

// import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;

// import android.os.Parcelable;
// import android.os.ParcelUuid;
// import android.os.AsyncTask;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothSocket;

// import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
// import android.content.BroadcastReceiver;

// import android.database.DataSetObserver;

// import android.widget.Toast;
// import android.util.Log;

class DistoXA3Comm extends DistoXComm
{

  DistoXA3Comm( TopoDroidApp app )
  {
    super( app );
  }

  // public void resume()
  // {
  //   // if ( mCommThread != null ) { mCommThread.resume(); }
  // }

  // public void suspend()
  // {
  //   // if ( mCommThread != null ) { mCommThread.suspend(); }
  // }


  /** must be overridden to call create proper protocol
   * @param in      input
   * @param out     output
   *        device  TDInstance.device
   *        app     context
   */
  protected DistoXProtocol createProtocol( DataInputStream in, DataOutputStream out )
  {
    return (new DistoXA3Protocol( in, out, TDInstance.device, mApp ));
  }
  // -------------------------------------------------------- 

  /** send the set/unset calib-mode command
   * note called within connectSocket()
   * nothing to read (only write) --> no AsyncTask
   *
   * @param turn_on   whether to turn on or off the DistoX calibration mode
   * @return true if success
   */
  // private boolean setCalibMode( boolean turn_on )
  // {
  //   return sendCommand( turn_on? 0x31 : 0x30 ); 
  // }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return true if success
   */
  @Override
  boolean toggleCalibMode( String address, int type )
  {
    if ( ! checkCommThreadNull() ) {
      TDLog.Error( "toggle Calib Mode address " + address + " not null RFcomm thread" );
      return false;
    }
    boolean ret = false;
    if ( connectSocket( address ) ) {
      byte[] result = null;
      // byte[] result = new byte[4];
      // if ( ! mProtocol.read8000( result ) ) { // FIXME ASYNC
      result = mProtocol.readMemory( DeviceA3Details.mStatusAddress ); // TODO TEST THIS
      if ( result == null ) { 
        TDLog.Error( "toggle Calib Mode A3 failed read 8000" );
      } else {
        ret = setCalibMode( DeviceA3Details.isNotCalibMode( result[0] ) );
        // if ( DeviceA3Details.isNotCalibMode( result[0] ) ) {
        //   ret = mProtocol.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
        // } else {
        //   ret = mProtocol.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
        // }
      }
    }
    destroySocket();
    return ret;
  }

  String readA3HeadTail( String address, byte[] command, int[] head_tail )
  {
    String res = null;
    // if ( TDInstance.deviceType() == Device.DISTO_A3 ) {
      if ( ! checkCommThreadNull() ) return null;
      if ( connectSocket( address ) ) {
        DistoXA3Protocol protocol = (DistoXA3Protocol)mProtocol;
        res = protocol.readA3HeadTail( command, head_tail );
        // FIXME ASYNC new CommandThread( mProtocol, READ_HEAD_TAIL, haed_tail ); NOTE int[] instead of byte[]
        // TDLog.Log( TDLog.LOG_COMM, "read Head Tail() result " + res );
      }
      destroySocket( );
    // }
    return res;
  }
  
  int readA3Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! checkCommThreadNull() ) return -1;
    from &= 0xfff8; // was 0x7ff8
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 )   to   = 0x8000;
    int n = 0;
    if ( from < to ) {
      if ( connectSocket( address ) ) {
        DistoXA3Protocol protocol = (DistoXA3Protocol)mProtocol;
        n = protocol.readMemory( from, to, memory );
        // FIXME ASYNC new CommandThread( mProtocol, READ_MEMORY, memory ) Note...
      }
      destroySocket( );
    }
    return n;
  }

  /** swap hot bit in the range [from, to) [only A3]
   */
  int swapA3HotBit( String address, int from, int to )
  {
    if ( ! checkCommThreadNull() ) return -1;
    if ( TDInstance.deviceType() != Device.DISTO_A3 ) return -2;

    from &= 0xfff8; // was 0x7ff8
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 )   to   = 0x8000;

    int n = 0;
    if ( from != to ) {
      if ( connectSocket( address ) ) {
        DistoXA3Protocol protocol = (DistoXA3Protocol)mProtocol;
        do {
          if ( to == 0 ) {
            to = 0x8000 - 8;
          } else {
            to -= 8;
          }
          // Log.v( TopoDroidApp.TAG, "comm swap hot bit at addr " + to/8 );
          if ( ! protocol.swapA3HotBit( to ) ) break;
          ++ n;
        } while ( to != from );
        // FIXME ASYNC new CommandThread( mProtocol, SWAP_HOT_BITS, from, to ) Note...
        // TDLog.Log( TDLog.LOG_COMM, "swap Hot Bit swapped " + n + "data" );
      }
      destroySocket( );
    }
    return n;
  }


}
