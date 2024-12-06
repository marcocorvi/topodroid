/* @file ImportCavwayCsvTask.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid Cavway CSV import task (text file only)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.utils.TDLog;

import com.topodroid.TDX.MainWindow;
import com.topodroid.TDX.SurveyInfo;

import android.os.ParcelFileDescriptor;

// import java.lang.ref.WeakReference;

import java.util.ArrayList;
// import java.io.InputStreamReader;
 
public class ImportCavwayCsvTask extends ImportTask
{
  // public ImportCavwayCsvTask( MainWindow main, InputStreamReader isr )
  // {
  //   super( main, isr );
  // }

  public ImportCavwayCsvTask( MainWindow main, ParcelFileDescriptor pfd )
  {
    super( main, pfd );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    TDLog.v("IMPORT Cavway task: " + str[0] );
    long sid = 0;
    try {
      ParserCavwayCsv parser = new ParserCavwayCsv( isr, str[0] ); 
      if ( ! parser.isValid() ) return -2L;
      if ( mApp.get() == null ) return -1L;
      if ( hasSurveyName( parser.mName ) ) {
        return -1L;
      }
      sid = mApp.get().setSurveyFromName( parser.mName, SurveyInfo.DATAMODE_NORMAL, false ); // IMPORT CaveSniper no update

      // app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
      // // app_data.updateSurveyDeclination( sid, parser.mDeclination );
      // app_data.updateSurveyInitStation( sid, parser.initStation() );
      updateSurveyMetadata( sid, parser );

      ArrayList< ParserShot > shots  = parser.getShots();
      // ArrayList< ParserShot > splays = parser.getSplays();
      long id = insertImportShots( sid, 1, shots ); // start id = 1
    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }
}
