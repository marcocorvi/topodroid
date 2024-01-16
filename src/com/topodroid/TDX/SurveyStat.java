/* @file SurveyStat.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey stat info (lengths and counts)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

class SurveyStat
{
  long id;  // survey id
  float lengthLeg;
  float extLength;
  float planLength;
  float lengthDuplicate;
  float lengthSurface;

  int countLeg;
  int countDuplicate;
  int countSurface;
  int countSplay;

  int countStation;
  int countLoop;
  int countComponent;

  float averageM;
  float averageG;
  float averageD;
  float stddevM;
  float stddevG;
  float stddevD;

  float[] G; // accelerations
  float[] M; // magnetic fields
  float[] D; // magnetic dips

  int nrMGD;
  int deviceNr;     // number of devices
  String deviceCnt; // shots per device

  long minMillis;
  long maxMillis;

  /** default cstr - everything zero, except ID
   * @param sid   survey ID
   */
  public SurveyStat( long sid )
  {
    id = sid;
    lengthLeg  = 0.0f;
    extLength  = 0.0f;
    planLength = 0.0f;
    lengthDuplicate = 0.0f;
    lengthSurface   = 0.0f;
    countLeg = 0;
    countDuplicate = 0;
    countSurface   = 0;
    countSplay     = 0;
    countStation   = 0;
    countLoop      = 0;
    countComponent = 0;
    averageM = 0;
    averageG = 0;
    averageD = 0;
    stddevM  = 0;
    stddevG  = 0;
    stddevD  = 0;
    nrMGD = 0;
    deviceNr = 0;
    deviceCnt = "";
  }

}
