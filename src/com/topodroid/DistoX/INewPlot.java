/* @file INewPlot.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid NewPlot interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

interface INewPlot 
{
  // void makeNewPlot( String name, long type, String start, String view );

  /** make a new plot
   * @param name     plot name
   * @param start    origin station
   * @param extended whether the plot is extended profile
   * @param project  projection azimuth (for projected profile) ?
   */
  void makeNewPlot( String name, String start, boolean extended, int project );

  /* FIXME_SKETCH_3D *
  void makeNewSketch3d(  String name, String start, String next );
   * END_SKETCH_3D */

  /** @return true if the specified plot exists already
   * @param name   plot name
   */
  boolean hasSurveyPlot( String name );

  /** @return true if the specified station exists already
   * @param start   station name
   */
  boolean hasSurveyStation( String start );

  void doProjectionDialog( String name, String start );
}


