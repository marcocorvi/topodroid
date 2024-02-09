/* @file ImportTRobotTask.java
 *
 * @author marco corvi
 * @date feb 2024
 *
 * @brief TopoDroid TopoRobot import task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.DataHelper;
import com.topodroid.TDX.SurveyInfo;
import com.topodroid.TDX.MainWindow;
import com.topodroid.TDX.TDPath;

import com.topodroid.mag.WorldMagneticModel;

import android.os.ParcelFileDescriptor;

// import java.lang.ref.WeakReference;

import java.util.ArrayList;

// import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
  
// NOTE survey name must be guaranteed not be in the db
public class ImportTRobotTask extends ImportTask
{
  // /** cstr 
  //  * @param main   main application window
  //  * @param isr    input reader
  //  */
  // public ImportTRobotTask( MainWindow main, InputStreamReader isr )
  // {
  //   super( main, isr );
  // }

  /** cstr 
   * @param main   main application window
   * @param pfd    parcel file descriptor
   */
  public ImportTRobotTask( MainWindow main, ParcelFileDescriptor pfd )
  {
    super( main, pfd );
  }

  /** execute import task
   * @param str params: str[0] survey name
   */
  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ParserTopoRobot parser = new ParserTopoRobot( isr, str[0], false ); // apply_declination = false
      if ( ! parser.isValid() ) return -2L;
      if ( mApp.get() == null ) return -1L;
      String survey = parser.mName;
      if ( hasSurveyName( survey ) ) {
        return -1L;
      }

      sid = mApp.get().setSurveyFromName( survey, SurveyInfo.DATAMODE_NORMAL, false );

      DataHelper app_data = TopoDroidApp.mData;
      app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
      app_data.updateSurveyDeclination( sid, parser.mDeclination );
      // app_data.updateSurveyInitStation( sid, parser.initStation() );
      if ( parser.mTeam != null ) {
        app_data.updateSurveyTeam( sid, parser.mTeam );
      }
      // updateSurveyMetadata( sid, parser );

      ArrayList< ParserShot > shots  = parser.getShots();
      long id = insertImportShots( sid, 1, shots ); // start id = 1

      ArrayList< ParserShot > splays = parser.getSplays();
      app_data.insertImportShots( sid, id, splays );

      ParserTopoRobot.TRobotFix fix = parser.fix;
      // runOnUiThread( new Runnable() {
        if ( fix != null ) {
          WorldMagneticModel wmm = new WorldMagneticModel( mApp.get() );
          double h_ell = wmm.geoidToEllipsoid( fix.lat, fix.lng, fix.alt ); 
          app_data.insertFixed( sid, -1L, fix.station, fix.lng, fix.lat, h_ell, fix.alt, "", 0, 0, -1, -1 ); // status=0 source=0 (unknown) accur=-1,-1 (undef)
        }

        try {
          String notepath = TDPath.getNoteTRobotFile( survey );
          BufferedWriter bw = TDFile.getTopoDroidFileWriter( notepath );
          if ( bw != null ) {
            PrintWriter pw = new PrintWriter( bw );
            parser.writeAnnotation( pw );
            pw.flush();
            pw.close();
          }
        } catch ( IOException e ) {
          TDLog.e("TR parser task error " + e.getMessage() );
        }
      // } );

    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }

}

