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

// import java.lang.ref.WeakReference;

import java.util.ArrayList;
 
class ImportCompassTask extends ImportTask
{
  private int mDatamode;

  ImportCompassTask( MainWindow main )
  {
    super( main );
    mDatamode = TDSetting.mImportDatamode;
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ParserCompass parser = new ParserCompass( str[0], true ); // apply_declination = true
      if ( mApp.get() == null ) return -1L;
      DataHelper app_data = TopoDroidApp.mData;
      if ( app_data.hasSurveyName( parser.mName ) ) {
        return -1L;
      }
      sid = mApp.get().setSurveyFromName( parser.mName, mDatamode, false, false ); // IMPORT DAT no update, no forward
      app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
      app_data.updateSurveyDeclination( sid, parser.mDeclination, false );
      app_data.updateSurveyInitStation( sid, parser.initStation(), false );

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
