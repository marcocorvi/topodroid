/* @file ItemPickerDialog.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing item picker dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyDialog;
// import com.topodroid.ui.ItemButton;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;
import com.topodroid.common.SymbolType;
import com.topodroid.common.PointScale;

import java.util.ArrayList;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.content.Context;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import android.widget.GridView;

class ItemPickerDialog extends MyDialog
                       implements View.OnClickListener
                       , IItemPicker
                       // , View.OnLongClickListener
                       // , AdapterView.OnItemClickListener
{
  private static float DIMXP = 1.6f; // 1.8f;
  private static float DIMXL = 2.2f;
  private static float DIMYL = 1.9f;
  // private static int DIMPD = 2;
  private static int DIMMX = 5;
  private static int DIMMY = 2;

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

  // private DrawingWindow mParent;
  private WeakReference<ItemDrawer> mParent;

  //* private ListView    mList = null;
  private GridView    mList = null;
  // private GridView    mGrid  = null;
  // private GridView    mGridL = null;
  // private GridView    mGridA = null;
  private ItemAdapter mPointAdapter = null;
  private ItemAdapter mLineAdapter  = null;
  private ItemAdapter mAreaAdapter  = null;
  private ItemAdapter mAdapter = null;

  // private ItemButton[] mRecent = null;

  // static int mLinePos;
  // static int mAreaPos;

 private SymbolPointLibrary mPointLib;
 private SymbolLineLibrary  mLineLib;
 private SymbolAreaLibrary  mAreaLib;

  /**
   * @param context   context
   * @param parent    DrawingWindow parent
   * @param type      drawing type
   */
  ItemPickerDialog( Context context, ItemDrawer parent, long type, int item_type  )
  {
    super( context, R.string.ItemPickerDialog );
    mParent  = new WeakReference<ItemDrawer>( parent );

    mPlotType = type;
    mItemType = item_type; // mParent.mSymbol;
 
    mPointLib = BrushManager.getPointLib();
    mLineLib  = BrushManager.getLineLib();
    mAreaLib  = BrushManager.getAreaLib();

    mScale         = parent.getPointScale();
    mSelectedPoint = parent.mCurrentPoint;
    mSelectedLine  = parent.mCurrentLine;
    mSelectedArea  = parent.mCurrentArea;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    // requestWindowFeature(Window.FEATURE_NO_TITLE);

    DIMXP = Float.parseFloat( mContext.getResources().getString( R.string.dimxp ) );
    DIMXL = Float.parseFloat( mContext.getResources().getString( R.string.dimxl ) );
    DIMYL = Float.parseFloat( mContext.getResources().getString( R.string.dimyl ) );
    // DIMPD = Integer.parseInt( mContext.getResources().getString( R.string.dimpd ) );
    DIMMX = Integer.parseInt( mContext.getResources().getString( R.string.dimmx ) );
    DIMMY = Integer.parseInt( mContext.getResources().getString( R.string.dimmy ) );

    if ( mPointLib == null || mLineLib == null || mAreaLib == null ) {
      dismiss();
      TDToast.makeWarn( "Symbols not ready" );
      return;
    }
    // TDLog.v("Dims " + DIMXP + " " + DIMXL + " " + DIMYL + " " + DIMMX + " " + DIMMY );

    // createAdapters( ( TDSetting.mPickerType == TDSetting.PICKER_LIST /* || TDSetting.mPickerType == TDSetting.PICKER_RECENT */ ) );
    createAdapters( true );
    
    // if ( TDSetting.mPickerType == TDSetting.PICKER_GRID || TDSetting.mPickerType == TDSetting.PICKER_GRID_3 ) {
    //   setContentView(R.layout.item_picker2_dialog);
    //   getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ); // NullPointerException

    //   mGrid  = (GridView) findViewById(R.id.item_grid);
    //   mGridL = (GridView) findViewById(R.id.item_grid_line);
    //   mGridA = (GridView) findViewById(R.id.item_grid_area);
    //   if ( TDSetting.mPickerType == TDSetting.PICKER_GRID ) {
    //     mGridL.setVisibility( View.GONE );
    //     mGridA.setVisibility( View.GONE );
    //   } else {
    //     LinearLayout.LayoutParams params;
    //     params = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, 0 );
    //     params.weight = 10 + mPointAdapter.size();
    //     mGrid.setLayoutParams(  params );
    //     params = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, 0 );
    //     params.weight = 10 + mLineAdapter.size();
    //     mGridL.setLayoutParams( params );
    //     params = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, 0 );
    //     params.weight = 10 + mAreaAdapter.size();
    //     mGridA.setLayoutParams( params );
    //   }
    //   // mGrid.setOnItemClickListener( this );
    //   // mGrid.setDividerHeight( 2 );
    //   mList = null;
    // } else { // PICKER_LIST || PICKER_RECENT
      setContentView(R.layout.item_picker_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ); // NullPointerException

      // * mList = (ListView) findViewById(R.id.item_list);
      mList = (GridView) findViewById(R.id.item_list);
      // mList.setOnItemClickListener( this );
      // * mList.setDividerHeight( 1 );
      // mGrid  = null;
      // mGridL = null;
      // mGridA = null;

      // LinearLayout recent_layout = (LinearLayout) findViewById( R.id.layout2 );
      // mRecent = new ItemButton[ TDSetting.mRecentNr ];
      // int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
      // int lh = LinearLayout.LayoutParams.WRAP_CONTENT;
      // LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(lh,lw);
      // lllp.setMargins(DIMMX, DIMMY, DIMMX, DIMMY);
      // for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
      //   mRecent[k] = new ItemButton( mContext );
      //   mRecent[k].setOnClickListener( this );
      //   // mRecent[k].setOnLongClickListener( this );
      //   recent_layout.addView( mRecent[k], lllp );
      // }
    // }
    
    mBTpoint = (Button) findViewById(R.id.item_point);
    mBTline  = (Button) findViewById(R.id.item_line );
    mBTarea  = (Button) findViewById(R.id.item_area );
    mBTsize  = (Button) findViewById(R.id.size);
    // mBTleft  = (Button) findViewById(R.id.item_left );
    // mBTright = (Button) findViewById(R.id.item_right );
    mSeekBar = (SeekBar) findViewById(R.id.seekbar );
    // mBTcancel  = (Button) findViewById(R.id.item_cancel );
    // mBTok    = (Button) findViewById(R.id.item_ok   );

    // if ( TDSetting.mPickerType == TDSetting.PICKER_GRID_3 ) {
    //   mBTpoint.setVisibility( View.GONE );
    //   mBTline.setVisibility( View.GONE );
    //   mBTarea.setVisibility( View.GONE );
    // } else {
      mBTpoint.setOnClickListener( this );
      mBTline.setOnClickListener( this );
      mBTarea.setOnClickListener( this );
    // }
    mBTsize.setOnClickListener( this );
    // mBTsize.setOnLongClickListener( new View.OnLongClickListener() {
    //   @Override
    //   public boolean onLongClick( View v ) {
    //     if ( mScale < PointScale.SCALE_XL ) {
    //       ++mScale;
    //       mParent.get().setPointScale( mScale );
    //       setTheTitle();
    //     }
    //     return true;
    //   }
    // } );

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

    // TDLog.v( "ItemPickerDialog ... createAdapters" );
    updateList();
    // updateRecentButtons( mItemType );

    setTypeAndItem( mItemType, getAdapterPosition() );
    // setTheTitle();
  }

  private void setSeekBarProgress()
  {
    boolean orientable = false;
    ItemSymbol item = null;
    if ( mItemType == SymbolType.POINT &&  mPointAdapter != null ) {
      item = mPointAdapter.get( mPointAdapter.getSelectedPos() );
    } else if ( mItemType == SymbolType.AREA &&  mAreaAdapter != null ) {
      item = mAreaAdapter.get( mAreaAdapter.getSelectedPos() );
    }
    if ( item != null ) {
      SymbolInterface symbol = item.mSymbol;
      if ( symbol != null && symbol.isOrientable() ) {
        mSeekBar.setProgress( (180+symbol.getAngle())%360 );
        orientable = true;
      }
    }
    mSeekBar.setEnabled( orientable );
  }

  private void setItemAngle( int angle )
  {
    ItemSymbol item = null;
    Symbol[] symbols = null;
    if ( mItemType == SymbolType.POINT && mPointAdapter != null ) {
      int pos = mPointAdapter.getSelectedPos();
      item = mPointAdapter.get( pos );
      symbols = ItemDrawer.mRecentPoint;
      mPointAdapter.setItemOrientation( pos, angle );
      // setPointOrientation( pos, angle );
    } else if ( mItemType == SymbolType.AREA && mAreaAdapter != null ) {
      int pos = mAreaAdapter.getSelectedPos();
      item = mAreaAdapter.get( pos );
      symbols = ItemDrawer.mRecentArea;
      mAreaAdapter.setItemOrientation( pos, angle );
      // setAreaOrientation( pos, angle );
    }
    // if ( item != null ) {
    //   // item.setAngle( angle );
    //   if ( symbols != null ) {
    //     for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
    //       Symbol p = symbols[k];
    //       if ( p == null ) break;
    //       if ( p == item.mSymbol ) {
    //         if ( mRecent != null ) mRecent[k].resetPaintPath( p.getPaint(), p.getPath(), DIMXP, DIMXP );
    //         break;
    //       }
    //     }
    //   }
    // }
  }

  private int getAdapterPosition()
  {
    int index = 0;
    switch ( mItemType ) {
      case SymbolType.POINT:
        if ( mPointAdapter != null ) index = mPointAdapter.getSelectedPos();
        break;
      case SymbolType.LINE:
        if ( mLineAdapter != null ) index = mLineAdapter.getSelectedPos();
        break;
      case SymbolType.AREA:
        if ( mAreaAdapter != null ) index = mAreaAdapter.getSelectedPos();
        break;
    }
    return index;
  }

  // private void setRecentButtons( Symbol[] symbols, float sx, float sy )
  // {
  //   if ( mRecent == null ) return;
  //   for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
  //     Symbol p = symbols[k];
  //     if ( p == null ) {
  //       mRecent[k].setVisibility( View.INVISIBLE );
  //     } else {
  //       mRecent[k].resetPaintPath( p.getPaint(), p.getPath(), sx, sy );
  //       mRecent[k].setVisibility( View.VISIBLE );
  //     }
  //   }
  // }

  // private void updateRecentButtons( int item_type ) 
  // {
  //   if ( TDSetting.mPickerType != TDSetting.PICKER_GRID_3 ) {
  //     // float sx=1.0f, sy=1.0f;
  //     if ( item_type == SymbolType.POINT ) {
  //       mBTsize.setVisibility( View.VISIBLE );
  //       setRecentButtons( ItemDrawer.mRecentPoint, DIMXP, DIMXP );
  //     } else if ( item_type == SymbolType.LINE ) {
  //       mBTsize.setVisibility( View.GONE );
  //       setRecentButtons( ItemDrawer.mRecentLine, DIMXL, DIMYL );
  //     } else if ( item_type == SymbolType.AREA ) {
  //       mBTsize.setVisibility( View.GONE );
  //       setRecentButtons( ItemDrawer.mRecentArea, DIMXL, DIMYL );
  //     }
  //   // } else {
  //     // nothing
  //   }
  // }
        
  private void createAdapters( boolean use_text )
  {
    // if ( TDLevel.overBasic ) 
    {
      mPointAdapter = new ItemAdapter( mContext, this, SymbolType.POINT, R.layout.item, new ArrayList< ItemSymbol >() );
      int np = mPointLib.size();
      for ( int i=0; i<np; ++i ) {
        SymbolPoint p = (SymbolPoint)mPointLib.getSymbolByIndex( i );
        if ( p.isEnabled() && ( /* TDLevel.overAdvanced || */ ! p.isThName( SymbolLibrary.SECTION ) ) ) { // FIXME_SECTION_POINT 
          mPointAdapter.add( new ItemSymbol( mContext, this, SymbolType.POINT, i, p, use_text ) );
        }
      }
    }

    mLineAdapter  = new ItemAdapter( mContext, this, SymbolType.LINE, R.layout.item, new ArrayList< ItemSymbol >() );
    int nl = mLineLib.size();
    for ( int j=0; j<nl; ++j ) {
      SymbolLine l = (SymbolLine)mLineLib.getSymbolByIndex( j );
      if ( l.isEnabled() ) {
        mLineAdapter.add( new ItemSymbol( mContext, this, SymbolType.LINE, j, l, use_text ) );
      }
    }

    // if ( TDLevel.overBasic )
    {
      mAreaAdapter  = new ItemAdapter( mContext, this, SymbolType.AREA, R.layout.item, new ArrayList< ItemSymbol >() );
      int na = mAreaLib.size();
      for ( int k=0; k<na; ++k ) {
        SymbolArea a = (SymbolArea)mAreaLib.getSymbolByIndex( k );
        if ( a.isEnabled() ) {
          mAreaAdapter.add( new ItemSymbol( mContext, this, SymbolType.AREA, k, a, use_text ) );
        }
      }
    }
    mPointAdapter.setSelectedItem( mSelectedPoint );
    mLineAdapter.setSelectedItem( mSelectedLine ); 
    mAreaAdapter.setSelectedItem( mSelectedArea );
  }

  private void updateList()
  {
    // TDLog.v( "ItemPickerDialog ... update List type " + mItemType );
    // if ( TDSetting.mPickerType != TDSetting.PICKER_GRID_3 ) {
      switch ( mItemType ) {
        case SymbolType.POINT:
          // if ( TDLevel.overBasic )
          {
            mAdapter = mPointAdapter;
            mBTpoint.getBackground().setColorFilter( TDColor.LIGHT_BLUE, PorterDuff.Mode.LIGHTEN );
            mBTline.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
            mBTarea.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
            mSeekBar.setVisibility( View.VISIBLE );
            setSeekBarProgress();
          }
          break;
        case SymbolType.LINE:
          mAdapter = mLineAdapter;
          mBTpoint.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
          mBTline.getBackground().setColorFilter( TDColor.LIGHT_BLUE, PorterDuff.Mode.LIGHTEN );
          mBTarea.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
          mSeekBar.setVisibility( View.INVISIBLE );
          break;
        case SymbolType.AREA:
          // if ( TDLevel.overBasic )
          {
            mAdapter = mAreaAdapter;
            mBTpoint.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
            mBTline.getBackground().setColorFilter( TDColor.LIGHT_GRAY, PorterDuff.Mode.DARKEN );
            mBTarea.getBackground().setColorFilter( TDColor.LIGHT_BLUE, PorterDuff.Mode.LIGHTEN );
            mSeekBar.setVisibility( View.VISIBLE );
          }
          break;
      }
      if ( mAdapter != null ) {
        if ( mList != null ) {
          mList.setAdapter( mAdapter );
          mList.invalidate();
        // } else if ( mGrid != null ) {
        //   mGrid.setAdapter( mAdapter );
        //   mGrid.invalidate();
        }
      }
    // } else {
    //   setShowSelected();
    //   mGrid.setAdapter( mPointAdapter );
    //   mGrid.invalidate();
    //   mGridL.setAdapter( mLineAdapter );
    //   mGridL.invalidate();
    //   mGridA.setAdapter( mAreaAdapter );
    //   mGridA.invalidate();
    // }
  }
 
  // used by triple grid GRID_3
  private void setShowSelected()
  {
      switch ( mItemType ) {
        case SymbolType.POINT:
          mPointAdapter.setShowSelected( true );
          mLineAdapter.setShowSelected( false );
          mAreaAdapter.setShowSelected( false );
        break;
        case SymbolType.LINE:
          mPointAdapter.setShowSelected( false );
          mLineAdapter.setShowSelected( true );
          mAreaAdapter.setShowSelected( false );
        break;
        case SymbolType.AREA:
          mPointAdapter.setShowSelected( false );
          mLineAdapter.setShowSelected( false );
          mAreaAdapter.setShowSelected( true );
        break;
      }
  }

  private void setTheTitle()
  {
    StringBuilder title = new StringBuilder();
    switch ( mItemType ) {
      case SymbolType.POINT: 
        title.append( "[" );
        title.append( PointScale.scaleToStringUC( mScale ) );
        title.append( "] " );
        title.append( mContext.getResources().getString( R.string.POINT ) );
        title.append( " " );
        title.append( BrushManager.getPointName( mSelectedPoint ) );
        break;
      case SymbolType.LINE: 
        title.append( mContext.getResources().getString( R.string.LINE ) );
        title.append( " " );
        title.append( BrushManager.getLineName( mSelectedLine ) );
        break;
      case SymbolType.AREA: 
        title.append( mContext.getResources().getString( R.string.AREA ) );
        title.append( " " );
        title.append( BrushManager.getAreaName( mSelectedArea ) );
        break;
    }
    // TDLog.v( "set title " + title.toString() );
    setTitle( title.toString() );
  }

  // pos 
  public void setTypeAndItem( int type, int index )
  {
    // TDLog.v( "set TypeAndItem type " + mItemType  + " item " + index );
    mItemType = type;
    ItemSymbol is;
    switch ( type ) {
      case SymbolType.POINT: 
        if ( mPointAdapter != null /* && TDLevel.overBasic */ ) {
          is = mPointAdapter.get( index );
          // TDLog.v( "set TypeAndItem type point pos " + index + " index " + is.mIndex );
          mSelectedPoint = is.mIndex;
          // mParent.get().pointSelected( is.mIndex, false ); // mPointAdapter.getSelectedItem() );
          setSeekBarProgress();
          mBTpoint.setTextColor( TDColor.SYMBOL_ON );
          // mBTline.setTextColor(  TDColor.SYMBOL_TAB );
          // mBTarea.setTextColor(  TDColor.SYMBOL_TAB );
        }
        break;
      case SymbolType.LINE: 
        if ( mLineAdapter != null ) {
          is = mLineAdapter.get( index );
          // TDLog.v( "set TypeAndItem type line pos " + index + " index " + is.mIndex + " " + is.mSymbol.getName() );
          if ( mPlotType != PlotType.PLOT_SECTION || ! BrushManager.isLineSection( is.mIndex ) ) {
            mSelectedLine = is.mIndex;
            // mParent.get().lineSelected( is.mIndex, false ); // mLineAdapter.getSelectedItem() );
          // } else {
            /* nothing */
          }
          mSeekBar.setEnabled( false );
          // mBTpoint.setTextColor( TDColor.SYMBOL_TAB );
          mBTline.setTextColor(  TDColor.SYMBOL_ON );
          // mBTarea.setTextColor(  TDColor.SYMBOL_TAB );
        }
        break;
      case SymbolType.AREA: 
        if ( mAreaAdapter != null /* && TDLevel.overBasic */ ) {
          // mAreaPos = index;
          is = mAreaAdapter.get( index );
          // TDLog.v( "set TypeAndItem type area pos " + index + " index " + is.mIndex );
          mSelectedArea = is.mIndex;
          // mParent.get().areaSelected( is.mIndex, false ); // mAreaAdapter.getSelectedItem() );
          setSeekBarProgress();
          // mBTpoint.setTextColor( TDColor.SYMBOL_TAB );
          // mBTline.setTextColor(  TDColor.SYMBOL_TAB );
          mBTarea.setTextColor(  TDColor.SYMBOL_ON );
        }
        break;
    }
    // cancel();
    setShowSelected();
    setTheTitle();
  }

  // this is called tapping the tab-buttons on the top
  private void setTypeFromCurrent( )
  {
    switch ( mItemType ) {
      case SymbolType.POINT: 
        // if ( TDLevel.overBasic ) 
        {
          // mParent.get().pointSelected( mSelectedPoint, false );
          // mSeekBar.setEnabled( BrushManager.mPointLib.isPointOrientable( mSelectedPoint ) );
          if ( mPointAdapter != null ) setTypeAndItem( mItemType, mPointAdapter.getSelectedPos() );
          mBTpoint.setTextColor( TDColor.SYMBOL_ON );
          mBTline.setTextColor(  TDColor.SYMBOL_TAB );
          mBTarea.setTextColor(  TDColor.SYMBOL_TAB );
        }
        break;
      case SymbolType.LINE: 
        // if ( ! PlotType.isAnySection( mPlotType ) ) {
        //   mParent.get().lineSelected( mSelectedLine, false );
        // } else {
        // }
        if ( mLineAdapter != null ) setTypeAndItem( mItemType, mLineAdapter.getSelectedPos() );
        mBTpoint.setTextColor( TDColor.SYMBOL_TAB );
        mBTline.setTextColor(  TDColor.SYMBOL_ON );
        mBTarea.setTextColor(  TDColor.SYMBOL_TAB );
        break;
      case SymbolType.AREA: 
        // if ( TDLevel.overBasic ) 
        {
          // mParent.get().areaSelected( mSelectedArea, false );
          if ( mAreaAdapter != null ) setTypeAndItem( mItemType, mAreaAdapter.getSelectedPos() );
          mBTpoint.setTextColor( TDColor.SYMBOL_TAB );
          mBTline.setTextColor(  TDColor.SYMBOL_TAB );
          mBTarea.setTextColor(  TDColor.SYMBOL_ON );
        }
        break;
    }
    // setTypeAndItem( getAdapterPosition() );
    setTheTitle();
  }

  // void rotatePoint( int angle )
  // {
  //   if ( mPointAdapter == null ) return;
  //   if ( TDLevel.overBasic && mItemType == SymbolType.POINT ) {
  //     // TDLog.v( "rotate point " + mSelectedPoint );
  //     mPointAdapter.rotatePoint( mSelectedPoint, angle );
  //   }
  // }

  private void setPointOrientation( int pos, int angle )
  {
    if ( mPointAdapter == null ) return;
    if ( /* TDLevel.overBasic && */ mItemType == SymbolType.POINT ) {
      mPointAdapter.setItemOrientation( pos, angle );
      // ItemSymbol item = mPointAdapter.getSelectedItem();
      // if ( item != null ) {
      //   item.setAngle( angle );
      //   // angle -= (int) item.mSymbol.getAngle();
      //   // mPointAdapter.rotateItem( pos, angle );
      // }
    }
  }

  private void setAreaOrientation( int pos, int angle )
  {
    if ( mAreaAdapter == null ) return;
    if ( /* TDLevel.overBasic && */ mItemType == SymbolType.AREA ) {
      mAreaAdapter.setItemOrientation( pos, angle );
      // ItemSymbol item = mAreaAdapter.getSelectedItem();
      // if ( item != null ) {
      //   item.setAngle( angle );
      //   // angle -= (int) item.mSymbol.getAngle();
      //   // mAreaAdapter.rotateItem( pos, angle );
      // }
    }
  }

  @Override
  public void onBackPressed ()
  {
    // TDLog.v( "onBackPressed type " + mItemType );
    itemSelected();
    cancel();
  }

  private void itemSelected()
  {
    switch ( mItemType ) {
      case SymbolType.POINT: 
        // if ( TDLevel.overBasic )
        if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
          // TDLog.v( "item picker selected point " + mSelectedPoint );
          mParent.get().pointSelected( mSelectedPoint, true ); // true = update tools toolbar
        }
        break;
      case SymbolType.LINE: 
        if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
          // TDLog.v( "item picker selected line " + mSelectedLine );
          mParent.get().lineSelected( mSelectedLine, true ); 
	}
        break;
      case SymbolType.AREA: 
        // if ( TDLevel.overBasic )
        if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
          // TDLog.v( "item picker selected area " + mSelectedArea );
          mParent.get().areaSelected( mSelectedArea, true );
        }
        break;
    }
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.v( "ItemPicker onClick()" );
    switch (view.getId()) {
      case R.id.item_point:
        // if ( TDLevel.overBasic )
        {
          if ( mItemType != SymbolType.POINT ) {
            mItemType = SymbolType.POINT;
            updateList();
            // updateRecentButtons( mItemType );
            setTypeFromCurrent( );
          }
        }
        break;
      case R.id.item_line:
        if ( mItemType != SymbolType.LINE ) {
          mItemType = SymbolType.LINE;
          updateList();
          // updateRecentButtons( mItemType );
          setTypeFromCurrent( );
        }
        break;
      case R.id.item_area:
        // if ( TDLevel.overBasic )
        {
          if ( mItemType != SymbolType.AREA ) {
            mItemType = SymbolType.AREA;
            updateList();
            // updateRecentButtons( mItemType );
            setTypeFromCurrent( );
          }
        }
        break;
      case R.id.size:
        if ( mScale < PointScale.SCALE_XL ) {
          ++ mScale;
        } else {
          mScale = PointScale.SCALE_XS;
        }
        if ( mParent.get() != null && ! mParent.get().isFinishing() ) mParent.get().setPointScale( mScale );
        setTheTitle();
        break;
    }

    // if ( mRecent != null ) { // this select the symbol and closes the dialog
    //   if ( view instanceof ItemButton) {
    //     ItemButton iv = (ItemButton)view;
    //     // if ( iv != null ) { // always true
    //       for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
    //         if ( iv == mRecent[k] ) {
    //           setRecent( k );
    //           closeDialog();
    //           return;
    //         }
    //       }
    //     // }
    //   } else {
    //     TDLog.Error("View is not ItemButton"); // it is ok
    //   }
    // }
     
    // dismiss();
  }
  
  // // This onLongClick moves the long-clicked symbol to the first place, however this is not necessary
  // public boolean onLongClick( View v )
  // {
  //   ItemButton ib = (ItemButton)v;
  //   if ( ib != null ) {
  //     for ( int k=0; k<TDSetting.mRecentNr; ++k ) {
  //       if ( mRecent[k] == ib ) {
  //         // TDLog.v( "long click view " + k + " " + ItemDrawer.mRecentPoint[k].getThName() );
  //         if ( k > 0 ) {
  //           if ( mItemType == SymbolType.POINT ) {
  //             Symbol pt = ItemDrawer.mRecentPoint[k];
  //             for ( ; k > 0; --k ) ItemDrawer.mRecentPoint[k] = ItemDrawer.mRecentPoint[k-1];
  //             ItemDrawer.mRecentPoint[0] = pt;
  //           } else if ( mItemType == SymbolType.LINE ) {
  //             Symbol ln = ItemDrawer.mRecentLine[k];
  //             for ( ; k > 0; --k ) ItemDrawer.mRecentLine[k] = ItemDrawer.mRecentLine[k-1];
  //             ItemDrawer.mRecentLine[0] = ln;
  //           } else if ( mItemType == SymbolType.AREA ) {
  //             Symbol ar = ItemDrawer.mRecentArea[k];
  //             for ( ; k > 0; --k ) ItemDrawer.mRecentArea[k] = ItemDrawer.mRecentArea[k-1];
  //             ItemDrawer.mRecentArea[0] = ar;
  //           }
  //           // setRecentSymbol( sym );
  //           updateRecentButtons( mItemType );
  //         }
  //         break;
  //       }
  //     }
  //   } else {
  //     TDLog.Error("long click null view");
  //   }
  //   return true;
  // }

  // @Override
  // public void onItemClick( AdapterView adapter, View view, int pos, long id )
  // {
  //    TDLog.v( "ItemPicker onItemCLick()" );
  //    if ( mAdapter != null ) mAdapter.doClick( view );
  // }

  private void setRecent( int k )
  {
    if ( mItemType == SymbolType.POINT ) {
      mSelectedPoint = setRecentSymbol( ItemDrawer.mRecentPoint[k] );
    } else if ( mItemType == SymbolType.LINE ) {
      mSelectedLine  = setRecentSymbol( ItemDrawer.mRecentLine[k] );
    } else if ( mItemType == SymbolType.AREA ) {
      mSelectedArea  = setRecentSymbol( ItemDrawer.mRecentArea[k] );
    }
  }

  private int setRecentSymbol( Symbol p )
  {
    int index = -1;
    if ( p != null ) {
      if ( mAdapter != null ) {
        index = mAdapter.setSelectedItem( p ); // selected symbol index
      }
      setSeekBarProgress();
    }
    return index;
  } 

  public void closeDialog()
  {  
    itemSelected();
    dismiss();
  }
}
