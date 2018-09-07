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
      DataHelper app_data = TopoDroidApp.mData;
      ParserCompass parser = new ParserCompass( str[0], true ); // apply_declination = true
      ArrayList< ParserShot > shots  = parser.getShots();
      ArrayList< ParserShot > splays = parser.getSplays();
      if ( app_data.hasSurveyName( parser.mName ) ) {
        return -1L;
      }
      sid = mApp.setSurveyFromName( parser.mName, mDatamode, false, false ); // IMPORT DAT no update, no forward
      app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
      app_data.updateSurveyDeclination( sid, parser.mDeclination, false );
      app_data.updateSurveyInitStation( sid, parser.initStation(), false );

      long id = 1; // start id = 1
      if ( mDatamode == SurveyInfo.DATAMODE_NORMAL ) {
        id = app_data.insertShots( sid, id, shots );
      } else { // SurveyInfo.DATAMODE_DIVING
        id = app_data.insertShotsDiving( sid, id, shots );
      }
      app_data.insertShots( sid, id, splays );
    } catch ( ParserException e ) {
      // TDToast.make(mActivity, R.string.file_parse_fail );
    }
    return sid;
  }
}
