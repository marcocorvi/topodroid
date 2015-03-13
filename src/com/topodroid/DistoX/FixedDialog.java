/* @file FixedDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120603 created 
 * 20130131 intent: Proj4 coord conversion
 * 20140609 geomag
 */
package com.topodroid.DistoX;

// import java.Thread;
// import java.util.regex.Pattern;
import java.util.Locale;

import android.widget.ArrayAdapter;

import android.app.Dialog;
import android.os.Bundle;

import android.text.InputType;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.text.InputType;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

import android.widget.Toast;

// import android.util.Log;


public class FixedDialog extends Dialog
                         implements View.OnClickListener
{
  private Context mContext;
  private SurveyActivity mParent;
  private DistoXLocation mSubParent;
  private FixedInfo mFxd;

  private GridView mGrid;
  private ArrayAdapter< View > mAdapter;

  // private TextView mTVdata;
  private EditText mETstation;
  private EditText mETdecl;
  private TextView mTVcrs;
  private TextView mTVfix_station;
  private Button   mButtonDrop;
  private Button   mButtonDecl;
  private Button   mButtonGeomag;
  private Button   mButtonStation;
  private Button   mButtonConvert;
  private Button   mButtonCancel;

  public FixedDialog( Context context, SurveyActivity parent, DistoXLocation sub_parent, FixedInfo fxd )
  {
    super(context);
    mContext     = context;
    mParent      = parent;
    mSubParent   = sub_parent;
    mFxd         = fxd;
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
    // TopoDroidLog.Log( TopoDroidLog.LOG_FIXED, "FixedDialog onCreate" );
    setContentView(R.layout.fixed_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mButtonGeomag = (Button) findViewById( R.id.fix_geomag );
    mETdecl = (EditText) findViewById( R.id.fix_decl );
    mETdecl.setText( Float.toString( mParent.getDeclination() ) );
    mButtonDecl = (Button) findViewById( R.id.fix_save_decl );

    mButtonConvert = (Button) findViewById( R.id.fix_convert );
    mTVcrs = (TextView) findViewById( R.id.fix_crs );

    // mBTstation    = (Button) findViewById( R.id.fix_station );
    mETstation    = (EditText) findViewById( R.id.fix_station_value );
    mETstation.setText( mFxd.name );
    mButtonStation = (Button) findViewById( R.id.fix_save_station );

    mButtonDrop    = (Button) findViewById(R.id.fix_drop );
    // mButtonOK      = (Button) findViewById(R.id.fix_ok );
    // mButtonCancel  = (Button) findViewById(R.id.fix_cancel );

    setTitle( mFxd.toLocString() );
    
    mButtonGeomag.setOnClickListener( this );
    mButtonDrop.setOnClickListener( this );
    mButtonDecl.setOnClickListener( this );
    mButtonStation.setOnClickListener( this );
    mButtonConvert.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "FixedDialog onClick() button " + b.getText().toString() );

    if ( b == mButtonStation ) {
      String station = mETstation.getText().toString().trim();
      if ( station.length() == 0 ) {
        String error = mContext.getResources().getString( R.string.error_station_required );
        mETstation.setError( error );
        return;
      }
      if ( mParent.updateFixed( mFxd, station ) ) {
        mFxd.name = station;
        mSubParent.refreshList();
      } else {
        String error = mContext.getResources().getString( R.string.error_station_already_fixed );
        mETstation.setError( error );
        return;
      }
    } else if ( b == mButtonConvert ) {
      if ( mTVcrs.getText() != null ) {
        mParent.tryProj4( this, mTVcrs.getText().toString(), mFxd );
      }
      return;
    } else if ( b == mButtonGeomag ) {
      float decl = GeodeticHeight.getGeomag( mFxd );
      if ( decl > -180 ) {
        mETdecl.setText( String.format(Locale.ENGLISH, "%.4f", decl ) );  // can skip Locale
      } else {
        Toast.makeText( mParent, R.string.no_geomag, Toast.LENGTH_SHORT).show();
      }
      return;
    } else if ( b == mButtonDecl ) {
      if ( mETdecl.getText() != null ) {
        String decl_str = mETdecl.getText().toString();
        if ( decl_str != null && decl_str.length() > 0 ) {
          try {
            float decl = Float.parseFloat( decl_str );
            mParent.setDeclination( decl );
          } catch ( NumberFormatException e ) {
            String error = mContext.getResources().getString( R.string.error_declination_number );
            mETdecl.setError( error );
            return;
          }
        }
      }
    } else if ( b == mButtonDrop ) {
      mParent.dropFixed( mFxd );
      mSubParent.refreshList();
      dismiss();
    // } else { // b == mButtonCancel
    //   dismiss();
    }
  }
}
