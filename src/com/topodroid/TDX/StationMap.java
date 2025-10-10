/* @file Accuracy.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid station name mapping
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;


class StationMap
{
  String mFrom;
  String mTo;

  StationMap( String station ) 
  { 
    mFrom = station;
    mTo   = station;
  }

  StationMap( String from, String to )
  { 
    mFrom = from;
    mTo   = to;
  }

  boolean startsWith( String prefix ) 
  {
    return ( mFrom.startsWith( prefix ) || mTo.startsWith( prefix ) );
  }

}
