/* @file DialogMeasure.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D measure result dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.Cave3X.R;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

class DialogMeasure extends Dialog 
                    implements View.OnClickListener
{
    // private Cave3DView mCave3Dview;
    private Context mContext;
    private TglMeasure mMeasure;

    public DialogMeasure( Context context, TglMeasure measure )
    {
      super(context);
      mContext   = context;
      mMeasure   = measure;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cave3d_station_distance_dialog);
        getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

        Button btn = (Button) findViewById( R.id.btn_close );
        btn.setOnClickListener( this );

        TextView tv = ( TextView ) findViewById(R.id.st_name);
        tv.setText( mMeasure.st1.name + " - " + mMeasure.st2.name );

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
          tv.setText( String.format(Locale.US, mContext.getResources().getString(R.string.cave_length), mMeasure.dcave ));
        } else {
          tv.setVisibility( View.GONE );
        }

        setTitle( R.string.STATIONS_DISTANCE );
    }

    @Override
    public void onClick(View v)
    {
      // TDLog.v( "Measure onClick()" );
      dismiss();
    }
}

