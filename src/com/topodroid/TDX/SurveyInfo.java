/* @file SurveyInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey info (name, date, comment etc)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;


import android.widget.EditText;

public class SurveyInfo
{
  final static int XSECTION_SHARED  = 0;
  final static int XSECTION_PRIVATE = 1;

  public final static int DATAMODE_NORMAL  = 0;
  public final static int DATAMODE_DIVING  = 1;

  public final static int SURVEY_EXTEND_NORMAL  = 90;
  public final static int SURVEY_EXTEND_LEFT    = -1000;
  public final static int SURVEY_EXTEND_RIGHT   =  1000;

  public final static float DECLINATION_MAX = 720;    // twice 360
  public final static float DECLINATION_UNSET = 1080; // three times 360

  public long id;
  public String name;  // survey name
  public String date;  // YYYY.MM.DD
  String team;         // surveyors names
  float  declination;  // declination [degree]
  String comment;      // comment - description
  String initStation;  // start station
  int xsections;       // 0: shared, 1: private
  int datamode;        // normal or diving
  int mExtend;         // survey extend

  SurveyInfo copy() 
  {
    SurveyInfo ret = new SurveyInfo();
    ret.id    = id;
    ret.name  = name;
    ret.date  = date;
    ret.team  = team;
    ret.declination = declination;
    ret.comment = comment;
    ret.initStation = initStation;
    ret.xsections   = xsections;
    ret.datamode    = datamode;
    ret.mExtend     = mExtend;
    return ret;
  }

  /** @return true if the data-mode is diving
   */
  boolean isDivingMode() { return datamode == DATAMODE_DIVING; }

  // boolean isSectionPrivate() { return xsection == 1; }

  /** @return true if the specified extend if LEFT ( ie, less than -999 )
   * @param extend  the specified extend
   */
  static boolean isSurveyExtendLeft( int extend ) { return extend < -999; }

  /** @return true if the specified extend if RIGHT ( ie, greater than +999 )
   * @param extend  the specified extend
   */
  static boolean isSurveyExtendRight( int extend ) { return extend  > 999; }

  // /** @return true if the survey extend if LEFT ( ie, less than -999 )
  //  */
  // boolean isSurveyExtendLeft( )  { return mExtend < -999; }

  // /** @return true if the survey extend if RIGHT ( ie, greater than +999 )
  //  */
  // boolean isSurveyExtendRight( ) { return mExtend  > 999; }

  // /** @return the survey "extend"
  //  */
  // int getSurveyExtend() { return mExtend; }

  // /** set the survey "extend" 
  //  * @param extend   new extend value
  //  * @note the extend is always set in the range [0,360)
  //  */
  // void setSurveyExtend( int extend ) { mExtend = TDMath.in360( extend ); }

  /** @return true if declination is set
   */
  public boolean hasDeclination() { return declination < DECLINATION_MAX; }

  /** @return the declination [degrees]  or 0 if not-defined
   */
  public float getDeclination()
  {
    if ( declination < DECLINATION_MAX ) return declination;
    return 0;
  }

  /** @return the value of the declination from the string presentation [degrees]
   * @param et    string presentation (as edit text)
   */
  public static float declination( EditText et )
  {
    float decl = DECLINATION_UNSET;
    if ( et != null && et.getText() != null ) {
      String decl_str = et.getText().toString().trim();
      if ( /* decl_str != null && */ decl_str.length() > 0 ) { // ALWAYS true
        decl_str = decl_str.replace(',', '.');
        try {
          decl = Float.parseFloat( decl_str );
	  if ( decl < -360 || decl > 360 ) decl = DECLINATION_UNSET;
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse Float error: declination " + decl_str );
        }
      }
    }
    return decl;
  }

  /** @return false if declination is in range [-360,360], or edit text-field is empty
   * @param et    string presentation (as edit text)
   */
  static boolean declinationOutOfRange( EditText et )
  {
    if ( et != null && et.getText() != null ) {
      String decl_str = et.getText().toString().trim();
      if ( /* decl_str != null && */ decl_str.length() == 0 ) return true;
      decl_str = decl_str.replace(',', '.');
      try {
        float decl = Float.parseFloat( decl_str );
        if ( decl < -360 || decl > 360 ) return true;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "parse Float error: declination " + decl_str );
        return true;
      }
    }
    return false;
  }

}
