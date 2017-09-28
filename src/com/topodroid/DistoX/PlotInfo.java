/* @file PlotInfo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketch metadata
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.res.Resources;

// import android.util.Log;

class PlotInfo
{
  // sketch types
  // TODO NEVER CHANGE - THESE ARE WRITTEN IN TDR FILES
  public static final long PLOT_NULL       = -1; 
  public static final long PLOT_X_SECTION  = 0; // X-section at a station (defined in PLAN plot)
  public static final long PLOT_PLAN       = 1;
  public static final long PLOT_EXTENDED   = 2;
  public static final long PLOT_H_SECTION  = 3; // leave the place but do not use
  public static final long PLOT_PHOTO      = 4;
  public static final long PLOT_SECTION    = 5; // section-line cross-section
  public static final long PLOT_SKETCH_3D  = 6;
  public static final long PLOT_XH_SECTION = 7; // X-H_sectiuon at a station (defined in EXT plot)
  public static final long PLOT_PROFILE    = 8; // projected profile

  public long surveyId; // survey id
  public long id;       // plot id
  public String name;   // name of the plot
  public int type;      // type of the plot
  public String start;  // base station
  public String view;   // viewed station (barrier)
  public String hide;   // hiding stations / parent plot (xsections)
  public String nick;   // nickname (xsections)
  public float xoffset; // display X-offset
  public float yoffset; // display Y-offset
  public float zoom;    // display zoom
  public float azimuth; // vertical cross-section azimuth / profile projection azimuth
  public float clino;   // inclination (0 for plan cross-sections)
  public int csxIndex = -1;  // numerical index for cSurvey xsection exports

  static boolean isVertical( long type ) 
  { return ( type == PLOT_EXTENDED || type == PLOT_PROFILE || type == PLOT_SECTION || type == PLOT_X_SECTION ); }

  static boolean isSection( long t )  { return t == PLOT_SECTION   || t == PLOT_H_SECTION; }
  static boolean isXSection( long t ) { return t == PLOT_X_SECTION || t == PLOT_XH_SECTION; }
  static boolean isAnySection( long t ) { return t == PLOT_SECTION || t == PLOT_H_SECTION 
                                            || t == PLOT_X_SECTION || t == PLOT_XH_SECTION; }
  static boolean isPhoto( long t )    { return t == PLOT_PHOTO; }
  static boolean isAnySectionOrPhoto( long t )
  { return t == PLOT_SECTION || t == PLOT_H_SECTION || t == PLOT_X_SECTION || t == PLOT_XH_SECTION || t == PLOT_PHOTO; }

  static boolean isSketch2D( long t ) { return t == PLOT_PLAN || t == PLOT_EXTENDED || t == PLOT_PROFILE; }
  static boolean isProfile(  long t ) { return t == PLOT_EXTENDED || t == PLOT_PROFILE; }
  static boolean isExtended( long t ) { return t == PLOT_EXTENDED; }
  static boolean isProjeted( long t ) { return t == PLOT_PROFILE; }
  static boolean isPlan(     long t ) { return t == PLOT_PLAN; }

  static boolean isSketch3D( long t ) { return t == PLOT_SKETCH_3D; }

  boolean isSectionPrivate() { return isAnySectionOrPhoto(type) && hide != null && hide.length() > 0; }
  boolean isSectionShared() { return isAnySectionOrPhoto(type) && ( hide == null || hide.length() == 0 ); }
  String getSectionParent() 
  {
    if ( ! isAnySectionOrPhoto(type) ) return null;
    return hide;
  }

  // public static final String[] plotType = {
  //   "X-SECTION",  // vertical cross section
  //   "PLAN",       // plan
  //   "EXTENDED",   // extended elevation
  //   "H-SECTION",  // horizontal cross-section
  //   "PHOTO",      // photo section
  //   "SECTION",
  //   "SKETCH-3D",
  //   "XH-SECTION"
  // };

  static String plotTypeString( int type, Resources res )
  {
    switch (type) {
      case 0:
        return "X-SECTION";
      case 1:
        return res.getString( R.string.plan );
      case 2:
        return res.getString( R.string.extended );
      case 3:
        // return "H-SECTION";
        return res.getString( R.string.h_section );
      case 4:
        // return "PHOTO";
        return res.getString( R.string.menu_photo );
      case 5:
        return "SECTION";
      case 6:
        return "SKETCH-3D";
      case 7:
        return "XH-SECTION";
      case 8:
        return res.getString( R.string.profile );
    }
    return "Unknown type";
  }
       
   
  // static long toPlotType( String type ) 
  // {
  //   for ( int k=0; k<7; ++k ) {
  //     if ( type.equals( plotType[k] ) ) return k;
  //   }
  //   return PLOT_PLAN;
  // }

  static final String[] projName = { // therion projection names
    "none", "plan", "extended", "none", "none", "none", "sketch_3d", "none", "elevation"
  };
  // static final String[] plotName = { // plot list names
  //   "+", "==", "||", "x", "[o]", "<>", "3d", "><"
  // };


  // void dump()
  // {
  //   Log.v( TopoDroidApp.TAG, surveyId + "-" + id + " " + name + " type " + type + " start " + start );
  // }

  public void setId( long i, long sid )
  {
    id = i;
    surveyId = sid;
  }

  public String getTypeString() 
  {
    return projName[ type ];
  }

  // public static int getTypeValue( String type )
  // {
  //    if ( type.equals("X-SECTION") )  return (int)PLOT_X_SECTION;
  //    if ( type.equals("PLAN") )       return (int)PLOT_PLAN;
  //    if ( type.equals("EXTENDED") )   return (int)PLOT_EXTENDED;
  //    if ( type.equals("H-SECTION") )  return (int)PLOT_H_SECTION;
  //    if ( type.equals("PHOTO") )      return (int)PLOT_PHOTO;
  //    if ( type.equals("SECTION") )    return (int)PLOT_SECTION;
  //    if ( type.equals("SKETCH-3D") )  return (int)PLOT_SKETCH_3D;
  //    if ( type.equals("XH-SECTION") ) return (int)PLOT_XH_SECTION;
  //    if ( type.equals("PROFILE") )    return (int)PLOT_PROFILE;
  //    return (int)PLOT_PLAN;
  // }

}
