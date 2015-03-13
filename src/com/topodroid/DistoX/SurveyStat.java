/** @file SurveyStat.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey stat info (lengths and counts)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120715 created
 */
package com.topodroid.DistoX;

class SurveyStat
{
  public long id;  // survey id
  public float lengthLeg;
  public float lengthDuplicate;
  public float lengthSurface;

  public int countLeg;
  public int countDuplicate;
  public int countSurface;
  public int countSplay;

  public int countStation;
  public int countLoop;
  public int countComponent;
}
