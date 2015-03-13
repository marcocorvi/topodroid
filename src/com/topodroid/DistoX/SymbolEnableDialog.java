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
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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

class SymbolEnableDialog extends Dialog
                         implements View.OnClickListener
{
  private int mType; // symbols type

  private  Button mBTpoint;
  private  Button mBTline;
  private  Button mBTarea;
  // private  Button mBTsave;
  // private  Button mBTcancel;
  // private  Button mBTok;

  private Context mContext;
  private Activity mParent;

  private ListView    mList;
  private SymbolAdapter mPointAdapter;
  private SymbolAdapter mLineAdapter;
  private SymbolAdapter mAreaAdapter;


  SymbolEnableDialog( Context context, Activity parent )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mType    = DrawingActivity.SYMBOL_LINE; // default symbols are lines
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

    mBTline.setOnClickListener( this );
    if ( TopoDroidSetting.mLevelOverBasic ) {
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

    mPointAdapter = new SymbolAdapter( mParent, R.layout.symbol, new ArrayList<EnableSymbol>() );
    mLineAdapter  = new SymbolAdapter( mParent, R.layout.symbol, new ArrayList<EnableSymbol>() );
    mAreaAdapter  = new SymbolAdapter( mParent, R.layout.symbol, new ArrayList<EnableSymbol>() );

    if ( TopoDroidSetting.mLevelOverBasic ) {
      SymbolPointLibrary point_lib = DrawingBrushPaths.mPointLib;
      if ( point_lib == null ) return false;
      int np = point_lib.mAnyPointNr;
      for ( int i=0; i<np; ++i ) {
        mPointAdapter.add( new EnableSymbol( mParent, DrawingActivity.SYMBOL_POINT, i, point_lib.getAnyPoint( i ) ) );
      }
    }

    SymbolLineLibrary line_lib   = DrawingBrushPaths.mLineLib;
    if ( line_lib == null ) return false;
    int nl = line_lib.mAnyLineNr;
    for ( int j=0; j<nl; ++j ) {
      mLineAdapter.add( new EnableSymbol( mParent, DrawingActivity.SYMBOL_LINE, j, line_lib.getAnyLine( j ) ) );
    }

    if ( TopoDroidSetting.mLevelOverBasic ) {
      SymbolAreaLibrary area_lib   = DrawingBrushPaths.mAreaLib;
      if ( area_lib == null ) return false;
      int na = area_lib.mAnyAreaNr;
      for ( int k=0; k<na; ++k ) {
        mAreaAdapter.add( new EnableSymbol( mParent, DrawingActivity.SYMBOL_AREA, k, area_lib.getAnyArea( k ) ) );
      }
    }

    // Log.v( TopoDroidApp.TAG, "SymbolEnableDialog ... symbols " + np + " " + nl + " " + na );
    return true;
  }

  private void updateList()
  {
    // Log.v( TopoDroidApp.TAG, "SymbolEnableDialog ... updateList type " + mType );
    switch ( mType ) {
      case DrawingActivity.SYMBOL_POINT:
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mList.setAdapter( mPointAdapter );
          mBTpoint.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
          mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        }
        break;
      case DrawingActivity.SYMBOL_LINE:
        mList.setAdapter( mLineAdapter );
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        break;
      case DrawingActivity.SYMBOL_AREA:
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mList.setAdapter( mAreaAdapter );
          mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mBTarea.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        }
        break;
    }
    mList.invalidate();
  }

  @Override
  public void onClick(View view)
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "DrawingLinePickerDialog::onClick" );
    int type = -1;
    switch (view.getId()) {
      case R.id.symbol_point:
        if ( TopoDroidSetting.mLevelOverBasic ) type = DrawingActivity.SYMBOL_POINT;
        break;
      case R.id.symbol_line:
        type = DrawingActivity.SYMBOL_LINE;
        break;
      case R.id.symbol_area:
        if ( TopoDroidSetting.mLevelOverBasic ) type = DrawingActivity.SYMBOL_AREA;
        break;
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
    if ( TopoDroidSetting.mLevelOverBasic ) {
      mPointAdapter.updateSymbols( "p_" );
      SymbolPointLibrary point_lib = DrawingBrushPaths.mPointLib;
      if ( point_lib != null ) point_lib.makeEnabledList();
    }

    mLineAdapter.updateSymbols( "l_" );
    SymbolLineLibrary line_lib   = DrawingBrushPaths.mLineLib;
    if ( line_lib  != null ) line_lib.makeEnabledList();

    if ( TopoDroidSetting.mLevelOverBasic ) {
      mAreaAdapter.updateSymbols( "a_" );
      SymbolAreaLibrary area_lib   = DrawingBrushPaths.mAreaLib;
      if ( area_lib  != null ) area_lib.makeEnabledList();
    }
  }
}

