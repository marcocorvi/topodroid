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

import android.os.AsyncTask;

class ClosureTask extends AsyncTask< Void, Void, Void >
{
  TDNum  num;
  String format;
  NumStation sf;
  NumStation st;
  float d, b, c;

  ClosureTask( TDNum num0, String fmt, NumStation sf0, NumStation st0, float d0, float b0, float c0 )
  {
    num    = num0;
    format = fmt;
    sf = sf0;
    st = st0;
    d  = d0;
    b  = b0;
    c  = c0;
  }

  @Override
  protected Void doInBackground( Void ... v ) 
  {
    NumShortpath short_path = num.shortestPath( sf, st); 
    if ( short_path != null ) {
      num.addClosure( num.getClosureError( format, st, sf, d, b, c, short_path, Math.abs( d ) ) );
    }
    return null;
  }
  
}
