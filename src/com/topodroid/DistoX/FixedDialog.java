/* @file FixedDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey fix point edit dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.regex.Pattern;
import java.util.Locale;

import android.widget.ArrayAdapter;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
import android.inputmethodservice.KeyboardView;

import android.net.Uri;

// import android.widget.Toast;

import android.util.Log;


public class FixedDialog extends MyDialog
                         implements View.OnClickListener
{
  private FixedActivity mParent;
  private FixedInfo mFxd;

  // private TextView mTVdata;
  private TextView mTVlng;
  private TextView mTVlat;
  private TextView mTValt;
  private TextView mTVasl;

  private TextView mTVstation;
  private EditText mETcomment;
  private TextView mTVdecl;
  private TextView mTVcrs;
  private TextView mTVfix_station;
  private Button   mButtonDrop;
  private Button   mButtonDecl;
  private Button   mButtonView;
  // private Button   mButtonWmm;
  private Button   mButtonStation;
  private Button   mButtonConvert;
  private Button   mButtonCancel;

  private WorldMagneticModel mWMM;

  public FixedDialog( Context context, FixedActivity parent, FixedInfo fxd )
  {
    super( context, R.string.FixedDialog );
    mParent      = parent;
    mFxd         = fxd;
    mWMM = new WorldMagneticModel( mContext );
  }
  
  void setCSto( String cs )
  {
    mTVcrs.setText( cs );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_FIXED, "FixedDialog onCreate" );
    setContentView(R.layout.fixed_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mTVlng = (TextView) findViewById( R.id.fix_lng );
    mTVlat = (TextView) findViewById( R.id.fix_lat );
    mTValt = (TextView) findViewById( R.id.fix_alt );
    mTVasl = (TextView) findViewById( R.id.fix_asl );

    mTVdecl = (EditText) findViewById( R.id.fix_decl );
    {
      int year = TopoDroidUtil.year();
      int month = TopoDroidUtil.month();
      int day = TopoDroidUtil.day();
      MagElement elem = mWMM.computeMagElement( mFxd.lat, mFxd.lng, mFxd.alt, year, month, day );
      mTVdecl.setText( String.format(Locale.ENGLISH, "%.4f", elem.Decl ) );
    }

    mButtonDecl = (Button) findViewById( R.id.fix_save_decl );
    mButtonView = (Button) findViewById( R.id.fix_view );
    mButtonConvert = (Button) findViewById( R.id.fix_convert );
    mButtonStation = (Button) findViewById( R.id.fix_save_station );

    mTVcrs     = (TextView) findViewById( R.id.fix_crs );
    mTVstation = (TextView) findViewById( R.id.fix_station );
    mETcomment = (EditText) findViewById( R.id.fix_comment );
    mTVstation.setText( mFxd.name );
    mETcomment.setText( mFxd.name );

    mButtonDrop    = (Button) findViewById(R.id.fix_drop );
    // mButtonOK      = (Button) findViewById(R.id.fix_ok );
    // mButtonCancel  = (Button) findViewById(R.id.fix_cancel );

    mTVlng.setText( String.format( Locale.ENGLISH, "%.6f", mFxd.lng ) );
    mTVlat.setText( String.format( Locale.ENGLISH, "%.6f", mFxd.lat ) );
    mTValt.setText( String.format( Locale.ENGLISH, "%.0f", mFxd.alt ) );
    mTVasl.setText( String.format( Locale.ENGLISH, "%.0f", mFxd.asl ) );
    
    mButtonDrop.setOnClickListener( this );
    mButtonDecl.setOnClickListener( this );
    mButtonView.setOnClickListener( this );
    mButtonStation.setOnClickListener( this );
    mButtonConvert.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "FixedDialog onClick() button " + b.getText().toString() );

    if ( b == mButtonStation ) {
      String comment = mETcomment.getText().toString();
      if ( comment == null ) comment = "";
      mFxd.comment = comment;
      mParent.updateFixedComment( mFxd, comment );
    } else if ( b == mButtonConvert ) {
      if ( mTVcrs.getText() != null ) {
        mParent.tryProj4( this, mTVcrs.getText().toString(), mFxd );
      }
      return;
    } else if ( b == mButtonDecl ) {
      if ( mTVdecl.getText() != null ) {
        String decl_str = mTVdecl.getText().toString();
        if ( decl_str != null && decl_str.length() > 0 ) {
          decl_str = decl_str.replaceAll( ",", "." );
          try {
            mParent.setDeclination( Float.parseFloat( decl_str ) );
          } catch ( NumberFormatException e ) {
            String error = mContext.getResources().getString( R.string.error_declination_number );
            mTVdecl.setError( error );
            return;
          }
        }
      }
    } else if ( b == mButtonView ) {
      Uri uri = Uri.parse( "geo:" + mFxd.lat + "," + mFxd.lng );
      mContext.startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
    } else if ( b == mButtonDrop ) {
      mParent.dropFixed( mFxd );
      dismiss();
    // } else { // b == mButtonCancel
    //   dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    dismiss();
  }

}
