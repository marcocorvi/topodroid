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
package com.topodroid.inport;

import com.topodroid.Cave3X.MainWindow;
import com.topodroid.Cave3X.SurveyInfo;

// import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.io.InputStreamReader;
 
public class ImportCaveSniperTask extends ImportTask
{
  public ImportCaveSniperTask( MainWindow main, InputStreamReader isr )
  {
    super( main, isr );
  }

  @Override
  protected Long doInBackground( String... str )
  {
    long sid = 0;
    try {
      ParserCaveSniper parser = new ParserCaveSniper( isr, str[0] ); 
      if ( ! parser.isValid() ) return -2L;
      if ( mApp.get() == null ) return -1L;
      if ( hasSurveyName( parser.mName ) ) {
        return -1L;
      }
      sid = mApp.get().setSurveyFromName( parser.mName, SurveyInfo.DATAMODE_NORMAL, false ); // IMPORT CaveSniper no update

      // app_data.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
      // // app_data.updateSurveyDeclination( sid, parser.mDeclination );
      // app_data.updateSurveyInitStation( sid, parser.initStation() );
      updateSurveyMetadata( sid, parser );

      ArrayList< ParserShot > shots  = parser.getShots();
      // ArrayList< ParserShot > splays = parser.getSplays();
      long id = insertImportShots( sid, 1, shots ); // start id = 1
    } catch ( ParserException e ) {
      // TDToast.makeBad( R.string.file_parse_fail );
    }
    return sid;
  }
}
