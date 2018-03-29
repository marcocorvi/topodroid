/* @file TopoDroidComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

// import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
// import android.content.BroadcastReceiver;

import android.widget.Toast;
// import android.util.Log;

class TopoDroidComm
{
  protected TopoDroidApp mApp;
  protected String mAddress;
  protected DistoXProtocol mProtocol;
  protected boolean mCalibMode;   //!< whether the device is in calib-mode

  protected boolean mBTConnected;

  byte[] mCoeff;

// -----------------------------------------------------------

  protected volatile int nReadPackets;

  public boolean isConnected() { return mBTConnected; }

  protected class RfcommThread extends Thread
  {
    private DistoXProtocol mProto;
    private int toRead; // number of packet to read
    // private ILister mLister;
    private Handler mLister; // FIXME LISTER
    private long mLastShotId;   // last shot id

    private volatile boolean doWork = true;

    void cancelWork()
    {
      if ( mProto != null ) mProto.mMaxTimeout = 0;
      doWork = false;
    }

    /** 
     * @param protocol    communication protocol
     * @param to_read     number of data to read (use -1 to read forever until timeout or an exception)
     */
    RfcommThread( DistoXProtocol protocol, int to_read, Handler /* ILister */ lister ) // FIXME LISTER
    {
      toRead = to_read;
      mProto = protocol;
      mLister = lister;
      nReadPackets = 0; // reset nr of read packets
      // mLastShotId = 0;
      // TDLog.Log( TDLog.LOG_COMM, "RFcomm thread cstr ToRead " + toRead );
    }

    public void run()
    {
      boolean hasG = false;
      doWork = true;

      // TDLog.Log( TDLog.LOG_COMM, "RFcomm thread running ... to_read " + toRead );
      while ( doWork && nReadPackets != toRead ) {
        // TDLog.Log( TDLog.LOG_COMM, "RFcomm loop: read " + nReadPackets + " to-read " + toRead );
        
        int res = mProto.readPacket( toRead >= 0 );
        // TDLog.Log( TDLog.LOG_COMM, "RFcomm readPacket returns " + res );
        if ( res == DistoXProtocol.DISTOX_PACKET_NONE ) {
          if ( toRead == -1 ) {
            doWork = false;
          } else {
            try {
              // TDLog.Log( TDLog.LOG_COMM, "RFcomm sleeping 1000 " );
              Thread.sleep( TDSetting.mWaitConn );
            } catch (InterruptedException e) {
              // TDLog.Log( TDLog.LOG_COMM, "RFcomm thread sleep interrupt");
            }
          }
        } else if ( res == DistoXProtocol.DISTOX_ERR_OFF ) {
          // TDLog.Error( "RFcomm readPacket returns ERR_OFF " );
          // if ( TDSetting.mCommType == 1 && TDSetting.mAutoReconnect ) { // FIXME ACL_DISCONNECT
          //   mApp.mDataDownloader.setConnected( false );
          //   mApp.notifyStatus();
          //   closeSocket( );
          //   mApp.notifyDisconnected();
          // }
          doWork = false;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_DATA ) {
          ++nReadPackets;
          double d = mProto.mDistance;
          double b = mProto.mBearing;
          double c = mProto.mClino;
          double r = mProto.mRoll;
          // extend is unset to start
          // long extend = TDAzimuth.computeLegExtend( b ); // DBlock.EXTEND_UNSET; FIXME-EXTEND 
          // TDLog.Log( TDLog.LOG_COMM, "DATA PACKET " + d + " " + b + " " + c );
          // NOTE type=0 shot is DistoX-type
          long status = ( d > TDSetting.mMaxShotLength )? TDStatus.OVERSHOOT : TDStatus.NORMAL;
          mLastShotId = TopoDroidApp.mData.insertDistoXShot( mApp.mSID, -1L, d, b, c, r, DBlock.EXTEND_IGNORE, status, true );
          if ( mLister != null ) { // FIXME LISTER sendMessage with mLastShotId only
            Message msg = mLister.obtainMessage( Lister.UPDATE );
            Bundle bundle = new Bundle();
            bundle.putLong( Lister.BLOCK_ID, mLastShotId );
            msg.setData(bundle);
            mLister.sendMessage(msg);
            if ( mApp.distoType() == Device.DISTO_A3 && TDSetting.mWaitData > 10 ) {
              try {
                Thread.sleep( TDSetting.mWaitData ); // slowdown
              } catch ( InterruptedException e ) { }
            }
          }
          // if ( mLister != null ) {
          //   DBlock blk = new DBlock( );
          //   blk.setId( mLastShotId, mApp.mSID );
          //   blk.mLength  = (float)d;
          //   blk.mBearing = (float)b;
          //   blk.mClino   = (float)c;
          //   blk.mRoll    = (float)r;
          //   mLister.updateBlockList( blk );
          // }
        } else if ( res == DistoXProtocol.DISTOX_PACKET_G ) {
          // TDLog.Log( TDLog.LOG_DISTOX, "G PACKET" );
          ++nReadPackets;
          hasG = true;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_M ) {
          // TDLog.Log( TDLog.LOG_DISTOX, "M PACKET" );
          ++nReadPackets;
          // get G and M from mProto and save them to store
          // TDLog.Log( TDLog.LOG_PROTO, "save G " + mProto.mGX + " " + mProto.mGY + " " + mProto.mGZ + 
          //                   " M " + mProto.mMX + " " + mProto.mMY + " " + mProto.mMZ );
          long cblk = TopoDroidApp.mDData.insertGM( mApp.mCID, mProto.mGX, mProto.mGY, mProto.mGZ, mProto.mMX, mProto.mMY, mProto.mMZ );
          if ( mLister != null ) {
            Message msg = mLister.obtainMessage( Lister.UPDATE );
            Bundle bundle = new Bundle();
            bundle.putLong( Lister.BLOCK_ID, cblk );
            msg.setData(bundle);
            mLister.sendMessage(msg);
          }
          if ( ! hasG ) {
            TDLog.Error( "data without G packet " + nReadPackets );
            mApp.mActivity.runOnUiThread( new Runnable() {
              public void run() {
                TDToast.makeBG(mApp, "data without G: " + nReadPackets, TDColor.FIXED_RED );
              }
            } );
          }
          hasG = false;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_REPLY ) {
          // TODO jandle packet reply
	  //
          // byte[] addr = mProto.getAddress();
          // byte[] reply = mProto.getReply();
          // String result = String.format("%02x %02x %02x %02x at %02x%02x",
          //   reply[0], reply[1], reply[2], reply[3], addr[1], addr[0] );
          // TDLog.Log( TDLog.LOG_DISTOX, "REPLY PACKET: " + result ); 

          // if ( addr[0] == (byte)0x00 && addr[1] == (byte)0x80 ) { // 0x8000
          //   // TDLog.Log( TDLog.LOG_DISTOX, "toggle reply" );
          //   // if ( (reply[0] & CALIB_BIT) == 0 ) {
          //   //     mProto.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
          //   // } else {
          //   //     mProto.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
          //   // }
          // } else if ( ( addr[1] & (byte)0x80) == (byte)0x80 ) { // REPLY TO READ/WRITE-CALIBs
          //   // TDLog.Log( TDLog.LOG_DISTOX, "write reply" );
          //   // mProto.setWrittenCalib( true );
          // } else if ( addr[0] == 0x20 && addr[1] == (byte)0xC0 ) { // C020 READ HEAD-TAIL
          //   // TDLog.Log( TDLog.LOG_DISTOX, "read head-tail reply");
          //   // mHead = (int)( reply[0] | ( (int)(reply[1]) << 8 ) );
          //   // mTail = (int)( reply[2] | ( (int)(reply[3]) << 8 ) );
          // }
        } else if ( res == DistoXProtocol.DISTOX_PACKET_VECTOR ) {
          ++nReadPackets;  // vector packet do count
          double acc  = mProto.mAcceleration;
          double mag  = mProto.mMagnetic;
          double dip  = mProto.mDip;
          double roll = mProto.mRoll;
          // TDLog.Log( TDLog.LOG_DISTOX, "VECTOR PACKET " + mLastShotId + " " + acc + " " + mag + " " + dip + " " + roll );
          if ( mApp.distoType() == Device.DISTO_X310 ) {
            TopoDroidApp.mData.updateShotAMDR( mLastShotId, mApp.mSID, acc, mag, dip, roll, true );
            if ( TDSetting.mWaitData > 10 ) {
              try {
                Thread.sleep( TDSetting.mWaitData ); // slowdown
              } catch ( InterruptedException e ) { }
            }
          }
        }
      }
      // TDLog.Log( TDLog.LOG_COMM, "RFcomm thread run() exiting");
      mRfcommThread = null;

      // FIXME_COMM
      // mApp.notifyConnState( );

    }
  };

  protected RfcommThread mRfcommThread;


  TopoDroidComm( TopoDroidApp app )
  {
    mApp          = app;
    mProtocol     = null;
    mAddress      = null;
    mRfcommThread = null;
    mCalibMode    = false;
    mBTConnected  = false;
    // TDLog.Log( TDLog.LOG_COMM, "TopoDroid Comm cstr");
  }

  void resume()
  {
    // if ( mRfcommThread != null ) { mRfcommThread.resume(); }
  }

  void suspend()
  {
    // if ( mRfcommThread != null ) { mRfcommThread.suspend(); }
  }


  protected void cancelRfcommThread()
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm cancel Rfcomm thread");
    if ( mRfcommThread != null ) {
      // TDLog.Log( TDLog.LOG_COMM, "cancel Rfcomm thread: thread is active");
      mRfcommThread.cancelWork();
      try {
        mRfcommThread.join();
      } catch ( InterruptedException e ) {
        // TDLog.Error( "cancel thread interrupt " + e.getMessage() );
      } finally {
        // TDLog.Log( TDLog.LOG_COMM, "cancel Rfcomm thread: nulling thread");
        mRfcommThread = null;
      }
    } else {
      // TDLog.Log( TDLog.LOG_COMM, "cancel Rfcomm thread: no thread");
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

  protected boolean startRfcommThread( int to_read, Handler /* ILister */ lister ) 
  {
    return false;
  }

  void disconnectRemoteDevice( )
  {
    // TDLog.Log( TDLog.LOG_COMM, "disconnect remote device ");
    cancelRfcommThread();
    closeProtocol();
  }

  protected boolean checkRfcommThreadNull( String msg ) { return ( mRfcommThread == null ); }

  protected boolean sendCommand( int cmd )
  {
    // TDLog.Log( TDLog.LOG_COMM, "VD comm send cmd " + cmd );
    boolean ret = false;
    if ( mProtocol != null ) {
      for (int k=0; k<3 && ! ret; ++k ) { // try three times
        ret |= mProtocol.sendCommand( (byte)cmd ); 
        // TDLog.Log( TDLog.LOG_COMM, "sendCommand " + cmd + " " + k + "-ret " + ret );
        try {
          Thread.sleep( TDSetting.mWaitCommand );
        } catch ( InterruptedException e ) {
        }
      }
    }
    return ret;
  }

  void setX310Laser( String address, int what, Handler /* ILister */ lister ) { }

  boolean toggleCalibMode( String address, int type ) { return false; }

  boolean writeCoeff( String address, byte[] coeff ) { return false; }

  boolean readCoeff( String address, byte[] coeff ) { return false; }

  String readHeadTail( String address, byte[] command, int[] head_tail ) { return null; }
  
  int readX310Memory( String address, int from, int to, List< MemoryOctet > memory ) { return -1; }

  int readA3Memory( String address, int from, int to, List< MemoryOctet > memory ) { return -1; }

  byte[] readMemory( String address, int addr ) { return null; }

  int swapHotBit( String address, int from, int to ) { return -1; }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  boolean connectDevice( String address, Handler /* ILister */ lister )
  {
    return false;
   }

  void disconnect() { }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  int downloadData( String address, Handler /* ILister */ lister )
  {
    return -1;
  }

  // ====================================================================================
  // FIRMWARE

  int dumpFirmware( String address, String filepath ) { return 0; }

  int uploadFirmware( String address, String filepath ) { return 0; }

};
