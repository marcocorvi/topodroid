/* @file DialogSurface.java
 *
 * @author marco corvi
 * @date mar 2020
 *
 * @brief DEM surface alpha dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.utils.TDLog;
// import com.topodroid.Cave3X.R;
// import com.topodroid.Cave3X.TDandroid;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Paint;

class DialogSurface extends Dialog 
                    implements View.OnClickListener
{
  private Context mContext;

  private SeekBar mETalpha;
  // private Button mBtnLoadDEM;
  // private Button mBtnLoadTexture;
  private CheckBox mCBproj;
  private CheckBox mCBtexture;
  // private EditText mDemFile;
  // private EditText mTextureFile;

  private TopoGL mApp;

  private boolean mHasLocation; // WITH-GPS
  private CheckBox mCBgps;
  private EditText mEast;
  private EditText mNorth;


  public DialogSurface( Context ctx, TopoGL app )
  {
    super( ctx );
    mContext = ctx;
    mApp  = app;
    // mHasLocation = FeatureChecker.checkLocation( ctx ); // WITH-GPS
    mHasLocation = TDandroid.checkLocation( ctx ); // WITH-GPS
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cave3d_surface_alpha_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mETalpha = ( SeekBar ) findViewById(R.id.alpha);
    mETalpha.setProgress( (int)(GlSurface.mAlpha * 255) );

    TextView mDemFile = (TextView) findViewById( R.id.dem_file );
    
    if ( mApp.mDEMname != null ) {
      mDemFile.setText( String.format( mContext.getResources().getString( R.string.dem_file ), mApp.mDEMname ) );
    } else {
      mDemFile.setVisibility( View.GONE );
    }

    mCBproj = (CheckBox) findViewById( R.id.projection );
    mCBtexture = (CheckBox) findViewById( R.id.texture );

    mCBgps = (CheckBox) findViewById( R.id.gps ); // WITH-GPS
    mEast  = (EditText) findViewById( R.id.east );
    mNorth = (EditText) findViewById( R.id.north );

    TextView mTextureFile = (TextView) findViewById( R.id.texture_file );
    Button btn_texture = (Button)findViewById( R.id.texture_load );
    if ( mApp.hasSurface() ) {
      if ( mApp.mTextureName != null ) {
        mTextureFile.setText( String.format( mContext.getResources().getString( R.string.texture_file ), mApp.mTextureName ) );
      } else {
        mTextureFile.setVisibility( View.GONE );
      }
      btn_texture.setOnClickListener( this );
    } else {
      mTextureFile.setVisibility( View.GONE );
      btn_texture.setVisibility( View.GONE );
      mCBgps.setVisibility( View.GONE ); // WITH-GPS
      mEast.setVisibility( View.GONE );
      mNorth.setVisibility( View.GONE );
    }

    mCBproj.setChecked( GlModel.surfaceLegsMode );
    mCBtexture.setChecked( GlModel.surfaceTexture );
    if ( mHasLocation ) { // WITH-GPS
      mCBgps.setChecked( mApp.getGPSstatus() );
    } else {
      mCBgps.setVisibility( View.GONE );
    }

    findViewById( R.id.button_ok ).setOnClickListener( this );
    findViewById( R.id.button_cancel ).setOnClickListener( this );
    findViewById( R.id.dem_load ).setOnClickListener( this );

    setTitle( R.string.ctitle_surface_alpha );
  }

  public void onClick(View view)
  {
    if ( view.getId() == R.id.dem_load ) {
      // (new DialogDEM( mContext, mApp )).show();
      mApp.selectDEMFile();
    } else if ( view.getId() == R.id.texture_load ) {
      // (new DialogTexture( mContext, mApp )).show();
      mApp.selectTextureFile();
    } else if ( view.getId() == R.id.button_ok ) {
      GlModel.surfaceLegsMode = mCBproj.isChecked();
      GlModel.surfaceTexture  = mCBtexture.isChecked();

      if ( mHasLocation ) { // WITH-GPS
        mApp.setGPSstatus( mCBgps.isChecked() );
      }
      if ( mEast.getText() != null && mNorth.getText() != null ) {
        try {
          double e = Double.parseDouble( mEast.getText().toString() );
          double n = Double.parseDouble( mNorth.getText().toString() );
          mApp.addGPSpoint( e, n );
        } catch ( NumberFormatException e ) { }
      }

      // TDLog.v( "Surface onClick()" );
      int alpha = mETalpha.getProgress();
      if ( 0 < alpha && alpha < 256 ) GlSurface.setAlpha( alpha/255.0f );

    // } else if ( view.getId() == R.id.button_cancel ) {
    //   // nothing
    }
    dismiss();
  }  

}

