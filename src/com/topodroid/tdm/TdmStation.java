/** @file TdmStation.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey station
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

class TdmStation
{
  String mName;
  float e, s, h, v;
  TdmSurvey mSurvey; // survey this station belongs to

  TdmStation( String name, float e0, float s0, float h0, float v0, TdmSurvey survey )
  {
    mName = name;
    e = e0;
    s = s0;
    h = h0;
    v = v0;
    mSurvey = survey;
  }

  String getFullName() 
  {
    if ( mName.indexOf('@') > 0 ) {
      return mName + '.' + mSurvey.getFullName();
    }
    return mName + '@' + mSurvey.getFullName();
  }

  String getName()
  {
    return mName; 
  }

}
