/* @file PlotType.java
 *
 * @author marco corvi
 * @date dec 2020
 *
 * @brief TopoDroid plot type
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.common;

import com.topodroid.DistoX.R;
import com.topodroid.DistoX.TDInstance;

import android.content.res.Resources;

public class PlotType
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
  public static final long PLOT_PROJECTED    = 8; // projected profile

  /** @return true if the type is for a vertical sketch
   * @param t   plot type
   */
  public static boolean isVertical( long t ) 
  { return ( t == PLOT_EXTENDED || t == PLOT_PROJECTED || t == PLOT_SECTION || t == PLOT_X_SECTION ); }

  /** @return true if the type is for a leg-xsection sketch
   * @param t   plot type
   */
  public static boolean isLegSection( long t )     { return t == PLOT_SECTION   || t == PLOT_H_SECTION; }

  /** @return true if the type is for a station-xsection sketch
   * @param t   plot type
   */
  public static boolean isStationSection( long t ) { return t == PLOT_X_SECTION || t == PLOT_XH_SECTION; }

  /** @return true if the type is for a xsection sketch
   * @param t   plot type
   */
  public static boolean isAnySection( long t ) { return t == PLOT_SECTION || t == PLOT_H_SECTION 
                                            || t == PLOT_X_SECTION || t == PLOT_XH_SECTION; }

  /** @return true if the type is for a xsection photo
   * @param t   plot type
   */
  public static boolean isPhoto( long t )    { return t == PLOT_PHOTO; }

  /** @return true if the type is for a xsection sketch or photo
   * @param t   plot type
   */
  public static boolean isAnySectionOrPhoto( long t )
  { return t == PLOT_SECTION || t == PLOT_H_SECTION || t == PLOT_X_SECTION || t == PLOT_XH_SECTION || t == PLOT_PHOTO; }

  /** @return true if the type is for a 2D plot (not xsection)
   * @param t   plot type
   */
  public static boolean isSketch2D( long t ) { return t == PLOT_PLAN || t == PLOT_EXTENDED || t == PLOT_PROJECTED; }

  /** @return true if the type is for a 2D profile sketch
   * @param t   plot type
   */
  public static boolean isProfile(  long t ) { return t == PLOT_EXTENDED || t == PLOT_PROJECTED; }

  /** @return true if the type is for a 2D extended profile sketch
   * @param t   plot type
   */
  public static boolean isExtended( long t ) { return t == PLOT_EXTENDED; }

  /** @return true if the type is for a 2D projected profile sketch
   * @param t   plot type
   */
  public static boolean isProjected( long t ) { return t == PLOT_PROJECTED; }

  /** @return true if the type is for a 2D plan sketch
   * @param t   plot type
   */
  public static boolean isPlan(     long t ) { return t == PLOT_PLAN; }

  /** @return true if the type is for a 3D sketch
   * @param t   plot type
   */
  public static boolean isSketch3D( long t ) { return t == PLOT_SKETCH_3D; }


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

  /** convert plot type to string
   * @param name   ...
   * @param type   plot type
   * @param res    resources
   * @return plot type as string
   */
  public static String plotTypeString( String name, int type, Resources res )
  {
    switch (type) {
      // case 0: // "X-SECTION";
      //   return String.format(res.getString( R.string.fmt_x_section ), name );
      case 1:
        return String.format(res.getString( R.string.fmt_plan ), name );
      case 2:
        return String.format(res.getString( R.string.fmt_extended ), name );
      // case 3: // "H-SECTION";
      //   return String.format(res.getString( R.string.fmt_h_section ), name );
      // case 4: // "PHOTO";
      //   return String.format(res.getString( R.string.fmt_photo ), name );
      // case 5: // "SECTION";
      //   return String.format(res.getString( R.string.fmt_v_section ), name );
      // case 6:
      //   return String.format(res.getString( R.string.fmt_sketch_3d ), name );
      // case 7: // XH-SECTION
      //   return String.format(res.getString( R.string.fmt_xh_section ), name );
      case 8:
        return String.format(res.getString( R.string.fmt_profile ), name );
    }
    return null; // cause a throw
  }

  /** convert plot type to string
   * @param type   plot type as string
   * @return plot type
   */
  public static long stringToPlotType( String type )
  {
    Resources res = TDInstance.context.getResources();
    if ( res.getString( R.string.extended ).equals( type ) ) {
      return PLOT_EXTENDED;
    } else if ( res.getString( R.string.profile ).equals( type ) ) {
      return PLOT_PROJECTED;
    // } else if ( res.getString( R.string.plan ).equals( type ) ) {
    //   return PlotType.PLOT_PLAN; 
    }
    return PlotType.PLOT_PLAN;
  }
   
  // static long toPlotType( String type ) 
  // {
  //   for ( int k=0; k<7; ++k ) {
  //     if ( type.equals( plotType[k] ) ) return k;
  //   }
  //   return PlotType.PLOT_PLAN;
  // }

  private static final String[] mTherionProjName = { // therion projection names
    "none", "plan", "extended", "none", "none", "none", "sketch_3d", "none", "elevation"
  };

  /** @return the Therion projection for a given type
   * @param type   plot type
   */
  public static String projName( int type ) { return mTherionProjName[ type ]; }

  // static final String[] plotName = { // plot list names
  //   "+", "==", "||", "x", "[o]", "<>", "3d", "><"
  // };

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
  //    if ( type.equals("PROFILE") )    return (int)PLOT_PROJECTED;
  //    return (int)PLOT_PLAN;
  // }

  /** @return true is the plot is multileg xsection
   * @param type plot type
   * @param to   plot TO station
   */
  public static boolean isMultilegSection( long type, String to )
  {
    return PlotType.isLegSection( type ) && ( to == null || to.length() == 0 );
  }
}

