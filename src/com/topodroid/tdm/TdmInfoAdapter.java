/* @file TdmInfoAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for TDM survey info
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
// import com.topodroid.utils.TDString;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.FixedInfo;
import com.topodroid.TDX.R;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;
// import android.widget.TextView.OnEditorActionListener;
import android.widget.AdapterView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

class TdmInfoAdapter extends ArrayAdapter< FixedInfo >
{
  private TopoDroidApp mApp;
  boolean show_ids;  //!< whether to show data ids
  private final LayoutInflater mLayoutInflater;

  /** cstr
   * @param ctx     context
   * @param id      ???
   * @param items   array list of surveys
   */
  TdmInfoAdapter( Context ctx, TopoDroidApp app, int id, ArrayList< TdmInput > items )
  {
    super( ctx, id );
    mApp     = app;
    for ( TdmInput item : items ) {
      TDLog.v("TDM INFO survey " + item.getSurveyName() );
      FixedInfo info = TopoDroidApp.mData.selectSurveyFixed( item.getSurveyName() );
      if ( info != null ) {
        info.comment = item.getSurveyName();
        add( info );
      }
    }
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    TDLog.v("INFO surveys " + items.size() + " infos " + getCount() );
  }

  /** @return a copy of the list of the items in the adapter
   */
  List< FixedInfo > getItems() 
  {
    ArrayList< FixedInfo > ret = new ArrayList<>();
    int size = getCount();
    for ( int k=0; k<size; ++k ) ret.add( (FixedInfo) getItem(k) );
    return ret;
  }

  /** @return the survey name at the given position
   * @param pos   survey position
   */
  public FixedInfo get( int pos ) 
  { 
    return ( pos < 0 || pos >= getCount() )? null : (FixedInfo)( getItem( pos ) );
  }

  private static class ViewHolder
  { 
    int      pos;
    TextView tvName;
    TextView tvStation;
    TextView tvWGS84;
    LinearLayout llCS;
    TextView tvCS;
    TextView tvCSdata;

    ViewHolder( TextView name, TextView station, TextView wgs84, LinearLayout ll_cs, TextView cs, TextView cs_data )
    {
      pos       = 0;
      tvName    = name;
      tvStation = station;
      tvWGS84   = wgs84;
      llCS      = ll_cs;
      tvCS      = cs;
      tvCSdata  = cs_data;
      // tvWGS84.setWidth( (int)(TopoDroidApp.mDisplayWidth) );
      // tvCSdata.setWidth( (int)(TopoDroidApp.mDisplayWidth) );
      // mSurvey      = null; 
    }

    /** fill the textviews with the data of a survey
     * @param info     survey info
     * @param adapter  not used
     */
    void setViewText( FixedInfo info, TdmInfoAdapter adapter )
    {
      if ( info == null ) return;
      tvName.setText( info.comment );
      if ( info != null ) { 
        tvStation.setText( info.name + ":" );
        // tvWGS84.setText(  String.format(Locale.US, "%.8f %.8f (%.0f) %.0f", info.lat, info.lng, info.h_ell, info.h_geo ) );
        tvWGS84.setText(  String.format(Locale.US, "%.8f %.8f %.0f", info.lat, info.lng, info.h_geo ) );
        if ( info.cs == null || info.cs.length() == 0 ) {
          llCS.setVisibility( View.GONE );
        } else {
          llCS.setVisibility( View.VISIBLE );
          tvCS.setText( info.cs + ":" );
          tvCSdata.setText(  String.format(Locale.US, "%.0f %.0f %.0f", info.cs_lat, info.cs_lng, info.cs_h_geo ) );
        }
      }

      int text_size = TDSetting.mTextSize;
      int text_size75 = (int)(  text_size*0.75f );
      if ( tvStation.getTextSize() != text_size ) {
        tvName.setTextSize(    text_size );
        tvStation.setTextSize( text_size );
        tvWGS84.setTextSize(   text_size75 );
        tvCS.setTextSize(      text_size75 );
        tvCSdata.setTextSize(  text_size75 );
        tvName.setTextColor( TDColor.LIGHT_BLUE );
        tvStation.setTextColor( TDColor.BLUE );
      }
    }

  } // ViewHolder

  /** @return the view for a position
   * @param pos         position
   * @param convertView convert-view, or null
   * @param parent      parent view-group
   */
  // @RecentlyNonNull
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    // TDLog.v( "get view at " + pos );
    FixedInfo survey = (FixedInfo)(getItem( pos ));

    ViewHolder holder; // = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.tdinfo_adapter, parent, false );
      holder = new ViewHolder( 
        (TextView)convertView.findViewById( R.id.name ),
        (TextView)convertView.findViewById( R.id.station ),
        (TextView)convertView.findViewById( R.id.wgs84 ),
        (LinearLayout)convertView.findViewById( R.id.ll_cs ),
        (TextView)convertView.findViewById( R.id.cs ),
        (TextView)convertView.findViewById( R.id.cs_data ) );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.pos = pos;
    // holder.mSurvey = survey;

    if ( survey != null ) {
      holder.setViewText( survey, this );
    }
    return convertView;
  }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
}

