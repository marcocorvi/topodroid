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
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
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
  private EditText mTValt;
  private EditText mTVasl;

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

  private MyKeyboard mKeyboard;
  private boolean editable;

  // private Button   mButtonCancel;

  private final WorldMagneticModel mWMM;


  FixedDialog( Context context, FixedActivity parent, FixedInfo fxd )
  {
    super( context, R.string.FixedDialog );
    mParent      = parent;
    mFxd         = fxd;
    mWMM = new WorldMagneticModel( mContext );
  }
  
  // void setCSto( String cs )
  // {
  //   mTVcrs.setText( cs );
  // }
  
  void setConvertedCoords( String cs, double lng, double lat, double alt, long n_dec )
  {
    mFxd.setCSCoords( cs, lng, lat, alt, n_dec );
    showConvertedCoords( );
  }

  private void showConvertedCoords( )
  {
    String cs = mFxd.cs;
    if ( cs != null && cs.length() > 0 ) {
      // setTitle( String.format(Locale.US, "%.2f %.2f %.1f", mFxd.cs_lng, mFxd.cs_lat, mFxd.cs_alt ) );
      mTVcrs.setText( cs );
      mTVcs_coords.setText( mFxd.toExportCSString() );
      mTVcrs.setVisibility( View.VISIBLE );
      mTVcs_coords.setVisibility( View.VISIBLE );
    } else {
      mTVcrs.setVisibility( View.INVISIBLE );
      mTVcs_coords.setVisibility( View.GONE );
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
    mTValt = (EditText) findViewById( R.id.fix_alt );
    mTVasl = (EditText) findViewById( R.id.fix_asl );

    mTVdecl = (EditText) findViewById( R.id.fix_decl );
    {
      int year  = TDUtil.year();
      int month = TDUtil.month() + 1; // MAG counts months from 1=Jan
      int day   = TDUtil.day();
      MagElement elem = mWMM.computeMagElement( mFxd.lat, mFxd.lng, mFxd.alt, year, month, day );
      mTVdecl.setText( String.format(Locale.US, "%.4f", elem.Decl ) );
    }

    mButtonDecl = (CheckBox) findViewById( R.id.fix_save_decl );
    mButtonView = (Button) findViewById( R.id.fix_view );

    mButtonClearConvert = (Button) findViewById( R.id.fix_clear_convert );
    mButtonConvert = (Button) findViewById( R.id.fix_convert );
    mTVcrs         = (TextView) findViewById( R.id.fix_crs );
    mTVcs_coords   = (TextView) findViewById( R.id.fix_cs_coords );

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
    mTValt.setText( String.format( Locale.US, "%.0f", mFxd.alt ) );
    mTVasl.setText( String.format( Locale.US, "%.0f", mFxd.asl ) );

    
    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard, -1 );
    if ( TDSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mTValt, MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mTVasl, MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mTVlng, flag );
      MyKeyboard.registerEditText( mKeyboard, mTVlat, flag );
      // mKeyboard.hide();
    } else {
      mKeyboard.hide();
    }

    KeyListener mKLlng = mTVlng.getKeyListener();
    KeyListener mKLlat = mTVlat.getKeyListener();
    KeyListener mKLalt = mTValt.getKeyListener();
    KeyListener mKLasl = mTVasl.getKeyListener();

    editable = ( mFxd.source == FixedInfo.SRC_MANUAL );
    MyKeyboard.setEditable( mTValt, mKeyboard, mKLalt, editable, MyKeyboard.FLAG_POINT );
    MyKeyboard.setEditable( mTVasl, mKeyboard, mKLasl, editable, MyKeyboard.FLAG_POINT );
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
          decl_str = decl_str.replaceAll( ",", "." );
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
        double alt = FixedInfo.string2real( mTValt.getText() );
        double asl = FixedInfo.string2real( mTVasl.getText() );
        if ( lat != mFxd.lat || lng != mFxd.lng || alt != mFxd.alt || asl != mFxd.asl ) {
          mParent.updateFixedData( mFxd, lng, lat, alt, asl );
        }
      }
    } else if ( b == mButtonClearConvert ) {
      mTVcrs.setText( "" );
      mTVcs_coords.setText( "" );
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
