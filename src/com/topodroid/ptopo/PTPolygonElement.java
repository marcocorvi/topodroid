/* @file PTPolygonElement.java
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief PocketTopo file IO
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ptopo;

import java.util.ArrayList;

import java.io.InputStream;
import java.io.OutputStream;

public class PTPolygonElement extends PTElement
{
    // int _point_count;  //!< number of points
    private ArrayList< PTPoint > _points;     //!< points
    private byte _color;  //!< 1=black, 2=gray, 3=brown, 4=b;lue, 5=red, 6=green

    PTPolygonElement()
    {
      super( ID_POLYGON_ELEMENT );
      // _point_count = 0;
      _points = new ArrayList<>();
      _color = 0;
    }

    // copy operations are ok 

    public int pointCount() { return _points.size(); }

    // void setPointCount( int pc ) 
    // {
    //   if ( _point_count != pc ) {
    //     _point_count = pc;
    //     _points.resize( _point_count );
    //   }
    // }

    public PTPoint point( int k ) { return _points.get(k); }

    public byte getColor() { return _color; }
    void setColor( byte c ) { _color = c; }

    /** insert a new point at the end
     * @param x point X coord [m]
     * @param y point Y coord [m]
     */
    int insertPoint( float x, float y )
    {
      int x0 = (int)(x * 1000.0f);
      int y0 = (int)(y * 1000.0f);
      _points.add( new PTPoint(x0, y0) );
      return _points.size();
    }

    void clear()
    {
      _points.clear();
    }

    // ------------------------------------------------------------


    @Override
    void read( InputStream fs )
    {
      _points.clear();
      int pc = PTFile.readInt( fs );
      for ( int k=0; k<pc; ++k ) {
        PTPoint p = new PTPoint();
        p.read( fs );
        _points.add( p );
      }
      _color = PTFile.readByte( fs );
    }
 
    @Override
    void write( OutputStream fs )
    {
      PTFile.writeByte( fs, _id );
      PTFile.writeInt( fs, _points.size() );
      for ( PTPoint p : _points ) p.write( fs );
      PTFile.writeByte( fs, _color );
    }

    // @Override
    // void print( )
    // {
    //   tdlog.v( "polygon: count " + _points.size() + " color " + _color );
    //   for ( PTPoint p : _points ) p.print();
    // }

/*
// void 
// PTpolygon_element::printTherion( OutputStream fp, int x0, int y0, // int  scale )
// {
//   if ( _point_count > 1 ) {
//     fprintf(fp, "    line %s\n", PtCmapActivity.getLineThName(_color) );
//     for (int k=0; k<_point_count; ++k ) {
//       double x =   XTHERION_FACTOR * (double)(_points[k].x() - x0)/1000.0;
//       double y = - XTHERION_FACTOR * (double)(_points[k].y() - y0)/1000.0;
//       fprintf(fp, "      %8.2f %8.2f \n", x, y );
//     }
//     fprintf(fp, "    endline\n\n");
//   } else if ( _point_count == 1 ) {
//     int k = 0;
//     double x =   XTHERION_FACTOR * (double)(_points[k].x() - x0)/1000.0;
//     double y = - XTHERION_FACTOR * (double)(_points[k].y() - y0)/1000.0;
//     fprintf(fp, "    point %.3f %.3f %s\n", x, y, PtCmapActivity.getPointThName(_color) ); // Locale.US
//   }
// }
// 
// void 
// PTpolygon_element::xtherionBounds( int x0, int y0, // int scale,
//                      double & xmin, double & ymin,
//                      double & xmax, double & ymax )
// {
//   for ( int k=0; k<_point_count; ++k) {
//     double x =   XTHERION_FACTOR * (double)(_points[k].x() - x0)/1000.0;
//     double y = - XTHERION_FACTOR * (double)(_points[k].y() - y0)/1000.0;
//     if ( x < xmin ) xmin = x;
//     if ( y < ymin ) ymin = y;
//     if ( x > xmax ) xmax = x;
//     if ( y > ymax ) ymax = y;
//   }
// }
*/

}


