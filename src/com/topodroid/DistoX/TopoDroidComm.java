/* @file TopoDroidComm.java
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
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

import java.util.UUID;
// import java.util.List;
// import java.util.concurrent.atomic.AtomicInteger; // FIXME_ATOMIC_INT

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

// import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
// import android.content.BroadcastReceiver;

class TopoDroidComm
{
  static final int COMM_RFCOMM = 0;
  static final int COMM_GATT   = 1;

  protected static final String SERVICE_STRING = "00001101-0000-1000-8000-00805F9B34FB";
  protected static final UUID   SERVICE_UUID = UUID.fromString( SERVICE_STRING );

  protected TopoDroidApp mApp;
  protected String mAddress;
  protected TopoDroidProtocol mProtocol;

  protected boolean mCalibMode;   //!< whether the device is in calib-mode

  protected boolean mBTConnected;

  byte[] mCoeff;

// -----------------------------------------------------------

  // private AtomicInteger nReadPackets; // FIXME_ATOMIC_INT
  protected volatile int nReadPackets;

  // int getNrReadPackets() { return ( nReadPackets == null )? 0 : nreadPackets.get(); } // FIXME_ATOMIC_INT 
  int getNrReadPackets() { return nReadPackets; }
  // void incNrReadPackets() { ++nReadPackets; }
  // void resetNrReadPackets() { nReadPackets = 0; }

  public boolean isConnected() { return mBTConnected; }

  protected class CommThread extends Thread
  {
    int mType;

    private TopoDroidProtocol mProtocol;
    private int toRead; // number of packet to read
    // private ILister mLister;
    Handler mLister = null; // FIXME_LISTER
    // private long mLastShotId;   // last shot id

    private volatile boolean doWork = true;
    private int mDataType;

    void cancelWork()
    {
      if ( mProtocol != null ) mProtocol.mMaxTimeout = 0;
      doWork = false;
    }

    /** 
     * @param protocol    communication protocol
     * @param to_read     number of data to read (use -1 to read forever until timeout or an exception)
     * @param lister      optional data lister
     */
    CommThread( int type, TopoDroidProtocol protocol, int to_read, Handler /* ILister */ lister, int data_type ) // FIXME_LISTER
    {
      mType  = type;
      toRead = to_read;
      mProtocol = protocol;
      mLister   = lister;
      mDataType = data_type;
      // reset nr of read packets 
      // nReadPackets = new AtomicInteger( 0 ); // FIXME_ATOMIC_INT
      nReadPackets = 0;
      // mLastShotId = 0;
      // TDLog.Log( TDLog.LOG_COMM, "RF comm thread cstr ToRead " + toRead );
    }

    /** This thread blocks on readPacket (socket read) and when a packet arrives 
     * it handles it
     */
    public void run()
    {
      doWork = true;
      mHasG  = false;

      // TDLog.Log( TDLog.LOG_COMM, "RF comm thread running ... to_read " + toRead );
      // Log.v( "DistoX-COMM", "RF comm thread ... to_read " + toRead );
      if ( mType == COMM_RFCOMM ) {
        while ( doWork && nReadPackets /* .get() */ != toRead ) {
          // TDLog.Log( TDLog.LOG_COMM, "RF comm loop: read " + getNrReadPackets() + " to-read " + toRead );
          
          int res = mProtocol.readPacket( (toRead >= 0), mDataType );
          // TDLog.Log( TDLog.LOG_COMM, "RF comm readPacket returns " + res );
          if ( res == TopoDroidProtocol.DISTOX_PACKET_NONE ) {
            if ( toRead == -1 ) {
              doWork = false;
            } else {
              // TDLog.Log( TDLog.LOG_COMM, "RF comm sleeping 1000 " );
              TDUtil.slowDown( TDSetting.mWaitConn, "RF comm thread sleep interrupt");
            }
          } else if ( res == TopoDroidProtocol.DISTOX_ERR_OFF ) {
            // TDLog.Error( "RF comm readPacket returns ERR_OFF " );
            // if ( TDSetting.mCommType == 1 && TDSetting.mAutoReconnect ) { // FIXME ACL_DISCONNECT
            //   mApp.mDataDownloader.setConnected( false );
            //   mApp.notifyStatus();
            //   closeSocket( );
            //   mApp.notifyDisconnected();
            // }
            doWork = false;
          } else {
            handleRegularPacket( res, mLister, mDataType );
          }
        }
      } else { // if ( mType == COMM_GATT ) 
        mProtocol.readPacket( true, mDataType ); // start reading a packet
      }
      // TDLog.Log( TDLog.LOG_COMM, "RF comm thread run() exiting");
      mCommThread = null;

      // FIXME_COMM
      // mApp.notifyConnState( );
    }
  }

  void handleRegularPacket( int res, Handler lister, int data_type )
  {
    if ( res == TopoDroidProtocol.DISTOX_PACKET_DATA ) {
      // nReadPackets.incrementAndGet(); // FIXME_ATOMIC_INT
      ++nReadPackets;
      double d = mProtocol.mDistance;
      double b = mProtocol.mBearing;
      double c = mProtocol.mClino;
      double r = mProtocol.mRoll;
      // extend is unset to start
      // long extend = TDAzimuth.computeLegExtend( b ); // DBlock.EXTEND_UNSET; FIXME_EXTEND 
      TDLog.Log( TDLog.LOG_COMM, "Comm D PACKET " + d + " " + b + " " + c );
      // Log.v( "DistoXBLE", "Comm D PACKET " + d + " " + b + " " + c );
      // NOTE type=0 shot is DistoX-type
      long status = ( d > TDSetting.mMaxShotLength )? TDStatus.OVERSHOOT : TDStatus.NORMAL;
      mLastShotId = TopoDroidApp.mData.insertDistoXShot( TDInstance.sid, -1L, d, b, c, r, DBlock.EXTEND_IGNORE, status, TDInstance.deviceAddress() );
      if ( lister != null ) { // FIXME_LISTER sendMessage with mLastShotId only
        Message msg = lister.obtainMessage( Lister.LIST_UPDATE );
        Bundle bundle = new Bundle();
        bundle.putLong( Lister.BLOCK_ID, mLastShotId );
        msg.setData(bundle);
        lister.sendMessage(msg);
        if ( TDInstance.deviceType() == Device.DISTO_A3 && TDSetting.mWaitData > 10 ) {
          TDUtil.slowDown( TDSetting.mWaitData );
        }
      }
      // if ( lister != null ) {
      //   DBlock blk = new DBlock( );
      //   blk.setId( mLastShotId, TDInstance.sid );
      //   blk.mLength  = (float)d;
      //   blk.mBearing = (float)b;
      //   blk.mClino   = (float)c;
      //   blk.mRoll    = (float)r;
      //   lister.updateBlockList( blk );
      // }
    } else if ( res == TopoDroidProtocol.DISTOX_PACKET_G ) {
      TDLog.Log( TDLog.LOG_COMM, "Comm G PACKET" );
      // nReadPackets.incrementAndGet(); // FIXME_ATOMIC_INT
      ++nReadPackets;
      mHasG = true;
    } else if ( res == TopoDroidProtocol.DISTOX_PACKET_M ) {
      TDLog.Log( TDLog.LOG_COMM, "Comm M PACKET" );
      // nReadPackets.incrementAndGet(); // FIXME_ATOMIC_INT
      ++nReadPackets;
      // get G and M from mProtocol and save them to store
      TDLog.Log( TDLog.LOG_COMM, "G " + mProtocol.mGX + " " + mProtocol.mGY + " " + mProtocol.mGZ + " M " + mProtocol.mMX + " " + mProtocol.mMY + " " + mProtocol.mMZ );
      long cblk = TopoDroidApp.mDData.insertGM( TDInstance.cid, mProtocol.mGX, mProtocol.mGY, mProtocol.mGZ, mProtocol.mMX, mProtocol.mMY, mProtocol.mMZ );
      if ( lister != null ) {
        Message msg = lister.obtainMessage( Lister.LIST_UPDATE );
        Bundle bundle = new Bundle();
        bundle.putLong( Lister.BLOCK_ID, cblk );
        msg.setData(bundle);
        lister.sendMessage(msg);
      }
      if ( ! mHasG ) {
        TDLog.Error( "data without G packet " + nReadPackets /* getNrReadPackets() */ );
        TopoDroidApp.mActivity.runOnUiThread( new Runnable() {
          public void run() {
            TDToast.makeBG("data without G: " + nReadPackets /* getNrReadPackets() */, TDColor.FIXED_RED );
          }
        } );
      }
      mHasG = false;
    } else if ( res == TopoDroidProtocol.DISTOX_PACKET_REPLY ) {
      // TODO handle packet reply
      //
      // byte[] addr = mProtocol.getAddress();
      // byte[] reply = mProtocol.getReply();
      // String result = String.format("%02x %02x %02x %02x at %02x%02x",
      //   reply[0], reply[1], reply[2], reply[3], addr[1], addr[0] );
      // TDLog.Log( TDLog.LOG_DISTOX, "REPLY PACKET: " + result ); 

      // if ( addr[0] == (byte)0x00 && addr[1] == (byte)0x80 ) { // 0x8000
      //   // TDLog.Log( TDLog.LOG_DISTOX, "toggle reply" );
      //   // if ( (reply[0] & CALIB_BIT) == 0 ) {
      //   //     mProtocol.sendCommand( (byte)Device.CALIB_ON );
      //   // } else {
      //   //     mProtocol.sendCommand( (byte)Device.CALIB_OFF );
      //   // }
      // } else if ( ( addr[1] & (byte)0x80) == (byte)0x80 ) { // REPLY TO READ/WRITE-CALIBs
      //   // TDLog.Log( TDLog.LOG_DISTOX, "write reply" );
      //   // mProtocol.setWrittenCalib( true );
      // } else if ( addr[0] == 0x20 && addr[1] == (byte)0xC0 ) { // C020 READ HEAD-TAIL
      //   // TDLog.Log( TDLog.LOG_DISTOX, "read head-tail reply");
      //   // mHead = (int)( reply[0] | ( (int)(reply[1]) << 8 ) );
      //   // mTail = (int)( reply[2] | ( (int)(reply[3]) << 8 ) );
      // }
    } else if ( res == TopoDroidProtocol.DISTOX_PACKET_VECTOR ) {
      // vector packet do count
      // nReadPackets.incrementAndGet(); // FIXME_ATOMIC_INT
      ++nReadPackets;
      double acc  = mProtocol.mAcceleration;
      double mag  = mProtocol.mMagnetic;
      double dip  = mProtocol.mDip;
      double roll = mProtocol.mRoll;
      boolean backshot = mProtocol.mBackshot;
      TDLog.Log( TDLog.LOG_COMM, "Comm V PACKET " + mLastShotId + " " + acc + " " + mag + " " + dip + " " + roll );
      if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
        TopoDroidApp.mData.updateShotAMDR( mLastShotId, TDInstance.sid, acc, mag, dip, roll, backshot );
        if ( TDSetting.mWaitData > 10 ) {
          TDUtil.slowDown( TDSetting.mWaitData );
        }
      }
    }
  }

  protected CommThread mCommThread;
  boolean mHasG = false;
  long mLastShotId;   // last shot id

  protected TopoDroidComm( TopoDroidApp app )
  {
    mApp          = app;
    mProtocol     = null;
    mAddress      = null;
    mCommThread = null;
    mCalibMode    = false;
    mBTConnected  = false;
    // TDLog.Log( TDLog.LOG_COMM, "TopoDroid Comm cstr");
  }

  void resume()
  {
    // if ( mCommThread != null ) { mCommThread.resume(); }
  }

  void suspend()
  {
    // if ( mCommThread != null ) { mCommThread.suspend(); }
  }


  protected void cancelCommThread()
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm cancel Comm thread");
    if ( mCommThread != null ) {
      // TDLog.Log( TDLog.LOG_COMM, "cancel Comm thread: thread is active");
      mCommThread.cancelWork();
      try {
        mCommThread.join();
      } catch ( InterruptedException e ) {
        // TDLog.Error( "cancel thread interrupt " + e.getMessage() );
      } finally {
        // TDLog.Log( TDLog.LOG_COMM, "cancel Comm thread: nulling thread");
        mCommThread = null;
      }
    // } else {
      // TDLog.Log( TDLog.LOG_COMM, "cancel Comm thread: no thread");
    }
  }

  // -------------------------------------------------------- 
  // PROTOCOL

  protected void closeProtocol()
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm close protocol");
    // if ( mProtocol != null ) mProtocol.closeIOstreams();
    mProtocol = null;
  }

  protected boolean startCommThread( int to_read, Handler /* ILister */ lister, int data_type ) 
  {
    return false;
  }

  void disconnectRemoteDevice( )
  {
    // TDLog.Log( TDLog.LOG_COMM, "disconnect remote device ");
    cancelCommThread();
    closeProtocol();
  }

  protected boolean checkCommThreadNull( ) { return ( mCommThread == null ); }

  protected boolean sendCommand( int cmd )
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm send cmd " + cmd );
    boolean ret = false;
    if ( mProtocol != null ) {
      for (int k=0; k<3 && ! ret; ++k ) { // try three times
        ret = mProtocol.sendCommand( (byte)cmd ); // was ret |= ...
        // TDLog.Log( TDLog.LOG_COMM, "sendCommand " + cmd + " " + k + "-ret " + ret );
        // Log.v( "DistoX", "sendCommand " + cmd + " " + k + "-ret " + ret );
        TDUtil.slowDown( TDSetting.mWaitCommand, "send command sleep interrupted"); // it is ok to be interrupted
      }
    }
    return ret;
  }

  boolean toggleCalibMode( String address, int type ) { return false; }

  boolean writeCoeff( String address, byte[] coeff ) { return false; }

  boolean readCoeff( String address, byte[] coeff ) { return false; }

  byte[] readMemory( String address, int addr ) { return null; }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  boolean connectDevice( String address, Handler /* ILister */ lister, int data_type )
  {
    return false;
   }

  void disconnectDevice() { }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  // @param data_type    either shot or calib (or all)
  int downloadData( String address, Handler /* ILister */ lister, int data_type )
  {
    return -1;
  }

}
