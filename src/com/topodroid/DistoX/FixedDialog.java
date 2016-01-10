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


public class FixedDialog extends Dialog
                         implements View.OnClickListener
{
  private Context mContext;
  private SurveyActivity mParent;
  private LocationDialog mSubParent;
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
  private Button   mButtonView;
  private Button   mButtonGeomag;
  private Button   mButtonStation;
  private Button   mButtonConvert;
  private Button   mButtonOrthometric;
  private Button   mButtonEllipsoidic;
  private Button   mButtonCancel;

  private MyKeyboard mKeyboard = null;

  public FixedDialog( Context context, SurveyActivity parent, LocationDialog sub_parent, FixedInfo fxd )
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
    mETalt.setText( String.format( Locale.ENGLISH, "%.0f", alt ) );
  }

  private void setETasl( double asl )
  {
    if ( asl <= -999 ) return;
    mETasl.setText( String.format( Locale.ENGLISH, "%.0f", asl ) );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_FIXED, "FixedDialog onCreate" );
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

    mButtonView = (Button) findViewById( R.id.fix_view );

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

    mETlng.setText( String.format( Locale.ENGLISH, "%.6f", mFxd.lng ) );

    mETlat.setText( String.format( Locale.ENGLISH, "%.6f", mFxd.lat ) );

    setETalt( mFxd.alt );
    setETasl( mFxd.asl );
    
    mButtonGeomag.setOnClickListener( this );
    mButtonDrop.setOnClickListener( this );
    mButtonDecl.setOnClickListener( this );
    mButtonView.setOnClickListener( this );
    mButtonStation.setOnClickListener( this );
    mButtonConvert.setOnClickListener( this );
    mButtonOrthometric.setOnClickListener( this );
    mButtonEllipsoidic.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                  R.xml.my_keyboard, R.xml.my_keyboard_qwerty );

    if ( TDSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mETlng, MyKeyboard.FLAG_POINT_DEGREE );
      MyKeyboard.registerEditText( mKeyboard, mETlat, MyKeyboard.FLAG_POINT_DEGREE );
      MyKeyboard.registerEditText( mKeyboard, mETalt, MyKeyboard.FLAG_POINT  );
      MyKeyboard.registerEditText( mKeyboard, mETasl, MyKeyboard.FLAG_POINT  );
      MyKeyboard.registerEditText( mKeyboard, mETdecl, MyKeyboard.FLAG_POINT  );
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETstation, flag );
    } else {
      mKeyboard.hide();
      mETlng.setInputType( TopoDroidConst.NUMBER_DECIMAL_SIGNED );
      mETlat.setInputType( TopoDroidConst.NUMBER_DECIMAL_SIGNED );
      mETalt.setInputType( TopoDroidConst.NUMBER_DECIMAL );
      mETasl.setInputType( TopoDroidConst.NUMBER_DECIMAL );
      mETdecl.setInputType( TopoDroidConst.NUMBER_DECIMAL_SIGNED );
      if ( TDSetting.mStationNames == 1 ) {
        mETstation.setInputType( TopoDroidConst.NUMBER_DECIMAL );
      }
    }
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "FixedDialog onClick() button " + b.getText().toString() );

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
      WorldMagneticModel wmm = new WorldMagneticModel( mContext );
      mFxd.alt = wmm.geoidToEllipsoid( mFxd.lat, mFxd.lng, mFxd.asl );
      mParent.updateFixedAltitude( mFxd );
      setETalt( mFxd.alt );
      mSubParent.refreshList();
      return;
    } else if ( b == mButtonEllipsoidic ) { // compute Ellipsoidic --> Orthometric
      try {
        mFxd.alt = Double.parseDouble( mETalt.getText().toString() );
      } catch ( NumberFormatException e ) {
        mETalt.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
        return;
      }
      WorldMagneticModel wmm = new WorldMagneticModel( mContext );
      mFxd.asl = wmm.ellipsoidToGeoid( mFxd.lat, mFxd.lng, mFxd.alt );
      mParent.updateFixedAltitude( mFxd );
      setETasl( mFxd.asl );
      mSubParent.refreshList();
      return;
    } else if ( b == mButtonGeomag ) {
      WorldMagneticModel wmm = new WorldMagneticModel( mContext );
      int year = TopoDroidUtil.year();
      int month = TopoDroidUtil.month();
      int day = TopoDroidUtil.day();
      // Log.v("DistoX", " Date " + year + " " + month + " " + day );

      MagElement elem = wmm.computeMagElement( mFxd.lat, mFxd.lng, mFxd.alt, year, month, day );
      mETdecl.setText( String.format(Locale.ENGLISH, "%.4f", elem.Decl ) );
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
    } else if ( b == mButtonView ) {
      Uri uri = Uri.parse( "geo:" + mFxd.lat + "," + mFxd.lng );
      mContext.startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
    } else if ( b == mButtonDrop ) {
      mParent.dropFixed( mFxd );
      mSubParent.refreshList();
      dismiss();
    // } else { // b == mButtonCancel
    //   dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( TDSetting.mKeyboard ) {
      if ( mKeyboard.isVisible() ) {
        mKeyboard.hide();
        return;
      }
    }
    dismiss();
  }

}
