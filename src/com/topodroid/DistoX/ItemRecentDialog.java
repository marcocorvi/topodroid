/** @file ItemRecentDialog.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.view.Window;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

// import android.widget.TextView;
import android.widget.ListView;
import android.widget.GridView;

import android.util.Log;

class ItemRecentDialog extends Dialog
                       implements View.OnClickListener
                       , View.OnLongClickListener
                       // , IItemPicker
                       // , AdapterView.OnItemClickListener
{
  private int mItemType; // items type
  private int mPointPos;  // item point position
  private int mLinePos;   // item line  position
  private int mAreaPos;   // item area  position
  private long mPlotType;

  private  Button mBTpoint;
  private  Button mBTline;
  private  Button mBTarea;
  // private  Button mBTok;
  private SeekBar mSeekBar;

  private Context mContext;
  // private DrawingActivity mParent;
  private ItemDrawer mParent;

  //* private ListView    mList = null;
  private GridView    mList = null;
  private GridView    mGrid = null;
  private ItemAdapter mPointAdapter = null;
  private ItemAdapter mLineAdapter  = null;
  private ItemAdapter mAreaAdapter  = null;
  private boolean mUseText = false;
  private ItemAdapter mAdapter = null;

  // private LinearLayout mRecentLayout;
  private ItemButton mRecentP[];
  private ItemButton mRecentL[];
  private ItemButton mRecentA[];

  private Button mBTsize;
  int mScale;

  // static int mLinePos;
  // static int mAreaPos;

  /**
   * @param context   context
   * @param parent    DrawingActivity parent
   * @param type      drawing type
   */
  ItemRecentDialog( Context context, ItemDrawer parent, long type )
  {
    super( context );
    mContext = context;
    mParent  = parent;

    mPlotType = type;
    mScale = mParent.getPointScale(); // DrawingPointPath.SCALE_M;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    // requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    setContentView(R.layout.item_recent_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    // mRecentLayout = (LinearLayout) findViewById( R.id.layout_point );
    mRecentP = new ItemButton[ ItemDrawer.NR_RECENT ];
    mRecentL = new ItemButton[ ItemDrawer.NR_RECENT ];
    mRecentA = new ItemButton[ ItemDrawer.NR_RECENT ];
    mRecentP[0] = ( ItemButton ) findViewById( R.id.recent0p );
    mRecentP[1] = ( ItemButton ) findViewById( R.id.recent1p );
    mRecentP[2] = ( ItemButton ) findViewById( R.id.recent2p );
    mRecentP[3] = ( ItemButton ) findViewById( R.id.recent3p );
    // mRecentP[4] = ( ItemButton ) findViewById( R.id.recent4p );
    mRecentL[0] = ( ItemButton ) findViewById( R.id.recent0l );
    mRecentL[1] = ( ItemButton ) findViewById( R.id.recent1l );
    mRecentL[2] = ( ItemButton ) findViewById( R.id.recent2l );
    mRecentL[3] = ( ItemButton ) findViewById( R.id.recent3l );
    // mRecentL[4] = ( ItemButton ) findViewById( R.id.recent4l );
    mRecentA[0] = ( ItemButton ) findViewById( R.id.recent0a );
    mRecentA[1] = ( ItemButton ) findViewById( R.id.recent1a );
    mRecentA[2] = ( ItemButton ) findViewById( R.id.recent2a );
    mRecentA[3] = ( ItemButton ) findViewById( R.id.recent3a );
    // mRecentA[4] = ( ItemButton ) findViewById( R.id.recent4a );
    for ( int k=0; k<ItemDrawer.NR_RECENT; ++k ) {
      mRecentP[k].setOnClickListener( this );
      mRecentL[k].setOnClickListener( this );
      mRecentA[k].setOnClickListener( this );
      mRecentP[k].setOnLongClickListener( this );
      mRecentL[k].setOnLongClickListener( this );
      mRecentA[k].setOnLongClickListener( this );
    }
    
    mBTpoint = (Button) findViewById(R.id.point);
    mBTline  = (Button) findViewById(R.id.line );
    mBTarea  = (Button) findViewById(R.id.area );
    mBTsize  = (Button) findViewById(R.id.size );
    mSeekBar = (SeekBar) findViewById(R.id.seekbar );

    mBTpoint.setOnClickListener( this );
    mBTline.setOnClickListener( this );
    mBTarea.setOnClickListener( this );
    mBTsize.setOnClickListener( this );
    mBTsize.setOnLongClickListener( this );
   
    // FIXME how to use the slider ?
    mSeekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
      public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
        if ( fromUser ) {
          setItemAngle( (180 + progress)%360 );
        }
      }
      public void onStartTrackingTouch(SeekBar seekbar) { }
      public void onStopTrackingTouch(SeekBar seekbar) { }
    } );

   
    // requestWindowFeature( Window.FEATURE_NO_TITLE );

    // Log.v( TopoDroidApp.TAG, "ItemRecentDialog ... createAdapters" );
    updateRecentButtons( );

    setSeekBarProgress();

    setTheTitle();
  }

  void setItemAngle( int angle ) 
  {
    Symbol symbol = ItemDrawer.mRecentPoint[0];
    if ( symbol.isOrientable() ) {
      symbol.setAngle( angle );
      mRecentP[0].reset( symbol.getPaint(), symbol.getPath(), 1.5f, 1.5f );
      mRecentP[0].invalidate();
    }
  }

  private void setSeekBarProgress()
  {
    int angle = 0;
    Symbol symbol = ItemDrawer.mRecentPoint[0];
    if ( symbol.isOrientable() ) angle = symbol.getAngle();
    int progress = (180+angle)%360;
    mSeekBar.setProgress( progress );
  }

  private void setRecentButtons( ItemButton recent[], Symbol symbols[], float sx, float sy )
  {
    for ( int k=0; k<ItemDrawer.NR_RECENT; ++k ) {
      Symbol p = symbols[k];
      if ( p == null ) break;
      recent[k].reset( p.getPaint(), p.getPath(), sx, sy );
      recent[k].invalidate();
    }
  }

  private void updateRecentButtons( )
  {
    // float sx=1.0f, sy=1.0f;
    setRecentButtons( mRecentP, ItemDrawer.mRecentPoint, 1.5f, 1.5f ); // sx*1.5f, sy*1.5f
    setRecentButtons( mRecentL, ItemDrawer.mRecentLine, 2.0f, 1.7f ); // sx*2.0f, sy*1.7f
    setRecentButtons( mRecentA, ItemDrawer.mRecentArea, 2.0f, 1.7f ); // sx*2.0f, sy*1.7f
  }
        
  // void rotatePoint( int angle )
  // {
  //   if ( mPointAdapter == null ) return;
  //   if ( TopoDroidSetting.mLevelOverBasic && mItemType == DrawingActivity.SYMBOL_POINT ) {
  //     // Log.v( TopoDroidApp.TAG, "rotate point " + mParent.mCurrentPoint );
  //     mPointAdapter.rotatePoint( mParent.mCurrentPoint, angle );
  //   }
  // }

  // void setPointOrientation( int angle )
  // {
  //   // TODO ???
  // }

  private void setTheTitle()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "[" + DrawingPointPath.scaleToStringUC( mScale ) + "] " );
    sb.append( (ItemDrawer.mRecentPoint[0] != null)? ItemDrawer.mRecentPoint[0].getName() : "-" );
    sb.append( " | " + ((ItemDrawer.mRecentLine[0] != null)? ItemDrawer.mRecentLine[0].getName() : "-") );
    sb.append( " | " + ((ItemDrawer.mRecentArea[0] != null)? ItemDrawer.mRecentArea[0].getName() : "-") );
    setTitle( sb.toString() );
  }

  private void setFirstPoint( int k )
  {
    // String title = String.format( mContext.getResources().getString( R.string.title_draw_point ),
    //   ((ItemDrawer.mRecentPoint[k] != null)? ItemDrawer.mRecentPoint[k].getName() : "-") );
    // setTitle( title );
    if ( k <= 0 || k >= ItemDrawer.NR_RECENT ) return;
    ItemDrawer.updateRecentPoint( ItemDrawer.mRecentPoint[k] );
    setRecentButtons( mRecentP, ItemDrawer.mRecentPoint, 1.5f, 1.5f ); // sx*1.5f, sy*1.5f
    setTheTitle();
  }

  private void setFirstLine( int k )
  {
    // String title = String.format( mContext.getResources().getString( R.string.title_draw_line ),
    //   ((ItemDrawer.mRecentLine[k] != null)? ItemDrawer.mRecentLine[k].getName() : "-") );
    // setTitle( title );
    if ( k <= 0 || k >= ItemDrawer.NR_RECENT ) return;
    ItemDrawer.updateRecentLine( ItemDrawer.mRecentLine[k] );
    setRecentButtons( mRecentL, ItemDrawer.mRecentLine, 2.0f, 1.7f ); 
    setTheTitle();
  }

  private void setFirstArea( int k )
  {
    // String title = String.format( mContext.getResources().getString( R.string.title_draw_area ),
    //   ((ItemDrawer.mRecentArea[k] != null)? ItemDrawer.mRecentArea[k].getName() : "-") );
    // setTitle( title );
    if ( k <= 0 || k >= ItemDrawer.NR_RECENT ) return;
    ItemDrawer.updateRecentArea( ItemDrawer.mRecentArea[k] );
    setRecentButtons( mRecentA, ItemDrawer.mRecentArea, 2.0f, 1.7f ); 
    setTheTitle();
  }

  private void setPoint( int k )
  {
    int index = DrawingBrushPaths.mPointLib.getSymbolIndex( ItemDrawer.mRecentPoint[k] );
    if ( index >= 0 ) {
      mParent.mCurrentPoint = index;
      mParent.pointSelected( index, true );
    }
  }

  private void setLine( int k )
  {
    int index = DrawingBrushPaths.mLineLib.getSymbolIndex( ItemDrawer.mRecentLine[k] );
    if ( index >= 0 ) {
      mParent.mCurrentLine = index;
      mParent.lineSelected( index, true );
    }
  }

  private void setArea( int k )
  {
    int index = DrawingBrushPaths.mAreaLib.getSymbolIndex( ItemDrawer.mRecentArea[k] );
    if ( index >= 0 ) {
      mParent.mCurrentArea = index;
      mParent.areaSelected( index, true );
    }
  }

  @Override
  public boolean onLongClick(View view)
  {
    switch (view.getId()) {
      case R.id.recent0p: /* setFirstPoint(0); */ return true;
      case R.id.recent1p: setFirstPoint(1); return true;
      case R.id.recent2p: setFirstPoint(2); return true;
      case R.id.recent3p: setFirstPoint(3); return true;
      // case R.id.recent4p: setFirstPoint(4); return true;
      case R.id.recent0l: /* setFirstLine(0); */ return true;
      case R.id.recent1l: setFirstLine(1); return true;
      case R.id.recent2l: setFirstLine(2); return true;
      case R.id.recent3l: setFirstLine(3); return true;
      // case R.id.recent4l: setFirstLine(4); return true;
      case R.id.recent0a: /* setFirstArea(0); */ return true;
      case R.id.recent1a: setFirstArea(1); return true;
      case R.id.recent2a: setFirstArea(2); return true;
      case R.id.recent3a: setFirstArea(3); return true;
      // case R.id.recent4a: setFirstArea(4); return true;

      case R.id.size:
        if ( mScale < DrawingPointPath.SCALE_XL ) {
          ++ mScale;
          mParent.setPointScale( mScale );
          setTheTitle();
        }
        return true;
    }
    return false;
  }

  @Override
  public void onClick(View view)
  {
    int index = -1;
    // Log.v("DistoX", "ItemPicker onClick()" );
    switch (view.getId()) {
      case R.id.recent0p: setPoint(0); break;
      case R.id.recent1p: setPoint(1); break;
      case R.id.recent2p: setPoint(2); break;
      case R.id.recent3p: setPoint(3); break;
      // case R.id.recent4p: setPoint(4); break;
      case R.id.recent0l: setLine(0); break;
      case R.id.recent1l: setLine(1); break;
      case R.id.recent2l: setLine(2); break;
      case R.id.recent3l: setLine(3); break;
      // case R.id.recent4l: setLine(4); break;
      case R.id.recent0a: setArea(0); break;
      case R.id.recent1a: setArea(1); break;
      case R.id.recent2a: setArea(2); break;
      case R.id.recent3a: setArea(3); break;
      // case R.id.recent4a: setArea(4); break;

      case R.id.point:
        new ItemPickerDialog( mContext, mParent, mPlotType, DrawingActivity.SYMBOL_POINT ). show();
        break;
      case R.id.line:
        new ItemPickerDialog( mContext, mParent, mPlotType, DrawingActivity.SYMBOL_LINE ). show();
        break;
      case R.id.area:
        new ItemPickerDialog( mContext, mParent, mPlotType, DrawingActivity.SYMBOL_AREA ). show();
        break;
      case R.id.size:
        if ( mScale > DrawingPointPath.SCALE_XS ) {
          -- mScale;
          mParent.setPointScale( mScale );
          setTheTitle();
        }
        return;
      default: 
        break;
    }
    dismiss();
  }

  // implements
  // public void closeDialog() {} 
}
