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
 * CHANGES
 * 20131118 background color for non-acceptable shots 
 * 20140515 added parent
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
                          implements OnClickListener, OnLongClickListener
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
  ArrayList< DistoXDBlock > getSplaysAtId( long id )
  {
    ArrayList< DistoXDBlock > ret = new ArrayList< DistoXDBlock >();
    for ( int k = 0; k < mItems.size(); ++k ) {
      DistoXDBlock b = mItems.get( k );
      if ( b.mId == id ) {
        int k1 = k-1;
        while ( k1 > 0 && mItems.get( k1 ).mType == DistoXDBlock.BLOCK_SPLAY ) --k1;
        int k2 = k;
        while ( k2 < mItems.size() && mItems.get( k2 ).mType == DistoXDBlock.BLOCK_SPLAY ) ++k2;
        for ( ++k1; k1<k2; ++k1 ) ret.add( mItems.get(k1) );
        break;
      }
    }
    return ret;
  }

  void addBlock( DistoXDBlock blk ) 
  {
    mItems.add( blk );
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

  public DistoXDBlock get( int pos ) { return mItems.get(pos); }
 
  private class ViewHolder
  { 
    TextView tvId;
    TextView tvFrom;
    TextView tvTo;
    TextView tvLength;
    TextView tvCompass;
    TextView tvClino;
    TextView tvNote;
    TextView textView;
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.dblock_row, null );
      holder = new ViewHolder();
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
    DistoXDBlock b = mItems.get( pos );
    setViewText( holder, b );
    b.mView = convertView;
    convertView.setVisibility( b.mVisible );
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  public int size() { return mItems.size(); }

  private void setViewText( ViewHolder holder, DistoXDBlock b ) 
  {
    holder.tvId.setText( String.format( "%1$d", b.mId ) );

    holder.tvFrom.setText( b.mFrom );
    holder.tvTo.setText( b.mTo );

    holder.tvLength.setText(  String.format(Locale.ENGLISH, "%1$.2f", b.mLength * TopoDroidSetting.mUnitLength ) );
    holder.tvCompass.setText( String.format(Locale.ENGLISH, "%1$.1f", b.mBearing * TopoDroidSetting.mUnitAngle ) );
    holder.tvClino.setText(   String.format(Locale.ENGLISH, "%1$.1f", b.mClino * TopoDroidSetting.mUnitAngle ) );
    holder.tvNote.setText( b.toNote() );

    holder.tvFrom.setOnClickListener( this );
    holder.tvTo.setOnClickListener( this );
    if ( TopoDroidSetting.mLevelOverBasic ) {
      holder.tvFrom.setOnLongClickListener( this );
      holder.tvTo.setOnLongClickListener( this );
    }

    if ( holder.tvFrom.getTextSize() != TopoDroidSetting.mTextSize ) {
      holder.tvId.setTextSize( TopoDroidSetting.mTextSize );
      holder.tvFrom.setTextSize( TopoDroidSetting.mTextSize );
      holder.tvTo.setTextSize( TopoDroidSetting.mTextSize );
      holder.tvLength.setTextSize( TopoDroidSetting.mTextSize );
      holder.tvCompass.setTextSize( TopoDroidSetting.mTextSize );
      holder.tvClino.setTextSize( TopoDroidSetting.mTextSize );
      holder.tvNote.setTextSize( TopoDroidSetting.mTextSize );
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
  void updateBlockView( DistoXDBlock blk ) 
  {
    for ( int k=0; k<mItems.size(); ++k ) {
      if ( mItems.get(k) == blk) {
        View v = blk.mView;
        if ( v != null ) {
          ViewHolder holder = (ViewHolder) v.getTag();
          if ( holder != null ) setViewText( holder, blk );
          v.setVisibility( blk.mVisible );
        }
        return;
      }
    }
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
    TextView tv = (TextView) view;
    if ( tv != null ) {
      String st = tv.getText().toString();
      mParent.setCurrentStationName( st );
    }
    return true;
  }

}

