/* @file StationAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for survey stations
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
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
// import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;

import android.text.TextWatcher;
import android.text.Editable;

// import androidx.annotation.RecentlyNonNull;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Locale;

class StationAdapter extends ArrayAdapter< StationMap >
{
  private static final int START = 0;
  // private Context mContext;
  private final SurveyWindow mParent;
  private ArrayList< StationMap > mStation;
  private final LayoutInflater mLayoutInflater;

  /** cstr
   * @param ctx     context
   * @param parent  parent window
   * @param id      resource id
   * @param items   set of station name maps
   */
  StationAdapter( Context ctx, SurveyWindow parent, int id, ArrayList< StationMap > items )
  {
    super( ctx, id );
    // TDLog.v( "Station Adapter cstr");
    // mContext = ctx;
    mParent  = parent;
    mStation = items;
    addAll( items );

    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }



  /** @return a copy of the list of the items in the adapter
   */
  List< StationMap > getItems() { return mStation; }

  /** @return the station-map at the given position
   * @param pos   station position
   */
  public StationMap get( int pos ) 
  { 
    return ( pos < START || pos >= getCount() )? null : (StationMap)( getItem( pos ) );
  }

  private class ViewHolder implements TextWatcher
                           // , TextView.OnEditorActionListener
                           // , OnClickListener
                           // , OnLongClickListener
  { 
    int      pos;    // item position
    TextView tvFrom;
    EditText tvTo;
    StationMap mStation;  

    ViewHolder( TextView from, EditText to )
    {
      pos       = 0;
      tvFrom    = from;
      tvTo      = to;
      mStation  = null; // REVISE_RECENT
      // tvTo.setOnEditorActionListener( this );
      tvTo.addTextChangedListener( this );
    }

    @Override
    public void afterTextChanged( Editable s ) {
      // TODO Auto-generated method stub
      // CharSequence cs = convert( tvTo.getText().toString() );
      if ( mStation != null ) mStation.mTo = tvTo.getText().toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      // TODO Auto-generated method stub
    }

    /** fill the text-views with the data of a block
     * @param b    station name-map
     */
    void setViewData( StationMap b )
    {
      if ( b == null ) return;
      tvFrom.setText( b.mFrom );
      tvTo.setText( b.mTo );
      mStation = b;
    }

    // @Override
    // public boolean onEditorAction( TextView v, int action, KeyEvent event )
    // {
    //   TDLog.v("on editor action " + action + " text " + v.getText() ); // + " event key code " + event.getKeyCode() );
    //   if ( pos < START ) return false;
    //   if ( mStation != null ) {
    //     if ( ( event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) || action == EditorInfo.IME_ACTION_DONE ) {
    //       mStation.mTo = tvTo.getText().toString();
    //       InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //       imm.hideSoftInputFromWindow( v.getWindowToken(), 0);
    //     } else {
    //       if ( v == tvFrom ) {
    //         // tvFrom.setText( mStation.mFrom );
    //       } else if ( v == tvTo ) {
    //         // tvTo.setText( mStation.mTo );
    //       }
    //     }
    //   }
    //   return true; // action consumed
    // }
    // };


  } // ViewHolder

  /** @return the view for a position
   * @param pos         position
   * @param convertView convert-view, or null
   * @param parent      parent view-group
   * 
   * @note getView is repeatedly called to layout the list of shots
   */
  // @RecentlyNonNull
  @SuppressLint("WrongConstant")
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    // TDLog.v("get view at pos " + pos );
    StationMap b = (StationMap)(getItem( pos ));

    ViewHolder holder; // = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.two_columns, parent, false );
      holder = new ViewHolder( 
        (TextView)convertView.findViewById( R.id.from ),
        (EditText)convertView.findViewById( R.id.to ) );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.pos = pos;
    holder.setViewData( b );

    // if ( b != null ) { 
    //   b.setView( convertView ); // TODO the next statement could be conditioned to this returning true
    //   holder.setViewText( b, this );
    //   convertView.setVisibility( b.getVisible() );
    // }
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

  void setTargetNames()
  {
  }

}

