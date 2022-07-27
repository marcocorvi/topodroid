/* @file ImportZipTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid ZIP import task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDLog;
import com.topodroid.TDX.R;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.Archiver;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.MainWindow;


// import java.lang.ref.WeakReference;

// import java.util.ArrayList;

// import java.io.InputStream;
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.ParcelFileDescriptor;

import android.net.Uri;

public class ImportZipTask extends ImportTask
{
  boolean mForce;
  Uri mUri;

  public ImportZipTask( MainWindow main, Uri uri, boolean force )
  { 
    super(main );
    mUri = uri;
    mForce = force;
  }

  @Override
  protected Long doInBackground( String... str )
  {

    String filename = str[0];
    TopoDroidApp app = mApp.get();
    if ( app == null ) return -7L;
    // String name = filename.replace(".zip", "");

    ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( mUri );
    if ( pfd == null ) {
      Archiver archiver = new Archiver( );
      return (long)archiver.unArchive( app, TDPath.getZipFile( filename ), /* name, */ mForce );
    } else {
      long ret = -1L;
      try {
        fis = TDsafUri.docFileInputStream( pfd ); // super.fis
        ret = (long)Archiver.unArchive( app, fis );
        fis.close(); 
      } catch ( IOException e ) {
        TDLog.Error("Error " + e.getMessage() );
      } finally {
        TDsafUri.closeFileDescriptor( pfd );
      }
      return ret;
    }
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPostExecute(Long result)
  {
    mProgress.dismiss();
    if ( mMain.get() != null && ! mMain.get().isFinishing() ) {
      mMain.get().setTheTitle( );
      if ( result < -5 ) {
        TDToast.makeBad( R.string.unzip_fail );
      } else if ( result == -5 ) {
        TDToast.makeBad( R.string.unzip_fail_sqlite );
      } else if ( result == -4 ) {
        TDToast.makeBad( R.string.unzip_fail_survey );
      } else if ( result == -3 ) {
        TDToast.makeBad( R.string.unzip_fail_db );
      } else if ( result == -2 ) {
        TDToast.makeBad( R.string.unzip_fail_td );
      } else if ( result == -1 ) {
        TDToast.makeBad( R.string.import_already );
      } else {
        mMain.get().updateDisplay( );
        if ( result == 1 ) {
          TDToast.makeWarn( R.string.import_zip_newer );
        } else { // result == 0
          TDToast.make( R.string.import_zip_ok );
        }
      }
    }
  }
}

