/* @file FixedDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey fix point edit dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import com.topodroid.mag.MagElement;
import com.topodroid.mag.WorldMagneticModel;

// import java.util.regex.Pattern;
import java.util.Locale;

// import android.widget.ArrayAdapter;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
// import android.content.DialogInterface;
import android.content.Intent;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.widget.GridView;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
import android.inputmethodservice.KeyboardView;

import android.text.method.KeyListener; 

import android.net.Uri;


class FixedDialog extends MyDialog
                  implements View.OnClickListener
{
  private final FixedActivity mParent;
  private FixedInfo mFxd;

  // private TextView mTVdata;
  private EditText mTVlng;
  private EditText mTVlat;
  // private EditText mTVh_ell;
  private EditText mTVh_geo;

  private TextView mETstation;
  private EditText mETcomment;
  private TextView mTVdecl;
  private TextView mTVfix_station;
  private Button   mButtonDrop;
  private CheckBox mButtonDecl;
  private Button   mButtonView;
  // private Button   mButtonWmm;
  private Button   mButtonSave;

  private Button   mButtonClearConvert;
  private Button   mButtonConvert;
  private TextView mTVcrs;
  private TextView mTVcs_coords; // converted coords
  private TextView mTVconvergence;

  private MyKeyboard mKeyboard;
  private boolean editable;

  // private Button   mButtonCancel;

  private final WorldMagneticModel mWMM;


  FixedDialog( Context context, FixedActivity parent, FixedInfo fxd )
  {
    super( context, null, R.string.FixedDialog ); // null app
    mParent      = parent;
    mFxd         = fxd;
    mWMM = new WorldMagneticModel( mContext );
  }
  
  // void setCSto( String cs )
  // {
  //   mTVcrs.setText( cs );
  // }
  
  void setConvertedCoords( String cs, double lng, double lat, double h_ell, long n_dec, double conv )
  {
    mFxd.setCSCoords( cs, lng, lat, h_ell, n_dec, conv );
    showConvertedCoords( );
  }

  private void showConvertedCoords( )
  {
    String cs = mFxd.cs;
    if ( cs != null && cs.length() > 0 ) {
      // setTitle( String.format(Locale.US, "%.2f %.2f %.1f", mFxd.cs_lng, mFxd.cs_lat, mFxd.cs_h_geo ) );
      mTVcrs.setText( cs );
      mTVcs_coords.setText( mFxd.toExportCSString() );
      mTVcrs.setVisibility( View.VISIBLE );
      mTVcs_coords.setVisibility( View.VISIBLE );
      // TODO show convergence
      double conv = mFxd.getConvergence();
      mTVconvergence.setText( String.format(Locale.US, "{%.3f}", conv ) );
      mTVconvergence.setVisibility( View.VISIBLE );
    } else {
      mTVcrs.setVisibility( View.INVISIBLE );
      mTVcs_coords.setVisibility( View.GONE );
      mTVconvergence.setVisibility( View.GONE );
    }
  }

  long getFixedId() { return mFxd.id; }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_FIXED, "FixedDialog onCreate" );
    initLayout( R.layout.fixed_dialog, R.string.title_fixed_edit );

    mTVlng = (EditText) findViewById( R.id.fix_lng );
    mTVlat = (EditText) findViewById( R.id.fix_lat );
    // mTVh_ell = (EditText) findViewById( R.id.fix_h_ell );
    mTVh_geo = (EditText) findViewById( R.id.fix_h_geo );

    mTVdecl = (EditText) findViewById( R.id.fix_decl );
    {
      int year  = TDUtil.year();
      int month = TDUtil.month() + 1; // MAG counts months from 1=Jan
      int day   = TDUtil.day();
      MagElement elem = mWMM.computeMagElement( mFxd.lat, mFxd.lng, mFxd.h_ell, year, month, day );
      mTVdecl.setText( String.format(Locale.US, "%.4f", elem.Decl ) );
    }

    mButtonDecl = (CheckBox) findViewById( R.id.fix_save_decl );
    mButtonView = (Button) findViewById( R.id.fix_view );

    mButtonClearConvert = (Button) findViewById( R.id.fix_clear_convert );
    mButtonConvert = (Button) findViewById( R.id.fix_convert );
    mTVcrs         = (TextView) findViewById( R.id.fix_crs );
    mTVcs_coords   = (TextView) findViewById( R.id.fix_cs_coords );
    mTVconvergence = (TextView) findViewById( R.id.fix_convergence );

    mETstation = (TextView) findViewById( R.id.fix_station );
    mETcomment = (EditText) findViewById( R.id.fix_comment );
    mETstation.setText( mFxd.name );
    mETcomment.setText( mFxd.comment );

    mButtonSave = (Button) findViewById( R.id.fix_save );
    mButtonDrop    = (Button) findViewById(R.id.fix_drop );
    // mButtonOK      = (Button) findViewById(R.id.fix_ok );
    // mButtonCancel  = (Button) findViewById(R.id.fix_cancel );
    // TDLog.v("FIXED info " + mFxd.lng + " " + mFxd.lat );
    int flag = MyKeyboard.FLAG_POINT_DEGREE;
    if ( TDSetting.mUnitLocation == TDUtil.DEGREE ) {
      mTVlng.setText( FixedInfo.double2degree( mFxd.lng ) );
      mTVlat.setText( FixedInfo.double2degree( mFxd.lat ) );
    } else { // TDUtil.DDMMSS
      mTVlng.setText( FixedInfo.double2ddmmss( mFxd.lng ) );
      mTVlat.setText( FixedInfo.double2ddmmss( mFxd.lat ) );
    }
    // mTVh_ell.setText( String.format( Locale.US, "%.0f", mFxd.h_ell ) );
    mTVh_geo.setText( String.format( Locale.US, "%.0f", mFxd.h_geo ) );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard, -1 );
    if ( TDSetting.mKeyboard ) {
      if ( TDSetting.mNegAltitude ) {
        // MyKeyboard.registerEditText( mKeyboard, mTVh_ell, MyKeyboard.FLAG_POINT_SIGN );
        MyKeyboard.registerEditText( mKeyboard, mTVh_geo, MyKeyboard.FLAG_POINT_SIGN );
      } else {
        // MyKeyboard.registerEditText( mKeyboard, mTVh_ell, MyKeyboard.FLAG_POINT );
        MyKeyboard.registerEditText( mKeyboard, mTVh_geo, MyKeyboard.FLAG_POINT );
      }
      MyKeyboard.registerEditText( mKeyboard, mTVlng, flag );
      MyKeyboard.registerEditText( mKeyboard, mTVlat, flag );
      // mKeyboard.hide();
    } else {
      mKeyboard.hide();
    }

    KeyListener mKLlng = mTVlng.getKeyListener();
    KeyListener mKLlat = mTVlat.getKeyListener();
    // KeyListener mKLalt = mTVh_ell.getKeyListener();
    KeyListener mKLh_geo = mTVh_geo.getKeyListener();

    editable = ( mFxd.source == FixedInfo.SRC_MANUAL );
    if ( TDSetting.mNegAltitude ) {
      // MyKeyboard.setEditable( mTVh_ell, mKeyboard, mKLalt, editable, MyKeyboard.FLAG_POINT_SIGN );
      MyKeyboard.setEditable( mTVh_geo, mKeyboard, mKLh_geo, editable, MyKeyboard.FLAG_POINT_SIGN );
    } else {
      // MyKeyboard.setEditable( mTVh_ell, mKeyboard, mKLalt, editable, MyKeyboard.FLAG_POINT );
      MyKeyboard.setEditable( mTVh_geo, mKeyboard, mKLh_geo, editable, MyKeyboard.FLAG_POINT );
    }
    MyKeyboard.setEditable( mTVlng, mKeyboard, mKLlng, editable, flag );
    MyKeyboard.setEditable( mTVlat, mKeyboard, mKLlat, editable, flag );
    
    mButtonDrop.setOnClickListener( this );
    mButtonView.setOnClickListener( this );
    mButtonSave.setOnClickListener( this );
    mButtonClearConvert.setOnClickListener( this );
    mButtonConvert.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );

    showConvertedCoords( );
  }

  @Override
  public void onClick(View v) 
  {
    MyKeyboard.close( mKeyboard );

    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "FixedDialog onClick() button " + b.getText().toString() );

    if ( b == mButtonSave ) {
      String station = mETstation.getText().toString();
      if ( /* station == null || */ station.length() == 0 ) {
        mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
        return;
      }
      String comment = mETcomment.getText().toString();
      // if ( comment == null ) comment = "";
      if ( mButtonDecl.isChecked() && mTVdecl.getText() != null ) {
        String decl_str = mTVdecl.getText().toString();
        if ( /* decl_str != null && */ decl_str.length() > 0 ) {
          decl_str = TDString.commaToPoint( decl_str );
          try {
            mParent.setDeclination( Float.parseFloat( decl_str ) );
          } catch ( NumberFormatException e ) {
            mTVdecl.setError( mContext.getResources().getString( R.string.error_declination_number ) );
            return;
          }
        }
      }

      mFxd.name = station;
      mFxd.comment = comment;
      mParent.updateFixedNameComment( mFxd, station, comment );

      if ( editable ) {
        double lat = FixedInfo.string2double( mTVlat.getText() );
        double lng = FixedInfo.string2double( mTVlng.getText() );
        // double h_ell = FixedInfo.string2real( mTVh_ell.getText() );
        double h_geo = FixedInfo.string2real( mTVh_geo.getText() );
        if ( lat != mFxd.lat || lng != mFxd.lng /* || h_ell != mFxd.h_ell */ || h_geo != mFxd.h_geo ) {
          // get ellipsoid height from geoid height
          double h_ell = mWMM.geoidToEllipsoid( lat, lng, h_geo );
          mParent.updateFixedData( mFxd, lng, lat, h_ell, h_geo );
        }
      }
    } else if ( b == mButtonClearConvert ) {
      mTVcrs.setText( "" );
      mTVcs_coords.setText( "" );
      mTVconvergence.setText( "" );
      mParent.clearConvertedCoords( mFxd );
      return;
    } else if ( b == mButtonConvert ) {
      String cs_to = mFxd.hasCSCoords() ? mFxd.cs : TDSetting.mCRS;
      mParent.tryProj4( this, cs_to, mFxd );
      return;
    } else if ( b == mButtonView ) {
      Uri uri = Uri.parse( "geo:" + mFxd.lat + "," + mFxd.lng + "?q=" + mFxd.lat + "," + mFxd.lng );
      mContext.startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
    } else if ( b == mButtonDrop ) {
      mParent.dropFixed( mFxd );
    // } else { // b == mButtonCancel
    //   /* nothing */
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}
