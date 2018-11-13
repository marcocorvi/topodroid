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

  final static float DECLINATION_MAX = 999;
  final static float DECLINATION_UNSET = 1000;

  long id;
  String name;
  String date;
  String team;
  float  declination;
  String comment;
  String initStation;
  int xsections; // 0: shared, 1: private
  int datamode;

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
    return ret;
  }

  boolean hasDeclination() { return declination < DECLINATION_MAX; }

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

  static boolean declinationOutOfRange( EditText et )
  {
    if ( et == null && et.getText() == null ) return false;
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
    return false;
  }
}
