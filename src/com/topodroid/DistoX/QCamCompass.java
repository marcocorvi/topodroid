/** @file QCamCompass.java
 *
 * @author marco corvi
 * @date jan. 2017
 *
 * @brief TopoDroid quick cam compass
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

import android.widget.Button;
import android.widget.TextView;
// import android.widget.FrameLayout;
import android.widget.Toast;

import android.util.Log;

public class QCamCompass extends Dialog
                         implements OnClickListener
                                  , IBearingAndClino
{
  Context mContext;
  QCamDrawingSurface mSurface;
  QCamBox mBox;
  Button buttonClick;
  Button buttonSave;
  Button buttonCancel;
  TextView mTVdata;
  float mBearing;
  float mClino;
  boolean mHasBearingAndClino;
  ShotNewDialog mShotNewDialog;

  QCamCompass( Context context, ShotNewDialog dialog )
  {
    super( context );
    mContext = context;
    mShotNewDialog = dialog;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature( Window.FEATURE_NO_TITLE );
    setContentView(R.layout.qcam_compass);

    mSurface = (QCamDrawingSurface) findViewById( R.id.drawingSurface );
    mSurface.mQCam = this;

    buttonClick  = (Button) findViewById(R.id.buttonClick);
    buttonSave   = (Button) findViewById(R.id.buttonSave);
    buttonCancel = (Button) findViewById(R.id.buttonQuit);

    buttonClick.setOnClickListener( this );
    buttonSave.setOnClickListener( this );
    buttonCancel.setOnClickListener( this );

    mTVdata = (TextView)findViewById( R.id.data );
    mHasBearingAndClino = false; 

    mBox = new QCamBox( mContext );
    addContentView( mBox, new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
  }

  // implements
  public void setBearingAndClino( float b, float c )
  {
    // Log.v("DistoX", "QCam compass set bearing and clino " + b + " " + c );
    mBearing = b;
    mClino   = c;
    mTVdata.setText( String.format("%.2f %.2f", mBearing, mClino ) );
    mHasBearingAndClino = true;
  }

  @Override
  public void onClick(View v)
  {
    Button b = (Button)v;
    if ( b == buttonClick ) {
      TimerTask timer = new TimerTask( mContext, this, -TimerTask.Z_AXIS ); 
      timer.execute();
      return;
    } else if ( b == buttonSave ) {
      if ( mHasBearingAndClino && mShotNewDialog != null ) {
        mShotNewDialog.setBearingAndClino( mBearing, mClino );
      }
    } else if ( b == buttonCancel ) {
    }
    mSurface.close();
    try { Thread.sleep( 500 ); } catch ( InterruptedException e ) { }
    dismiss();
  }
}
