/* @file PTXSectionElement.java
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

import com.topodroid.utils.TDLog;

import java.io.FileInputStream;
import java.io.FileOutputStream;

// import android.util.Log;


public class PTXSectionElement extends PTElement
{
    private PTPoint _pos;
    private PTId _station;
    private int _direction; //!< -1 horizontal, >=0 projection azimuth
                        //!< (internal angle units)
    PTXSectionElement()
    {
      super( ID_XSECTION_ELEMENT );
      _pos = new PTPoint();
      _station = new PTId();
      _direction = 0;
    }

    public PTPoint position() { return _pos; }

    void setPosition( int x, int y ) { _pos.set(x,y); }

    public PTId station() { return _station; }

    /** get the direction (internal angle units)
     */
    public int direction() { return _direction; }

    /** get the direction [in degrees]
     */
    public float getDirection() 
    {
      if ( _direction == -1 ) return -1.0f;
      return (float)(_direction) * PTFile.INT16_2_DEG;
    }

    /** set the direction 
     * @param dir    direction [degrees] (neg. for horizontal)
     */
    void setDirection( float dir )
    {
      if ( dir < 0.0f ) _direction = -1;
      _direction = (int)( dir * PTFile.DEG_2_INT16 );
    }

    // ---------------------------------------------------------

    @Override
    void read( FileInputStream fs ) 
    {
      _pos.read( fs );
      _station.read( fs );
      _direction = PTFile.readInt( fs );
      TDLog.Log( TDLog.LOG_PTOPO,
         "PT XSection pos " + _pos._x + " " + _pos._y + " id " + _station._id + " dir " + _direction );
    }

    @Override
    void write( FileOutputStream fs ) 
    {
      PTFile.writeInt( fs, _id );
      _pos.write( fs );
      _station.write( fs );
      PTFile.writeInt( fs, _direction );
    }

    // @Override
    // void print() 
    // {
    //   Log.v( "DistoX-PT", "xsection: dir " + _direction );
    //   _pos.print();
    //   _station.print();
    // }

/*
    @Override
void 
PTxsection_element::printTherion( FILE * fp, int x0, int y0, int scale, 
                   String[] points,
                   String[] lines )
{
   double x =   XTHERION_FACTOR * (double)(_pos.x() - x0)/1000.0;
   double y = - XTHERION_FACTOR * (double)(_pos.y() - y0)/1000.0;
   std::string station = _station.toString();
   fprintf(fp, "    point %.3f %.3f section -scrap station_%s\n",  // Locale.US
     x, y, station.c_str() );
}

void 
PTxsection_element::xtherionBounds( int x0, int y0, int scale,
                     double & xmin, double & ymin,
                     double & xmax, double & ymax )
{
   double x =   XTHERION_FACTOR * (double)(_pos.x() - x0)/1000.0;
   double y = - XTHERION_FACTOR * (double)(_pos.y() - y0)/1000.0;
   if ( x < xmin ) xmin = x;
   if ( y < ymin ) ymin = y;
   if ( x > xmax ) xmax = x;
   if ( y > ymax ) ymax = y;
}
*/

}
