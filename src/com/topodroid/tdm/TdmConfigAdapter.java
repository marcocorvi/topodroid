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

import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.R;

import java.util.ArrayList;
import java.io.File;

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

  public TdmConfigAdapter( Context ctx, int id, ArrayList< TdmConfig > items, OnClickListener onClick )
  {
    super( ctx, id, items );
    mContext = ctx;
    mItems = items;
    mOnClick = onClick;
    // TDLog.v( "TdmConfigAdapter nr. items " + items.size() );
  }

  public TdmConfig get( int pos ) { return mItems.get(pos); }

  public TdmConfig get( String survey ) 
  {
    // TDLog.v("TdmConfig get survey >" + survey + "< size " + mItems.size() );
    if ( survey == null || survey.length() == 0 ) return null;
    for ( TdmConfig tdconfig : mItems ) {
      // TDLog.v("TdmConfig item >" + tdconfig.mName + "<" );
      if ( tdconfig.getSurveyName().equals( survey ) ) return tdconfig;
    }
    return null;
  }

  boolean deleteTdmConfig( String filepath )
  {
    File file = new File( filepath );
    boolean ret = file.delete();
    for ( TdmConfig tdconfig : mItems ) {
      if ( tdconfig.getFilepath().equals( filepath ) ) {
        mItems.remove( tdconfig );
        break;
      }
    }
    return ret;
  }

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

  public int size()
  {
    return mItems.size();
  }

}
