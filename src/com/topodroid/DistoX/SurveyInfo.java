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
package com.topodroid.DistoX;

import android.widget.EditText;

class SurveyInfo
{
  final static int XSECTION_SHARED  = 0;
  final static int XSECTION_PRIVATE = 1;

  final static int DATAMODE_NORMAL  = 0;
  final static int DATAMODE_DIVING  = 1;

  final static int EXTEND_NORMAL  = 90;
  final static int EXTEND_LEFT    = -1000;
  final static int EXTEND_RIGHT   =  1000;

  final static float DECLINATION_MAX = 720;    // twice 360
  final static float DECLINATION_UNSET = 1080; // three times 360

  long id;
  String name;
  String date;
  String team;
  float  declination;
  String comment;
  String initStation;
  int xsections; // 0: shared, 1: private
  int datamode;
  int mExtend;

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

  static boolean isExtendLeft( int extend ) { return extend < -999; }
  static boolean isExtendRight( int extend ) { return extend  > 999; }

  boolean isExtendLeft( )  { return mExtend < -999; }
  boolean isExtendRight( ) { return mExtend  > 999; }
  int getExtend() { return mExtend; }
  void setExtend( int extend ) { mExtend = TDMath.in360( extend ); }


  boolean hasDeclination() { return declination < DECLINATION_MAX; }

  // get the declination or 0 if not-defined
  float getDeclination()
  {
    if ( declination < DECLINATION_MAX ) return declination;
    return 0;
  }

  static float declination( EditText et )
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

  // test returns false if declination is in range [-360,360] 
  //                    or edit textfield is empty
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
