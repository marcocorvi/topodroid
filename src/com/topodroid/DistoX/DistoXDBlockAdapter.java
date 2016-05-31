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
                          implements OnClickListener
                                   , OnLongClickListener
{
  private Context mContext;
  private ShotActivity mParent;
  ArrayList< DistoXDBlock > mItems;
  boolean show_ids;  //!< whether to show data ids
  private LayoutInflater mLayoutInflater;

  public DistoXDBlockAdapter( Context ctx, ShotActivity parent, int id, ArrayList< DistoXDBlock > items )
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

  // called only by ShotActivity updateBlockList( blk )
  // this method changes the ArrayList of DistoxDBlock's
  //
  void addDataBlock( DistoXDBlock blk ) 
  {
    mItems.add( blk );
    // notifyDataSetChanged();
  }

  // called by ShotActivity::updateShotlist
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
    TextView tvId;
    TextView tvFrom;
    TextView tvTo;
    TextView tvLength;
    TextView tvCompass;
    TextView tvClino;
    TextView tvNote;
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    DistoXDBlock b = mItems.get( pos );
    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.dblock_row, null );
      holder = new ViewHolder();
      holder.blk       = b;
      holder.pos       = pos;
      holder.tvId      = (TextView) convertView.findViewById( R.id.id );
      holder.tvFrom    = (TextView) convertView.findViewById( R.id.from );
      holder.tvTo      = (TextView) convertView.findViewById( R.id.to );
      holder.tvLength  = (TextView) convertView.findViewById( R.id.length );
      holder.tvCompass = (TextView) convertView.findViewById( R.id.compass );
      holder.tvClino   = (TextView) convertView.findViewById( R.id.clino );
      holder.tvNote    = (TextView) convertView.findViewById( R.id.note );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    setViewText( holder, b );
    b.mView = convertView;
    convertView.setVisibility( b.mVisible );
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  public int size() { return mItems.size(); }

  private void setViewText( final ViewHolder holder, DistoXDBlock b ) 
  {
    holder.tvId.setText( String.format( "%1$d", b.mId ) );

    holder.tvFrom.setText( b.mFrom );
    holder.tvTo.setText( b.mTo );

    holder.tvLength.setText(  String.format(Locale.US, "%1$.2f", b.mLength * TDSetting.mUnitLength ) );
    holder.tvCompass.setText( String.format(Locale.US, "%1$.1f", b.mBearing * TDSetting.mUnitAngle ) );
    holder.tvClino.setText(   String.format(Locale.US, "%1$.1f", b.mClino * TDSetting.mUnitAngle ) );
    holder.tvNote.setText( b.toNote() );

    holder.tvFrom.setOnClickListener( this );
    holder.tvTo.setOnClickListener( this );

    holder.tvLength.setOnClickListener(
      new OnClickListener() {
        public void onClick( View v ) { mParent.onBlockClick( holder.blk, holder.pos ); }
      }
    );
    holder.tvCompass.setOnClickListener(
      new OnClickListener() {
        public void onClick( View v ) { mParent.onBlockClick( holder.blk, holder.pos ); }
      }
    );
    holder.tvClino.setOnClickListener(
      new OnClickListener() {
        public void onClick( View v ) { mParent.onBlockLongClick( holder.blk ); }
      }
    );

    if ( TDSetting.mLevelOverBasic ) {
      holder.tvFrom.setOnLongClickListener( this );
      holder.tvTo.setOnLongClickListener( this );
    }

    if ( holder.tvFrom.getTextSize() != TDSetting.mTextSize ) {
      holder.tvId.setTextSize( TDSetting.mTextSize );
      holder.tvFrom.setTextSize( TDSetting.mTextSize );
      holder.tvTo.setTextSize( TDSetting.mTextSize );
      holder.tvLength.setTextSize( TDSetting.mTextSize );
      holder.tvCompass.setTextSize( TDSetting.mTextSize );
      holder.tvClino.setTextSize( TDSetting.mTextSize );
      holder.tvNote.setTextSize( TDSetting.mTextSize );
    }

    if ( show_ids ) {
      holder.tvId.setVisibility( View.VISIBLE );
      holder.tvId.setTextColor( 0xff6666cc ); // light-blue
    } else {
      holder.tvId.setVisibility( View.GONE );
    }
    holder.tvFrom.setTextColor( b.color() );
    holder.tvTo.setTextColor( b.color() );
    if ( b.mType == DistoXDBlock.BLOCK_MAIN_LEG ) {
      if ( mParent.isCurrentStationName( b.mFrom ) ) {
        holder.tvFrom.setTextColor( 0xff00ff00 );
      } else if ( mParent.isCurrentStationName( b.mTo ) ) {
        holder.tvTo.setTextColor( 0xff00ff00 );
      }
    }
    holder.tvLength.setTextColor( b.color() );
    holder.tvCompass.setTextColor( b.color() );
    holder.tvClino.setTextColor( b.color() );
    holder.tvNote.setTextColor( b.color() );

    if ( b.isRecent( mParent.secondLastShotId() ) ) {
      holder.tvFrom.setBackgroundColor( 0xff000033 ); // dark-blue
      holder.tvTo.setBackgroundColor( 0xff000033 ); // dark-blue
    } 
    if ( ! b.isAcceptable( ) ) {
      holder.tvLength.setBackgroundColor( 0xff330000 ); // dark-red
      holder.tvCompass.setBackgroundColor( 0xff330000 ); // dark-red
      holder.tvClino.setBackgroundColor( 0xff330000 ); // dark-red
    } else {
      holder.tvLength.setBackgroundColor( 0xff000000 ); // black
      holder.tvCompass.setBackgroundColor( 0xff000000 ); // black
      holder.tvClino.setBackgroundColor( 0xff000000 ); // black
    }
  }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
  // called by ShotActivity::updateShot()
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


  public void onClick(View view)
  {
    TextView tv = (TextView) view;
    if ( tv != null ) {
      String st = tv.getText().toString();
      mParent.recomputeItems( st );
    }
  }

  public boolean onLongClick( View view ) 
  {
    if ( TDSetting.mLevelOverNormal ) {
      TextView tv = (TextView) view;
      if ( tv != null ) {
        String st = tv.getText().toString();
        mParent.setCurrentStationName( st );
      }
      return true;
    }
    return false;
  }

}

