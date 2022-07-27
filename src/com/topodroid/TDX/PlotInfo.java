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
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.common.PlotType;

// import android.content.res.Resources;

public class PlotInfo
{

  final static int ORIENTATION_PORTRAIT  = 0;
  final static int ORIENTATION_LANDSCAPE = 1;

  long surveyId; // survey id
  long id;       // plot id
  String name;   // name of the plot
  int type;      // type of the plot
  String start;  // reference station (origin), shots IDs (multileg xsection)
  String view;   // viewed station [leg xsections], barrier [plan/profile], empty (multileg xsection)
  String hide;   // hiding stations [plan/profile], parent plot [xsections]
  String nick;   // comment (xsections)
  float xoffset; // display X-offset
  float yoffset; // display Y-offset
  float zoom;    // display zoom
  float azimuth; // vertical x-section azimuth / projected-profile azimuth
  float clino;   // inclination (0 for plan x-sections)
  int csxIndex = -1;  // numerical index for cSurvey xsection exports
  int orientation = ORIENTATION_PORTRAIT;
  int maxscrap; // max index of a scrap
  float intercept = -1; //x-section leg intercept (-1 if undefined, 0 for station intercepts, -2 multileg xsection)
  Vector3D center;  // plot center for multileg xsections

  // /** @return true if the orientation is portrait
  //  */
  // boolean isPortrait()  { return orientation == ORIENTATION_PORTRAIT; }

  /** @return true if the orientation is landscape
   */
  boolean isLandscape() { return orientation == ORIENTATION_LANDSCAPE; }

  /** @return true if the xsection is private
   */
  boolean isSectionPrivate() { return PlotType.isAnySectionOrPhoto(type) && hide != null && hide.length() > 0; }

  /** @return true if the xsection is shared
   */
  boolean isSectionShared()  { return PlotType.isAnySectionOrPhoto(type) && ( hide == null || hide.length() == 0 ); }

  /** @return the name of the parent plot of a xsection
   */
  String getSectionParent() 
  {
    if ( ! PlotType.isAnySectionOrPhoto(type) ) return null;
    return hide;
  }
       
  // void dump( )
  // {
  //   tdlog.v( "PLOT " + surveyId + "-" + id + " " + name + " type " + type + " start " + start );
  // }

  /** set the plot ID
   * @param i    plot ID
   * @param sid  survey ID
   */
  public void setId( long i, long sid )
  {
    id = i;
    surveyId = sid;
  }

  /** @return the (string) plot type
   */
  String getTypeString() { return  PlotType.projName( type ); }

}
