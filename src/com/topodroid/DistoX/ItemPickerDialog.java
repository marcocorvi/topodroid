/** @file ItemPickerDialog.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20140303 symbol picker mode, list or grid
 * 20140417 bug-fix: onClick not reported timely. replaced List with Grid
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

import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

// import android.widget.TextView;
import android.widget.ListView;
import android.widget.GridView;

import android.util.Log;

class ItemPickerDialog extends Dialog
                       implements View.OnClickListener, IItemPicker
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
  private  Button mBTleft;
  private  Button mBTright;
  // private  Button mBTcancel;
  // private  Button mBTok;

  private Context mContext;
  // private DrawingActivity mParent;
  private ItemDrawer mParent;

  //* private ListView    mList = null;
  private GridView    mList = null;
  private GridView    mGrid = null;
  private ItemAdapter mPointAdapter;
  private ItemAdapter mLineAdapter;
  private ItemAdapter mAreaAdapter;
  private boolean mUseText = false;
  private ItemAdapter mAdapter = null;

  // static int mLinePos;
  // static int mAreaPos;

 private SymbolPointLibrary mPointLib;
 private SymbolLineLibrary  mLineLib;
 private SymbolAreaLibrary  mAreaLib;

  /**
   * @param context   context
   * @param parent    DrawingActivity parent
   * @param type      drawing type
   */
  ItemPickerDialog( Context context, ItemDrawer parent, long type )
  {
    super( context );
    mContext = context;
    mParent  = parent;

    mPlotType = type;
    mItemType = mParent.mSymbol;
 
    mPointLib = DrawingBrushPaths.mPointLib;
    mLineLib = DrawingBrushPaths.mLineLib;
    mAreaLib = DrawingBrushPaths.mAreaLib;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    // requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    if ( TopoDroidSetting.mPickerType == TopoDroidSetting.PICKER_LIST ) {
      mUseText = true;
      setContentView(R.layout.item_picker_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );

      //* mList = (ListView) findViewById(R.id.item_list);
      mList = (GridView) findViewById(R.id.item_list);
      // mList.setOnItemClickListener( this );
      //* mList.setDividerHeight( 1 );
      mGrid = null;
    } else {
      mUseText = false;
      setContentView(R.layout.item_picker2_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );

      mGrid = (GridView) findViewById(R.id.item_grid);
      // mGrid.setOnItemClickListener( this );
      // mGrid.setDividerHeight( 2 );
      mList = null;
    }
    
    mBTpoint = (Button) findViewById(R.id.item_point);
    mBTline  = (Button) findViewById(R.id.item_line );
    mBTarea  = (Button) findViewById(R.id.item_area );
    mBTleft  = (Button) findViewById(R.id.item_left );
    mBTright = (Button) findViewById(R.id.item_right );
    // mBTcancel  = (Button) findViewById(R.id.item_cancel );
    // mBTok    = (Button) findViewById(R.id.item_ok   );

    mBTline.setOnClickListener( this );
    if ( TopoDroidSetting.mLevelOverBasic ) {
      mBTpoint.setOnClickListener( this );
      mBTarea.setOnClickListener( this );
      mBTleft.setOnClickListener( this );
      mBTright.setOnClickListener( this );
    } else {
      mBTpoint.setVisibility( View.GONE );
      mBTarea.setVisibility( View.GONE );
      mBTleft.setVisibility( View.GONE );
      mBTright.setVisibility( View.GONE );
    }
    // mBTcancel.setOnClickListener( this );
    // mBTok.setOnClickListener( this );

    // requestWindowFeature( Window.FEATURE_NO_TITLE );

    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... createAdapters" );
    createAdapters();
    updateList();

    setTypeAndItem( getAdapterPosition() );
  }

  private int getAdapterPosition()
  {
    int index = 0;
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT:
        index = mPointAdapter.getSelectedPos();
        break;
      case DrawingActivity.SYMBOL_LINE:
        index = mLineAdapter.getSelectedPos();
        break;
      case DrawingActivity.SYMBOL_AREA:
        index = mAreaAdapter.getSelectedPos();
        break;
    }
    return index;
  }

  void createAdapters()
  {
    mPointAdapter = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );
    mLineAdapter  = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );
    mAreaAdapter  = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );

    if ( TopoDroidSetting.mLevelOverBasic ) {
      int np = mPointLib.mAnyPointNr;
      for ( int i=0; i<np; ++i ) {
        SymbolPoint p = mPointLib.getAnyPoint( i );
        if ( p.isEnabled() ) {
          mPointAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_POINT, i, p, mUseText ) );
        }
      }
    }

    int nl = mLineLib.mAnyLineNr;
    for ( int j=0; j<nl; ++j ) {
      SymbolLine l = mLineLib.getAnyLine( j );
      if ( l.isEnabled() ) {
        mLineAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_LINE, j, l, mUseText ) );
      }
    }

    if ( TopoDroidSetting.mLevelOverBasic ) {
      int na = mAreaLib.mAnyAreaNr;
      for ( int k=0; k<na; ++k ) {
        SymbolArea a = mAreaLib.getAnyArea( k );
        if ( a.isEnabled() ) {
          mAreaAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_AREA, k, a, mUseText ) );
        }
      }
    }

    mPointAdapter.setSelectedItem( mParent.mCurrentPoint );
    mLineAdapter.setSelectedItem( mParent.mCurrentLine ); 
    mAreaAdapter.setSelectedItem( mParent.mCurrentArea );
  }

  private void updateList()
  {
    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... updateList type " + mItemType );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT:
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mAdapter = mPointAdapter;
          mBTpoint.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
          mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        }
        break;
      case DrawingActivity.SYMBOL_LINE:
        mAdapter = mLineAdapter;
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        break;
      case DrawingActivity.SYMBOL_AREA:
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mAdapter = mAreaAdapter;
          mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mBTarea.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        }
        break;
    }
    if ( mAdapter != null ) {
      if ( mList != null ) {
        mList.setAdapter( mAdapter );
        mList.invalidate();
      } else if ( mGrid != null ) {
        mGrid.setAdapter( mAdapter );
        mGrid.invalidate();
      }
    }
  }

  // pos 
  public void setTypeAndItem( int index )
  {
    // Log.v( TopoDroidLog.TAG, "setTypeAndItem type " + mItemType  + " item " + index );

    ItemSymbol is;
    String title = "";
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        if ( TopoDroidSetting.mLevelOverBasic ) {
          is = mPointAdapter.get( index );
          // Log.v( TopoDroidLog.TAG, "setTypeAndItem type point pos " + index + " index " + is.mIndex );
          mParent.mCurrentPoint = is.mIndex;
          mParent.pointSelected( is.mIndex ); // mPointAdapter.getSelectedItem() );
          title = mContext.getResources().getString( R.string.POINT ) + " " +
                  mPointLib.getAnyPointName( is.mIndex );
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        // mLinePos = index;
        is = mLineAdapter.get( index );
        // Log.v( TopoDroidLog.TAG, "setTypeAndItem type line pos " + index + " index " + is.mIndex );
        if ( mPlotType != PlotInfo.PLOT_SECTION || is.mIndex != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
          mParent.mCurrentLine = is.mIndex;
          mParent.lineSelected( is.mIndex ); // mLineAdapter.getSelectedItem() );
        } else {
        }
        title = mContext.getResources().getString( R.string.LINE ) + " " +
                mLineLib.getLineName( is.mIndex );
        break;
      case DrawingActivity.SYMBOL_AREA: 
        if ( TopoDroidSetting.mLevelOverBasic ) {
          // mAreaPos = index;
          is = mAreaAdapter.get( index );
          mParent.mCurrentArea = is.mIndex;
          mParent.areaSelected( is.mIndex ); // mAreaAdapter.getSelectedItem() );
          title = mContext.getResources().getString( R.string.AREA ) + " " +
                  mAreaLib.getAreaName( is.mIndex );
        }
        break;
    }
    // cancel();
    setTitle( title );
  }

  private void setTypeFromCurrent( )
  {
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mParent.pointSelected( mParent.mCurrentPoint );
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        if ( mPlotType != PlotInfo.PLOT_SECTION ) {
          mParent.lineSelected( mParent.mCurrentLine );
        } else {
        }
        break;
      case DrawingActivity.SYMBOL_AREA: 
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mParent.areaSelected( mParent.mCurrentArea );
        }
        break;
    }
    setTypeAndItem( getAdapterPosition() );
  }

  void rotatePoint( int angle )
  {
    if ( TopoDroidSetting.mLevelOverBasic && mItemType == DrawingActivity.SYMBOL_POINT ) {
      // Log.v( TopoDroidApp.TAG, "rotate point " + mParent.mCurrentPoint );
      mPointAdapter.rotatePoint( mParent.mCurrentPoint, angle );
    }
  }

  @Override
  public void onBackPressed ()
  {
    // Log.v( TopoDroidApp.TAG, "onBackPressed type " + mItemType );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mParent.pointSelected( mParent.mCurrentPoint );
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        mParent.lineSelected( mParent.mCurrentLine ); 
        break;
      case DrawingActivity.SYMBOL_AREA: 
        if ( TopoDroidSetting.mLevelOverBasic ) {
          mParent.areaSelected( mParent.mCurrentArea );
        }
        break;
    }
    cancel();
  }

  @Override
  public void onClick(View view)
  {
    // Log.v("DistoX", "ItemPicker onClick()" );
    switch (view.getId()) {
      case R.id.item_point:
        if ( TopoDroidSetting.mLevelOverBasic ) {
          if ( mItemType != DrawingActivity.SYMBOL_POINT ) {
            mItemType = DrawingActivity.SYMBOL_POINT;
            updateList();
            setTypeFromCurrent( );
          }
        }
        break;
      case R.id.item_line:
        if ( mItemType != DrawingActivity.SYMBOL_LINE ) {
          mItemType = DrawingActivity.SYMBOL_LINE;
          updateList();
          setTypeFromCurrent( );
        }
        break;
      case R.id.item_area:
        if ( TopoDroidSetting.mLevelOverBasic ) {
          if ( mItemType != DrawingActivity.SYMBOL_AREA ) {
            mItemType = DrawingActivity.SYMBOL_AREA;
            updateList();
            setTypeFromCurrent( );
          }
        }
        break;
      case R.id.item_left:
        if ( TopoDroidSetting.mLevelOverBasic ) rotatePoint( -10 );
        break;
      case R.id.item_right:
        if ( TopoDroidSetting.mLevelOverBasic ) rotatePoint( 10 );
        break;
      // case R.id.item_cancel:
      //   dismiss();
      //   break;
      // case R.id.item_ok:
      //   setTypeFromCurrent();
      //   break;
      default: 
        // if ( mAdapter != null ) mAdapter.doClick( view );
        // if ( mList != null ) mList.invalidate();
        break;
    }
    // dismiss();
  }

  // @Override
  // public void onItemClick( AdapterView adapter, View view, int pos, long id )
  // {
  //    Log.v( "DistoX", "ItemPicker onItemCLick()" );
  //    if ( mAdapter != null ) mAdapter.doClick( view );
  // }
}
