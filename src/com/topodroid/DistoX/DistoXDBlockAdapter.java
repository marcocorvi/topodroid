/** @file DistoXDBlockAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for survey data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
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

import android.util.Log;

class DistoXDBlockAdapter extends ArrayAdapter< DistoXDBlock >
                          implements OnLongClickListener
                                   // , OnClickListener
{
  private Context mContext;
  private ShotWindow mParent;
  ArrayList< DistoXDBlock > mItems;
  boolean show_ids;  //!< whether to show data ids
  private LayoutInflater mLayoutInflater;

  public DistoXDBlockAdapter( Context ctx, ShotWindow parent, int id, ArrayList< DistoXDBlock > items )
  {
    super( ctx, id, items );
    mContext = ctx;
    mParent  = parent;
    mItems   = items;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  /** this is not efficient because it scans the list of shots
   *  skips oven non-mail_leg, still it would be better to keep a tree of 
   *  station names
   */
  boolean hasStation( String name ) 
  {
    if ( name == null ) return false;
    for ( DistoXDBlock b : mItems ) {
      if ( b.mType != DistoXDBlock.BLOCK_MAIN_LEG ) continue;
      if ( name.equals( b.mFrom ) || name.equals( b.mTo ) ) return true;
    }
    return false;
  }

  /** get all the splays around a splay-block (from the prev leg to the next leg)
   * @param id    id of the given splay-block
   */
  ArrayList< DistoXDBlock > getSplaysAtId( long id, String name )
  {
    ArrayList< DistoXDBlock > ret = new ArrayList< DistoXDBlock >();
    for ( int k = 0; k < mItems.size(); ++k ) {
      DistoXDBlock b = mItems.get( k );
      if ( b.mId == id ) {
        int k1 = k-1;
        for ( ; k1 > 0; --k1 ) {
          DistoXDBlock b1 = mItems.get( k1 );
          if ( b1.mType != DistoXDBlock.BLOCK_SPLAY || ! name.equals( b1.mFrom ) ) break;
        }
        int k2 = k;
        for ( ; k2 < mItems.size(); ++k2 ) {
          DistoXDBlock b2 = mItems.get( k2 );
          if ( b2.mType != DistoXDBlock.BLOCK_SPLAY || ! name.equals( b2.mFrom ) ) break;
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
  void addDataBlock( DistoXDBlock blk ) 
  {
    mItems.add( blk );
    // notifyDataSetChanged();
  }

  // called by ShotWindow::updateShotlist
  //  
  void reviseBlockWithPhotos( List< PhotoInfo > photos )
  {
    for ( DistoXDBlock b : mItems ) b.mWithPhoto = false; 
    for ( PhotoInfo p : photos ) {
      // mark block with p.shotid
      for ( DistoXDBlock b : mItems ) {
        if ( b.mId == p.shotid ) { 
          b.mWithPhoto = true;
          break;
        }
      }
    }
  }

  void updateBlockName( long id, String from, String to ) 
  {
    for ( DistoXDBlock b : mItems ) {
      if ( b.mId == id ) {
        b.mFrom = from;
        b.mTo = to;
        break;
      }
    }
  }

  ArrayList< DistoXDBlock > getItemsForAssign()
  {
    ArrayList< DistoXDBlock > ret = new ArrayList<DistoXDBlock>();
    int size = mItems.size();
    int k = size-1;
    for ( ; k > 0; --k ) {
      DistoXDBlock blk = mItems.get(k);
      if ( blk.mFrom.length() > 0 && blk.mTo.length() > 0 ) break;
    }
    for ( ; k < size; ++k ) {
      ret.add( mItems.get(k) );
    }
    return ret;
  }

  public DistoXDBlock get( int pos ) { return mItems.get(pos); }

  // public DistoXDBlock getBlockById( long id ) 
  // {
  //   for ( DistoXDBlock b : mItems ) if ( b.mId == id ) return b;
  //   return null;
  // }
 
  private class ViewHolder
  { 
    DistoXDBlock blk;
    int          pos;
    // TextView tvId;
    // TextView tvFrom;
    // TextView tvTo;
    // TextView tvLength;
    // TextView tvCompass;
    // TextView tvClino;
    // TextView tvNote;
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    DistoXDBlock b = mItems.get( pos );
    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.dblock_row, null );
      holder = new ViewHolder();
      holder.blk       = null;
      holder.pos       = pos;
      // holder.tvId      = (TextView) convertView.findViewById( R.id.id );
      // holder.tvFrom    = (TextView) convertView.findViewById( R.id.from );
      // holder.tvTo      = (TextView) convertView.findViewById( R.id.to );
      // holder.tvLength  = (TextView) convertView.findViewById( R.id.length );
      // holder.tvCompass = (TextView) convertView.findViewById( R.id.compass );
      // holder.tvClino   = (TextView) convertView.findViewById( R.id.clino );
      // holder.tvNote    = (TextView) convertView.findViewById( R.id.note );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
      if ( holder.blk != null ) {
        holder.blk.mView = null;
      }
    }
    holder.blk = b;
    b.mView = convertView;
    setViewText( holder, b );
    convertView.setVisibility( b.mVisible );
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  public int size() { return mItems.size(); }

  private void setViewText( final ViewHolder holder, DistoXDBlock b )
  {
    DistoXDBlock blk = holder.blk;
    if ( b != blk ) return;
    View view = blk.mView;
    if ( view == null ) return;
    TextView tvId      = (TextView) view.findViewById( R.id.id );
    TextView tvFrom    = (TextView) view.findViewById( R.id.from );
    TextView tvTo      = (TextView) view.findViewById( R.id.to );
    TextView tvLength  = (TextView) view.findViewById( R.id.length );
    TextView tvCompass = (TextView) view.findViewById( R.id.compass );
    TextView tvClino   = (TextView) view.findViewById( R.id.clino );
    TextView tvNote    = (TextView) view.findViewById( R.id.note );

    tvId.setText( String.format( "%1$d", blk.mId ) );
    tvFrom.setText( blk.mFrom );
    tvTo.setText( blk.mTo );
    tvLength.setText(  String.format(Locale.US, "%1$.2f", blk.mLength * TDSetting.mUnitLength ) );
    tvCompass.setText( String.format(Locale.US, "%1$.1f", blk.mBearing * TDSetting.mUnitAngle ) );
    tvClino.setText(   String.format(Locale.US, "%1$.1f", blk.mClino * TDSetting.mUnitAngle ) );
    tvNote.setText( blk.toNote() );

    OnClickListener toggle = new OnClickListener() {
        public void onClick( View v ) { mParent.recomputeItems( ((TextView)v).getText().toString(), holder.pos ); }
    };
    OnClickListener edit1 = new OnClickListener() {
        public void onClick( View v ) { mParent.onBlockClick( holder.blk, holder.pos ); }
    };
    OnLongClickListener edit10 = new OnLongClickListener() {
        public boolean onLongClick( View v ) { mParent.highlightBlock( holder.blk ); return true; }
    };
    OnClickListener edit2 = new OnClickListener() {
        public void onClick( View v ) { mParent.onBlockLongClick( holder.blk ); }
    };
    tvFrom.setOnClickListener( toggle );
    tvTo.setOnClickListener( toggle );
    tvLength.setOnClickListener( edit1 );
    tvCompass.setOnClickListener( edit1 );
    tvLength.setOnLongClickListener( edit10 );
    tvCompass.setOnLongClickListener( edit10 );
    tvClino.setOnClickListener( edit2 );
    tvNote.setOnClickListener( edit2 );

    if ( TDSetting.mLevelOverBasic ) {
      tvFrom.setOnLongClickListener( this );
      tvTo.setOnLongClickListener( this );
    }

    int col = blk.color();
    int text_size = TDSetting.mTextSize;

    if ( tvFrom.getTextSize() != text_size ) {
      tvId.setTextSize(      text_size );
      tvFrom.setTextSize(    text_size );
      tvTo.setTextSize(      text_size );
      tvLength.setTextSize(  text_size );
      tvCompass.setTextSize( text_size );
      tvClino.setTextSize(   text_size );
      tvNote.setTextSize(    text_size );
    }

    if ( show_ids ) {
      tvId.setVisibility( View.VISIBLE );
      tvId.setTextColor( 0xff6666cc ); // light-blue
    } else {
      tvId.setVisibility( View.GONE );
    }
    if ( blk.mType == DistoXDBlock.BLOCK_MAIN_LEG ) {
      tvFrom.setTextColor( ( mParent.isCurrentStationName( blk.mFrom) )? 0xff00ff00 : col );
      tvTo.setTextColor(   ( mParent.isCurrentStationName( blk.mTo )  )? 0xff00ff00 : col );
    } else {
      tvFrom.setTextColor( col );
      tvTo.setTextColor(   col );
    }
    tvLength.setTextColor(  col );
    tvCompass.setTextColor( col );
    tvClino.setTextColor(   col );
    tvNote.setTextColor(    col );

    if ( blk.isRecent( mParent.secondLastShotId() ) ) {
      tvFrom.setBackgroundColor( 0xff000033 ); // dark-blue
      tvTo.setBackgroundColor( 0xff000033 ); // dark-blue
    } 
    if ( ! blk.isAcceptable( ) ) {
      tvLength.setBackgroundColor( 0xff330000 ); // dark-red
      tvCompass.setBackgroundColor( 0xff330000 ); // dark-red
      tvClino.setBackgroundColor( 0xff330000 ); // dark-red
    } else {
      tvLength.setBackgroundColor( 0xff000000 ); // black
      tvCompass.setBackgroundColor( 0xff000000 ); // black
      tvClino.setBackgroundColor( 0xff000000 ); // black
    }
  }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
  // called by ShotWindow::updateShot()
  //
  DistoXDBlock updateBlockView( long blk_id ) 
  {
    for ( DistoXDBlock b : mItems ) {
      if ( b.mId == blk_id ) { // use block id instead of block itself
        View v = b.mView;
        if ( v != null ) {
          ViewHolder holder = (ViewHolder) v.getTag();
          if ( holder != null ) setViewText( holder, b );
          v.setVisibility( b.mVisible );
          v.invalidate();
        }
        return b;
      }
    }
    return null;
  }

  private void updateBlocksName( )
  {
    for ( DistoXDBlock b : mItems ) {
      if ( b.mType == DistoXDBlock.BLOCK_MAIN_LEG ) {
        View v = b.mView;
        if ( v != null ) {
          // ViewHolder holder = (ViewHolder) v.getTag();
          // if ( holder != null ) {
          //   holder.tvFrom.setTextColor( b.color() );
          //   holder.tvTo.setTextColor( b.color() );
          //   if ( mParent.isCurrentStationName( b.mFrom ) ) {
          //     holder.tvFrom.setTextColor( 0xff00ff00 );
          //   } else if ( mParent.isCurrentStationName( b.mTo ) ) {
          //     holder.tvTo.setTextColor( 0xff00ff00 );
          //   }
          // }
          TextView tvFrom = (TextView) v.findViewById( R.id.from );
          TextView tvTo   = (TextView) v.findViewById( R.id.to );
          tvFrom.setTextColor( b.color() );
          tvTo.setTextColor( b.color() );
          if ( mParent.isCurrentStationName( b.mFrom ) ) {
            tvFrom.setTextColor( 0xff00ff00 );
          } else if ( mParent.isCurrentStationName( b.mTo ) ) {
            tvTo.setTextColor( 0xff00ff00 );
          }
          v.setVisibility( b.mVisible );
          v.invalidate();
        }
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
    if ( TDSetting.mLevelOverNormal ) {
      TextView tv = (TextView) view;
      if ( tv != null ) {
        String st = tv.getText().toString();
        mParent.setCurrentStationName( st );
        updateBlocksName();
      }
      return true;
    }
    return false;
  }

}

