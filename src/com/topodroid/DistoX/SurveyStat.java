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
package com.topodroid.DistoX;

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
  float[] M; // magn. fields
  float[] D; // magn. dips

  int nrMGD;
}
