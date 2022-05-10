/* @file FixedAddDialog.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief TopoDroid dialog to enter long-lat data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import com.topodroid.mag.WorldMagneticModel;

import java.util.Locale;

import android.net.Uri;

import android.os.Bundle;

import android.content.Context;
import android.content.Intent;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;

// import android.text.ClipboardManager; // deprecated API-11
import android.content.ClipboardManager;
import android.content.ClipData;

import android.inputmethodservice.KeyboardView;

class FixedAddDialog extends MyDialog
                     implements View.OnClickListener
                     , View.OnLongClickListener
{
  private final FixedActivity mParent;

  private MyKeyboard mKeyboard = null;
  private EditText mETstation;
  private EditText mETcomment;
  private EditText mETlng;
  private EditText mETlat;
  private EditText mEThell; // altitude ellipsoid
  private EditText mEThgeo; // altitude geoid

  private Button   mBtnNS;
  private Button   mBtnEW;
  private Button   mBtnOK;
  private Button   mBtnProj4;
  private Button   mBtnView;
  private Button   mBtnClipboard;

  private double  mLng, mLat, mHEll, mHGeo;
  private boolean mNorth, mEast;

  FixedAddDialog( Context context, FixedActivity parent )
  {
    super( context, R.string.FixedAddDialog );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.fixed_add_dialog, R.string.title_fixed_add );

    mETstation = (EditText) findViewById(R.id.edit_name );
    mETcomment = (EditText) findViewById(R.id.edit_comment );

    mETstation.setOnLongClickListener( this );

    mETlng  = (EditText) findViewById( R.id.edit_long );
    mETlat  = (EditText) findViewById( R.id.edit_lat  );
    mEThell = (EditText) findViewById( R.id.edit_alt  );
    mEThgeo = (EditText) findViewById( R.id.edit_asl  );

    mNorth = true;
    mEast  = true;

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      if ( TDSetting.mStationNames == 1 ) {
        MyKeyboard.registerEditText( mKeyboard, mETstation, MyKeyboard.FLAG_POINT );
      } else {
        MyKeyboard.registerEditText( mKeyboard, mETstation, MyKeyboard.FLAG_POINT_LCASE_2ND );
      }
      MyKeyboard.registerEditText( mKeyboard, mETlng,  MyKeyboard.FLAG_POINT_SIGN_DEGREE );
      MyKeyboard.registerEditText( mKeyboard, mETlat,  MyKeyboard.FLAG_POINT_SIGN_DEGREE );
      MyKeyboard.registerEditText( mKeyboard, mEThell, MyKeyboard.FLAG_POINT_SIGN  );
      MyKeyboard.registerEditText( mKeyboard, mEThgeo, MyKeyboard.FLAG_POINT_SIGN  );
    } else {
      mKeyboard.hide();
      mETlng.setInputType(  TDConst.TEXT );
      mETlat.setInputType(  TDConst.TEXT );
      mEThell.setInputType( TDConst.NUMBER_DECIMAL );
      mEThgeo.setInputType( TDConst.NUMBER_DECIMAL );
      if ( TDSetting.mStationNames == 1 ) {
        mETstation.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }

    mBtnNS = (Button) findViewById(R.id.button_NS);
    mBtnNS.setOnClickListener( this );
    mBtnEW = (Button) findViewById(R.id.button_EW);
    mBtnEW.setOnClickListener( this );
    mBtnOK = (Button) findViewById(R.id.button_ok);
    mBtnOK.setOnClickListener( this );
    mBtnProj4 = (Button) findViewById(R.id.button_proj4);
    mBtnProj4.setOnClickListener( this );
    mBtnView = (Button) findViewById(R.id.button_view);
    mBtnView.setOnClickListener( this );
    mBtnClipboard = (Button) findViewById(R.id.button_clipboard);
    mBtnClipboard.setOnClickListener( this );
  }

  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  private boolean getLngLat()
  {
    String longit = mETlng.getText().toString();
    if ( /* longit == null || */ longit.length() == 0 ) {
      mETlng.setError( mContext.getResources().getString( R.string.error_long_required ) );
      return false;
    }
    String latit = mETlat.getText().toString();
    if ( /* latit == null || */ latit.length() == 0 ) {
      mETlat.setError( mContext.getResources().getString( R.string.error_lat_required) );
      return false;
    }
    mLng = FixedInfo.string2double( longit );
    if ( mLng < -1000 ) {
      mETlng.setError( mContext.getResources().getString( R.string.error_long_required ) );
      return false;
    } 
    mLat = FixedInfo.string2double( latit );
    if ( mLat < -1000 ) {
      mETlat.setError( mContext.getResources().getString( R.string.error_lat_required) );
      return false;
    }
    if ( ! mNorth ) mLat = - mLat;
    if ( ! mEast )  mLng = - mLng; 
    return true;
  }

  // set the cordinates 
  // @param lng longitude
  // @param lat latitude
  // @param alt geoid altitude
  void setCoordsGeo( double lng, double lat, double alt )
  {
    mETlng.setText(  FixedInfo.double2string( lng ) );
    mETlat.setText(  FixedInfo.double2string( lat ) );
    // mEThell.setText( String.format( Locale.US, "%.1f", alt ) );
    mEThgeo.setText( String.format( Locale.US, "%.1f", alt ) );
  }

  @Override
  public void onClick(View v) 
  {
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );
  
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "FixedAddDialog onClick() button " + b.getText().toString() ); 

    mNorth = mBtnNS.getText().toString().equals("N");
    mEast  = mBtnEW.getText().toString().equals("E");

    if ( b == mBtnNS ) {
      mBtnNS.setText( mNorth ? "S" : "N" );
      return;
    } else if ( b == mBtnEW ) {
      mBtnEW.setText( mEast ? "W" : "E" );
      return;
    } else if ( b == mBtnView ) {
      if ( getLngLat() ) {
        Uri uri = Uri.parse( "geo:" + mLat + "," + mLng + "?q=" + mLat + "," + mLng );
        mContext.startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
      }
    } else if ( b == mBtnClipboard ) {
      ClipboardManager cm = (ClipboardManager)mContext.getSystemService( Context.CLIPBOARD_SERVICE ); 
      boolean toast = true;
      if ( cm != null && cm.hasPrimaryClip() ) {
        // CharSequence text = cm.getText();
        ClipData clip = cm.getPrimaryClip();
        if ( clip.getItemCount() > 0 ) {
          CharSequence text = clip.getItemAt(0).coerceToText( mContext );
          if ( text != null ) {
            String str = text.toString();
            String[] val = str.split(",");
            if ( val.length > 1 ) {
              toast = false;
              mETlng.setText( val[0] );
              mETlat.setText( val[1] );
              if ( val.length > 2 ) mEThell.setText( val[2] );
            }
          }
        }
      }
      if ( toast ) {
        TDToast.makeBad( R.string.empty_clipboard );
      }
      return;
    } else if ( b == mBtnProj4 ) {
      mParent.getProj4Coords( this );
      return;
    } else if ( b == mBtnOK ) {
      String name = mETstation.getText().toString();
      if ( /* name == null || */ name.length() == 0 ) {
        mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
        return;
      }
      String comment = mETcomment.getText().toString();
      // if ( comment == null ) comment = "";
      if ( getLngLat() ) {
        String altit = mEThell.getText().toString();
        String aslit = mEThgeo.getText().toString();
        if ( ( /* altit == null || */ altit.length() == 0 ) && ( /* aslit == null || */ aslit.length() == 0 ) ) {
          mEThell.setError( mContext.getResources().getString( R.string.error_alt_required) );
          return;
        }
        mHEll = -1000.0;
        mHGeo = -1000.0;
        if ( ( /* altit == null || */ altit.length() == 0 ) ) {
          try {
            mHGeo = Double.parseDouble( aslit.replace(",", ".") );
          } catch ( NumberFormatException e ) {
            mEThgeo.setError( mContext.getResources().getString( R.string.error_invalid_number) );
            return;
          }
          WorldMagneticModel wmm = new WorldMagneticModel( mContext );
          mHEll = wmm.geoidToEllipsoid( mLat, mLng, mHGeo );
        } else {
          try {
            mHEll = Double.parseDouble( altit.replace(",", ".") );
          } catch ( NumberFormatException e ) {
            mEThell.setError( mContext.getResources().getString( R.string.error_invalid_number) );
            return;
          }
          if ( ( /* aslit == null || */ aslit.length() == 0 ) ) {
            WorldMagneticModel wmm = new WorldMagneticModel( mContext );
            mHGeo = wmm.ellipsoidToGeoid( mLat, mLng, mHEll );
          } else {
            try {
              mHGeo = Double.parseDouble( aslit.replace(",", ".") );
            } catch ( NumberFormatException e ) {
              mEThgeo.setError( mContext.getResources().getString( R.string.error_invalid_number) );
              return;
            }
          }
        }
        mParent.addFixedPoint( name, mLng, mLat, mHEll, mHGeo, comment, FixedInfo.SRC_MANUAL );
      } else {
        return;
      }
    }
    onBackPressed();
  }

  @Override
  public void onBackPressed()
  {
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}

