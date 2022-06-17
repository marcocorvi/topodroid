/* @file DialogMeasure.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D measure result dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;

// import java.io.StringWriter;
// import java.io.PrintWriter;

import java.util.Locale;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

class DialogMeasure extends MyDialog 
                    implements View.OnClickListener
{
  // private Cave3DView mCave3Dview;
  private TglMeasure mMeasure;

  /** cstr
   * @param context context
   * @param measure measurement results
   */
  public DialogMeasure( Context context, TglMeasure measure )
  {
    super( context, null, R.string.DialogMeasure ); // null app
    mMeasure   = measure;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      initLayout( R.layout.cave3d_station_distance_dialog, R.string.STATIONS_DISTANCE );

      ((Button) findViewById( R.id.button_close )).setOnClickListener( this );

      TextView tv = ( TextView ) findViewById(R.id.st_name);
      tv.setText( mMeasure.st1.getFullName() + " - " + mMeasure.st2.getFullName() );

      tv = ( TextView ) findViewById(R.id.st_east);
      tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.cave_east), mMeasure.de ) );
      tv = ( TextView ) findViewById(R.id.st_north);
      tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.cave_north), mMeasure.dn ) );
      tv = ( TextView ) findViewById(R.id.st_vert);
      tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.cave_vert), mMeasure.dz ) );
      tv = ( TextView ) findViewById(R.id.st_dist);
      tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.cave_distance), mMeasure.d3 ));
      tv = ( TextView ) findViewById(R.id.st_horz);
      tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.horz_distance), mMeasure.d2 ));
      tv = ( TextView ) findViewById(R.id.st_angle);
      tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.cave_angle), mMeasure.azimuth, mMeasure.clino ));

      tv = ( TextView ) findViewById(R.id.st_cave_pathlength);
      if ( mMeasure.dcave > 0 ) {
        tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.cave_length), mMeasure.dcave, mMeasure.getDistPos(), mMeasure.getDistNeg() ));
      } else {
        tv.setVisibility( View.GONE );
      }

  }

  /** respond to user taps: close the dialog
   * @param v tapped view
   * @note only button_close
   */
  @Override
  public void onClick( View v )
  {
    // TDLog.v( "Measure onClick()" );
    dismiss();
  }
}

