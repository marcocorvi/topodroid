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
package com.topodroid.inport;

import com.topodroid.utils.TDLog;

import com.topodroid.DistoX.SurveyInfo;
import com.topodroid.DistoX.DataHelper;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.MainWindow;


// import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.io.InputStreamReader;

public class ImportVisualTopoTask extends ImportTask
{
  private boolean mLrud;
  private boolean mLegFirst;
  private boolean mTrox;

  public ImportVisualTopoTask( MainWindow main, InputStreamReader isr, ImportData data )
  {
    super( main, isr );
    mLrud     = data.mLrud;
    mLegFirst = data.mLeg;
    mTrox     = data.mTrox;
    // TDLog.v("Import VTopo " + mTrox );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ImportParser parser = null;
      if ( mTrox ) {
        parser = new ParserVisualTopoX( isr, str[0], true, mLrud, mLegFirst ); // apply_declination = true
      } else {
        parser = new ParserVisualTopo( isr, str[0], true, mLrud, mLegFirst ); // apply_declination = true
      }
      if ( ! parser.isValid() ) {
        TDLog.v("invalid parser");
        return -2L;
      }
      if ( mApp.get() == null ) return -1L;
      if ( hasSurveyName( parser.mName ) ) {
        TDLog.v("survey " + parser.mName + " already present");
        return -1L;
      }

      sid = mApp.get().setSurveyFromName( parser.mName, SurveyInfo.DATAMODE_NORMAL, false );

      DataHelper app_data = TopoDroidApp.mData;
      // app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
      // app_data.updateSurveyDeclination( sid, parser.mDeclination );
      // app_data.updateSurveyInitStation( sid, parser.initStation() );
      updateSurveyMetadata( sid, parser );

      ArrayList< ParserShot > shots  = parser.getShots();
      long id = insertImportShots( sid, 1, shots ); // start id = 1

      ArrayList< ParserShot > splays = parser.getSplays();
      insertImportShots( sid, id, splays );
    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }

}
