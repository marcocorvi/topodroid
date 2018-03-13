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

import android.widget.Toast;
   
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
    mMain.updateDisplay( );
    if ( result < -5 ) {
      Toast.makeText( mMain, R.string.unzip_fail, Toast.LENGTH_SHORT).show();
    } else if ( result == -5 ) {
      Toast.makeText( mMain, R.string.unzip_fail_sqlite, Toast.LENGTH_SHORT).show();
    } else if ( result == -4 ) {
      Toast.makeText( mMain, R.string.unzip_fail_survey, Toast.LENGTH_SHORT).show();
    } else if ( result == -3 ) {
      Toast.makeText( mMain, R.string.unzip_fail_db, Toast.LENGTH_SHORT).show();
    } else if ( result == -2 ) {
      Toast.makeText( mMain, R.string.unzip_fail_td, Toast.LENGTH_SHORT).show();
    } else if ( result == -1 ) {
      Toast.makeText( mMain, R.string.import_already, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( mMain, R.string.import_zip_ok, Toast.LENGTH_SHORT).show();
    }
  }
}

