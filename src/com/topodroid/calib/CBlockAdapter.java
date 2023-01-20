/* @file CBlockAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for calibration data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.GMActivity;
import com.topodroid.TDX.R;

import android.content.Context;

import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.Paint;
import android.graphics.Rect;

// import android.support.annotation.NonNull;

import java.util.ArrayList;
// import java.util.LinkedList;

public class CBlockAdapter extends ArrayAdapter< CBlock >
{
  private final GMActivity mParent;
  private ArrayList< CBlock > items;  // list if calibration data
  private ArrayList<CBlock> searchResult = null;
  // private final Context context;
  private final LayoutInflater mLayoutInflater;

  /** cstr
   * @param ctx    context
   * @param id     resource layout of the array entries
   * @param items  array items
   */
  public CBlockAdapter( Context ctx, GMActivity parent, int id, ArrayList< CBlock > items )
  {
    super( ctx, id, items );
    mParent = parent;
    // this.context = ctx;
    this.items = items;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    searchResult = new ArrayList<CBlock>();
  }

  // @return the CBlock item at the given position in the array
  public CBlock get( int pos ) { return items.get(pos); }


  /** handle a user tap on the data values
   * @param pos   item position
   * @param cblk  data block
   */
  public void dataClick( int pos, CBlock cblk )
  {
    // TDLog.v( "onClick " + v.getId() );
    mParent.itemDataClick( pos, cblk );
  }
  
  /** update the display view of a block
   * @param cblk   calib block
   */
  public void updateView( CBlock cblk )
  {
    View v = cblk.getView();
    if ( v == null ) {
      // TDLog.v("UPDATE view null view - id " + cblk.mId );
      return;
    }
    ViewHolder holder = (ViewHolder)v.getTag();
    // assert( holder.mBlock.mId == cblk.mId );
    // TDLog.v("UPDATE set view holder block ");
    holder.setViewBlock( cblk );
  }

  class ViewHolder implements OnClickListener
  {
    CBlockAdapter mParent; // could be the GMActivity
    int pos;
    CBlock mBlock = null;
    View   mView  = null;
    TextView tvId;
    TextView tvGroup;
    TextView tvError;
    TextView tvData;

    ViewHolder( CBlockAdapter parent, View v, TextView id, TextView grp, TextView error, TextView data )
    {
      mParent = parent;
      pos     = 0;
      mView   = v;
      tvId    = id;
      tvGroup = grp;
      tvError = error;
      tvData  = data;
      mBlock  = null;
      tvData.setOnClickListener( this );
    }

    /** handle a user tap - forward to the CBlock adapter
     * @param v tapped view
     */
    public void onClick( View v ) 
    {
      if ( (TextView)v == tvData ) {
        mParent.dataClick( pos, mBlock );
      } 
    }

    void setViewBlock( CBlock b ) 
    { 
      if ( mBlock != b ) {
        if ( mBlock != null ) mBlock.setView( null );
        mBlock = b;
      }
      if ( b == null ) return;
      mBlock.setView( mView ); 

      String id    = b.idString();
      String group = b.groupString();
      String error = b.errorString();
      String data  = b.dataString();
      tvId.setText(    id );
      tvGroup.setText( group );
      tvError.setText( error );
      tvData.setText(  data );

      int text_size = TDSetting.mTextSize;
      if ( tvGroup.getTextSize() != text_size ) {
        tvId.setTextSize(    text_size * 0.8f );
        tvGroup.setTextSize( text_size );
        tvError.setTextSize( text_size );
        tvData.setTextSize(  text_size * 0.8f );
      }

      tvId.setTextColor( b.color() );
      tvGroup.setTextColor( b.color() );
      tvError.setTextColor( b.color() );
      tvData.setTextColor( b.color() );

      LinearLayout layout = (LinearLayout)mView.findViewById( R.id.cblock_row );
      Paint paint = tvGroup.getPaint();
      Rect bounds = new Rect();
      String text = id + group + error + data;
      paint.getTextBounds( text, 0, text.length(), bounds );
      layout.setMinimumWidth( (int)( bounds.right * 1.2f) ); // 60 = 4 * marginh

      // tw.setWidth( (int)(TopoDroidApp.mDisplayWidth * 1.5) );

      setBgColors( b );
    }

    void setBgColors( CBlock b ) 
    {
      if ( b == null ) return;
      setIdBgColor( );
      setGroupBgColor( );
      setDataBgColor( b );
      setErrorBgColor( searchResult.contains( b ) );
    }

    void setDataBgColor( CBlock b )
    {
      if ( b.isSaturated() ) {      // saturated data
        tvData.setBackgroundColor( TDColor.DARK_BROWN );
      } else if ( b.isGZero() ) {   // G=0 data
        tvData.setBackgroundColor( TDColor.DARK_VIOLET );
      } else if ( b.mStatus != 0 ) {
        tvData.setBackgroundColor( TDColor.DARK_GRAY );
      } else if ( b.mError > TDMath.DEG2RAD ) { // 1 degree
        tvData.setBackgroundColor( TDColor.MID_RED );
      } else if ( b.isFar() || b.isOffGroup() ) {
        tvData.setBackgroundColor( TDColor.DARK_GREEN );
      } else {
        tvData.setBackgroundColor( TDColor.BLACK );
      }
    }

    void setErrorBgColor( boolean highlight )
    {
      if ( highlight ) {
        tvError.setBackgroundColor( TDColor.DARK_ORANGE );
      } else {
        tvError.setBackgroundColor( TDColor.BLACK );
      }
    }

    void setGroupBgColor( )
    {
      tvGroup.setBackgroundColor( TDColor.BLACK );
    }

    void setIdBgColor( )
    {
      tvId.setBackgroundColor( TDColor.BLACK );
    }

  } // ViewHolder

  // @NonNull
  @Override
  public View getView( int pos, View convertView, /* @NonNull */ ViewGroup parent )
  {
    CBlock b = items.get( pos );
    ViewHolder holder;
    View v = convertView;
    if ( v == null ) {
      try { 
        v = mLayoutInflater.inflate( R.layout.cblock_row, parent, false ); // FIXME inflate may produce NullPointerException
      } catch ( NullPointerException e ) {
        TDLog.Error("CBlock adapter inflate view: null pointer");
        return null;
      }
      holder = new ViewHolder( this, v, 
                               (TextView)v.findViewById( R.id.id ),
                               (TextView)v.findViewById( R.id.group ),
                               (TextView)v.findViewById( R.id.error ),
                               (TextView)v.findViewById( R.id.data ) );
      v.setTag( holder );
    } else {
      holder = (ViewHolder)v.getTag();
    }
    holder.pos = pos;
    holder.setViewBlock( b );
    return v;
  }

  /** find the data with error above a given value
   * @param error   minimum error
   * @return ...
   */
  public boolean searchData( float error )
  {
    error *= TDMath.DEG2RAD;
    searchResult.clear();
    for ( int j =0; j < items.size(); ++j  ) {
      CBlock cblj = get(j);
      // TDLog.v("CBlock group " + cblj.mGroup + " error " + cblj.mError );
      if ( cblj.mGroup > 0 && cblj.mError >= error ) {
        int k = 0;
        for ( ; k < searchResult.size(); ++k ) {
          if ( searchResult.get( k ).mError < cblj.mError ) break;
        }
        // TDLog.v("CBlock error " + cblj.mError + " list size " + searchResult.size() + " insert at " + k );
        searchResult.add( k, cblj );
      }
      View v = cblj.getView();
      if ( v != null ) { 
        ((ViewHolder)v.getTag()).setErrorBgColor( searchResult.contains( cblj ) );
      }
    }
    return searchResult.size() > 0;
  }

  public boolean clearSearchResult()
  { 
    if ( searchResult.size() == 0 ) return false;
    for ( CBlock cblk : searchResult ) {
      View v = cblk.getView();
      if ( v != null ) {
        ((ViewHolder)v.getTag()).setErrorBgColor( false );
      }
    }
    searchResult.clear();
    return true;
  }

  // public int getPos( CBlock cblk )
  // { 
  //   for ( int k=0; k<items.size(); ++k ) {
  //     if ( items.get(k) == cblk ) return k;
  //     // if ( items.get(k).mId == cblk.mId ) return k;
  //   }
  //   return -1;
  // }
  
}

