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

class ItemPickerDialog extends Dialog
                       implements View.OnClickListener
                       , IItemPicker
                       // , View.OnLongClickListener
                       // , AdapterView.OnItemClickListener
{
  private int mItemType; // items type
  private int mPointPos;  // item point position
  private int mLinePos;   // item line  position
  private int mAreaPos;   // item area  position
  private long mPlotType;
  private int mScale;

  private  Button mBTpoint;
  private  Button mBTline;
  private  Button mBTarea;
  private  Button mBTsize;
  // private  Button mBTleft;
  // private  Button mBTright;
  // private  Button mBTcancel;
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
    super( context );
    mContext = context;
    mParent  = parent;

    mPlotType = type;
    mItemType = item_type; // mParent.mSymbol;
 
    mPointLib = DrawingBrushPaths.mPointLib;
    mLineLib = DrawingBrushPaths.mLineLib;
    mAreaLib = DrawingBrushPaths.mAreaLib;

    mScale = mParent.getPointScale();
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    // requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    if ( TopoDroidSetting.mPickerType == TopoDroidSetting.PICKER_GRID ) {
      mUseText = false;
      setContentView(R.layout.item_picker2_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );

      mGrid = (GridView) findViewById(R.id.item_grid);
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
      mGrid = null;
    }

    mRecentLayout = (LinearLayout) findViewById( R.id.layout2 );
    mRecent = new ItemButton[4];
    mRecent[0] = ( ItemButton ) findViewById( R.id.recent0 );
    mRecent[0].setOnClickListener( this );
    mRecent[1] = ( ItemButton ) findViewById( R.id.recent1 );
    mRecent[1].setOnClickListener( this );
    mRecent[2] = ( ItemButton ) findViewById( R.id.recent2 );
    mRecent[2].setOnClickListener( this );
    mRecent[3] = ( ItemButton ) findViewById( R.id.recent3 );
    mRecent[3].setOnClickListener( this );
    
    mBTpoint = (Button) findViewById(R.id.item_point);
    mBTline  = (Button) findViewById(R.id.item_line );
    mBTarea  = (Button) findViewById(R.id.item_area );
    mBTsize  = (Button) findViewById(R.id.size);
    // mBTleft  = (Button) findViewById(R.id.item_left );
    // mBTright = (Button) findViewById(R.id.item_right );
    mSeekBar = (SeekBar) findViewById(R.id.seekbar );
    // mBTcancel  = (Button) findViewById(R.id.item_cancel );
    // mBTok    = (Button) findViewById(R.id.item_ok   );

    mBTline.setOnClickListener( this );
    mBTpoint.setOnClickListener( this );
    mBTarea.setOnClickListener( this );
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
    createAdapters();
    updateList();
    updateRecentButtons( mItemType );

    setTypeAndItem( getAdapterPosition() );
    setTheTitle();
  }

  private void setSeekBarProgress()
  {
    if ( mItemType == DrawingActivity.SYMBOL_POINT &&  mPointAdapter != null ) {
      int index = mPointAdapter.getSelectedPos();
      ItemSymbol item = mPointAdapter.get( index );
      if ( item != null ) {
        SymbolInterface symbol = item.mSymbol;
        if ( symbol != null ) {
          int progress = (180+symbol.getAngle())%360;
          mSeekBar.setProgress( progress );
          // Log.v("DistoX", "set progress " + progress );
        }
      }
    }
  }

  private void setItemAngle( int angle )
  {
    if ( mItemType == DrawingActivity.SYMBOL_POINT &&  mPointAdapter != null ) {
      int index = mPointAdapter.getSelectedPos();
      // Log.v("DistoX", "set item " + index + " angle " + angle );
      mPointAdapter.setPointOrientation( index, angle );
 
      // ItemSymbol item = mPointAdapter.get( index );
      // if ( item != null ) {
      //   SymbolInterface symbol = item.mSymbol;
      //   if ( symbol != null && symbol.isOrientable() ) {
      //     symbol.setAngle( angle );
      //   }
      // }
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
    for ( int k=0; k<ItemDrawer.NR_RECENT; ++k ) {
      Symbol p = symbols[k];
      if ( p == null ) break;
      mRecent[k].reset( p.getPaint(), p.getPath(), sx, sy );
    }
  }

  private void updateRecentButtons( int item_type ) 
  {
    // float sx=1.0f, sy=1.0f;
    if ( item_type == DrawingActivity.SYMBOL_POINT ) {
      mBTsize.setVisibility( View.VISIBLE );
      setRecentButtons( ItemDrawer.mRecentPoint, 1.5f, 1.5f ); // sx*1.5f, sy*1.5f
    } else if ( item_type == DrawingActivity.SYMBOL_LINE ) {
      mBTsize.setVisibility( View.GONE );
      setRecentButtons( ItemDrawer.mRecentLine, 2.0f, 1.7f ); // sx*2.0f, sy*1.7f
    } else if ( item_type == DrawingActivity.SYMBOL_AREA ) {
      mBTsize.setVisibility( View.GONE );
      setRecentButtons( ItemDrawer.mRecentArea, 2.0f, 1.7f ); // sx*2.0f, sy*1.7f
    }
  }
        
  private void createAdapters()
  {

    // if ( TopoDroidSetting.mLevelOverBasic ) 
    {
      mPointAdapter = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );
      int np = mPointLib.mAnyPointNr;
      for ( int i=0; i<np; ++i ) {
        SymbolPoint p = mPointLib.getAnyPoint( i );
        if ( p.isEnabled() ) {
          mPointAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_POINT, i, p, mUseText ) );
        }
      }
      mPointAdapter.setSelectedItem( mParent.mCurrentPoint );
    }

    mLineAdapter  = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );
    int nl = mLineLib.mAnyLineNr;
    for ( int j=0; j<nl; ++j ) {
      SymbolLine l = mLineLib.getAnyLine( j );
      if ( l.isEnabled() ) {
        mLineAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_LINE, j, l, mUseText ) );
      }
    }
    mLineAdapter.setSelectedItem( mParent.mCurrentLine ); 

    // if ( TopoDroidSetting.mLevelOverBasic )
    {
      mAreaAdapter  = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );
      int na = mAreaLib.mAnyAreaNr;
      for ( int k=0; k<na; ++k ) {
        SymbolArea a = mAreaLib.getAnyArea( k );
        if ( a.isEnabled() ) {
          mAreaAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_AREA, k, a, mUseText ) );
        }
      }
      mAreaAdapter.setSelectedItem( mParent.mCurrentArea );
    }
  }

  private void updateList()
  {
    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... updateList type " + mItemType );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT:
        // if ( TopoDroidSetting.mLevelOverBasic )
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
        // if ( TopoDroidSetting.mLevelOverBasic )
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
        title.append( mPointLib.getAnyPointName( mParent.mCurrentPoint ) );
        break;
      case DrawingActivity.SYMBOL_LINE: 
        title.append( mContext.getResources().getString( R.string.LINE ) );
        title.append( " " );
        title.append( mLineLib.getLineName( mParent.mCurrentLine ) );
        break;
      case DrawingActivity.SYMBOL_AREA: 
        title.append( mContext.getResources().getString( R.string.AREA ) );
        title.append( " " );
        title.append( mAreaLib.getAreaName( mParent.mCurrentArea ) );
        break;
    }
    setTitle( title.toString() );
  }


  // pos 
  public void setTypeAndItem( int index )
  {
    // Log.v( TopoDroidLog.TAG, "setTypeAndItem type " + mItemType  + " item " + index );
    ItemSymbol is;
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        if ( mPointAdapter != null /* && TopoDroidSetting.mLevelOverBasic */ ) {
          is = mPointAdapter.get( index );
          // Log.v( TopoDroidLog.TAG, "setTypeAndItem type point pos " + index + " index " + is.mIndex );
          mParent.mCurrentPoint = is.mIndex;
          mParent.pointSelected( is.mIndex, false ); // mPointAdapter.getSelectedItem() );
          setSeekBarProgress();
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        if ( mLineAdapter != null ) {
          is = mLineAdapter.get( index );
          // Log.v( TopoDroidLog.TAG, "setTypeAndItem type line pos " + index + " index " + is.mIndex );
          if ( mPlotType != PlotInfo.PLOT_SECTION || is.mIndex != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
            mParent.mCurrentLine = is.mIndex;
            mParent.lineSelected( is.mIndex, false ); // mLineAdapter.getSelectedItem() );
          } else {
          }
        }
        break;
      case DrawingActivity.SYMBOL_AREA: 
        if ( mAreaAdapter != null /* && TopoDroidSetting.mLevelOverBasic */ ) {
          // mAreaPos = index;
          is = mAreaAdapter.get( index );
          mParent.mCurrentArea = is.mIndex;
          mParent.areaSelected( is.mIndex, false ); // mAreaAdapter.getSelectedItem() );
        }
        break;
    }
    // cancel();
  }

  private void setTypeFromCurrent( )
  {
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        // if ( TopoDroidSetting.mLevelOverBasic ) 
        {
          mParent.pointSelected( mParent.mCurrentPoint, false );
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        if ( mPlotType != PlotInfo.PLOT_SECTION ) {
          mParent.lineSelected( mParent.mCurrentLine, false );
        } else {
        }
        break;
      case DrawingActivity.SYMBOL_AREA: 
        // if ( TopoDroidSetting.mLevelOverBasic ) 
        {
          mParent.areaSelected( mParent.mCurrentArea, false );
        }
        break;
    }
    setTypeAndItem( getAdapterPosition() );
    setTheTitle();
  }

  // void rotatePoint( int angle )
  // {
  //   if ( mPointAdapter == null ) return;
  //   if ( TopoDroidSetting.mLevelOverBasic && mItemType == DrawingActivity.SYMBOL_POINT ) {
  //     // Log.v( TopoDroidApp.TAG, "rotate point " + mParent.mCurrentPoint );
  //     mPointAdapter.rotatePoint( mParent.mCurrentPoint, angle );
  //   }
  // }

  void setPointOrientation( int angle )
  {
    if ( mPointAdapter == null ) return;
    if ( /* TopoDroidSetting.mLevelOverBasic && */ mItemType == DrawingActivity.SYMBOL_POINT ) {
      // Log.v( TopoDroidApp.TAG, "rotate point " + mParent.mCurrentPoint );
      mPointAdapter.setPointOrientation( mParent.mCurrentPoint, angle );
      // ItemSymbol item = mPointAdapter.getSelectedItem();
      // if ( item != null ) {
      //   angle -= (int) item.mSymbol.getAngle();
      //   mPointAdapter.rotatePoint( mParent.mCurrentPoint, angle );
      // }
    }
  }


  @Override
  public void onBackPressed ()
  {
    // Log.v( TopoDroidApp.TAG, "onBackPressed type " + mItemType );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        // if ( TopoDroidSetting.mLevelOverBasic )
        {
          mParent.pointSelected( mParent.mCurrentPoint, true );
        }
        break;
      case DrawingActivity.SYMBOL_LINE: 
        mParent.lineSelected( mParent.mCurrentLine, true ); 
        break;
      case DrawingActivity.SYMBOL_AREA: 
        // if ( TopoDroidSetting.mLevelOverBasic )
        {
          mParent.areaSelected( mParent.mCurrentArea, true );
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
      case R.id.recent0:
        setRecent(0);
        break;
      case R.id.recent1:
        setRecent(1);
        break;
      case R.id.recent2:
        setRecent(2);
        break;
      case R.id.recent3:
        setRecent(3);
        break;
      case R.id.item_point:
        // if ( TopoDroidSetting.mLevelOverBasic )
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
        // if ( TopoDroidSetting.mLevelOverBasic )
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

      // case R.id.item_left:
      //   if ( TopoDroidSetting.mLevelOverBasic ) rotatePoint( -10 );
      //   break;
      // case R.id.item_right:
      //   if ( TopoDroidSetting.mLevelOverBasic ) rotatePoint( 10 );
      //   break;

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
    }
  } 
    

  public void closeDialog()
  {  
    dismiss();
  }
}
