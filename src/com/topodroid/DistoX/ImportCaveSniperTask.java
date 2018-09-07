/* @file ImportCaveSniperTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid CaveSniper import task (text file only)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
 
class ImportCaveSniperTask extends ImportTask
{
  ImportCaveSniperTask( MainWindow main )
  {
    super( main );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      DataHelper app_data = TopoDroidApp.mData;
      ParserCaveSniper parser = new ParserCaveSniper( str[0] ); 
      ArrayList< ParserShot > shots  = parser.getShots();
      ArrayList< ParserShot > splays = parser.getSplays();
      if ( app_data.hasSurveyName( parser.mName ) ) {
        return -1L;
      }
      sid = mApp.setSurveyFromName( parser.mName, SurveyInfo.DATAMODE_NORMAL, false, false ); // IMPORT CaveSniper no update, no forward
      app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
      // app_data.updateSurveyDeclination( sid, parser.mDeclination, false );
      app_data.updateSurveyInitStation( sid, parser.initStation(), false );

      long id = app_data.insertShots( sid, 1, shots ); // start id = 1
      // app_data.insertShots( sid, id, splays );
    } catch ( ParserException e ) {
      // TDToast.make(mActivity, R.string.file_parse_fail );
    }
    return sid;
  }
}
