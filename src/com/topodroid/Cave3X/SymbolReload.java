/* @file SymbolReload.java
 *
 * @author marco corvi
 * @date jan 2017 
 *
 * @brief TopoDroid drawing symbol load/reload dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.ui.MyDialog;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
// import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.Window;
// import android.view.WindowManager;

// import android.widget.LinearLayout;


class SymbolReload extends MyDialog
                          implements View.OnClickListener
{
  private final TopoDroidApp mApp;

  private Button mBtnInstall;
  private Button mBtnReplace;
  // private Button mBtnCancel;

  private CheckBox mCBspeleo;
  private CheckBox mCBextra;
  private CheckBox mCBmine;
  private CheckBox mCBgeo;
  private CheckBox mCBarcheo;
  private CheckBox mCBanthro;
  private CheckBox mCBpaleo;
  private CheckBox mCBbio;
  private CheckBox mCBkarst;

  private boolean mAll;

  SymbolReload( Context context, TopoDroidApp app, boolean all )
  {
    super(context, R.string.SymbolReload );
    mApp = app;
    mAll = all;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_NO_TITLE);
    // getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog::onCreate" );
    initLayout( R.layout.symbol_reload, R.string.symbol_reload );

    TextView tv = (TextView) findViewById( R.id.text_version );

    mCBspeleo = (CheckBox) findViewById( R.id.ckb_speleo );
    mCBextra  = (CheckBox) findViewById( R.id.ckb_extra  );
    mCBmine   = (CheckBox) findViewById( R.id.ckb_mine   );
    mCBgeo    = (CheckBox) findViewById( R.id.ckb_geo    );
    mCBarcheo = (CheckBox) findViewById( R.id.ckb_archeo );
    mCBanthro = (CheckBox) findViewById( R.id.ckb_anthro );
    mCBpaleo  = (CheckBox) findViewById( R.id.ckb_paleo  );
    mCBbio    = (CheckBox) findViewById( R.id.ckb_bio    );
    mCBkarst  = (CheckBox) findViewById( R.id.ckb_karst  );

    mBtnInstall = (Button) findViewById( R.id.button_add );
    mBtnReplace = (Button) findViewById( R.id.button_replace );
    mBtnReplace.setOnClickListener( this );
    // mBtnCancel  = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );

    if ( ! mAll ) {
      mCBspeleo.setChecked( true );
      mCBspeleo.setVisibility( View.GONE );
      mCBextra.setVisibility( View.GONE );
      mCBmine.setVisibility( View.GONE );
      mCBgeo.setVisibility( View.GONE );
      mCBarcheo.setVisibility( View.GONE );
      mCBanthro.setVisibility( View.GONE );
      mCBpaleo.setVisibility( View.GONE );
      mCBbio.setVisibility( View.GONE );
      mCBkarst.setVisibility( View.GONE );
      mBtnInstall.setVisibility( View.GONE );
      String version = TopoDroidApp.mDData.getValue( "symbol_version" );
      tv.setText( String.format( mApp.getResources().getString(R.string.symbols_ask), TDVersion.SYMBOL_VERSION, version ) );
    } else {
      mBtnInstall.setOnClickListener( this );
    }
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "SymbolReload onClick button " + b.getText().toString() );

    if ( b == mBtnInstall ) {
      mApp.reloadSymbols( false,  // do not clear first 
                          mCBspeleo.isChecked(),
                          mCBextra.isChecked(),
                          mCBmine.isChecked(),
                          mCBgeo.isChecked(),
                          mCBarcheo.isChecked(),
                          mCBanthro.isChecked(),
                          mCBpaleo.isChecked(),
                          mCBbio.isChecked(),
                          mCBkarst.isChecked()
                        );
    } else if ( b == mBtnReplace ) {
      mApp.reloadSymbols( true, // clear first
                          mCBspeleo.isChecked(),
                          mCBextra.isChecked(),
                          mCBmine.isChecked(),
                          mCBgeo.isChecked(),
                          mCBarcheo.isChecked(),
                          mCBanthro.isChecked(),
                          mCBpaleo.isChecked(),
                          mCBbio.isChecked(),
                          mCBkarst.isChecked()
                        );
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    dismiss();
  }

}

