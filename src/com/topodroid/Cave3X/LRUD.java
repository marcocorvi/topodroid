/* @file LRUD.java
 *
 * @author marco corvi
 * @date dec 2014
 *
 * @brief TopoDroid LRUD struct
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

class LRUD 
{
  float l, r, u, d;

  LRUD() 
  {
    l = 0.0f; // left
    r = 0.0f; // right
    u = 0.0f; // up
    d = 0.0f; // down
  }

  LRUD( LRUD lrud ) // copy cstr
  {
    l = lrud.l;
    r = lrud.r;
    u = lrud.u;
    d = lrud.d;
  }

}
