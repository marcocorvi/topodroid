/** @file ParserSketch.java
 *
 * @author marco corvi
 * @date apr 2020
 *
 * @brief TopoDroid Sketch-export parser
 *
 * Usage:
 *    ParserSketch Sketch = new ParserSketch( filename );
 *    if ( Sketch.valid() ) { // use data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.utils.TDLog;

import java.util.ArrayList;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ParserSketch 
{
  private boolean mValid;    // header is valid
  public String mFilename; // Sketch filename
  public String mName;
  public int mType; // 1 PLAN, 2 PROFILE
  public int mAzimuth;
  public ArrayList< SketchPoint > mPoints;
  public ArrayList< SketchLine  > mLines;
  public ArrayList< SketchLine  > mAreas;
  public double xoff, yoff, zoff; // sketch offset

  // sketch points has model_coords: offset + sketch_vector

  // ------------------------------------------------- LOG
  void log()
  {
    TDLog.v("Sketch Pts " + mPoints.size() + " lines " + mLines.size() + " areas " + mAreas.size() );
    for ( SketchPoint pt : mPoints ) TDLog.v("Pt " + pt.thname + " " + pt.x + " " + pt.y + " " + pt.z );
    for ( SketchLine  ln : mLines  ) TDLog.v("Ln " + ln.thname + " " + ln.size() );
    for ( SketchLine  ln : mAreas  ) TDLog.v("Ar " + ln.thname + " " + ln.size() );
  }

  // ------------------------------------------------- 

  public ParserSketch( String filename )
  {
    mFilename = filename;
    xoff = yoff = zoff = 0;
  }

  public boolean valid() { return mValid; }

  public boolean readData( )
  {
    mValid = false;
    mPoints = new ArrayList<>();
    mLines  = new ArrayList<>();
    mAreas  = new ArrayList<>();
    try {
      FileReader fr = new FileReader( mFilename );
      BufferedReader br = new BufferedReader( fr );
      String line = br.readLine();
      if ( line == null ) return false;
      line = line.trim().replaceAll( "\\s+", " " );
      String[] vals = line.split(" ");
      if ( vals.length < 7 ) return false; // SCRAP name type azimuth x y z
      mName = vals[1];
      mType = Integer.parseInt( vals[2] );
      mAzimuth = Integer.parseInt( vals[3] );
      xoff = Double.parseDouble( vals[4] );
      yoff = Double.parseDouble( vals[5] );
      zoff = Double.parseDouble( vals[6] );
      // TDLog.v("Sketch " + mName + " type " + mType + " offset " + xoff + " " + yoff + " " + zoff );
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim().replaceAll( "\\s+", " " );
        // TDLog.v("TopoGL-SKETCH", line );
        vals = line.split(" ");
        if ( line.startsWith( "LINE" ) || line.startsWith( "AREA" ) ) {
          float red   = Float.parseFloat( vals[2] );
          float green = Float.parseFloat( vals[3] );
          float blue  = Float.parseFloat( vals[4] );
          boolean is_area = line.startsWith("AREA");
          float alpha = is_area? Float.parseFloat( vals[5] ) : 1.0f ;
          SketchLine ln = new SketchLine( vals[1], red, green, blue, alpha );
          while ( ( line = br.readLine() ) != null ) {
            if ( line.startsWith( "ENDLINE" ) ) {
              mLines.add( ln );
              break;
            }
            if ( line.startsWith( "ENDAREA" ) ) {
              mAreas.add( ln );
              break;
            }
            line = line.trim().replaceAll( "\\s+", " " );
            vals = line.split(" ");
            double x = Double.parseDouble( vals[0] );
            double y = Double.parseDouble( vals[1] );
            double z = Double.parseDouble( vals[2] );
            // if ( is_area ) TDLog.v("Sketch area <", line + "> x " + x + " y " + y + " z " + z );
            ln.insertPoint( x, y, z );
          }
        } else if ( line.startsWith( "POINT" ) ) {
          String th = vals[1];
          // azimuth = vals[2]
          double x = Double.parseDouble( vals[3] );
          double y = Double.parseDouble( vals[4] );
          double z = Double.parseDouble( vals[5] );
          mPoints.add( new SketchPoint( x, y, z, th ) );
        }
      }
      fr.close();
    } catch ( IOException e ) { }
    mValid = true;
    return true;
  }
            
}

