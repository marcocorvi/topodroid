/* @file ImportTherionTask.java
 *
 * @author marco corvi
 * @date march 2017
 *
 * @brief TopoDroid Therion import task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

  
// NOTE survey name must be guaranteed not be in the db
class ImportTherionTask extends ImportTask
{
  ImportTherionTask( MainWindow main )
  {
    super( main );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ParserTherion parser = new ParserTherion( str[0], true ); // apply_declination = true
      ArrayList< ParserShot > shots  = parser.getShots();
      ArrayList< ParserShot > splays = parser.getSplays();
      ArrayList< ParserTherion.Station > stations = parser.getStations();
      ArrayList< ParserTherion.Fix > fixes = parser.getFixes();

      sid = mApp.setSurveyFromName( str[1], false ); // IMPORT TH no forward
      mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle, false );
      mApp.mData.updateSurveyDeclination( sid, parser.mDeclination, false );
      mApp.mData.updateSurveyInitStation( sid, parser.initStation(), false );

      long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
      mApp.mData.insertShots( sid, id, splays );

      // FIXME this suppose CS long-lat, ie, e==long, n==lat
      // WorldMagneticModel wmm = new WorldMagneticModel( mApp );
      // for ( ParserTherion.Fix fix : fixes ) {
      //   // double asl = fix.z;
      //   double alt = wmm.geoidToEllipsoid( fix.n, fix.e, fix.z );
      //   mApp.mData.insertFixed( sid, -1L, fix.name, fix.e, fix.n, alt, fix.z, "", 0 );
      // }

      for ( ParserTherion.Station st : stations ) {
        mApp.mData.insertStation( sid, st.name, st.comment, st.flag );
      }
    } catch ( ParserException e ) {
      // Toast.makeText(mActivity, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
    }
    return sid;
  }

}

