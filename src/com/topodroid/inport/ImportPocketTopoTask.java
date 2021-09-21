/* @file ImportPocketTopoTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid PocketTopo import task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.utils.TDLog;
import com.topodroid.Cave3X.SurveyInfo;
import com.topodroid.Cave3X.MainWindow;


// import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.io.InputStream;

public class ImportPocketTopoTask extends ImportTask
{
  public ImportPocketTopoTask( MainWindow main, InputStream fis ) 
  {
    super( main, fis );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      TDLog.v( "import PocketTopo: survey " + str[1] );
      // import PocketTopo (only data for the first trip)
      ParserPocketTopo parser = new ParserPocketTopo( fis, str[0], str[1], true ); // apply_declination = true
      if ( ! parser.isValid() ) return -2L;
      if ( mApp.get() == null ) return -1L;
      if ( hasSurveyName( parser.mName ) ) {
        return -1L;
      }

      sid = mApp.get().setSurveyFromName( parser.mName, SurveyInfo.DATAMODE_NORMAL, false ); // no update

      // app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
      // app_data.updateSurveyDeclination( sid, parser.mDeclination );
      // app_data.updateSurveyInitStation( sid, parser.initStation() );
      updateSurveyMetadata( sid, parser );

      ArrayList< ParserShot > shots  = parser.getShots();
      long id = insertImportShots( sid, 1, shots ); // start id = 1
      TDLog.Log( TDLog.LOG_PTOPO, "SID " + sid + " inserted shots. return " + id );

      if ( parser.mStartFrom != null ) {
        mApp.get().insert2dPlot( sid, "1", parser.mStartFrom, true, 0 ); // true = plan-extended plot, 0 = proj_dir
      }

      // DBlock blk = mApp.get().mData.selectShot( 1, sid );
      // String plan = parser.mOutline;
      // String extended = parser.mSideview;
      // if ( blk != null /* && plan != null || extended != null */ ) {
      //   // insert plot in DB
      //   // long pid = 
      //     mApp.get().insert2dPlot( sid, "1", blk.mFrom );

      //   if ( plan == null ) plan = "";
      //   if ( extended == null ) extended = "";
      //   TDLog.Log( TDLog.LOG_PTOPO, "SID " + sid + " scraps " + plan.length() + " " + extended.length() );
      //   try {
      //     FIXME tdr vs. th2
      //     String filename1 = TDPath.getTh2File( parser.mName + "-1p.th2" );
      //     TDPath.checkPath( filename1 );
      //     FileWriter fw1 = new FileWriter( filename1 );
      //     PrintWriter pw1 = new PrintWriter( fw1 );
      //     pw1.format("%s", plan );
      //     
      //     String filename2 = TDPath.getTh2File( parser.mName + "-1s.th2" );
      //     TDPath.checkPath( filename2 );
      //     FileWriter fw2 = new FileWriter( filename2 );
      //     PrintWriter pw2 = new PrintWriter( fw2 );
      //     pw2.format("%s", extended );

      //   } catch ( IOException e ) {
      //     TDLog.Error( "SID " + sid + " scraps IO error " + e );
      //   }
      // }
      
    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }
}

