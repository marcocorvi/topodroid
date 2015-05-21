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

import java.io.StringWriter;
import java.io.PrintWriter;

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
  private boolean mAltIsAsl;

  private GridView mGrid;
  private ArrayAdapter< View > mAdapter;

  // private TextView mTVdata;
  private EditText mETlng;
  private EditText mETlat;
  private EditText mETalt;
  private EditText mETasl;

  private EditText mETstation;
  private EditText mETdecl;
  private TextView mTVcrs;
  private TextView mTVfix_station;
  private Button   mButtonDrop;
  private Button   mButtonDecl;
  private Button   mButtonGeomag;
  private Button   mButtonStation;
  private Button   mButtonConvert;
  private Button   mButtonOrthometric;
  private Button   mButtonEllipsoidic;
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

  private void setETalt( double alt )
  {
    if ( alt <= -999 ) return;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.ENGLISH, "%.0f", alt );
    mETalt.setText( sw.getBuffer().toString() );
  }

  private void setETasl( double asl )
  {
    if ( asl <= -999 ) return;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.ENGLISH, "%.0f", asl );
    mETasl.setText( sw.getBuffer().toString() );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidLog.Log( TopoDroidLog.LOG_FIXED, "FixedDialog onCreate" );
    setContentView(R.layout.fixed_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mETlng = (EditText) findViewById( R.id.fix_lng );
    mETlat = (EditText) findViewById( R.id.fix_lat );
    mETalt = (EditText) findViewById( R.id.fix_alt );
    mETasl = (EditText) findViewById( R.id.fix_asl );

    mButtonGeomag = (Button) findViewById( R.id.fix_geomag );
    mETdecl = (EditText) findViewById( R.id.fix_decl );
    mETdecl.setText( Float.toString( mParent.getDeclination() ) );
    mButtonDecl = (Button) findViewById( R.id.fix_save_decl );

    mButtonConvert = (Button) findViewById( R.id.fix_convert );
    mTVcrs = (TextView) findViewById( R.id.fix_crs );

    mButtonOrthometric = (Button) findViewById( R.id.fix_orthometric );
    mButtonEllipsoidic = (Button) findViewById( R.id.fix_ellipsoidic );

    // mBTstation    = (Button) findViewById( R.id.fix_station );
    mETstation    = (EditText) findViewById( R.id.fix_station_value );
    mETstation.setText( mFxd.name );
    mButtonStation = (Button) findViewById( R.id.fix_save_station );

    mButtonDrop    = (Button) findViewById(R.id.fix_drop );
    // mButtonOK      = (Button) findViewById(R.id.fix_ok );
    // mButtonCancel  = (Button) findViewById(R.id.fix_cancel );

    // setTitle( mFxd.toLocString() );
    StringWriter sw1 = new StringWriter();
    PrintWriter  pw1 = new PrintWriter( sw1 );
    pw1.format( Locale.ENGLISH, "%.6f", mFxd.lng );
    mETlng.setText( sw1.getBuffer().toString() );

    StringWriter sw2 = new StringWriter();
    PrintWriter  pw2 = new PrintWriter( sw2 );
    pw2.format( Locale.ENGLISH, "%.6f", mFxd.lat );
    mETlat.setText( sw2.getBuffer().toString() );

    setETalt( mFxd.alt );
    setETasl( mFxd.asl );
    
    mButtonGeomag.setOnClickListener( this );
    mButtonDrop.setOnClickListener( this );
    mButtonDecl.setOnClickListener( this );
    mButtonStation.setOnClickListener( this );
    mButtonConvert.setOnClickListener( this );
    mButtonOrthometric.setOnClickListener( this );
    mButtonEllipsoidic.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "FixedDialog onClick() button " + b.getText().toString() );

    if ( b == mButtonStation ) {
      double lng = FixedInfo.string2double( mETlng.getText().toString() );
      if ( lng < -1000 ) {
        mETlng.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
        return;
      }
      double lat = FixedInfo.string2double( mETlat.getText().toString() );
      if ( lat < -1000 ) {
        mETlat.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
        return;
      }
      double alt = -1000;
      String altstr = mETalt.getText().toString();
      if ( altstr != null && altstr.length() > 0 ) {
        try {
          alt = Double.parseDouble( altstr );
        } catch ( NumberFormatException e ) {
          mETalt.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
          return;
        }
      }
      double asl = -1000;
      String aslstr = mETasl.getText().toString();
      if ( aslstr != null && aslstr.length() > 0 ) {
        try {
          asl = Double.parseDouble( aslstr );
        } catch ( NumberFormatException e ) {
          mETasl.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
          return;
        }
      }

      String station = mETstation.getText().toString().trim();
      if ( station.length() == 0 ) {
        mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
        return;
      }
      if ( mParent.updateFixed( mFxd, station ) ) {
        mFxd.name = station;
        mFxd.lng = lng;
        mFxd.lat = lat;
        mFxd.alt = alt;
        mFxd.asl = asl;
        mSubParent.refreshList();
        mParent.updateFixedData( mFxd );
        mSubParent.refreshList();
      } else {
        mETstation.setError( mContext.getResources().getString( R.string.error_station_already_fixed ) );
        return;
      }
    } else if ( b == mButtonConvert ) {
      if ( mTVcrs.getText() != null ) {
        mParent.tryProj4( this, mTVcrs.getText().toString(), mFxd );
      }
      return;

    } else if ( b == mButtonOrthometric ) { // compute Orthometric --> Ellipsoidic
      try {
        mFxd.asl = Double.parseDouble( mETasl.getText().toString() );
      } catch ( NumberFormatException e ) {
        mETasl.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
        return;
      }
      if ( mFxd.asl > -999 ) {
        double gh = GeodeticHeight.geodeticHeight( mFxd.lat, mFxd.lng );
        if ( gh > -999 ) {
          mFxd.alt = mFxd.asl + gh;
          mParent.updateFixedAltitude( mFxd );
          setETalt( mFxd.alt );
          mSubParent.refreshList();
        } else {
          Toast.makeText( mParent, R.string.lookup_fail, Toast.LENGTH_SHORT).show();
        }
      }
      return;
    } else if ( b == mButtonEllipsoidic ) { // compute Ellipsoidic --> Orthometric
      try {
        mFxd.alt = Double.parseDouble( mETalt.getText().toString() );
      } catch ( NumberFormatException e ) {
        mETalt.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
        return;
      }
      if ( mFxd.alt > -999 ) {
        double gh = GeodeticHeight.geodeticHeight( mFxd.lat, mFxd.lng );
        if ( gh > -999 ) {
          mFxd.asl = mFxd.alt - gh;
          mParent.updateFixedAltitude( mFxd );
          setETasl( mFxd.asl );
          mSubParent.refreshList();
        } else {
          Toast.makeText( mParent, R.string.lookup_fail, Toast.LENGTH_SHORT).show();
        }
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
          decl_str = decl_str.replaceAll( ",", "." );
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
