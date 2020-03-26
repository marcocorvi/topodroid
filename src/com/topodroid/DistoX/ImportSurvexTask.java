/* @file ImportSurvexTask.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid Survex import task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

// import java.lang.ref.WeakReference;

import java.util.ArrayList;

  
// NOTE survey name must be guaranteed not be in the db
class ImportSurvexTask extends ImportTask
{
  ImportSurvexTask( MainWindow main )
  {
    super( main );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      DataHelper app_data = TopoDroidApp.mData;
      ParserSurvex parser = new ParserSurvex( str[0], true ); // apply_declination = true
      if ( ! parser.isValid() ) return -2L;
      if ( mApp.get() == null ) return -1L;

      sid = mApp.get().setSurveyFromName( parser.mName, SurveyInfo.DATAMODE_NORMAL, false );
      app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
      app_data.updateSurveyDeclination( sid, parser.surveyDeclination() );
      app_data.updateSurveyInitStation( sid, parser.initStation() );

      ArrayList< ParserShot > shots  = parser.getShots();
      long id = app_data.insertImportShots( sid, 1, shots ); // start id = 1

      ArrayList< ParserShot > splays = parser.getSplays();
      app_data.insertImportShots( sid, id, splays );

      // FIXME this suppose CS long-lat, ie, e==long, n==lat
      // WorldMagneticModel wmm = new WorldMagneticModel( mApp.get() );
      // ArrayList< ParserSurvex.Fix > fixes = parser.getFixes();
      // for ( ParserSurvex.Fix fix : fixes ) {
      //   // double asl = fix.z;
      //   double alt = wmm.geoidToEllipsoid( fix.n, fix.e, fix.z );
      //   app_data.insertFixed( sid, -1L, fix.name, fix.e, fix.n, alt, fix.z, "", 0 );
      // }

      // ArrayList< ParserSurvex.Station > stations = parser.getStations();
      // for ( ParserSurvex.Station st : stations ) {
      //   app_data.insertStation( sid, st.name, st.comment, st.flag );
      // }
    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }

}

