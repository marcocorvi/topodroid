/** @file ThStatus.java
 *
 * @author marco corvi
 * @date apr 2026
 *
 * @brief Therion status while reading Therion commands
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.th;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDString;

import com.topodroid.TDX.Cave3DCS;
import com.topodroid.TDX.Cave3DFix;

public class ThStatus 
{
  public static final int DATA_NONE       = 0;
  public static final int DATA_NORMAL     = 1;
  public static final int DATA_DIMENSION  = 2; 

  public static final int FLIP_NONE       = 0;
  public static final int FLIP_HORIZONTAL = 1;
  public static final int FLIP_VERTICAL   = 2;

  public int in_data = 0; // 0 none, 1 normal, 2 dimension
  public boolean in_surface = false;
  public boolean in_centerline = false;
  public boolean in_survey = false;
  public boolean in_map = false;
  public boolean in_splay = false;

  public double units_len = 1;
  public double units_ber = 1;
  public double units_cln = 1;

  public ThStatus( double ul, double ub, double uc )
  {
    units_len = ul;
    units_ber = ub;
    units_cln = uc;
  }

  /** handle command "flags"
   * @param vals command tokens
   * @param idx  token index
   */
  public int handleFlags( String[] vals, int idx, int flags )
  {
    for ( idx = TDString.nextIndex( vals, idx ); idx < vals.length; idx = TDString.nextIndex( vals, idx ) ) {
      if ( vals[idx].equals("not") ) { 
        if ( (idx = TDString.nextIndex( vals, idx )) < vals.length ) {
          if ( vals[idx].equals("duplicate") ) {
            flags &= ~0x00000001;
          } else if ( vals[idx].equals("surface") ) {
            flags &= ~0x00000002;
          } else if ( vals[idx].equals("splay") ) {
            in_splay = false;
          } 
        }
      } else {
        if ( vals[idx].equals("duplicate") ) {
          flags |= 0x00000001;
        } else if ( vals[idx].equals("surface") ) {
          flags |= 0x00000002;
        } else if ( vals[idx].equals("splay") ) {
          in_splay = true;
        }
      }
    }
    return flags;
  }

  /** handle command "fix"
   * @param vals command tokens
   * @param idx  token index
   * @param path survey pathname
   * @param cs   coordinate system
   */
  public static Cave3DFix handleFix( String[] vals, int idx, String path, Cave3DCS cs )
  {
    idx = TDString.nextIndex( vals, idx );
    if ( idx < vals.length ) {
      String name = makeName( vals[idx], path );
      TDLog.v( "TH command fix " + name );
      try { 
        idx = TDString.nextIndex( vals, idx );
        if ( idx < vals.length ) {
          double x = Double.parseDouble( vals[idx] );
          // TDLog.v( "TH fix x " + x );
          idx = TDString.nextIndex( vals, idx );
          if ( idx < vals.length ) {
            double y = Double.parseDouble( vals[idx] );
            // TDLog.v( "TH fix y " + y );
            idx = TDString.nextIndex( vals, idx );
            if ( idx < vals.length ) {
              double z = Double.parseDouble( vals[idx] );
              return new Cave3DFix( name, x, y, z, cs, 1, 1, 0.0 ); // no WGS84 - FIXME M_TO_UNITS - zero convergence
              // TDLog.v( "TH adding fix " + name + ": " + x + " " + y + " " + z );
            } else {
              TDLog.e( "TH fix " + name + " missing Z");
            }
          } else {
            TDLog.e( "TH fix " + name + " missing Y");
          }
        } else {
          TDLog.e( "TH fix " + name + " missing X");
        }
      } catch ( NumberFormatException e ) {
        TDLog.e( "TH Fix " + name + " station error: " + e.getMessage() );
      }
    }
    return null;
  }

  /** handle the command "flip"
   * @param flip   command argument
   * @return int-value of the flip
   */
  public static int parseFlip( String flip )
  {
    if ( flip.equals("horizontal") ) return FLIP_HORIZONTAL;
    if ( flip.equals("vertical") ) return FLIP_VERTICAL;
    return FLIP_NONE;
  }

  /** compose the name of a station interposing a pathname
   * @param in     parent 
   * @param path   pathname
   */
  public static String makeName( String in, String path )
  {
    // TDLog.v("TH make name " + in + " " + path );
    int index = in.indexOf('@');
    if ( index > 0 ) {
      // 20240910 
      // return in.substring(0,index) + "@" + path + "." + in.substring(index+1);
      return in + "." + path;
    } else {
      return in + "@" + path;
    }
  }

}
