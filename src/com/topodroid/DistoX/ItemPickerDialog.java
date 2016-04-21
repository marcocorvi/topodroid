/** @file ItemPickerDialog.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing item picker dialog
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

class ItemPickerDialog extends MyDialog
                       implements View.OnClickListener
                       , IItemPicker
                       // , View.OnLongClickListener
                       // , AdapterView.OnItemClickListener
{
  private final static float DIMXP = 1.8f;
  private final static float DIMXL = 2.2f;
  private final static float DIMYL = 1.9f;

  private int mItemType; // items type
  private int mPointPos;  // item point position
  private int mLinePos;   // item line  position
  private int mAreaPos;   // item area  position
  private long mPlotType;
  private int mScale;
  private int mSelectedPoint;
  private int mSelectedLine;
  private int mSelectedArea;

  private  Button mBTpoint;
  private  Button mBTline;
  private  Button mBTarea;
  private  Button mBTsize;
  // private  Button mBTleft;
  // private  Button mBTright;
  // private  Button mBTcancel;
  // private  Button mBTok;
  private SeekBar mSeekBar;

  // private DrawingActivity mParent;
  private ItemDrawer mParent;

  //* private ListView    mList = null;
  private GridView    mList = null;
  private GridView    mGrid  = null;
  private GridView    mGridL = null;
  private GridView    mGridA = null;
  private ItemAdapter mPointAdapter = null;
  private ItemAdapter mLineAdapter  = null;
  private ItemAdapter mAreaAdapter  = null;
  private boolean mUseText = false;
  private ItemAdapter mAdapter = null;

  private LinearLayout mRecentLayout;
  private ItemButton mRecent[];

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
  ItemPickerDialog( Context context, ItemDrawer parent, long type, int item_type  )
  {
    super( context, R.string.ItemPickerDialog );
    mParent  = parent;

    mPlotType = type;
    mItemType = item_type; // mParent.mSymbol;
 
    mPointLib = DrawingBrushPaths.mPointLib;
    mLineLib = DrawingBrushPaths.mLineLib;
    mAreaLib = DrawingBrushPaths.mAreaLib;

    mScale = mParent.getPointScale();
    mSelectedPoint = mParent.mCurrentPoint;
    mSelectedLine  = mParent.mCurrentLine;
    mSelectedArea  = mParent.mCurrentArea;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    // requestWindowFeature(Window.FEATURE_NO_TITLE);

    createAdapters();
    
    if ( TDSetting.mPickerType == TDSetting.PICKER_GRID || TDSetting.mPickerType == TDSetting.PICKER_GRID_3 ) {
      mUseText = false;
      setContentView(R.layout.item_picker2_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );

      mGrid  = (GridView) findViewById(R.id.item_grid);
      mGridL = (GridView) findViewById(R.id.item_grid_line);
      mGridA = (GridView) findViewById(R.id.item_grid_area);
      if ( TDSetting.mPickerType == TDSetting.PICKER_GRID ) {
        mGridL.setVisibility( View.GONE );
        mGridA.setVisibility( View.GONE );
      } else {
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, 0 );
        params.weight = 10 + mPointAdapter.size();
        mGrid.setLayoutParams(  params );
        params = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, 0 );
        params.weight = 10 + mLineAdapter.size();
        mGridL.setLayoutParams( params );
        params = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, 0 );
        params.weight = 10 + mAreaAdapter.size();
        mGridA.setLayoutParams( params );
      }
      // mGrid.setOnItemClickListener( this );
      // mGrid.setDividerHeight( 2 );
      mList = null;
    } else { // PICKER_LIST || PICKER_RECENT
      mUseText = true;
      setContentView(R.layout.item_picker_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );

      //* mList = (ListView) findViewById(R.id.item_list);
      mList = (GridView) findViewById(R.id.item_list);
      // mList.setOnItemClickListener( this );
      //* mList.setDividerHeight( 1 );
      mGrid  = null;
      mGridL = null;
      mGridA = null;
    }

    mRecentLayout = (LinearLayout) findViewById( R.id.layout2 );
    mRecent = new ItemButton[ TDSetting.mRecentNr ];
    for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
      mRecent[k] = new ItemButton( mContext );
      mRecent[k].setOnClickListener( this );
      mRecentLayout.addView( mRecent[k] );
    }
    
    mBTpoint = (Button) findViewById(R.id.item_point);
    mBTline  = (Button) findViewById(R.id.item_line );
    mBTarea  = (Button) findViewById(R.id.item_area );
    mBTsize  = (Button) findViewById(R.id.size);
    // mBTleft  = (Button) findViewById(R.id.item_left );
    // mBTright = (Button) findViewById(R.id.item_right );
    mSeekBar = (SeekBar) findViewById(R.id.seekbar );
    // mBTcancel  = (Button) findViewById(R.id.item_cancel );
    // mBTok    = (Button) findViewById(R.id.item_ok   );

    if ( TDSetting.mPickerType == TDSetting.PICKER_GRID_3 ) {
      mBTpoint.setVisibility( View.GONE );
      mBTline.setVisibility( View.GONE );
      mBTarea.setVisibility( View.GONE );
    } else {
      mBTpoint.setOnClickListener( this );
      mBTline.setOnClickListener( this );
      mBTarea.setOnClickListener( this );
    }
    mBTsize.setOnClickListener( this );
    mBTsize.setOnLongClickListener( new View.OnLongClickListener() {
      @Override
      public boolean onLongClick( View v ) {
        if ( mScale < DrawingPointPath.SCALE_XL ) {
          ++mScale;
          mParent.setPointScale( mScale );
          setTheTitle();
        }
        return true;
      }
    } );

    // mBTleft.setOnClickListener( this );
    // mBTright.setOnClickListener( this );
    mSeekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
        public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
          if ( fromUser ) {
            setItemAngle( (180 + progress)%360 );
          }
        }
        public void onStartTrackingTouch(SeekBar seekbar) { }
        public void onStopTrackingTouch(SeekBar seekbar) { }
    } );
    // } else {
    //   mBTpoint.setVisibility( View.GONE );
    //   mBTarea.setVisibility( View.GONE );
    //   // mBTleft.setVisibility( View.GONE );
    //   // mBTright.setVisibility( View.GONE );
    //   mSeekBar.setVisibility( View.GONE );
    // }
    // mBTcancel.setOnClickListener( this );
    // mBTok.setOnClickListener( this );

    // requestWindowFeature( Window.FEATURE_NO_TITLE );

    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... createAdapters" );
    updateList();
    updateRecentButtons( mItemType );

    setTypeAndItem( mItemType, getAdapterPosition() );
    // setTheTitle();
  }

  private void setSeekBarProgress()
  {
    boolean orientable = false;
    if ( mItemType == DrawingActivity.SYMBOL_POINT &&  mPointAdapter != null ) {
      int index = mPointAdapter.getSelectedPos();
      ItemSymbol item = mPointAdapter.get( index );
      if ( item != null ) {
        SymbolInterface symbol = item.mSymbol;
        if ( symbol != null && symbol.isOrientable() ) {
          int progress = (180+symbol.getAngle())%360;
          mSeekBar.setProgress( progress );
          // Log.v("DistoX", "set progress " + progress );
          orientable = true;
        }
      }
    }
    mSeekBar.setEnabled( orientable );
  }

  private void setItemAngle( int angle )
  {
    if ( mItemType == DrawingActivity.SYMBOL_POINT && mPointAdapter != null ) {
      int index = mPointAdapter.getSelectedPos();
      // Log.v("DistoX", "set item " + index + " angle " + angle );
      // mPointAdapter.setPointOrientation( index, angle );
 
      ItemSymbol item = mPointAdapter.get( index );
      if ( item != null ) {
        item.setAngle( angle );
        Symbol[] symbols = ItemDrawer.mRecentPoint;
        for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
          Symbol p = symbols[k];
          if ( p == null ) break;
          if ( p == item.mSymbol ) {
            mRecent[k].reset( p.getPaint(), p.getPath(), DIMXP, DIMXP );
            break;
          }
        }
      }
    }
  }

  private int getAdapterPosition()
  {
    int index = 0;
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT:
        if ( mPointAdapter != null ) index = mPointAdapter.getSelectedPos();
        break;
      case DrawingActivity.SYMBOL_LINE:
        if ( mLineAdapter != null ) index = mLineAdapter.getSelectedPos();
        break;
      case DrawingActivity.SYMBOL_AREA:
        if ( mAreaAdapter != null ) index = mAreaAdapter.getSelectedPos();
        break;
    }
    return index;
  }

  private void setRecentButtons( Symbol symbols[], float sx, float sy )
  {
    for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
      Symbol p = symbols[k];
      if ( p == null ) {
        mRecent[k].setVisibility( View.INVISIBLE );
      } else {
        mRecent[k].reset( p.getPaint(), p.getPath(), sx, sy );
        mRecent[k].setVisibility( View.VISIBLE );
      }
    }
  }

  private void updateRecentButtons( int item_type ) 
  {
    if ( TDSetting.mPickerType != TDSetting.PICKER_GRID_3 ) {
      // float sx=1.0f, sy=1.0f;
      if ( item_type == DrawingActivity.SYMBOL_POINT ) {
        mBTsize.setVisibility( View.VISIBLE );
        setRecentButtons( ItemDrawer.mRecentPoint, DIMXP, DIMXP );
      } else if ( item_type == DrawingActivity.SYMBOL_LINE ) {
        mBTsize.setVisibility( View.GONE );
        setRecentButtons( ItemDrawer.mRecentLine, DIMXL, DIMYL );
      } else if ( item_type == DrawingActivity.SYMBOL_AREA ) {
        mBTsize.setVisibility( View.GONE );
        setRecentButtons( ItemDrawer.mRecentArea, DIMXL, DIMYL );
      }
    } else {
      // nothing
    }
  }
        
  private void createAdapters()
  {
    // if ( TDSetting.mLevelOverBasic ) 
    {
      mPointAdapter = new ItemAdapter( mContext, this, DrawingActivity.SYMBOL_POINT, 
                                       R.layout.item, new ArrayList<ItemSymbol>() );
      int np = mPointLib.mSymbolNr;
      for ( int i=0; i<np; ++i ) {
        SymbolPoint p = (SymbolPoint)mPointLib.getSymbolByIndex( i );
        if ( p.isEnabled() ) {
          mPointAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_POINT, i, p, mUseText ) );
        }
      }
      mPointAdapter.setSelectedItem( mSelectedPoint );
    }

    mLineAdapter  = new ItemAdapter( mContext, this, DrawingActivity.SYMBOL_LINE,
                                     R.layout.item, new ArrayList<ItemSymbol>() );
    int nl = mLineLib.mSymbolNr;
    for ( int j=0; j<nl; ++j ) {
      SymbolLine l = (SymbolLine)mLineLib.getSymbolByIndex( j );
      if ( l.isEnabled() ) {
        mLineAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_LINE, j, l, mUseText ) );
      }
    }
    mLineAdapter.setSelectedItem( mSelectedLine ); 

    // if ( TDSetting.mLevelOverBasic )
    {
      mAreaAdapter  = new ItemAdapter( mContext, this, DrawingActivity.SYMBOL_AREA,
                                       R.layout.item, new ArrayList<ItemSymbol>() );
      int na = mAreaLib.mSymbolNr;
      for ( int k=0; k<na; ++k ) {
        SymbolArea a = (SymbolArea)mAreaLib.getSymbolByIndex( k );
        if ( a.isEnabled() ) {
          mAreaAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_AREA, k, a, mUseText ) );
        }
      }
      mAreaAdapter.setSelectedItem( mSelectedArea );
    }
  }

  private void updateList()
  {
    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... update List type " + mItemType );
    if ( TDSetting.mPickerType != TDSetting.PICKER_GRID_3 ) {
      switch ( mItemType ) {
        case DrawingActivity.SYMBOL_POINT:
          // if ( TDSetting.mLevelOverBasic )
          {
            mAdapter = mPointAdapter;
            mBTpoint.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
            mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
            mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
            mSeekBar.setVisibility( View.VISIBLE );
            setSeekBarProgress();
          }
          break;
        case DrawingActivity.SYMBOL_LINE:
          mAdapter = mLineAdapter;
          mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mBTline.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
          mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
          mSeekBar.setVisibility( View.INVISIBLE );
          break;
        case DrawingActivity.SYMBOL_AREA:
          // if ( TDSetting.mLevelOverBasic )
          {
            mAdapter = mAreaAdapter;
            mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
            mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
            mBTarea.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
            mSeekBar.setVisibility( View.INVISIBLE );
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
    } else {
      mGrid.setAdapter( mPointAdapter );
      mGrid.invalidate();
      mGridL.setAdapter( mLineAdapter );
      mGridL.invalidate();
      mGridA.setAdapter( mAreaAdapter );
      mGridA.invalidate();
    }
  }

  private void setTheTitle()
  {
    StringBuilder title = new StringBuilder();
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        title.append( "[" );
        title.append( DrawingPointPath.scaleToStringUC( mScale ) );
        title.append( "] " );
        title.append( mContext.getResources().getString( R.string.POINT ) );
        title.append( " " );
        title.append( mPointLib.getSymbolName( mSelectedPoint ) );
        break;
      case DrawingActivity.SYMBOL_LINE: 
        title.append( mContext.getResources().getString( R.string.LINE ) );
        title.append( " " );
        title.append( mLineLib.getSymbolName( mSelectedLine ) );
        break;
      case DrawingActivity.SYMBOL_AREA: 
        title.append( mContext.getResources().getString( R.string.AREA ) );
        title.append( " " );
        title.append( mAreaLib.getSymbolName( mSelectedArea ) );
        break;
    }
    // Log.v("DistoX", "set title " + title.toString() );
    setTitle( title.toString() );
  }

  // pos 
  public void setTypeAndItem( int type, int index )
  {
    // Log.v( TDLog.TAG, "set TypeAndItem type " + mItemType  + " item " + index );
    mItemType = type;
    ItemSymbol is;
    switch ( type ) {
      case DrawingActivity.SYMBOL_POINT: 
        if ( mPointAdapter != null /* && TDSetting.mLevelOverBasic */ ) {
          is = mPointAdapter.get( index );
          // Log.v( TDLog.TAG, "set TypeAndItem type point pos " + index + " index " + is.mIndex );
          mSelectedPoint = is.mIndex;
          // mParent.pointSelected( is.mIndex, false ); // mPointAdapter.getSelectedItem() );
          setSeekBarProgress();
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        if ( mLineAdapter != null ) {
          is = mLineAdapter.get( index );
          // Log.v( TDLog.TAG, "set TypeAndItem type line pos " + index + " index " + is.mIndex );
          if ( mPlotType != PlotInfo.PLOT_SECTION || is.mIndex != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
            mSelectedLine = is.mIndex;
            // mParent.lineSelected( is.mIndex, false ); // mLineAdapter.getSelectedItem() );
          } else {
          }
          mSeekBar.setEnabled( false );
        }
        break;
      case DrawingActivity.SYMBOL_AREA: 
        if ( mAreaAdapter != null /* && TDSetting.mLevelOverBasic */ ) {
          // mAreaPos = index;
          is = mAreaAdapter.get( index );
          // Log.v( TDLog.TAG, "set TypeAndItem type area pos " + index + " index " + is.mIndex );
          mSelectedArea = is.mIndex;
          // mParent.areaSelected( is.mIndex, false ); // mAreaAdapter.getSelectedItem() );
          mSeekBar.setEnabled( false );
        }
        break;
    }
    // cancel();
    setTheTitle();
  }

  // this is called tapping the tab-buttons on the top
  private void setTypeFromCurrent( )
  {
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        // if ( TDSetting.mLevelOverBasic ) 
        {
          // mParent.pointSelected( mSelectedPoint, false );
          // mSeekBar.setEnabled( DrawingBrushPaths.mPointLib.isPointOrientable( mSelectedPoint ) );
          if ( mPointAdapter != null ) setTypeAndItem( mItemType, mPointAdapter.getSelectedPos() );
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        // if ( ! PlotInfo.isAnySection( mPlotType ) ) {
        //   mParent.lineSelected( mSelectedLine, false );
        // } else {
        // }
        if ( mLineAdapter != null ) setTypeAndItem( mItemType, mLineAdapter.getSelectedPos() );
        break;
      case DrawingActivity.SYMBOL_AREA: 
        // if ( TDSetting.mLevelOverBasic ) 
        {
          // mParent.areaSelected( mSelectedArea, false );
          if ( mAreaAdapter != null ) setTypeAndItem( mItemType, mAreaAdapter.getSelectedPos() );
        }
        break;
    }
    // setTypeAndItem( getAdapterPosition() );
    setTheTitle();
  }

  // void rotatePoint( int angle )
  // {
  //   if ( mPointAdapter == null ) return;
  //   if ( TDSetting.mLevelOverBasic && mItemType == DrawingActivity.SYMBOL_POINT ) {
  //     // Log.v( TopoDroidApp.TAG, "rotate point " + mSelectedPoint );
  //     mPointAdapter.rotatePoint( mSelectedPoint, angle );
  //   }
  // }

  void setPointOrientation( int angle )
  {
    if ( mPointAdapter == null ) return;
    if ( /* TDSetting.mLevelOverBasic && */ mItemType == DrawingActivity.SYMBOL_POINT ) {
      // Log.v( TopoDroidApp.TAG, "rotate point " + mSelectedPoint );
      mPointAdapter.setPointOrientation( mSelectedPoint, angle );
      // ItemSymbol item = mPointAdapter.getSelectedItem();
      // if ( item != null ) {
      //   angle -= (int) item.mSymbol.getAngle();
      //   mPointAdapter.rotatePoint( mSelectedPoint, angle );
      // }
    }
  }


  @Override
  public void onBackPressed ()
  {
    // Log.v( TopoDroidApp.TAG, "onBackPressed type " + mItemType );
    itemSelected();
    cancel();
  }

  private void itemSelected()
  {
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        // if ( TDSetting.mLevelOverBasic )
        {
          // Log.v("DistoX", "selected point " + mSelectedPoint );
          mParent.pointSelected( mSelectedPoint, true );
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        // Log.v("DistoX", "selected line " + mSelectedLine );
        mParent.lineSelected( mSelectedLine, true ); 
        break;
      case DrawingActivity.SYMBOL_AREA: 
        // if ( TDSetting.mLevelOverBasic )
        {
          // Log.v("DistoX", "selected area " + mSelectedArea );
          mParent.areaSelected( mSelectedArea, true );
        }
        break;
    }
  }

  @Override
  public void onClick(View view)
  {
    // Log.v("DistoX", "ItemPicker onClick()" );
    switch (view.getId()) {
      case R.id.item_point:
        // if ( TDSetting.mLevelOverBasic )
        {
          if ( mItemType != DrawingActivity.SYMBOL_POINT ) {
            mItemType = DrawingActivity.SYMBOL_POINT;
            updateList();
            updateRecentButtons( mItemType );
            setTypeFromCurrent( );
          }
        }
        break;
      case R.id.item_line:
        if ( mItemType != DrawingActivity.SYMBOL_LINE ) {
          mItemType = DrawingActivity.SYMBOL_LINE;
          updateList();
          updateRecentButtons( mItemType );
          setTypeFromCurrent( );
        }
        break;
      case R.id.item_area:
        // if ( TDSetting.mLevelOverBasic )
        {
          if ( mItemType != DrawingActivity.SYMBOL_AREA ) {
            mItemType = DrawingActivity.SYMBOL_AREA;
            updateList();
            updateRecentButtons( mItemType );
            setTypeFromCurrent( );
          }
        }
        break;
      case R.id.size:
        if ( mScale > DrawingPointPath.SCALE_XS ) {
          -- mScale;
          mParent.setPointScale( mScale );
          setTheTitle();
        }
        break;
    }

    try {
      ItemButton iv = (ItemButton)view;
      if ( iv != null ) {
        for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
          if ( iv == mRecent[k] ) {
            setRecent( k );
            return;
          }
        }
      }
    } catch ( ClassCastException e ) { } // it is ok
     
    // dismiss();
  }

  // @Override
  // public void onItemClick( AdapterView adapter, View view, int pos, long id )
  // {
  //    Log.v( "DistoX", "ItemPicker onItemCLick()" );
  //    if ( mAdapter != null ) mAdapter.doClick( view );
  // }

  private void setRecent( int k )
  {
    Symbol p = null;
    if ( mItemType == DrawingActivity.SYMBOL_POINT ) {
      p = ItemDrawer.mRecentPoint[k];
    } else if ( mItemType == DrawingActivity.SYMBOL_LINE ) {
      p = ItemDrawer.mRecentLine[k];
    } else if ( mItemType == DrawingActivity.SYMBOL_AREA ) {
      p = ItemDrawer.mRecentArea[k];
    }
    if ( p != null ) {
      if ( mAdapter != null ) mAdapter.setSelectedItem( p );
      setSeekBarProgress();
    }
  } 
    

  public void closeDialog()
  {  
    itemSelected();
    dismiss();
  }
}
