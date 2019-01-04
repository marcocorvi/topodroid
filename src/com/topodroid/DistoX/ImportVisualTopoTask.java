/* @file ImportVisualTopoTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid VisualTopo import task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.lang.ref.WeakReference;

import java.util.ArrayList;


class ImportVisualTopoTask extends ImportTask
{

  ImportVisualTopoTask( MainWindow main )
  {
    super( main );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ParserVisualTopo parser = new ParserVisualTopo( str[0], true ); // apply_declination = true
      if ( mApp.get() == null ) return -1L;
      DataHelper app_data = TopoDroidApp.mData;
      if ( app_data.hasSurveyName( parser.mName ) ) {
        return -1L;
      }

      sid = mApp.get().setSurveyFromName( parser.mName, SurveyInfo.DATAMODE_NORMAL, false, false );
      app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
      app_data.updateSurveyDeclination( sid, parser.mDeclination, false );
      app_data.updateSurveyInitStation( sid, parser.initStation(), false );

      ArrayList< ParserShot > shots  = parser.getShots();
      long id = app_data.insertShots( sid, 1, shots ); // start id = 1

      ArrayList< ParserShot > splays = parser.getSplays();
      app_data.insertShots( sid, id, splays );
    } catch ( ParserException e ) {
      // TDToast.make(mActivity, R.string.file_parse_fail );
    }
    return sid;
  }

}
