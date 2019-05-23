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

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

// import android.util.Log;

class DBlockAdapter extends ArrayAdapter< DBlock >
                          implements OnLongClickListener
                                   // , OnClickListener
{
  // private Context mContext;
  private ShotWindow mParent;
  ArrayList< DBlock > mItems;
  ArrayList< DBlock > mSelect;
  boolean show_ids;  //!< whether to show data ids
  private LayoutInflater mLayoutInflater;
  private boolean diving;

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
  }

  int[] searchStation( String name, boolean splays )
  {
    if ( name == null || name.length() == 0 ) return null;
    ArrayList<Integer> res = new ArrayList<>();
    for ( int pos=0; pos < mItems.size(); ++pos ) {
      DBlock blk = mItems.get( pos );
      if ( blk.isSplay() && splays && name.equals( blk.mFrom ) ) {
        res.add( new Integer(pos) );
      } else if ( blk.isLeg() && ( name.equals( blk.mFrom ) || name.equals( blk.mTo ) ) ) {
        res.add( new Integer(pos) );
      }
    }
    if ( res.size() == 0 ) return null;
    int[] ret = new int[ res.size() ];
    for ( int k=0; k<res.size(); ++k ) ret[k] = res.get(k).intValue();
    return ret;
  }

  int[] searchShot( long flag )
  {
    ArrayList<Integer> res = new ArrayList<>();
    if ( flag == DBlock.FLAG_NO_EXTEND ) {
      for ( int pos=0; pos < mItems.size(); ++pos ) {
        DBlock blk = mItems.get( pos );
        if ( blk.getExtend( ) > 1 ) {
          res.add( new Integer(pos) );
        }
      }
    } else { // real flag
      for ( int pos=0; pos < mItems.size(); ++pos ) {
        DBlock blk = mItems.get( pos );
        if ( blk.hasFlag( flag ) ) {
          res.add( new Integer(pos) );
        }
      }
    }
    if ( res.size() == 0 ) return null;
    int[] ret = new int[ res.size() ];
    for ( int k=0; k<res.size(); ++k ) ret[k] = res.get(k).intValue();
    return ret;
  }
    
  boolean isMultiSelect() { return ( mSelect.size() > 0 ); }

  boolean multiSelect(int pos ) 
  {
    DBlock blk = mItems.get( pos );
    if ( blk != null ) {
      if ( mSelect.size() > 0 ) {
        if ( mSelect.remove( blk ) ) {
          blk.mMultiSelected = false;
          if ( blk.mView != null ) blk.mView.setBackgroundColor( TDColor.TRANSPARENT );
        } else {
          mSelect.add( blk );
          blk.mMultiSelected = true;
          if ( blk.mView != null ) blk.mView.setBackgroundColor( TDColor.GRID );
        }
      } else {
        mSelect.add( blk );
        blk.mMultiSelected = true;
        if ( blk.mView != null ) blk.mView.setBackgroundColor( TDColor.GRID );
      }
    // } else {
      // Log.v("DistoX", "adapter multiselect. null blk. size " + mSelect.size() );
    }
    return ( mSelect.size() > 0 );
  }

  void clearMultiSelect() 
  { 
    for ( DBlock b : mSelect ) {
      if ( b.mView != null ) b.mView.setBackgroundColor( TDColor.TRANSPARENT );
      b.mMultiSelected = false;
    }
    mSelect.clear();
  }
   

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
        for ( ; k1 > 0; --k1 ) {
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

  // called only by ShotWindow updateBlockList( blk )
  // this method changes the ArrayList of DistoxDBlock's
  //
  boolean addDataBlock( DBlock blk ) 
  {
    if ( hasBlock( blk.mId ) ) return false;
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

  private boolean hasBlock( long id )
  {
    int size = mItems.size();
    if ( size == 0 ) return false;
    return ( id >= 0 && id <= mItems.get(size-1).mId );

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

  ArrayList< DBlock > getItemsForAssign()
  {
    ArrayList< DBlock > ret = new ArrayList<>();
    int size = mItems.size();
    int k = size-1;
    for ( ; k > 0; --k ) {
      DBlock blk = mItems.get(k);
      if ( blk.mFrom.length() > 0 && blk.mTo.length() > 0 ) break;
    }
    for ( ; k < size; ++k ) {
      ret.add( mItems.get(k) );
    }
    return ret;
  }

  public DBlock get( int pos ) 
  { 
    try {
      return mItems.get(pos);
    } catch ( IndexOutOfBoundsException e ) { }
    return null;
  }

  // public DBlock getBlockById( long id ) 
  // {
  //   for ( DBlock b : mItems ) if ( b.mId == id ) return b;
  //   return null;
  // }

  private class ViewHolder
  { 
    int      pos;
    TextView tvId;
    TextView tvFrom;
    TextView tvTo;
    TextView tvLength;
    // DBlock   mBlock;   // used to make sure blocks do not hold ref to a view, that does not belog to them REVISE_RECENT

    ViewHolder( TextView id, TextView from, TextView to, TextView len )
    {
      pos = 0;
      tvId      = id;
      tvFrom    = from;
      tvTo      = to;
      tvLength  = len;
      // mBlock    = null; // REVISE_RECENT
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

      OnClickListener toggle = new OnClickListener() {
        public void onClick( View v ) { mParent.recomputeItems( ((TextView)v).getText().toString(), pos ); }
      };

      tvFrom.setOnClickListener( toggle );
      tvTo.setOnClickListener( toggle );

      if ( TDLevel.overBasic ) {
        tvFrom.setOnLongClickListener( listener );
        tvTo.setOnLongClickListener( listener );
      }

      int col = b.color();
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
      // if ( b.isLeg() ) {
        if ( mParent.isCurrentStationName( b.mFrom ) ) {
          tvFrom.setTextColor( TDColor.LIGHT_GREEN );
        } else {
          tvFrom.setTextColor( col );
          tvTo.setTextColor( ( mParent.isCurrentStationName( b.mTo )  )? TDColor.LIGHT_GREEN : col );
        }
      // } else {
      //   tvFrom.setTextColor( col );
      //   tvTo.setTextColor(   col );
      // }
      tvLength.setTextColor( col );

      if ( b.isRecent( ) ) { 
	// b.mWasRecent = true;
        tvFrom.setBackgroundColor( TDColor.DARK_GREEN );
        tvTo.setBackgroundColor( TDColor.DARK_GREEN );
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
      convertView = mLayoutInflater.inflate( R.layout.dblock_row, parent, false );
      holder = new ViewHolder( 
        (TextView)convertView.findViewById( R.id.id ),
        (TextView)convertView.findViewById( R.id.from ),
        (TextView)convertView.findViewById( R.id.to ),
        (TextView)convertView.findViewById( R.id.length ) );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.pos = pos;
    // if ( holder.mBlock != null ) holder.mBlock.mView = null; // REVISE_RECENT
    // holder.mBlock = b;
    b.mView = convertView;

    holder.setViewText( b, this );
    b.mView.setBackgroundColor( b.mMultiSelected ? TDColor.GRID : TDColor.TRANSPARENT );
    convertView.setVisibility( b.mVisible );
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  public int size() { return mItems.size(); }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
  // called by ShotWindow::updateShot()
  //
  DBlock updateBlockView( long blk_id ) 
  {
    for ( DBlock b : mItems ) {
      if ( b.mId == blk_id ) { // use block id instead of block itself
        View v = b.mView;
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

  private void updateBlocksName( boolean set )
  {
    if ( ! set ) {
      for ( DBlock b : mItems ) {
        // if ( b.isLeg() ) {
          View v = b.mView;
          if ( v != null ) {
            TextView tvFrom = (TextView) v.findViewById( R.id.from );
            TextView tvTo   = (TextView) v.findViewById( R.id.to );
            tvFrom.setTextColor( b.color() );
            tvTo.setTextColor( b.color() );
          }
        // }
      }
    } else {
      for ( DBlock b : mItems ) {
        // if ( b.isLeg() ) {
          View v = b.mView;
          if ( v != null ) {
            TextView tvFrom = (TextView) v.findViewById( R.id.from );
            TextView tvTo   = (TextView) v.findViewById( R.id.to );
            tvFrom.setTextColor( b.color() );
            tvTo.setTextColor( b.color() );
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
  //     if ( blk.mView != null && blk.mWasRecent ) {
  //       if ( ! blk.isRecent() ) {
  //         blk.mWasRecent = false;
  //         ViewHolder holder = (ViewHolder) blk.mView.getTag();
  //         holder.tvFrom.setBackgroundColor( TDColor.BLACK );
  //         holder.tvTo.setBackgroundColor( TDColor.BLACK );
  //         blk.mView.invalidate();
  //         ++ revised;
  //       }
  //     }
  //   }
  //   return revised;
  // }

}

