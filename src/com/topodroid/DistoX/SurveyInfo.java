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

class SurveyInfo
{
  final static int XSECTION_SHARED  = 0;
  final static int XSECTION_PRIVATE = 1;

  final static int DATAMODE_NORMAL  = 0;
  final static int DATAMODE_DIVING  = 1;

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
}
