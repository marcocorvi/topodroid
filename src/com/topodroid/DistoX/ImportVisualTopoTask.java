/* @file ImportVisualTopoTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid VisualTopo import task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

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
      ArrayList< ParserShot > shots  = parser.getShots();
      ArrayList< ParserShot > splays = parser.getSplays();
      if ( mApp.mData.hasSurveyName( parser.mName ) ) {
        return -1L;
      }

      sid = mApp.setSurveyFromName( parser.mName, false );
      mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
      mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );
      mApp.mData.updateSurveyInitStation( sid, parser.initStation(), false );

      long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
      mApp.mData.insertShots( sid, id, splays );
    } catch ( ParserException e ) {
      // Toast.makeText(mActivity, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
    }
    return sid;
  }

}
