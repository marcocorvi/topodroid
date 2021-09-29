/* @file SurveyAdapter.java
 *
 * @author marco corvi
 * @date jul 2020
 *
 * @brief Cave3D adapter for surveys
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.List;

class SurveyAdapter extends ArrayAdapter< Cave3DSurvey >
{
  private Context mContext;
  private final TopoGL mParent;
  List< Cave3DSurvey > mItems;
  private final LayoutInflater mLayoutInflater;

  SurveyAdapter( Context ctx, TopoGL parent, int res_id, List< Cave3DSurvey > items )
  {
    super( ctx, res_id, items );
    mContext = ctx;
    mParent  = parent;
    mItems   = items;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  // return the sketch at the given position
  public Cave3DSurvey get( int pos ) 
  { 
    return ( pos < 0 || pos >= mItems.size() )? null : mItems.get(pos);
  }

  private class ViewHolder implements OnClickListener
  { 
    int      pos;
    Cave3DSurvey mSurvey;   // used to make sure blocks do not hold ref to a view, that does not belog to them REVISE_RECENT
    CheckBox cbShow;

    ViewHolder( CheckBox show )
    {
      pos      = 0;
      mSurvey  = null; 
      cbShow   = show;
      cbShow.setOnClickListener( this );
    }

    @Override
    public void onClick( View v )
    {
      if ( (CheckBox)v == cbShow ) {
        mSurvey.visible = cbShow.isChecked();
      }
    }

    void setSurvey( Cave3DSurvey b, int p )
    {
      // TDLog.v("holder set survey " + b.mName + " pos " + p );
      mSurvey = b;
      pos     = p;
      cbShow.setChecked( b.visible );
      cbShow.setText( b.name );
    }
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    // TDLog.v("sketch adapter get view " + pos );
    Cave3DSurvey b = mItems.get( pos );
    ViewHolder holder = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.survey_row, parent, false );
      holder = new ViewHolder( 
        (CheckBox)convertView.findViewById( R.id.cb_survey )
      );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.setSurvey( b, pos );
    // b.mView = convertView;
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  public int size() { return mItems.size(); }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }

  public boolean addSurvey( Cave3DSurvey survey ) 
  { 
    if ( survey == null || survey.name == null ) return false;
    for ( Cave3DSurvey item : mItems ) if ( item.name.equals( survey.name ) ) return false;
    mItems.add( survey );
    return true;
  }
 
}

