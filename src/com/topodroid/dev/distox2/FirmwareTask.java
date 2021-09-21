/* @file FirmwareTask.java
 *
 * @author marco corvi
 * @date nov 2020
 *
 * @brief TopoDroid data-download stop task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox2;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
// import com.topodroid.dev.distox2.DistoX310Comm;
import com.topodroid.Cave3X.TDToast;
import com.topodroid.Cave3X.TDInstance;
// import com.topodroid.Cave3X.TDPath;
import com.topodroid.Cave3X.R;

// import java.lang.ref.WeakReference;

import java.io.File;

import android.os.AsyncTask;
// import android.os.Handler;

                                  //  args        result
public class FirmwareTask extends AsyncTask< Void, Void, Integer >
{
  public static final int FIRMWARE_READ  = 0;
  public static final int FIRMWARE_WRITE = 1;

  // private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  private final DistoX310Comm mComm;
  private final int mMode;
  private long  mLength = 0;
  private final String mFilename;
  private static FirmwareTask running = null;
 

  /**
   * @param comm      communication class
   * @param mode      task mode
   * @param filename  file name
   */
  public FirmwareTask( /* TopoDroidApp app, */ DistoX310Comm comm, int mode, String filename )
  {
    // TDLog.Error( "Data Download Task cstr" );
    // TDLog.v( "data download task cstr");
    // mApp  = new WeakReference<TopoDroidApp>( app );
    mComm = comm;
    mMode = mode;
    mFilename = filename;
  }

  private int dumpFirmware( )
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return -1;
    // return mComm.dumpFirmware( TDInstance.deviceAddress(), TDPath.getBinFile( mFilename ) );
    TDLog.v( "task dump file " + mFilename );
    return mComm.dumpFirmware( TDInstance.deviceAddress(), TDFile.getExternalFile( "bin", mFilename ) );
  }

  private int uploadFirmware( )
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) {
      TDLog.Error( "Comm or Device null");
      return -1;
    }
    // String pathname = TDPath.getBinFile( mFilename );
    // mLength = (new File( pathname )).length(); // file must exists
    File file = TDFile.getExternalFile( "bin", mFilename );
    mLength = file.length(); // file must exists
    TDLog.v( "task upload file " + mFilename + " length " + mLength );
    // TDLog.LogFile( "Firmware upload address " + TDInstance.deviceAddress() );
    // if ( ! pathname.endsWith( "bin" ) ) {
    //   TDLog.LogFile( "Firmware upload file does not end with \"bin\"");
    //   return 0;
    // }
    // return mComm.uploadFirmware( TDInstance.deviceAddress(), pathname );
    return mComm.uploadFirmware( TDInstance.deviceAddress(), file );
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( Void... args )
  {
    if ( ! lock() ) return -2;
    int ret = -1;
    if ( mMode == FIRMWARE_READ  ) return dumpFirmware( );
    if ( mMode == FIRMWARE_WRITE ) return uploadFirmware( );
    unlock();
    return -3;
  }

  // @Override
  // protected void onProgressUpdate( Void... values)
  // {
  //   super.onProgressUpdate( values );
  //   // TDLog.Log( TDLog.LOG_COMM, "onProgressUpdate " + values );
  // }

  @Override
  protected void onPostExecute( Integer res )
  {
    int ret = res.intValue();
    if ( mMode == FIRMWARE_READ ) {
      // TDLog.LogFile( "Firmware dump to " + mFilename + " result: " + ret );
      TDLog.v( "Task Firmware dump to " + mFilename + " result: " + ret );
      if ( ret > 0 ) { 
        TDToast.makeLong( String.format( TDInstance.getResources().getString(R.string.firmware_file_dumped), mFilename, ret ) );
      }
    } else if ( mMode == FIRMWARE_WRITE ) {
      // TDLog.LogFile( "Firmware upload result: written " + ret + " bytes of " + mLength );
      TDLog.v( "Task Firmware upload result: written " + ret + " bytes of " + mLength );
      TDToast.makeLong( String.format( TDInstance.getResources().getString(R.string.firmware_file_uploaded), mFilename, ret, mLength ) );
    }
  }

  private synchronized boolean lock()
  {
    if ( running != null ) return false;
    running = this;
    return true;
  }

  private synchronized void unlock()
  {
    if ( running == this ) running = null;
  }

}
