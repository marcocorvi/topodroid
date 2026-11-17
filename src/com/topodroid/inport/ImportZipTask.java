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

import com.topodroid.util.TDsafUri;
import com.topodroid.util.TDLog;
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

import java.io.File;

import android.net.Uri;

public class ImportZipTask extends ImportTask
{
  boolean mForce;
  Uri mUri;
  File mDelete = null; // file to delete if the import fails
  boolean mCheck = false; // whether the manifest is checked first (as for a ZIP file picked by the user)
  static final long MANIFEST_BASE = -100L; // a failed manifest check is returned as MANIFEST_BASE plus its (negative) code

  public ImportZipTask( MainWindow main, Uri uri, boolean force )
  { 
    super(main );
    mUri = uri;
    mForce = force;
  }

  /** delete a file if the import fails (eg, the file has been downloaded only to be imported)
   * @param file   file
   * @return this task
   */
  public ImportZipTask setDeleteIfFail( File file )
  {
    mDelete = file;
    return this;
  }

  /** check the manifest of the archive before un-archiving it: the un-archive from a stream relies on the check, which sets
   * the name of the survey. A refusal of the manifest is shown with its own message, as for a ZIP file picked by the user
   * @return this task
   */
  public ImportZipTask setCheckManifest()
  {
    mCheck = true;
    return this;
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
      if ( mCheck ) {
        int manifest_ok = Archiver.getOkManifest( app, TDsafUri.docFileInputStream( pfd ) );
        TDsafUri.closeFileDescriptor( pfd );
        if ( manifest_ok < 0 ) return MANIFEST_BASE + manifest_ok;
        pfd = TDsafUri.docReadFileDescriptor( mUri );
        if ( pfd == null ) return ret;
      }
      try {
        fis = TDsafUri.docFileInputStream( pfd ); // super.fis
        ret = (long)Archiver.unArchive( app, fis );
        fis.close(); 
      } catch ( IOException e ) {
        TDLog.e("Error " + e.getMessage() );
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
    if ( result < 0 && mDelete != null ) mDelete.delete();
    if ( mMain.get() != null && ! mMain.get().isFinishing() ) {
      mMain.get().setTheTitle( );
      if ( result <= MANIFEST_BASE ) {
        mMain.get().toastBadManifest( (int)( result - MANIFEST_BASE ) );
      } else if ( result < -5 ) {
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
          TDToast.makeWarn( mCheck? R.string.archive_reset_color : R.string.import_zip_newer );
        } else { // result == 0
          TDToast.make( R.string.import_zip_ok );
        }
      }
    }
  }
}

