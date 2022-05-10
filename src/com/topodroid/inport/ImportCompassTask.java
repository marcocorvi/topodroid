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
package com.topodroid.inport;

import com.topodroid.TDX.MainWindow;
import com.topodroid.TDX.SurveyInfo;

// import java.lang.ref.WeakReference;

import java.util.ArrayList;

import java.io.InputStreamReader;
 
public class ImportCompassTask extends ImportTask
{
  private int mDatamode;
  private boolean mLrud;
  private boolean mLegFirst;

  public ImportCompassTask( MainWindow main, InputStreamReader isr, ImportData data )
  {
    super( main, isr );
    mDatamode = data.mDatamode; 
    mLrud     = data.mLrud;
    mLegFirst = data.mLeg;
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ParserCompass parser = new ParserCompass( isr, str[0], true, mLrud, mLegFirst ); // apply_declination = true
      if ( ! parser.isValid() ) return -2L;
      if ( mApp.get() == null ) return -1L;
      if ( hasSurveyName( parser.mName ) ) {
        return -1L;
      }
      sid = mApp.get().setSurveyFromName( parser.mName, mDatamode, false ); // IMPORT DAT no update

      // app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle ); 
      // app_data.updateSurveyDeclination( sid, parser.mDeclination ); 
      // app_data.updateSurveyInitStation( sid, parser.initStation() );
      updateSurveyMetadata( sid, parser );

      long id = 1; // start id = 1
      ArrayList< ParserShot > shots  = parser.getShots();
      if ( mDatamode == SurveyInfo.DATAMODE_NORMAL ) {
        id = insertImportShots( sid, id, shots );
      } else { // SurveyInfo.DATAMODE_DIVING
        id = insertImportShotsDiving( sid, id, shots );
      }
      ArrayList< ParserShot > splays = parser.getSplays();
      insertImportShots( sid, id, splays );
    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }
}
