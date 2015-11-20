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
  int nrRecent;

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
    nrRecent = TopoDroidSetting.mRecentNr;

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
    mRecentP = new ItemButton[ nrRecent ];
    mRecentL = new ItemButton[ nrRecent ];
    mRecentA = new ItemButton[ nrRecent ];
    LinearLayout layoutp = (LinearLayout) findViewById( R.id.layout_point );
    LinearLayout layoutl = (LinearLayout) findViewById( R.id.layout_line  );
    LinearLayout layouta = (LinearLayout) findViewById( R.id.layout_area  );

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 0, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 2, 5, 2 );
    lp.weight = 16;
    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp2.setMargins( 2, 2, 0, 2 );
    lp2.weight = 10;

    for ( int k=0; k<nrRecent; ++k ) {
      mRecentP[k] = new ItemButton( mContext );
      mRecentL[k] = new ItemButton( mContext );
      mRecentA[k] = new ItemButton( mContext );
      mRecentP[k].setOnClickListener( this );
      mRecentL[k].setOnClickListener( this );
      mRecentA[k].setOnClickListener( this );
      mRecentP[k].setOnLongClickListener( this );
      mRecentL[k].setOnLongClickListener( this );
      mRecentA[k].setOnLongClickListener( this );
      layoutp.addView( mRecentP[k], lp );
      layoutl.addView( mRecentL[k], lp );
      layouta.addView( mRecentA[k], lp );
    }

    mBTpoint = new Button( mContext ); mBTpoint.setText(">>"); mBTpoint.setTextSize(14);
    mBTline  = new Button( mContext ); mBTline.setText(">>");  mBTline.setTextSize(14);
    mBTarea  = new Button( mContext ); mBTarea.setText(">>");  mBTarea.setTextSize(14);
    layoutp.addView( mBTpoint, lp2 );
    layoutl.addView( mBTline,  lp2 );
    layouta.addView( mBTarea,  lp2 );
    
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
    for ( int k=0; k<nrRecent; ++k ) {
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
    if ( k <= 0 || k >= nrRecent ) return;
    ItemDrawer.updateRecentPoint( ItemDrawer.mRecentPoint[k] );
    setRecentButtons( mRecentP, ItemDrawer.mRecentPoint, 1.5f, 1.5f ); // sx*1.5f, sy*1.5f
    setTheTitle();
  }

  private void setFirstLine( int k )
  {
    // String title = String.format( mContext.getResources().getString( R.string.title_draw_line ),
    //   ((ItemDrawer.mRecentLine[k] != null)? ItemDrawer.mRecentLine[k].getName() : "-") );
    // setTitle( title );
    if ( k <= 0 || k >= nrRecent ) return;
    ItemDrawer.updateRecentLine( ItemDrawer.mRecentLine[k] );
    setRecentButtons( mRecentL, ItemDrawer.mRecentLine, 2.0f, 1.7f ); 
    setTheTitle();
  }

  private void setFirstArea( int k )
  {
    // String title = String.format( mContext.getResources().getString( R.string.title_draw_area ),
    //   ((ItemDrawer.mRecentArea[k] != null)? ItemDrawer.mRecentArea[k].getName() : "-") );
    // setTitle( title );
    if ( k <= 0 || k >= nrRecent ) return;
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
    Button b = (Button)view;
    for ( int k=0; k<nrRecent; ++k ) {
      if ( b == mRecentP[k] ) { setFirstPoint(k); return true; }
      if ( b == mRecentL[k] ) { setFirstLine(k); return true; }
      if ( b == mRecentA[k] ) { setFirstArea(k); return true; }
    }
    if ( b == mBTsize ) {
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
    Button b = (Button)view;
    for ( int k=0; k<nrRecent; ++k ) {
      if ( b == mRecentP[k] ) { setPoint(k); return; }
      if ( b == mRecentL[k] ) { setLine(k); return; }
      if ( b == mRecentA[k] ) { setArea(k); return; }
    }
    if ( b == mBTpoint ) {
      new ItemPickerDialog( mContext, mParent, mPlotType, DrawingActivity.SYMBOL_POINT ). show();
      return;
    } else if ( b == mBTline ) {
      new ItemPickerDialog( mContext, mParent, mPlotType, DrawingActivity.SYMBOL_LINE ). show();
      return;
    } else if ( b == mBTarea ) {
      new ItemPickerDialog( mContext, mParent, mPlotType, DrawingActivity.SYMBOL_AREA ). show();
      return;
    } else if ( b == mBTsize ) {
      if ( mScale > DrawingPointPath.SCALE_XS ) {
        -- mScale;
        mParent.setPointScale( mScale );
        setTheTitle();
      }
      return;
    }
    dismiss();
  }

  // implements
  // public void closeDialog() {} 
}
