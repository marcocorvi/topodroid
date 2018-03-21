/* @file ImportZipTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid ZIP import task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.ArrayList;

// import android.widget.Toast;
   
class ImportZipTask extends ImportTask
{

  ImportZipTask( MainWindow main ) { super(main); }

  @Override
  protected Long doInBackground( String... str )
  {
    String filename = str[0];
    Archiver archiver = new Archiver( mApp );

    int ret = archiver.unArchive( TDPath.getZipFile( filename ), filename.replace(".zip", ""));
    return (long)ret;
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPostExecute(Long result) {
    mMain.setTheTitle( );
    mProgress.dismiss();
    if ( result < -5 ) {
      TDToast.make( mMain, R.string.unzip_fail );
    } else if ( result == -5 ) {
      TDToast.make( mMain, R.string.unzip_fail_sqlite );
    } else if ( result == -4 ) {
      TDToast.make( mMain, R.string.unzip_fail_survey );
    } else if ( result == -3 ) {
      TDToast.make( mMain, R.string.unzip_fail_db );
    } else if ( result == -2 ) {
      TDToast.make( mMain, R.string.unzip_fail_td );
    } else if ( result == -1 ) {
      TDToast.make( mMain, R.string.import_already );
    } else {
      mMain.updateDisplay( );
      TDToast.make( mMain, R.string.import_zip_ok );
    }
  }
}

