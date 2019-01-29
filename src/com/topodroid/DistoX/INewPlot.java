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
  void makeNewPlot( String name, String start, boolean extended, int project );

  /* FIXME_SKETCH_3D *
  void makeNewSketch3d(  String name, String start, String next );
   * END_SKETCH_3D */

  boolean hasSurveyPlot( String name );

  boolean hasSurveyStation( String start );

  void doProjectionDialog( String name, String start );
}


