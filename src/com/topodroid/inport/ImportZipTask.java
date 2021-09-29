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

import com.topodroid.DistoX.R;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.Archiver;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.MainWindow;


// import java.lang.ref.WeakReference;

// import java.util.ArrayList;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImportZipTask extends ImportTask
{
  boolean mForce;

  public ImportZipTask( MainWindow main, InputStream fis, boolean force )
  { 
    super(main, fis );
    mForce = force;
  }

  @Override
  protected Long doInBackground( String... str )
  {
    String filename = str[0];
    TopoDroidApp app = mApp.get();
    if ( app == null ) return -7L;
    String name = filename.replace(".zip", "");
    if ( fis == null ) {
      // try { 
      //   fis = new FileInputStream( TDPath.getZipFile( filename ) );
      // } catch ( FileNotFoundException e ) { }
      // if ( fis == null ) return -6L;
      Archiver archiver = new Archiver( );
      return (long)archiver.unArchive( app, TDPath.getZipFile( filename ), name, mForce );
    }
    return (long)Archiver.unArchive( app, fis, name );
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

