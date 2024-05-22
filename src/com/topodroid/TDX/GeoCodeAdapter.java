/* @file GeoCodeAdapter.java
 *
 * @author marco corvi
 * @date may 2024 
 *
 * @brief TopoDroid adapter for survey geo codes
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
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;

// import androidx.annotation.RecentlyNonNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

class GeoCodeAdapter extends ArrayAdapter< GeoCode >
{
  private final LayoutInflater mLayoutInflater;
  private ArrayList< GeoCode > mGeoCodes;

  /** cstr
   * @param ctx     context
   * @param id      ???
   * @param items   array list of data-blocks
   */
  GeoCodeAdapter( Context ctx, int id, ArrayList< GeoCode > items )
  {
    super( ctx, id );
    // mContext = ctx;
    // mItems   = items;
    mGeoCodes = items;
    addAll( items );
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  /** @return the list of the items in the adapter
   */
  List< GeoCode > getItems() { return mGeoCodes; }


  /** @return the block at the given position
   * @param pos   block position
   */
  public GeoCode get( int pos ) 
  { 
    return ( pos < 0 || pos >= getCount() )? null : (GeoCode)( getItem( pos ) );
  }

  // public DBlock getBlockById( long id ) 
  // {
  //   for ( DBlock b : mItems ) if ( b.mId == id ) return b;
  //   return null;
  // }

  private class ViewHolder implements OnClickListener
  { 
    int      pos;
    CheckBox cbSelected;
    TextView tvDesc;
    GeoCode  mGeoCode;   // used to make sure blocks do not hold ref to a view, that does not belong to them REVISE_RECENT

    ViewHolder( CheckBox cb, TextView desc )
    {
      pos        = 0;
      cbSelected = cb;
      tvDesc     = desc;
      mGeoCode   = null; // REVISE_RECENT
      cb.setOnClickListener( this );
    }

    public void onClick( View v ) 
    {
      if ( v instanceof CheckBox ) {
        if ( mGeoCode != null ) {
          mGeoCode.setSelected( cbSelected.isChecked() );
        }
      }
    }

    // /** set the color of a geocode
    //  * @param b  geocode
    //  * @note this implements the TopoDroid data coloring policy
    //  */
    // void setColor( GeoCode geocode )
    // {
    //   if ( geocode == null ) return;
    //   int col = geocode.getColorByType();
    //   tvDesc.setBackgroundColor( col );
    // }
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
    GeoCode geocode = (GeoCode)(getItem( pos ));

    ViewHolder holder; // = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.geocode_row, parent, false );
      holder = new ViewHolder( 
        (CheckBox)convertView.findViewById( R.id.selected ),
        (TextView)convertView.findViewById( R.id.desc ) );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.pos      = pos;
    holder.mGeoCode = geocode;

    if ( geocode != null ) {
      holder.cbSelected.setChecked( geocode.isSelected() );
      holder.tvDesc.setText( geocode.getDescription() );
      convertView.setBackgroundColor( geocode.getColorByType() );
      // convertView.setVisibility( geocode.getVisible() );
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

  String makeCode()
  {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for ( GeoCode geocode : mGeoCodes ) {
      if ( geocode.isSelected() ) {
        if ( !first ) sb.append(" ");
        sb.append( geocode.getGeoCode() );
        first = false;
      }
    }
    return sb.toString();
  }

}

