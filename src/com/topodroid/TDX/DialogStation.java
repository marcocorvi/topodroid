/* @file DialogStation.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D drawing infos dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

class DialogStation extends MyDialog 
                    implements View.OnClickListener
{
  private Button mBtDistance;
  private Button mBtCenter;
  private TextView mTvSurface;

  private TopoGL     mApp;
  private TglParser  mParser;
  private Cave3DStation  mStation;
  private DEMsurface  mSurface;

  public DialogStation( Context context, TopoGL app, TglParser parser, String fullname, DEMsurface surface )
  {
    super(context, R.string.DialogStation );
    mApp     = app;
    mParser  = parser;
    mStation = mParser.getStation( fullname );
    mSurface = ( surface != null )? surface : parser.getSurface();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      initLayout( R.layout.cave3d_station_dialog, R.string.STATIONS );

      TextView tv = ( TextView ) findViewById(R.id.st_name);
      tv.setText( mStation.getFullName() );

      StringWriter sw1 = new StringWriter();
      PrintWriter  pw1 = new PrintWriter( sw1 );
      StringWriter sw2 = new StringWriter();
      PrintWriter  pw2 = new PrintWriter( sw2 );
      StringWriter sw3 = new StringWriter();
      PrintWriter  pw3 = new PrintWriter( sw3 );
      pw1.format(Locale.US, "E %.2f", mStation.x );
      pw2.format(Locale.US, "N %.2f", mStation.y );
      pw3.format(Locale.US, "Z %.2f", mStation.z );

      tv = ( TextView ) findViewById(R.id.st_east);
      tv.setText( sw1.getBuffer().toString() );
      tv = ( TextView ) findViewById(R.id.st_north);
      tv.setText( sw2.getBuffer().toString() );
      tv = ( TextView ) findViewById(R.id.st_vert);
      tv.setText( sw3.getBuffer().toString() );

      ((Button) findViewById( R.id.button_close )).setOnClickListener( this );

      mTvSurface  = (TextView) findViewById( R.id.st_surface );
      if ( mSurface != null ) {
        double zs = mSurface.computeZ( mStation.x, mStation.y );
        // TDLog.v("Surface station " + mStation.z + " surface " + zs );
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        pw.format(Locale.US, "Depth %.1f", zs - mStation.z );
        mTvSurface.setText( sw.getBuffer().toString() );
      } else {
        mTvSurface.setVisibility( View.GONE );
      }

  }

  @Override
  public void onClick(View v)
  {
    // only for button_close
    mApp.closeCurrentStation();
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    mApp.closeCurrentStation();
    super.onBackPressed();
  }
}

