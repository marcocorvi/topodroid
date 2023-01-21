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
import com.topodroid.utils.TDLog;

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
// import android.content.ClipDescription;

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
  // private EditText mEThell; // altitude ellipsoid
  private EditText mEThgeo; // altitude geoid [m]
  private EditText mETaccur; // accuracy [m]

  private Button   mBtnNS;
  private Button   mBtnEW;
  private Button   mBtnOK;
  private Button   mBtnProj4;
  private Button   mBtnView;
  private Button   mBtnClipboard;

  private double  mLng, mLat, mHEll, mHGeo;
  private boolean mNorth, mEast;
  // private boolean hasClipBoard = false; // always false

  /** cstr
   * @param context context
   * @param parent  parent activity
   */
   // param has_clipboard whether there are coords in the clipboard
  FixedAddDialog( Context context, FixedActivity parent /* , boolean has_clipboard */ )
  {
    super( context, null, R.string.FixedAddDialog ); // null app
    mParent = parent;
    // hasClipBoard = has_clipboard; // always false
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
    // mEThell = (EditText) findViewById( R.id.edit_h_ell  );
    mEThgeo  = (EditText) findViewById( R.id.edit_h_geo  );
    mETaccur = (EditText) findViewById( R.id.edit_accuracy  );

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
      if ( TDSetting.mNegAltitude ) {
        // MyKeyboard.registerEditText( mKeyboard, mEThell, MyKeyboard.FLAG_POINT_SIGN  );
        MyKeyboard.registerEditText( mKeyboard, mEThgeo, MyKeyboard.FLAG_POINT_SIGN  );
      } else {
        // MyKeyboard.registerEditText( mKeyboard, mEThell, MyKeyboard.FLAG_POINT  );
        MyKeyboard.registerEditText( mKeyboard, mEThgeo, MyKeyboard.FLAG_POINT  );
      }
      MyKeyboard.registerEditText( mKeyboard, mETaccur, MyKeyboard.FLAG_POINT  );
    } else {
      mKeyboard.hide();
      mETlng.setInputType(  TDConst.TEXT );
      mETlat.setInputType(  TDConst.TEXT );
      if ( TDSetting.mNegAltitude ) {
        // mEThell.setInputType( TDConst.NUMBER_DECIMAL_SIGNED );
        mEThgeo.setInputType( TDConst.NUMBER_DECIMAL_SIGNED );
      } else {
        // mEThell.setInputType( TDConst.NUMBER_DECIMAL );
        mEThgeo.setInputType( TDConst.NUMBER_DECIMAL );
      }
      mETaccur.setInputType( TDConst.NUMBER_DECIMAL );
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

    // if ( hasClipBoard ) getClipBoard( false ); // always false
  }

  /** react to a user long tap
   * @param v tapped view
   * @return true if tap has been handled (ie, always)
   */
  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  /** set the long-lat fields from the texts in the edit fields
   * @return true if successful
   */
  private boolean getLngLat()
  {
    String lng_it = mETlng.getText().toString(); // 20230118 local var "lng_it"
    if ( /* lng_it == null || */ lng_it.length() == 0 ) {
      mETlng.setError( mContext.getResources().getString( R.string.error_long_required ) );
      return false;
    }
    String lat_it = mETlat.getText().toString(); // 20230118 local var "lat_it"
    if ( /* lat_it == null || */ lat_it.length() == 0 ) {
      mETlat.setError( mContext.getResources().getString( R.string.error_lat_required) );
      return false;
    }
    mLng = FixedInfo.string2double( lng_it );
    if ( mLng < -1000 ) {
      mETlng.setError( mContext.getResources().getString( R.string.error_long_required ) );
      return false;
    } 
    mLat = FixedInfo.string2double( lat_it );
    if ( mLat < -1000 ) {
      mETlat.setError( mContext.getResources().getString( R.string.error_lat_required) );
      return false;
    }
    if ( ! mNorth ) mLat = - mLat;
    if ( ! mEast )  mLng = - mLng; 
    return true;
  }

  /** set the coordinates
   * @param lng longitude
   * @param lat latitude
   * @param h_geo geoid altitude
   * @note used to set the coordinates obtained from Proj4 coord conversion
   *       accuracy is not set
   */
  void setCoordsGeo( double lng, double lat, double h_geo )
  {
    mETlng.setText(  FixedInfo.double2string( lng ) );
    mETlat.setText(  FixedInfo.double2string( lat ) );
    // mEThell.setText( String.format( Locale.US, "%.1f", h_geo ) );
    mEThgeo.setText( String.format( Locale.US, "%.1f", h_geo ) );
    mETaccur.setText( "" );
  }

  /** retrieve coords from the clipboard
   * @param toast whether to toast message
   */
  private void getClipBoard( boolean toast )
  {
    ClipboardManager cm = (ClipboardManager)mContext.getSystemService( Context.CLIPBOARD_SERVICE ); 
    if ( cm != null && cm.hasPrimaryClip() ) {
      // CharSequence text = cm.getText();
      ClipData clip = cm.getPrimaryClip();
      // ClipDescription desc = clip.getDescription();
      // TDLog.v("FIXED clip items " + clip.getItemCount() + " desc " + desc.toString() );
      if ( clip.getItemCount() > 0 ) {
        CharSequence text = clip.getItemAt(0).coerceToText( mContext );
        if ( text != null ) {
          String str = text.toString();
          String[] val = str.split(",");
          if ( val.length > 1 ) {
            toast = false;
            mETlat.setText( val[0] );
            mETlng.setText( val[1] );
            if ( val.length > 2 ) {
              // mEThell.setText( val[2] );
              mLat = Double.parseDouble( val[0] );
              mLng = Double.parseDouble( val[1] );
              mHEll = Double.parseDouble( val[2] );
              WorldMagneticModel wmm = new WorldMagneticModel( mContext );
              mHGeo = wmm.ellipsoidToGeoid( mLat, mLng, mHEll );
              mEThgeo.setText( String.format(Locale.US, "%.1f", mHGeo ) );
            }
          }
        }
      }
    } 
    if ( toast ) {
      TDToast.makeBad( R.string.empty_clipboard );
    }
  }

  /** react to user tap
   * @param v tapped view:
   *    - button N/S
   *    - button E/W
   *    - button View
   *    - button clipboard
   *    - button convert (Proj4)
   *    - button save (OK)
   */
  @Override
  public void onClick(View v) 
  {
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );
  
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "FixedAddDialog onClick() button " + b.getText().toString() ); 

    mNorth  = mBtnNS.getText().toString().equals(getContext().getString(R.string.north));
    mEast  = mBtnEW.getText().toString().equals(getContext().getString(R.string.east));
    
    if ( b == mBtnNS ) {
      mBtnNS.setText( mNorth ? R.string.south : R.string.north );
      return;
    } else if ( b == mBtnEW ) {
      mBtnEW.setText( mEast ? R.string.west : R.string.east );
      return;
    } else if ( b == mBtnView ) {
      if ( getLngLat() ) {
        Uri uri = Uri.parse( "geo:" + mLat + "," + mLng + "?q=" + mLat + "," + mLng );
        mContext.startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
      }
    } else if ( b == mBtnClipboard ) {
      getClipBoard( true );
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
      if ( mParent.hasFixed( name ) ) {
        mETstation.setError( mContext.getResources().getString( R.string.error_station_fixed ) );
        return;
      }
      String comment = mETcomment.getText().toString();
      // if ( comment == null ) comment = "";
      if ( getLngLat() ) {
        // String h_ell_str = mEThell.getText().toString();
        String h_geo_str = mEThgeo.getText().toString();
        if ( ( /* h_ell_str == null || h_ell_str.length() == 0 ) && ( h_geo_str == null || */ h_geo_str.length() == 0 ) ) {
          mEThgeo.setError( mContext.getResources().getString( R.string.error_alt_required) );
          return;
        }
        mHEll = -1000.0;
        mHGeo = -1000.0;
        // if ( ( /* h_ell_str == null || */ h_ell_str.length() == 0 ) ) {
          try {
            mHGeo = Double.parseDouble( h_geo_str.replace(",", ".") );
          } catch ( NumberFormatException e ) {
            mEThgeo.setError( mContext.getResources().getString( R.string.error_invalid_number) );
            return;
          }
          WorldMagneticModel wmm = new WorldMagneticModel( mContext );
          mHEll = wmm.geoidToEllipsoid( mLat, mLng, mHGeo );
        // } else {
        //   try {
        //     mHEll = Double.parseDouble( h_ell_str.replace(",", ".") );
        //   } catch ( NumberFormatException e ) {
        //     mEThell.setError( mContext.getResources().getString( R.string.error_invalid_number) );
        //     return;
        //   }
        //   if ( ( /* h_geo_str == null || */ h_geo_str.length() == 0 ) ) {
        //     WorldMagneticModel wmm = new WorldMagneticModel( mContext );
        //     mHGeo = wmm.ellipsoidToGeoid( mLat, mLng, mHEll );
        //   } else {
        //     try {
        //       mHGeo = Double.parseDouble( h_geo_str.replace(",", ".") );
        //     } catch ( NumberFormatException e ) {
        //       mEThgeo.setError( mContext.getResources().getString( R.string.error_invalid_number) );
        //       return;
        //     }
        //   }
        // }
        double accur = -1;
        if ( mETaccur.getText() != null ) {
          String accur_str = mETaccur.getText().toString();
          if ( accur_str.length() > 0 ) {
            try {
              accur = Double.parseDouble( accur_str );
            } catch ( NumberFormatException e ) {
              TDLog.Error( e.getMessage() );
            }
          }
        }
        mParent.addFixedPoint( name, mLng, mLat, mHEll, mHGeo, comment, FixedInfo.SRC_MANUAL, accur, -1 ); // NOTE vert accuracy is always unset
      } else {
        return;
      }
    }
    onBackPressed();
  }

  /** react to BACK tap: dismiss the dialog
   */
  @Override
  public void onBackPressed()
  {
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}

