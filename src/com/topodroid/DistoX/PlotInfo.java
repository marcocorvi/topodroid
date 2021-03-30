/* @file PlotInfo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketch metadata
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.common.PlotType;

import android.content.res.Resources;

// import android.util.Log;

public class PlotInfo
{

  final static int ORIENTATION_PORTRAIT  = 0;
  final static int ORIENTATION_LANDSCAPE = 1;

  long surveyId; // survey id
  long id;       // plot id
  String name;   // name of the plot
  int type;      // type of the plot
  String start;  // reference station (origin)
  String view;   // viewed station [xsections], barrier [plan/profile]
  String hide;   // hiding stations [plan/profile], parent plot [xsections]
  String nick;   // nickname (xsections)
  float xoffset; // display X-offset
  float yoffset; // display Y-offset
  float zoom;    // display zoom
  float azimuth; // vertical x-section azimuth / profile projection azimuth
  float clino;   // inclination (0 for plan x-sections)
  int csxIndex = -1;  // numerical index for cSurvey xsection exports
  int orientation = ORIENTATION_PORTRAIT;
  int maxscrap; // max index of a scrap
  float intercept = -1; //x-section leg intercept (-1 if undefined, 0 for station intercepts)

  // boolean isPortrait()  { return orientation == ORIENTATION_PORTRAIT; }
  boolean isLandscape() { return orientation == ORIENTATION_LANDSCAPE; }

  boolean isSectionPrivate() { return PlotType.isAnySectionOrPhoto(type) && hide != null && hide.length() > 0; }
  boolean isSectionShared()  { return PlotType.isAnySectionOrPhoto(type) && ( hide == null || hide.length() == 0 ); }
  String getSectionParent() 
  {
    if ( ! PlotType.isAnySectionOrPhoto(type) ) return null;
    return hide;
  }
       
  // void dump()
  // {
  //   Log.v( "DistoX-PLOT", surveyId + "-" + id + " " + name + " type " + type + " start " + start );
  // }

  public void setId( long i, long sid )
  {
    id = i;
    surveyId = sid;
  }

  String getTypeString() { return  PlotType.projName( type ); }

}
