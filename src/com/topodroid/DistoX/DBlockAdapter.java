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
package com.topodroid.DistoX;

import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

class DBlockAdapter extends ArrayAdapter< DBlock >
                    implements OnLongClickListener
                    // , OnClickListener
{
  // private Context mContext;
  private final ShotWindow mParent;
  ArrayList< DBlock > mItems;
  ArrayList< DBlock > mSelect;
  boolean show_ids;  //!< whether to show data ids
  private final LayoutInflater mLayoutInflater;
  private final boolean diving;
  private SearchResult mSearch;

  // private ArrayList< View > mViews;

  DBlockAdapter( Context ctx, ShotWindow parent, int id, ArrayList< DBlock > items )
  {
    super( ctx, id, items );
    // mContext = ctx;
    mParent  = parent;
    mItems   = items;
    mSelect  = new ArrayList<>();
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    // mViews = new ArrayList<>();
    diving = (TDInstance.datamode == SurveyInfo.DATAMODE_DIVING);
    // Log.v("DistoX", "DBlock Adapter, diving " + diving );
    mSearch = new SearchResult();
  }

  void searchStation( String name, boolean splays )
  {
    mSearch.reset( name );
    if ( name == null || name.length() == 0 ) return; // null;
    for ( int pos=0; pos < mItems.size(); ++pos ) {
      DBlock blk = mItems.get( pos );
      if ( blk.isSplay() && splays && name.equals( blk.mFrom ) ) {
        mSearch.add( pos );
        blk.setBackgroundColor( TDColor.SEARCH );
      } else if ( blk.isLeg() && ( name.equals( blk.mFrom ) || name.equals( blk.mTo ) ) ) {
        mSearch.add( pos );
        blk.setBackgroundColor( TDColor.SEARCH );
      }
    }
    // Log.v("DistoX-SEARCH", " search station " + name + " results " + mSearch.size() );
    // return (mSearch.size() > 0);
  }

  void searchShot( long flag )
  {
    mSearch.reset( null );
    if ( flag == DBlock.FLAG_NO_EXTEND ) {
      for ( int pos=0; pos < mItems.size(); ++pos ) {
        DBlock blk = mItems.get( pos );
        if ( blk.getIntExtend( ) > 1 && blk.isLeg() ) {
          mSearch.add( pos );
          blk.setBackgroundColor( TDColor.SEARCH );
        }
      }
    } else { // real flag
      for ( int pos=0; pos < mItems.size(); ++pos ) {
        DBlock blk = mItems.get( pos );
        if ( blk.hasFlag( flag ) ) {
          mSearch.add( pos );
          blk.setBackgroundColor( TDColor.SEARCH );
        }
      }
    }
    // Log.v("DistoX-SEARCH", " search shot " + flag + " results " + mSearch.size() );
    // return (mSearch.size() > 0);
  }
    
  boolean isMultiSelect() { return ( mSelect.size() > 0 ); }

  boolean multiSelect(int pos ) 
  {
    if ( pos >= 0 && pos < mItems.size() ) {
      DBlock blk = mItems.get( pos );
      if ( blk != null ) {
        if ( mSelect.size() > 0 ) {
          if ( mSelect.remove( blk ) ) {
            blk.mMultiSelected = false;
            blk.setBackgroundColor( TDColor.TRANSPARENT );
          } else {
            mSelect.add( blk );
            blk.mMultiSelected = true;
            blk.setBackgroundColor( TDColor.GRID );
          }
        } else {
          mSelect.add( blk );
          blk.mMultiSelected = true;
          blk.setBackgroundColor( TDColor.GRID );
        }
      }
    // } else {
      // Log.v("DistoX", "adapter multiselect. null blk. size " + mSelect.size() );
    }
   
    return ( mSelect.size() > 0 );
  }

  void clearMultiSelect() 
  { 
    for ( DBlock b : mSelect ) {
      b.setBackgroundColor( TDColor.TRANSPARENT );
      b.mMultiSelected = false;
    }
    mSelect.clear();
  }

  void clearSearch()
  { 
    for ( Integer pos : mSearch.getPositions() ) {
      try { 
        DBlock b = mItems.get( pos.intValue()  );
        b.setBackgroundColor( TDColor.TRANSPARENT );
      } catch ( IndexOutOfBoundsException e ) { /* nothing i can do */ }
    }
    mSearch.clearSearch(); 
  }
  int  nextSearchPosition() { return mSearch.nextPos(); }
  String getSearchName()    { return mSearch.getName(); }

  /** this is not efficient because it scans the list of shots
   *  skips oven non-mail_leg, still it would be better to keep a tree of 
   *  station names
   */
  boolean hasStation( String name ) 
  {
    if ( name == null ) return false;
    for ( DBlock b : mItems ) {
      if ( ! b.isLeg() ) continue;
      if ( name.equals( b.mFrom ) || name.equals( b.mTo ) ) return true;
    }
    return false;
  }

  /** get all the splays around a splay-block (from the prev leg to the next leg)
   * @param id    id of the given splay-block
   */
  ArrayList< DBlock > getSplaysAtId( long id, String name )
  {
    ArrayList< DBlock > ret = new ArrayList<>();
    for ( int k = 0; k < mItems.size(); ++k ) {
      DBlock b = mItems.get( k );
      if ( b.mId == id ) {
        int k1 = k-1;
        for ( ; k1 >= 0; --k1 ) {
          DBlock b1 = mItems.get( k1 );
          if ( ! b1.isSplay() || ! name.equals( b1.mFrom ) ) break;
        }
        int k2 = k;
        for ( ; k2 < mItems.size(); ++k2 ) {
          DBlock b2 = mItems.get( k2 );
          if ( ! b2.isSplay() || ! name.equals( b2.mFrom ) ) break;
        }
        for ( ++k1; k1<k2; ++k1 ) ret.add( mItems.get(k1) );
        break;
      }
    }
    return ret;
  }

  // called only by ShotWindow updateDBlockList( blk )
  // this method changes the ArrayList of DistoxDBlock's
  //
  boolean addDataBlock( DBlock blk ) 
  {
    if ( ! blk.isScan() && hasBlock( blk.mId ) ) return false;
    mItems.add( blk );
    // notifyDataSetChanged();
    return true;
  }

  // called by ShotWindow::updateShotlist
  //  
  void reviseBlockWithPhotos( List< PhotoInfo > photos )
  {
    for ( DBlock b : mItems ) b.mWithPhoto = false; 
    for ( PhotoInfo p : photos ) {
      // mark block with p.shotid
      for ( DBlock b : mItems ) {
        if ( b.mId == p.shotid ) { 
          b.mWithPhoto = true;
          break;
        }
      }
    }
  }

  // @return true if the block was already in the list
  private boolean hasBlock( long id )
  {
    // Log.v("DistoX", "Data Adapter items size " + mItems.size() + " block id " + id );
    if ( id < 0 ) return false;
    int size = mItems.size();
    if ( size == 0 ) return false;
    DBlock last_blk = mItems.get( size-1 );
    // Log.v("DistoX", "Data Adapter ID " + id + " last_blk " + last_blk.mId + " scan " + last_blk.isScan() );
    if ( last_blk.isScan() ) return false;
    return ( id <= last_blk.mId )? true : false;
    // for ( DBlock b : mItems ) if ( b.mId == id ) return true;
    // return false;
  }

  void updateBlockName( long id, String from, String to ) 
  {
    for ( DBlock b : mItems ) {
      if ( b.mId == id ) {
        b.mFrom = from;
        b.mTo = to;
        break;
      }
    }
  }

  // return the list of blocks to assign station names
  // called only by ShotWindow.updateDBlockList
  List< DBlock > getItemsForAssign()
  {
    int size = mItems.size();
    int k = size-1;
    for ( ; k > 0; --k ) { // scan from last backword until a leg is found
      DBlock blk = mItems.get(k);
      if ( blk.mFrom.length() > 0 && blk.mTo.length() > 0 ) break;
    }
    // List< DBlock > ret = new ArrayList<>();
    // for ( ; k < size; ++k ) { // insert into return from the leg onward
    //   ret.add( mItems.get(k) );
    // }
    // return ret; // REPLACED 20191002
    return mItems.subList( k, size );
  }

  // return the block at the given position
  public DBlock get( int pos ) 
  { 
    return ( pos < 0 || pos >= mItems.size() )? null : mItems.get(pos);
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
    DBlock   mBlock;   // used to make sure blocks do not hold ref to a view, that does not belog to them REVISE_RECENT

    ViewHolder( TextView id, EditText from, EditText to, TextView len )
    {
      pos       = 0;
      editing   = false;
      // blkId     = -1; // DistoX-EDIT
      tvId      = id;
      tvFrom    = from;
      tvTo      = to;
      tvLength  = len;
      tvLength.setWidth( (int)(TopoDroidApp.mDisplayWidth) );
      mBlock    = null; // REVISE_RECENT
    }

    // OnClickListener on_click = new OnClickListener()
    // {
      @Override
      public void onClick( View v )
      {
        // Log.v("DistoX-EDIT", "onClick " + v.getId() );
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
        // Log.v("DistoX-EDIT", "onEditor " + v.getId() );
        if ( (TextView)v == tvLength ) return false;
        // action EditorInfo.IME_NULL = 0
        // Log.v("DistoX-EDIT", "FROM action " + action + " event " + ( (event == null)? "null": event.toString()) );
        // Log.v("DistoX-EDIT", "Blk " + mBlock.mFrom + " " + mBlock.mTo + " --> " + tvFrom.getText().toString() + " " + tvTo.getText().toString() );
        if ( mBlock != null ) {
          if ( ( event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) || action == EditorInfo.IME_ACTION_DONE ) {
            String f = tvFrom.getText().toString();
            String t = tvTo.getText().toString();
            mParent.updateShotName( mBlock.mId, f, t );
            mBlock.setBlockName( f, t, mBlock.isBackLeg() ); 
            setColor( mBlock );
            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( v.getWindowToken(), 0);
          }
        }
        editing = false;
        return true; // action consumed
      }
    // };
      @Override
      public boolean onLongClick( View v )
      {
        // Log.v("DistoX-EDIT", "onLongClick " + v.getId() );
        if ( (TextView)v == tvLength ) {
          return mParent.itemLongClick( v, pos );
        }
        return false;
      }

    void setViewText( DBlock b, OnLongClickListener listener )
    {
      tvId.setText( String.format(Locale.US, "%1$d", b.mId ) );
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
      if ( TDLevel.overNormal ) tvLength.setOnLongClickListener( this );

      if ( TDLevel.overBasic ) {
        tvFrom.setOnLongClickListener( listener );
        tvTo.setOnLongClickListener( listener );
      }

      if ( TDSetting.mEditableStations ) {
        tvFrom.setOnEditorActionListener( this );
        tvTo.setOnEditorActionListener( this );
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

   void setColor( DBlock b )
   {
      int col = b.getColorByType();
      tvFrom.setTextColor( ( mParent.isCurrentStationName( b.mFrom ) )? TDColor.LIGHT_GREEN : col );
      tvTo.setTextColor(   ( mParent.isCurrentStationName( b.mTo   ) )? TDColor.LIGHT_GREEN : col );
      tvLength.setTextColor( col );

      if ( b.isBacksight() ) {
        if ( b.isRecent() ) { 
          // b.mWasRecent = true;
          if ( b.isSplay() && ( col = b.getPaintColor() ) != 0 ) {
            tvFrom.setBackgroundColor( col & 0x99ffffff );
          } else {
            tvFrom.setBackgroundColor( TDColor.GREEN );
          }
          tvTo.setBackgroundColor( TDColor.GREEN );
        } else {
          if ( b.isSplay() && ( col = b.getPaintColor() ) != 0 ) {
            tvFrom.setBackgroundColor( col & 0x99ffffff );
          } else {
            tvFrom.setBackgroundColor( TDColor.BACK_YELLOW );
          }
          tvTo.setBackgroundColor( TDColor.BACK_YELLOW );
        } 
      } else {
        if ( b.isRecent() ) { 
          // b.mWasRecent = true;
          if ( b.isSplay() && ( col = b.getPaintColor() ) != 0 ) {
            tvFrom.setBackgroundColor( col & 0x99ffffff );
          } else {
            tvFrom.setBackgroundColor( TDColor.DARK_GREEN );
          }
          tvTo.setBackgroundColor( TDColor.DARK_GREEN );
        } else {
          if ( b.isSplay() && ( col = b.getPaintColor() ) != 0 ) {
            tvFrom.setBackgroundColor( col & 0x99ffffff );
          }
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
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    DBlock b = mItems.get( pos );
    ViewHolder holder; // = null;
    if ( convertView == null ) {
      if ( TDSetting.mEditableStations ) {
        convertView = mLayoutInflater.inflate( R.layout.dblock_row, parent, false );
      } else {
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
    b.setView( convertView );

    holder.setViewText( b, this );
    if ( mSearch.contains( pos ) ) {
      b.setBackgroundColor( TDColor.SEARCH );
    } else {
      b.setBackgroundColor( b.mMultiSelected ? TDColor.GRID : TDColor.TRANSPARENT );
    }
    convertView.setVisibility( b.mVisible );
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  // replaced by getCount()
  // public int size() { return mItems.size(); }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
  // called by ShotWindow::updateShot()
  //
  DBlock updateBlockView( long blk_id ) 
  {
    for ( DBlock b : mItems ) {
      if ( b.mId == blk_id ) { // use block id instead of block itself
        View v = b.getView();
        if ( v != null ) {
          ViewHolder holder = (ViewHolder) v.getTag();
          if ( holder != null ) holder.setViewText( b, this );
          v.setVisibility( b.mVisible );
          v.invalidate();
        }
        return b;
      }
    }
    return null;
  }

  // update block name text-color
  // @param set   whether to set green or normal color
  private void updateBlocksName( boolean set )
  {
    if ( ! set ) {
      for ( DBlock b : mItems ) {
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
      for ( DBlock b : mItems ) {
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
            v.setVisibility( b.mVisible );
            v.invalidate();
          }
        // }
      }
    }
  }

  // public void onClick(View view)
  // {
  //   TextView tv = (TextView) view;
  //   if ( tv != null ) {
  //     mParent.recomputeItems( tv.getText().toString() );
  //   }
  // }

  public boolean onLongClick( View view ) 
  {
    // Log.v("DistoX-EDIT", "onLongClick " + view.getId() );
    if ( TDLevel.overNormal ) {
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
  //   for ( DBlock blk : mItems ) {
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

}

