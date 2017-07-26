/** @file SymbolEnableDialog.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing symbol: 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnClickListener;

import android.view.Window;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

// import android.widget.TextView;
import android.widget.ListView;

// import android.util.Log;

class SymbolEnableDialog extends MyDialog
                         implements View.OnClickListener
{
  private int mType; // symbols type

  private  Button mBTpoint;
  private  Button mBTline;
  private  Button mBTarea;

  // private  Button mBTreload;

  // private  Button mBTsave;
  // private  Button mBTcancel;
  // private  Button mBTok;

  // private Activity mParent;
  private TopoDroidApp mApp;

  private ListView    mList;
  private SymbolAdapter mPointAdapter;
  private SymbolAdapter mLineAdapter;
  private SymbolAdapter mAreaAdapter;


  SymbolEnableDialog( Context context, /* Activity parent, */ TopoDroidApp app )
  {
    super( context, R.string.SymbolEnableDialog );
    // mParent  = parent;
    mApp     = app;
    mType    = Symbol.LINE; // default symbols are lines
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.symbol_enable_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mList = (ListView) findViewById(R.id.symbol_list);
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );
    
    // mBTsave    = (Button) findViewById(R.id.symbol_save );
    // mBTcancel  = (Button) findViewById(R.id.symbol_cancel );

    mBTline  = (Button) findViewById(R.id.symbol_line );
    mBTpoint = (Button) findViewById(R.id.symbol_point);
    mBTarea  = (Button) findViewById(R.id.symbol_area );

    // mBTreload = (Button) findViewById(R.id.symbol_reload );
    // if ( TDLevel.overNormal ) {
    //   mBTreload.setOnClickListener( this );
    // } else {
    //   mBTreload.setVisibility( View.GONE );
    // }
 
    mBTline.setOnClickListener( this );
    if ( TDLevel.overBasic ) {
      mBTpoint.setOnClickListener( this );
      mBTarea.setOnClickListener( this );
    }

    // Log.v( TopoDroidApp.TAG, "SymbolEnableDialog ... createAdapters" );
    if ( ! createAdapters() ) dismiss();

    // mList.setAdapter( mPointAdapter );
    updateList();
  }

  boolean createAdapters()
  {
    mPointAdapter = new SymbolAdapter( mContext, R.layout.symbol, new ArrayList<EnableSymbol>() );
    mLineAdapter  = new SymbolAdapter( mContext, R.layout.symbol, new ArrayList<EnableSymbol>() );
    mAreaAdapter  = new SymbolAdapter( mContext, R.layout.symbol, new ArrayList<EnableSymbol>() );

    if ( TDLevel.overBasic ) {
      SymbolPointLibrary point_lib = BrushManager.mPointLib;
      if ( point_lib == null ) return false;
      int np = point_lib.mSymbolNr;
      for ( int i=0; i<np; ++i ) {
        mPointAdapter.add( new EnableSymbol( mContext, Symbol.POINT, i, point_lib.getSymbolByIndex( i ) ) );
      }
    }

    SymbolLineLibrary line_lib   = BrushManager.mLineLib;
    if ( line_lib == null ) return false;
    int nl = line_lib.mSymbolNr;
    for ( int j=0; j<nl; ++j ) {
      mLineAdapter.add( new EnableSymbol( mContext, Symbol.LINE, j, line_lib.getSymbolByIndex( j ) ) );
    }

    if ( TDLevel.overBasic ) {
      SymbolAreaLibrary area_lib   = BrushManager.mAreaLib;
      if ( area_lib == null ) return false;
      int na = area_lib.mSymbolNr;
      for ( int k=0; k<na; ++k ) {
        mAreaAdapter.add( new EnableSymbol( mContext, Symbol.AREA, k, area_lib.getSymbolByIndex( k ) ) );
      }
    }

    // Log.v( TopoDroidApp.TAG, "SymbolEnableDialog ... symbols " + np + " " + nl + " " + na );
    return true;
  }

  private void updateList()
  {
    // Log.v( TopoDroidApp.TAG, "SymbolEnableDialog ... updateList type " + mType );
    switch ( mType ) {
      case Symbol.POINT:
        if ( TDLevel.overBasic ) {
          mList.setAdapter( mPointAdapter );
          mBTpoint.getBackground().setColorFilter( TDColor.LIGHT_BLUE, PorterDuff.Mode.LIGHTEN );
          mBTline.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
          mBTarea.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
        }
        break;
      case Symbol.LINE:
        mList.setAdapter( mLineAdapter );
        mBTpoint.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
        mBTline.getBackground().setColorFilter( TDColor.LIGHT_BLUE, PorterDuff.Mode.LIGHTEN );
        mBTarea.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
        break;
      case Symbol.AREA:
        if ( TDLevel.overBasic ) {
          mList.setAdapter( mAreaAdapter );
          mBTpoint.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
          mBTline.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
          mBTarea.getBackground().setColorFilter( TDColor.LIGHT_BLUE, PorterDuff.Mode.LIGHTEN );
        }
        break;
    }
    mList.invalidate();
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_PLOT, "DrawingLinePickerDialog::onClick" );
    int type = -1;
    switch (view.getId()) {
      case R.id.symbol_point:
        if ( TDLevel.overBasic ) type = Symbol.POINT;
        break;
      case R.id.symbol_line:
        type = Symbol.LINE;
        break;
      case R.id.symbol_area:
        if ( TDLevel.overBasic ) type = Symbol.AREA;
        break;
      // case R.id.symbol_reload:
      //   String old_version = mApp.mDData.getValue( "symbol_version" );
      //   if ( old_version == null ) old_version = "-";
      //   String message = String.format( mContext.getResources().getString( R.string.symbols_ask ), 
      //     mApp.SYMBOL_VERSION, old_version );
      //   TopoDroidAlertDialog.makeAlert( mContext, mContext.getResources(), message, // R.string.symbols_ask,
      //     new DialogInterface.OnClickListener() {
      //       @Override
      //       public void onClick( DialogInterface dialog, int btn ) {
      //         mApp.installSymbols( true );
      //         BrushManager.reloadAllLibraries( mContext.getResources() );
      //         createAdapters();
      //         updateList();
      //       }
      //     }
      //   );
      //   break;
      default:
        break;
    }
    if ( type >= 0 && type != mType ) {
      mType = type;
      updateList();
    }
    // dismiss();
  }

  public void onBackPressed()
  {
    saveSymbols();
    dismiss();
  }

  void saveSymbols()
  {
    if ( TDLevel.overBasic ) {
      mPointAdapter.updateSymbols( "p_" );
      SymbolPointLibrary point_lib = BrushManager.mPointLib;
      if ( point_lib != null ) point_lib.makeEnabledList();
    }

    mLineAdapter.updateSymbols( "l_" );
    SymbolLineLibrary line_lib   = BrushManager.mLineLib;
    if ( line_lib  != null ) line_lib.makeEnabledList();

    if ( TDLevel.overBasic ) {
      mAreaAdapter.updateSymbols( "a_" );
      SymbolAreaLibrary area_lib   = BrushManager.mAreaLib;
      if ( area_lib  != null ) area_lib.makeEnabledList();
    }
  }
}

