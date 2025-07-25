/* @file DBlockAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for survey data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDString;
import com.topodroid.prefs.TDSetting;

import android.annotation.SuppressLint;
import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
// import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.AdapterView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;

// import androidx.annotation.RecentlyNonNull;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Locale;

class DBlockAdapter extends ArrayAdapter< DBlock >
                    implements OnLongClickListener
                    // , OnClickListener
{
  private static final int START = 0;
  // private Context mContext;
  private final ShotWindow mParent;
  // ArrayList< DBlock > mItems;
  private ArrayList< DBlock > mSelect;
  boolean show_ids;  //!< whether to show data ids
  private final LayoutInflater mLayoutInflater;
  private final boolean diving;
  private SearchResult mSearch;

  private int lastPosAdd    = -1;
  private int lastPosRemove = -1;
  private DBlock lastMultiselectedBlock = null;

  /** cstr
   * @param ctx     context
   * @param parent  parent window
   * @param id      ???
   * @param items   array list of data-blocks
   */
  DBlockAdapter( Context ctx, ShotWindow parent, int id, ArrayList< DBlock > items )
  {
    super( ctx, id );
    TDLog.v( "DBlock Adapter cstr");
    // mContext = ctx;
    mParent  = parent;
    // mItems   = items;
    add( new DBlock() );
    addAll( items );

    mSelect  = new ArrayList<>();
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    diving = (TDInstance.datamode == SurveyInfo.DATAMODE_DIVING);
    // TDLog.v( "DBlock Adapter, diving " + diving );
    mSearch = new SearchResult();

    // lastPosAdd = -1;
    // lastPosRemove = -1;
  }

  /** search the data-blocks with a specified station 
   * @param name   name of the station to search
   * @param splays whether to include splays in the search
   * @note the private search-result is filled with the found blocks
   */
  void searchStation( String name, boolean splays )
  {
    mSearch.reset( name, false );
    if ( TDString.isNullOrEmpty( name ) ) return; // null;
    int size = getCount();
    for ( int pos=START; pos < size; ++pos ) {
      DBlock b = (DBlock)( getItem( pos ) );
      if ( b.isSplay() && splays && name.equals( b.mFrom ) ) {
        mSearch.add( pos );
        b.setBackgroundColor( TDColor.SEARCH );
      } else if ( b.isLeg() && ( name.equals( b.mFrom ) || name.equals( b.mTo ) ) ) {
        mSearch.add( pos );
        b.setBackgroundColor( TDColor.SEARCH );
      }
    }
    // TDLog.v( " search station " + name + " results " + mSearch.size() );
    // return (mSearch.size() > 0);
  }

  /** search the leg-blocks with given stations
   * @param st1  first station
   * @param st2  second station
   * @note the private search-result is filled with the found blocks
   */
  void searchLeg( String st1, String st2 )
  {
    mSearch.reset( st1 + " " + st2, true );
    int size = getCount();
    for ( int pos=START; pos < size; ++pos ) {
      DBlock b = (DBlock)( getItem( pos ) );
      if ( ! b.isLeg() ) continue;
      if ( ( b.mFrom.equals( st1 ) && b.mTo.equals( st2 ) ) || ( b.mFrom.equals( st2 ) && b.mTo.equals( st1 ) ) ) {
        mSearch.add( pos );
        b.setBackgroundColor( TDColor.SEARCH );
      }
    } 
    // TDLog.v( " search shot " + flag + " results " + mSearch.size() );
    // return (mSearch.size() > 0);
  }

  /** search the data-blocks with a given flag
   * @param flag   flag to search
   * @note the private search-result is filled with the found blocks
   */
  void searchShot( long flag )
  {
    mSearch.reset( null, false );
    int size = getCount();
    if ( flag == DBlock.FLAG_NO_EXTEND ) {
      for ( int pos=START; pos < size; ++pos ) {
        DBlock b = (DBlock)( getItem( pos ) );
        if ( b.getIntExtend( ) > 1 && b.isLeg() ) {
          mSearch.add( pos );
          b.setBackgroundColor( TDColor.SEARCH );
        }
      }
    } else if ( flag == DBlock.FLAG_REVERSE_SPLAY ) {
      for ( int pos=START; pos < size; ++pos ) {
        DBlock b = (DBlock)( getItem( pos ) );
        if ( b.isReverseSplay() ) {
          mSearch.add( pos );
          b.setBackgroundColor( TDColor.SEARCH );
        }
      }
    } else { // real flag
      for ( int pos=START; pos < size; ++pos ) {
        DBlock b = (DBlock)( getItem( pos ) );
        if ( b.hasFlag( flag ) ) {
          mSearch.add( pos );
          b.setBackgroundColor( TDColor.SEARCH );
        }
      }
    }
    // TDLog.v( " search shot " + flag + " results " + mSearch.size() );
    // return (mSearch.size() > 0);
  }

  /** @return true if there are blocks in the multiselection
   */
  boolean isMultiSelect() { return ( mSelect.size() > 0 ); }

  /** add a block to the multiselection
   * @param pos       block position
   * @param long_tap  whether it was a long-tap
   * @return the number of blocks in the multiselection
   */
  boolean multiSelect( int pos, boolean long_tap ) 
  {
    // TDLog.v("Multiselect " + pos + " of " + getCount() );
    if ( pos >= START && pos < getCount() ) {
      DBlock b = (DBlock)( getItem( pos ) );
      if ( b != null ) {
        if ( mSelect.size() > 0 ) {
          int newPos = pos;
          if ( mSelect.remove( b ) ) {
            b.mMultiSelected = false;
            // TDLog.v("last MS block color transp.: id " + b.mId );
            b.setBackgroundColor( TDColor.TRANSPARENT );
            // TDLog.v("multiselect remove " + b.mFrom + " " + b.mTo + " " + mSelect.size() );
            if ( long_tap && ( lastPosRemove > -1 ) ) { // continue to de-select blocks all way to the last de-selected item
              if (pos > lastPosRemove) {
                while ( --pos >= lastPosRemove ) {
                  removeItem(pos);
                }
              } else {
                while ( ++pos <= lastPosRemove ) {
                  removeItem(pos);
                }
              }
            }
            lastPosAdd = -1;
            lastPosRemove = newPos;
          } else {
            // 2024-09-29 reversed the order items are added to the multiselection
            // mSelect.add( b );
            // b.mMultiSelected = true;
            // b.setBackgroundColor( TDColor.GRID );
            // // TDLog.v("multiselect add " + b.mFrom + " " + b.mTo + " " + mSelect.size() );
            // if ( long_tap  && ( lastPosAdd > -1 ) ) { // continue to select block all the way to the last selected item
            //   if ( pos > lastPosAdd ) {
            //     while ( --pos > lastPosAdd ) {
            //       addItem(pos);
            //     }
            //   } else {
            //     while ( ++pos < lastPosAdd ) {
            //       addItem(pos);
            //     }
            //   }
            // }
            // lastPosAdd = newPos;
            // lastPosRemove = -1;
            // TDLog.v("multiselect add " + b.mFrom + " " + b.mTo + " " + mSelect.size() );
            if ( long_tap  && ( lastPosAdd > -1 ) ) { // continue to select block all the way to the last selected item
              if ( pos > lastPosAdd ) {
                while ( ++lastPosAdd < pos ) {
                  addItem( lastPosAdd );
                }
              } else {
                while ( --lastPosAdd > pos) {
                  addItem( lastPosAdd );
                }
              }
            }
            mSelect.add( b );
            b.mMultiSelected = true;
            b.setBackgroundColor( TDColor.GRID );
            lastPosAdd = newPos;
            lastPosRemove = -1;
          }
        } else {
          mSelect.add( b );
          b.mMultiSelected = true;
          b.setBackgroundColor( TDColor.GRID );
          // TDLog.v("multiselect start " + b.mFrom + " " + b.mTo + " " + mSelect.size() );
          lastPosAdd = pos;
          lastPosRemove = -1;
        }
        lastMultiselectedBlock = b;
      }
    // } else {
      // TDLog.v( "adapter multiselect. null blk. size " + mSelect.size() );
    }
   
    if ( mSelect.size() > 0 ) {
      return true;
    } else {
      clearLastMultiselected();
      return false;
    }
  }

  /** add an offset to the distance of the shots in multiselection
   * @param data_helper    database helper
   * @param offset         offset to add: the shot length becomes length + offset
   */
  void addOffset( DataHelper data_helper, float offset )
  {
    boolean tamper = Math.abs( offset ) > 0.1;
    for ( DBlock blk : mSelect ) {
      if ( tamper && ! blk.isManual() ) {
        if ( ! blk.isTampered() ) {
          blk.setTampered();
          data_helper.saveShotDistanceBearingClino( TDInstance.sid, blk );
        }
      }
      blk.mLength += offset;
      data_helper.updateShotDistance( blk.mId, TDInstance.sid, blk.mLength );
      updateBlockView( blk.mId );
    }
  }

  /** clear multiselected status
   */
  private void clearLastMultiselected()
  {
    lastPosAdd    = -1;
    lastPosRemove = -1;
    lastMultiselectedBlock = null;
  }

  /** remove a block from the multiselection set
   * @param pos   block position
   */
  private void removeItem(int pos)
  {
    DBlock b = (DBlock)( getItem( pos ) );
    if ( ! b.mMultiSelected ) return;
    mSelect.remove( b );
    b.mMultiSelected = false;
    b.setBackgroundColor( TDColor.TRANSPARENT );
  }

  /** add a block to the multiselection set
   * @param pos   block position
   */
  private void addItem(int pos)
  {
    DBlock b = (DBlock) getItem(pos);
    if (b.mMultiSelected) return;
    mSelect.add(b);
    b.mMultiSelected = true;
    b.setBackgroundColor(TDColor.GRID);
  }

  /** @return the number of items on selection
   */
  int getMultiSelectSize() { return mSelect.size(); }

  /** @return the list of the items on selection
   */
  List<DBlock> getMultiSelect() { return mSelect; }

  private boolean isShotMultiSelect( DBlock blk )
  {
    ListIterator< DBlock > it = mSelect.listIterator();
    while ( it.hasNext() ) {
      if ( blk == (DBlock)it.next() ) return true;
    }
    return false;
  }

  /** @return true if the multiselection contains the tail of the shots
   */
  boolean isMultiSelectTail()
  {
    int size = getCount();
    int pos = size - mSelect.size();
    if ( pos < 0 ) pos = 0; // extra safety
    for ( ; pos < size; ++pos ) {
      if ( ! isShotMultiSelect( (DBlock)( getItem( pos ) ) ) ) return false;
    }
    return true;
  } 

  /** clear the multiselection
   */
  void clearMultiSelect() 
  { 
    TDLog.v("clear multiselect");
    DBlock blk = lastMultiselectedBlock;
    clearLastMultiselected();
    for ( DBlock b : mSelect ) {
      b.mMultiSelected = false;
      b.setBackgroundColor( TDColor.TRANSPARENT );
    }
    mSelect.clear();
    if ( blk != null ) {
      // TDLog.v("Last MS block " + blk.mId );
      // blk.setBackgroundColor( TDColor.TRANSPARENT );
      View v = blk.getView();
      if ( v != null ) {
        TextView tvFrom = (TextView) v.findViewById( R.id.from );
        TextView tvTo   = (TextView) v.findViewById( R.id.to );
        int col = blk.getColorByType( );
        tvFrom.setTextColor( col );
        tvTo.setTextColor( col );
      }
    }
  }

  /** remove all items from the list, and reset the state variables
   */
  @Override
  public void clear()
  {
    super.clear();
    // TDLog.v("DBlockAdapter :: CLEAR");
    clearLastMultiselected();
    mSelect.clear();
    mSearch.reset( null, false );
  }

  /** clear the search-result
   */
  void clearSearch()
  { 
    for ( Integer pos : mSearch.getPositions() ) {
      try { 
        DBlock b = (DBlock)( getItem( pos ) );
        b.setBackgroundColor( TDColor.TRANSPARENT );
      } catch ( IndexOutOfBoundsException e ) { /* nothing i can do */ }
    }
    mSearch.clearSearch(); 
  }

  /** @return the position of the next result in the search
   */
  int  nextSearchPosition() { return mSearch.nextPos(); }

  /** @return the name of the current result in the search
   */
  String getSearchName()    { return mSearch.getName(); }

  /** @return true if the search result is about legs (station pairs)
   */
  boolean isSearchPair() { return mSearch.isSearchPair(); }

  /** this is not efficient because it scans the list of shots
   *  skips oven non-mail_leg, still it would be better to keep a tree of 
   *  station names
   */
  boolean hasStation( String name ) 
  {
    if ( name == null ) return false;
    int size = getCount();
    for ( int pos=START; pos < size; ++pos ) {
      DBlock b = (DBlock)( getItem( pos ) );
      if ( ! b.isLeg() ) continue;
      if ( name.equals( b.mFrom ) || name.equals( b.mTo ) ) return true;
    }
    return false;
  }

  /** get all the splays around a splay-block (from the prev leg to the next leg)
   * @param id    id of the given splay-block
   * @param name  expected splay station
   * @note the list is interrupted when there is a splay with station different from the given "name"
   */
  ArrayList< DBlock > getSplaysAtId( long id, String name )
  {
    ArrayList< DBlock > ret = new ArrayList<>();
    int size = getCount();
    for ( int k = START; k < size; ++k ) {
      DBlock b = (DBlock)( getItem( k ) );
      if ( b.mId == id ) {
        int k1 = k-1;
        if ( ! TDSetting.mSplayOnlyForward ) {
          for ( ; k1 >= START; --k1 ) {
            DBlock b1 = (DBlock)( getItem( k1 ) );
            if ( ! b1.isSplay() || ! name.equals( b1.mFrom ) ) break;
          }
        }
        int k2 = k;
        for ( ; k2 < size; ++k2 ) {
          DBlock b2 = (DBlock)( getItem( k2 ) );
          if ( ! b2.isSplay() || ! name.equals( b2.mFrom ) ) break;
        }
        for ( ++k1; k1<k2; ++k1 ) ret.add( (DBlock)( getItem( k1 ) ) );
        break;
      }
    }
    return ret;
  }

  /** add a data-block to the adapter
   * @param b  block to add
   *
   * @note called only by ShotWindow updateDBlockList( blk )
   * this method changes the ArrayList of DistoxDBlock's
   */
  boolean addDataBlock( DBlock b ) 
  {
    if ( ! b.isScan() && hasBlock( b.mId ) ) return false;
    add( b );
    // notifyDataSetChanged();
    return true;
  }

  /** revise the data-blocks with the given list of photos
   * @param photos   list of photos
   * @note called by ShotWindow::updateShotlist
   */  
  void reviseBlockWithPhotos( List< PhotoInfo > photos )
  {
    int size = getCount();
    for ( int pos=START; pos < size; ++pos ) {
      DBlock b = (DBlock)( getItem( pos ) );
      b.mWithPhoto = false; 
      for ( PhotoInfo p : photos ) { // mark block with p.shotid
        if ( b.mId == p.getItemId() ) { 
          b.mWithPhoto = true;
          break;
        }
      }
    }
  }

  /** @return true if the block was already in the list
   */
  private boolean hasBlock( long id )
  {
    // TDLog.v( "Data Adapter items size " + getCount() + " block id " + id );
    if ( id < START ) return false;
    int size = getCount();
    if ( size == 0 ) return false;
    DBlock last_blk = (DBlock)( getItem( size-1 ) );
    // TDLog.v( "Data Adapter ID " + id + " last_blk " + last_blk.mId + " scan " + last_blk.isScan() );
    if ( last_blk.isScan() ) return false;
    return ( id <= last_blk.mId )? true : false;
    // for ( DBlock b : mItems ) if ( b.mId == id ) return true;
    // return false;
  }

  /** update the name, ie, from-to stations, of a block
   * @param id    block ID
   * @param from  FROM station
   * @param to    TO station
   */
  void updateBlockName( long id, String from, String to ) 
  {
    int size = getCount();
    for ( int pos=START; pos < size; ++pos ) {
      DBlock b = (DBlock)( getItem( pos ) );
      if ( b.mId == id ) {
        b.mFrom = from;
        b.mTo = to;
        break;
      }
    }
  }

  /** @return a copy of the list of the items in the adapter
   */
  List< DBlock > getItems() 
  {
    ArrayList<DBlock> ret = new ArrayList<>();
    int size = getCount();
    for ( int k=START; k<size; ++k ) ret.add( (DBlock) getItem(k) );
    return ret;
  }

  /** @return the list of blocks to assign station names
   *    the list contains on the final shots beginning with the n-th last leg-shot
   * @param nth   n-th last leg
   * @note called only by ShotWindow.updateDBlockList with nth 1 or 2
   */
  List< DBlock > getItemsForAssign( int nth )
  {
    List< DBlock > ret = new ArrayList<>();
    int size = getCount();
    if ( size > 0 ) {
      int k = size-1;
      for ( ; k >= START; --k ) { // scan from last backward until a leg is found
        DBlock b = (DBlock)( getItem( k ) );
        if ( b.mFrom.length() > 0 && b.mTo.length() > 0 ) {
          -- nth;
          if ( nth <= 0 ) break;
        }
      }
      if ( k < START ) k = START;
      for ( ; k < size; ++k ) { // insert into return from the leg onward
        ret.add( (DBlock)( getItem(k) ) );
      }
    }
    return ret; // REPLACED 20191002
    // return mItems.subList( k, size );
  }

  /** @return the block at the given position
   * @param pos   block position
   */
  public DBlock get( int pos ) 
  { 
    return ( pos < START || pos >= getCount() )? null : (DBlock)( getItem( pos ) );
  }

  // public DBlock getBlockById( long id ) 
  // {
  //   for ( DBlock b : mItems ) if ( b.mId == id ) return b;
  //   return null;
  // }

  private class ViewHolder implements OnClickListener
                           , OnLongClickListener
                           , TextView.OnEditorActionListener
  { 
    int      pos;
    boolean  editing;
    // long     blkId; // DistoX-EDIT
    TextView tvId;
    EditText tvFrom;
    EditText tvTo;
    TextView tvLength;
    DBlock   mBlock;   // used to make sure blocks do not hold ref to a view, that does not belong to them REVISE_RECENT

    ViewHolder( TextView id, EditText from, EditText to, TextView len )
    {
      pos       = 0;
      editing   = false;
      // blkId     = -1; // DistoX-EDIT
      tvId      = id;
      tvFrom    = from;
      tvTo      = to;
      tvLength  = len;
      tvLength.setMinWidth( (int)(TopoDroidApp.mDisplayWidth) - tvFrom.getMinWidth() - tvTo.getMinWidth() );
      mBlock    = null; // REVISE_RECENT
    }

    // OnClickListener on_click = new OnClickListener()
    // {
      @Override
      public void onClick( View v )
      {
        if ( pos < START ) return;
        // TDLog.v( "onClick " + v.getId() );
        if ( (TextView)v == tvLength ) {
          mParent.itemClick( v, pos );
        } else {
          if ( editing ) {
            mParent.recomputeItems( ((TextView)v).getText().toString(), pos );
            editing = false;
          } else {
            editing = true;
          }
        }
      }
    // };

    // OnEditorActionListener on_edit = new OnEditorActionListener()
    // {
      @Override
      public boolean onEditorAction( TextView v, int action, KeyEvent event )
      {
        if ( pos < START ) return false;
        // TDLog.v( "onEditor " + v.getId() + " F " + tvFrom.getId() + " T " + tvTo.getId() );
        if ( (TextView)v == tvLength ) return false;
        // action EditorInfo.IME_NULL = 0
        // TDLog.v( "FROM action " + action + " event " + ( (event == null)? "null": event.toString()) );
        // TDLog.v( "Blk " + mBlock.mFrom + " " + mBlock.mTo + " --> " + tvFrom.getText().toString() + " " + tvTo.getText().toString() );
        if ( mBlock != null ) {
          if ( ( event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) || action == EditorInfo.IME_ACTION_DONE ) {
            String f = tvFrom.getText().toString();
            String t = tvTo.getText().toString();
            mParent.updateDBlockName( mBlock, f, t );
            // mBlock.setBlockName( f, t, mBlock.isBackLeg() ); moved to updateDBlockName
            setColor( mBlock );
            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( v.getWindowToken(), 0);
          } else {
            if ( v == tvFrom ) {
              tvFrom.setText( mBlock.mFrom );
            } else if ( v == tvTo ) {
              tvTo.setText( mBlock.mTo );
            }
          }
        }
        editing = false;
        return true; // action consumed
      }
    // };
      @Override
      public boolean onLongClick( View v )
      {
        if ( pos < START ) return false;
        // TDLog.v( "onLongClick " + v.getId() );
        if ( (TextView)v == tvLength ) {
          return mParent.itemLongClick( v, pos );
        }
        return false;
      }

    /** fill the text-views with the data of a block
     * @param b        block
     * @param listener listener of long-taps on the view
     */
    void setViewText( DBlock b, OnLongClickListener listener )
    {
      if ( b == null ) return;
      tvId.setText( String.format(Locale.US, "%1$d", b.getBlockIndexOrId() ) );
      tvFrom.setText( b.mFrom );
      tvTo.setText( b.mTo );
      if ( diving ) {
        tvLength.setText(  String.format(Locale.US, "%1$6.2f %2$5.1f %3$6.2f %4$s", 
          b.mDepth   * TDSetting.mUnitLength,
          b.mBearing * TDSetting.mUnitAngle,
          b.mLength  * TDSetting.mUnitLength,
          b.toNote() ) );
      } else { // normal
        tvLength.setText(  String.format(Locale.US, "%1$6.2f %2$5.1f %3$5.1f %4$s", 
          b.mLength  * TDSetting.mUnitLength,
          b.mBearing * TDSetting.mUnitAngle,
          b.mClino   * TDSetting.mUnitAngle,
          b.toNote() ) );
      }

      // OnClickListener toggle = new OnClickListener() {
      //   public void onClick( View v ) { mParent.recomputeItems( ((TextView)v).getText().toString(), pos ); }
      // };

      tvFrom.setOnClickListener( this );
      tvTo.setOnClickListener( this );
      tvLength.setOnClickListener( this );
      if ( TDLevel.overBasic ) tvLength.setOnLongClickListener( this );

      if ( TDLevel.overBasic ) {
        tvFrom.setOnLongClickListener( listener );
        tvTo.setOnLongClickListener( listener );
      }

      if ( TDSetting.mEditableStations ) {
        tvFrom.setOnEditorActionListener( this );
        tvTo.setOnEditorActionListener( this );
      } else {
        tvFrom.setOnEditorActionListener( null );
        tvTo.setOnEditorActionListener( null );
      }

      int text_size = TDSetting.mTextSize;

      if ( tvFrom.getTextSize() != text_size ) {
        tvId.setTextSize(      text_size*0.75f );
        tvFrom.setTextSize(    text_size );
        tvTo.setTextSize(      text_size );
        tvLength.setTextSize(  text_size );
      }

      if ( show_ids ) {
        tvId.setVisibility( View.VISIBLE );
        tvId.setTextColor( TDColor.LIGHT_BLUE );
      } else {
        tvId.setVisibility( View.GONE );
      }
      setColor( b );
   }

   /** set the color of a block
    * @param b  block
    * @note this implements the TopoDroid data coloring policy
    */
   void setColor( DBlock b )
   {
      if ( b == null ) return;
      int col = b.getColorByType();
      if ( b == lastMultiselectedBlock ) {
        // TDLog.v("Block " + b.mId + " is last MS " + b.mMultiSelected );
        tvFrom.setTextColor( TDColor.LIGHT_YELLOW );
        tvTo.setTextColor( TDColor.LIGHT_YELLOW );
      } else {
        tvFrom.setTextColor( ( mParent.isCurrentStationName( b.mFrom ) )? TDColor.LIGHT_GREEN : col );
        tvTo.setTextColor(   ( mParent.isCurrentStationName( b.mTo   ) )? TDColor.LIGHT_GREEN : col );
        tvLength.setTextColor( col );
      }

      if (b.mMultiSelected ) {
        b.setBackgroundColor( TDColor.GRID );
      } else {
        if ( b.isBacksight() ) {
          int color = b.isRecent()? TDColor.GREEN : TDColor.BACK_YELLOW;
          tvTo.setBackgroundColor( color );
          if ( b.isSplay() ) {
            if ( b.failBacksplay() ) {
              color = TDColor.VIOLET;
            } else if ( ( col = b.getPaintColor() ) != 0 ) {
              color = col & 0x99ffffff;
            }
          }
          tvFrom.setBackgroundColor( color );
        } else {
          int color = 0;
          if ( b.isRecent() ) {
            color = TDColor.DARK_GREEN;
            tvTo.setBackgroundColor( color );
          }
          if ( b.isSplay() ) {
            if ( b.failBacksplay() ) {
              color = TDColor.VIOLET;
            } else if ( ( col = b.getPaintColor() ) != 0 ) {
              color = col & 0x99ffffff;
            }
          }
          if ( color != 0 ) tvFrom.setBackgroundColor( color );
        }
      }

      if ( b.isCommented() ) {
        tvLength.setBackgroundColor( TDColor.VERYDARK_GRAY );
      } else if ( b.isMainLeg() && b.mLength < TDSetting.mMinLegLength ) {
        tvLength.setBackgroundColor( TDColor.BROWN );
      } else if ( mParent.isBlockMagneticBad( b ) ) {
        tvLength.setBackgroundColor( TDColor.DARK_RED );
      } else {
        tvLength.setBackgroundColor( TDColor.BLACK );
      }
    }
  } // ViewHolder

  /** @return the view for a position
   * @param pos         position
   * @param convertView convert-view, or null
   * @param parent      parent view-group
   */
  // @RecentlyNonNull
  @SuppressLint("WrongConstant")
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    DBlock b = (DBlock)(getItem( pos ));

    ViewHolder holder; // = null;
    if ( convertView == null ) {
      if ( TDSetting.mEditableStations ) {
        convertView = mLayoutInflater.inflate( R.layout.dblock_row, parent, false );
      } else {
        // TDLog.v( "get view at " + pos + " parent " + parent.getId() );
        convertView = mLayoutInflater.inflate( R.layout.dblock_row_noedit, parent, false );
      }
      holder = new ViewHolder( 
        (TextView)convertView.findViewById( R.id.id ),
        (EditText)convertView.findViewById( R.id.from ),
        (EditText)convertView.findViewById( R.id.to ),
        (TextView)convertView.findViewById( R.id.length ) );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.pos = pos;
    // if ( holder.mBlock != null ) holder.mBlock.mView = null; // REVISE_RECENT
    holder.mBlock = b;
    // holder.blkId = b.mId; // DistoX-EDIT

    if ( b != null ) {
      b.setView( convertView );
      holder.setViewText( b, this );
      if ( mSearch.contains( pos ) ) {
        b.setBackgroundColor( TDColor.SEARCH );
      } else {
        b.setBackgroundColor( b.mMultiSelected ? TDColor.GRID : TDColor.TRANSPARENT );
      }
      // TDLog.v( "DBlock adapter " + b.mId + " get view: view is " + ((convertView == null)? "null" : "non-null") );
      convertView.setVisibility( b.getVisible() );
    }
    return convertView;
  }

  // @Override
  // public int getCount() { 
  //   // TDLog.v( "get count " + mItems.size() );
  //   return mItems.size(); 
  // }

  // replaced by getCount()
  // public int size() { return mItems.size(); }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }

  void updateSelectBlocksView()
  {
    for ( DBlock b : mSelect ) {
      View v = b.getView();
      // TDLog.v( "DBlock adapter " + b.mId + " get type: view is " + ((v == null)? "null" : "non-null") );
      if ( v != null ) {
        ViewHolder holder = (ViewHolder) v.getTag();
        if ( holder != null ) {
          // TDLog.v( "holder set view text <" + b.mFrom + "> <" + b.mTo + ">" );
          holder.setViewText( b, this );
        }
        v.setVisibility( b.getVisible() );
        v.invalidate();
      }
    }
  }
 
  /** called by ShotWindow::updateShot()
   * @param blk_id  block id
   */
  @SuppressLint("WrongConstant")
  DBlock updateBlockView( long blk_id ) 
  {
    int size = getCount();
    for ( int pos=START; pos < size; ++pos ) {
      DBlock b = (DBlock)( getItem( pos ) );
      if ( b.mId == blk_id ) { // use block id instead of block itself
        View v = b.getView();
        // TDLog.v( "DBlock adapter " + b.mId + " get type: view is " + ((v == null)? "null" : "non-null") );
        if ( v != null ) {
          ViewHolder holder = (ViewHolder) v.getTag();
          if ( holder != null ) {
            // TDLog.v( "holder set view text <" + b.mFrom + "> <" + b.mTo + ">" );
            holder.setViewText( b, this );
          }
          v.setVisibility( b.getVisible() );
          v.invalidate();
        }
        return b;
      }
    }
    return null;
  }

  /** update block name and text-color
   * @param set   whether to set green or normal color
   */
  @SuppressLint("WrongConstant")
  private void updateBlocksName( boolean set )
  {
    int size = getCount();
    if ( ! set ) {
      for ( int pos=START; pos < size; ++pos ) {
        DBlock b = (DBlock)( getItem( pos ) );
        // if ( b.isLeg() ) {
          View v = b.getView();
          if ( v != null ) {
            TextView tvFrom = (TextView) v.findViewById( R.id.from );
            TextView tvTo   = (TextView) v.findViewById( R.id.to );
            int col = b.getColorByType( );
            tvFrom.setTextColor( col );
            tvTo.setTextColor( col );
          }
        // }
      }
    } else {
      for ( int pos=START; pos < size; ++pos ) {
        DBlock b = (DBlock)( getItem( pos ) );
        // if ( b.isLeg() ) {
          View v = b.getView();
          if ( v != null ) {
            TextView tvFrom = (TextView) v.findViewById( R.id.from );
            TextView tvTo   = (TextView) v.findViewById( R.id.to );
            int col = b.getColorByType( );
            tvFrom.setTextColor( col );
            tvTo.setTextColor( col );
            if ( mParent.isCurrentStationName( b.mFrom ) ) {
              tvFrom.setTextColor( TDColor.LIGHT_GREEN );
            } else if ( mParent.isCurrentStationName( b.mTo ) ) {
              tvTo.setTextColor( TDColor.LIGHT_GREEN );
            }
            // TDLog.v( "DBlock adapter " + b.mId + " update name, view is " + ((v == null)? "null" : "non-null") );
            v.setVisibility( b.getVisible() );
            v.invalidate();
          }
        // }
      }
    }
  }

  // /** react to a user tap
  //  * @param view   tapped view
  //  */
  // public void onClick(View view)
  // {
  //   TextView tv = (TextView) view;
  //   if ( tv != null ) {
  //     mParent.recomputeItems( tv.getText().toString() );
  //   }
  // }

  /** react to a user long-tap
   * @param view   tapped view
   * @return true if tap has been handled
   */
  public boolean onLongClick( View view ) 
  {
    // TDLog.v( "onLongClick " + view.getId() );
    if ( TDLevel.overBasic ) {
      TextView tv = (TextView) view;
      if ( tv != null ) {
        String st = tv.getText().toString();
        boolean set = mParent.setCurrentStationName( st );
        updateBlocksName( set );
      }
      return true;
    }
    return false;
  }

  // not used
  // go over the list and revised background of FROM-TO for recent shots // REVISE_RECENT
  //
  // int reviseLatest()
  // {
  //   int revised = 0;
  //   int size = getCount();
  //   for ( int pos=START; pos < size; ++pos ) {
  //     DBlock blk = (DBlock)( getItem( pos ) );
  //     if ( blk.getView() != null && blk.mWasRecent ) {
  //       if ( ! blk.isRecent() ) {
  //         blk.mWasRecent = false;
  //         ViewHolder holder = (ViewHolder) blk.getView().getTag();
  //         holder.tvFrom.setBackgroundColor( TDColor.BLACK );
  //         holder.tvTo.setBackgroundColor( TDColor.BLACK );
  //         blk.getView().invalidate();
  //         ++ revised;
  //       }
  //     }
  //   }
  //   return revised;
  // }

  /** this is used to drop blunder shots from the list
   * @note this should be called only if TDSetting.mBlunderShot is true
   */
  void dropBlunders() // BLUNDER
  {
    // if ( ! TDSetting.mBlunderShot ) return; // not necessary
    int pos = 0; 
    while ( pos < getCount() ) {
      DBlock blk = (DBlock)getItem(pos);
      if ( blk.getVisible() == View.GONE ) {
        remove( blk );
      } else {
        ++pos;
      }
    }
  }

}

