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

import com.topodroid.utils.TDLog;
import com.topodroid.packetX.MemoryOctet;

import android.util.Log;

// import java.nio.ByteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
// import java.util.ArrayList;
// import java.util.concurrent.atomic.AtomicInteger;


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
   *        device  TDInstance.deviceA
   *        app     context
   */
  protected DistoXProtocol createProtocol( DataInputStream in, DataOutputStream out )
  {
    return (new DistoXA3Protocol( in, out, TDInstance.deviceA, mApp ));
  }
  // -------------------------------------------------------- 

  /* send the set/unset calib-mode command
   * note called within connectSocket()
   * nothing to read (only write) --> no AsyncTask
   *
   * @param turn_on   whether to turn on or off the DistoX calibration mode
   * @return true if success
   */
  // private boolean setCalibMode( boolean turn_on )
  // {
  //   return sendCommand( turn_on? Device.CALIB_ON : Device.CALIB_OFF ); 
  // }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return true if success
   */
  @Override
  boolean toggleCalibMode( String address, int type )
  {
    if ( ! isCommThreadNull() ) {
      TDLog.Error( "toggle Calib Mode address " + address + " not null RFcomm thread" );
      return false;
    }
    boolean ret = false;
    if ( connectSocketAny( address ) ) {
      byte[] result = null;
      // byte[] result = new byte[4];
      // if ( ! mProtocol.read8000( result ) ) { // FIXME ASYNC
      result = mProtocol.readMemory( DeviceA3Details.mStatusAddress ); // TODO TEST THIS
      if ( result == null ) { 
        TDLog.Error( "toggle Calib Mode A3 failed read 8000" );
      } else {
        ret = setCalibMode( DeviceA3Details.isNotCalibMode( result[0] ) );
        // if ( DeviceA3Details.isNotCalibMode( result[0] ) ) {
        //   ret = mProtocol.sendCommand( (byte)Device.CALIB_ON ); 
        // } else {
        //   ret = mProtocol.sendCommand( (byte)Device.CALIB_OFF ); 
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
      if ( ! isCommThreadNull() ) return null;
      if ( connectSocketAny( address ) ) {
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
    if ( ! isCommThreadNull() ) return -1;
    from &= 0xfff8; // was 0x7ff8
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 )   to   = 0x8000;
    int n = 0;
    if ( from < to ) {
      if ( connectSocketAny( address ) ) {
        DistoXA3Protocol protocol = (DistoXA3Protocol)mProtocol;
        n = protocol.readMemory( from, to, memory );
        // FIXME ASYNC new CommandThread( mProtocol, READ_MEMORY, memory ) Note...
      }
      destroySocket( );
    }
    return n;
  }

  /** swap hot bit in the range [from, to) [only A3]
   * from and to are memory addresses - must be multiple of 8
   */
  int swapA3HotBit( String address, int from, int to, boolean on_off )
  {
    if ( ! isCommThreadNull() ) return -1;
    if ( TDInstance.deviceType() != Device.DISTO_A3 ) return -2;

    from &= 0xfff8; // was 0x7ff8
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 )   to   = 0x8000;

    int n = 0;
    if ( from != to ) {
      if ( connectSocketAny( address ) ) {
        DistoXA3Protocol protocol = (DistoXA3Protocol)mProtocol;
        do {
          if ( to == 0 ) {
            to = 0x8000 - 8;
          } else {
            to -= 8;
          }
          // Log.v( "DistoX-HT", "comm swap hot bit at addr " + to );
          if ( ! protocol.swapA3HotBit( to, on_off ) ) break;
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
