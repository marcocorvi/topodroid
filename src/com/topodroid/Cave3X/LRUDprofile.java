/* @file LRUDprofile.java
 *
 * @author marco corvi
 * @date dec 2014
 *
 * @brief TopoDroid LRUD with profile for Grottolf
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.utils.TDMath;

import java.util.ArrayList;

class LRUDprofile extends LRUD
{
  private class Profile
  {
    float clino;
    float distance;
    
    Profile( float c, float d ) 
    {
      clino = c;
      distance = d;
    }
  }

  float bearing;
  private ArrayList< Profile > data;

  LRUDprofile( float b )
  {
    super();
    bearing = b;
    data    = new ArrayList<>();
  }

  float getClino( int k )    { return data.get(k).clino; }
  float getDistance( int k ) { return data.get(k).distance; }

  void addData( float z, float r, float d )
  {
    float c = TDMath.atan2d( z, r );
    int k = 0;
    int k0 = data.size();
    for ( ; k<k0; ++k ) {
      if ( data.get(k).clino > c ) break;
    }
    // insert data before "k"
    data.add( k, new Profile(c,d) );
  }

  int size() { return data.size(); }

}
