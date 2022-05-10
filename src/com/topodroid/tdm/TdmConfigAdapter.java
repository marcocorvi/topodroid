/** @file TdmConfigAdapter.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Manager td-config adapter
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;

class TdmConfigAdapter extends ArrayAdapter< TdmConfig >
{
  private ArrayList< TdmConfig > mItems;
  private Context mContext;
  private OnClickListener mOnClick;

  /** cstr
   * @param ctx     context
   * @param id      resource id
   * @param items   list of configurations
   * @param onClick listener for taps on the items
   */
  public TdmConfigAdapter( Context ctx, int id, ArrayList< TdmConfig > items, OnClickListener onClick )
  {
    super( ctx, id, items );
    mContext = ctx;
    mItems   = items;
    mOnClick = onClick;
    TDLog.v( "TdmConfigAdapter nr. items " + items.size() );
  }

  /** @return the configuration at a given index
   * @param pos   index
   */
  public TdmConfig get( int pos ) 
  { 
    TDLog.v("TdmConfig get item at pos " + pos );
    return mItems.get(pos);
  }

  /** @return the configuration of a survey
   * @param survey  survey name
   */
  public TdmConfig getTdmConfig( String survey ) 
  {
    TDLog.v("TdmConfig get survey >" + survey + "< size " + mItems.size() );
    if ( survey == null || survey.length() == 0 ) return null;
    for ( TdmConfig tdconfig : mItems ) {
      TDLog.v("TdmConfig item >" + tdconfig.getSurveyName() + "<" );
      if ( tdconfig.getSurveyName().equals( survey ) ) return tdconfig;
    }
    return null;
  }

  /** delete a project config file
   * @param filepath  file path
   * @return true if success
   */
  boolean deleteTdmConfig( String filepath )
  {
    boolean ret = TDFile.deleteFile( filepath );
    for ( TdmConfig tdconfig : mItems ) {
      if ( tdconfig.hasFilepath( filepath ) ) {
        mItems.remove( tdconfig );
        break;
      }
    }
    return ret;
  }

  /** @return the view for a configuration 
   * @param pos         configuration index
   * @param convertView convertible (reusable) view
   * @param parent      ...
   */
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, null );
    }

    TdmConfig b = mItems.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      tw.setTextSize( TDSetting.mTextSize );
      // tw.setTextColor( b.color() );
    }
    v.setOnClickListener( mOnClick );
    return v;
  }

  /** @return number of configurations
   */
  public int size()
  {
    return mItems.size();
  }

}
