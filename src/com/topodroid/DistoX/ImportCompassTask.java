/* @file ImportCompassTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Compass import task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

// import java.lang.ref.WeakReference;

import java.util.ArrayList;
 
class ImportCompassTask extends ImportTask
{
  private int mDatamode;
  private boolean mLrud;
  private boolean mLegFirst;

  ImportCompassTask( MainWindow main, int datamode, boolean lrud, boolean leg_first )
  {
    super( main );
    mDatamode = datamode; 
    mLrud     = lrud;
    mLegFirst = leg_first;
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ParserCompass parser = new ParserCompass( str[0], true, mLrud, mLegFirst ); // apply_declination = true
      if ( ! parser.isValid() ) return -2L;
      if ( mApp.get() == null ) return -1L;
      DataHelper app_data = TopoDroidApp.mData;
      if ( app_data.hasSurveyName( parser.mName ) ) {
        return -1L;
      }
      sid = mApp.get().setSurveyFromName( parser.mName, mDatamode, false ); // IMPORT DAT no update
      app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
      app_data.updateSurveyDeclination( sid, parser.mDeclination );
      app_data.updateSurveyInitStation( sid, parser.initStation() );

      long id = 1; // start id = 1
      ArrayList< ParserShot > shots  = parser.getShots();
      if ( mDatamode == SurveyInfo.DATAMODE_NORMAL ) {
        id = app_data.insertImportShots( sid, id, shots );
      } else { // SurveyInfo.DATAMODE_DIVING
        id = app_data.insertImportShotsDiving( sid, id, shots );
      }
      ArrayList< ParserShot > splays = parser.getSplays();
      app_data.insertImportShots( sid, id, splays );
    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }
}
