/* @file ClosureTask.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief TopoDroid compute closure error (as string)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Locale;

import android.util.Log;

class ClosureTask extends AsyncTask< Void, Void, Void >
{
  TDNum  num;
  String format;
  ArrayList< NumShot > shots;
  ArrayList< NumShortpath > paths;
  NumStation sf;
  NumStation st;
  float d, b, c;
  String sfname, stname;
  double dv, dl, dh;

  ClosureTask( TDNum num0, String fmt, ArrayList<NumShot> shots0, ArrayList<NumShortpath> paths0, 
               NumStation sf0, NumStation st0, float d0, float b0, float c0 )
  {
    num    = num0;
    format = fmt;
    shots  = shots0;
    paths  = paths0;
    sf = sf0;
    st = st0;
    d  = d0;
    b  = b0;
    c  = c0;

    // initialize fixed values
    dv = Math.abs( sf.v - d * TDMath.sinDd(c) - st.v );  // closure vertical error
    double h0 = d * Math.abs( TDMath.cosDd(c) );
    double ds = Math.abs( sf.s - h0 * TDMath.cosDd( b ) - st.s ); // closure south error
    double de = Math.abs( sf.e + h0 * TDMath.sinDd( b ) - st.e ); // closure east error
    dh = ds*ds + de*de;
    dl = Math.sqrt( dh + dv*dv ); // closure 3d error
    dh = Math.sqrt( dh );         // closure horizontal error
    sfname = sf.name;
    stname = st.name;
  }

  private NumShortpath getShortpath( NumStation st )
  {
    for ( NumShortpath path : paths ) {
      if ( path.mStation == st ) return path;
    }
    return null;
  }

  @Override
  protected Void doInBackground( Void ... v ) 
  {
    compute();
    return null;
  }

  void compute()
  {
    // Log.v("DistoX", "shortest path " + sf.name + " " + st.name + " shots " + shots.size() + " stations " + paths.size() );
    Stack< NumShortpath > stack = new Stack<>();
    NumShortpath sp = getShortpath( sf );
    if ( sp == null ) return;
    sp.resetShortpath( null, 0, 0, 0 ); // clear first shortpath
    stack.push( sp );
    while ( ! stack.empty() ) {
      sp = stack.pop();
      NumStation s = sp.mStation;
      for ( NumShot e : shots ) {
        float len = e.length();
        if ( e.from == s /* && e.to != null */ ) {
          NumShortpath etp = getShortpath( e.to );
          if ( etp != null ) {
            float d = sp.mDist + len;
            if ( d < etp.mDist - 0.001f ) { // at least 1 mm shorter
              // Log.v("DistoX-LOOP", "set short dist T " + e.to.name + " : " + d );
              etp.resetShortpath( sp, sp.mNr+1, d, sp.mDist2 + len*len );
              stack.push( etp );
            }
          }
        } else if ( e.to == s /* && e.from != null */ ) {
          NumShortpath efp = getShortpath( e.from );
          if ( efp != null ) {
            float d = sp.mDist + len;
            if ( d < efp.mDist - 0.001f ) { // at least 1 mm shorter
              // Log.v("DistoX-LOOP", "set short dist F " + e.from.name + " : " + d );
              efp.resetShortpath( sp, sp.mNr+1, d, sp.mDist2 + len*len );
              // e.from.path = from;
              stack.push( efp );
            }
          }
        }
      }
    }
    // Log.v("DistoX", "Loop closure done " + sfname + " " + stname );
    sp = getShortpath( st );
    if ( sp != null && sp.mNr > 0 ) {
      int nr = 1 + sp.mNr;                  // loop shots
      double len  = Math.abs(d) + sp.mDist; // loop length
      // double len2 = d*d + sp.mDist2;
      int k  = sp.mNr;                      // safety for bailout
      StringBuilder sb = new StringBuilder();
      sb.append( sp.getName() );
      for ( sp = sp.mFrom; sp != null && k >= 0; sp = sp.mFrom ) {
        sb.append( "-" );
        sb.append( sp.getName() );
        -- k;
      }
      // Log.v("DistoX", "Loop " + sb.toString() ); 
      if ( len > 0 ) {
        double error = (dl*100) / len;
        double angle = TDMath.sqrt( nr ) * dl / len * TDMath.RAD2DEG;
        String description = String.format(Locale.US, format, sfname, stname, nr, dl, len, dh, dv, error, angle );
        // Log.v("DistoX", "Desc " + description );
        num.addClosure( new NumClosure( description, sb.toString() ) );
      }
    }
  }
  
}
