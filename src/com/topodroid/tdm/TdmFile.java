/** @file TdmFile.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TdmFile represents a cave project stored as a file
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;

class TdmFile 
{
  private String mName;                // filename without extension (for display purposes) or surveyname (TD database)
  private String mFilepath;            // tdconfig file (fullpath) or null

  /** cstr
   * @param filepath   file full pathname
   * @param surveyname presentation name: survey name or filename
   */
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
    TDLog.v( "TdmFile: name <" + mName + "> file <" + mFilepath + ">" );
  }

  /** @return true is this TDM file has a specified filepath
   * @param filepath  file pathname
   */
  public boolean hasFilepath( String filepath ) { return mFilepath.equals( filepath ); }

  /** @return the file full pathname
   */
  public String getFilepath() { return mFilepath; }

  /** @return the survey name
   */
  public String getSurveyName() { return mName; }

  /** @return the string presentation (ie, survey name)
   */
  public String toString() { return mName; }

}
