/** @file TdmFile.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TdmFile represents a cave project stored as a file
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import android.util.Log;

class TdmFile 
{
  private String mName;                // filename without extension (for display purposes) or surveyname (TD database)
  private String mFilepath;            // tdconfig file (fullpath) or null

  public TdmFile( String filepath, String surveyname )
  {
    mFilepath = filepath;
    if ( surveyname == null ) { // get name from file
      int pos = mFilepath.lastIndexOf('/');
      mName = ( pos >= 0 )? mFilepath.substring( pos+1 ) : mFilepath;
      mName = mName.replace(".tdconfig", "");
    } else {
      mName = surveyname;
    }
    Log.v("DistoX", "TdmFile: name <" + mName + "> file <" + mFilepath + ">" );
  }

  public String getFilepath() { return mFilepath; }

  public String getSurveyName() { return mName; }

  public String toString() { return mName; }

}
