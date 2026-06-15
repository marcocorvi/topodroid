/* @file SurveysAdapter.java
 *
 * @author marco corvi
 * @date may 2024 
 *
 * @brief TopoDroid adapter of survey list for survey bulk ops
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDColor;
import com.topodroid.util.TDString;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.SurveysDialog.SurveyChoice;

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

class SurveysAdapter extends ArrayAdapter< SurveyChoice >
{
  private final LayoutInflater mLayoutInflater;
  private List< SurveyChoice > mSurveys;

  /** cstr
   * @param ctx     context
   * @param id      ???
   * @param items   array list of data-blocks
   */
  SurveysAdapter( Context ctx, int id, List< SurveyChoice > items )
  {
    super( ctx, id );
    mSurveys = items;
    addAll( items );
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  /** @return the list of the items in the adapter
   */
  List< SurveyChoice > getItems() { return mSurveys; }


  /** @return the block at the given position
   * @param pos   block position
   */
  public SurveyChoice get( int pos ) 
  { 
    return ( pos < 0 || pos >= getCount() )? null : (SurveyChoice)( getItem( pos ) );
  }

  private class ViewHolder implements OnClickListener
  { 
    int      pos;
    CheckBox cbSelected;
    TextView tvDesc;
    SurveyChoice  mSurvey;   // used to make sure blocks do not hold ref to a view, that does not belong to them REVISE_RECENT

    ViewHolder( CheckBox cb, TextView desc )
    {
      pos        = 0;
      cbSelected = cb;
      tvDesc     = desc;
      mSurvey    = null; // REVISE_RECENT
      cb.setOnClickListener( this );
    }

    public void onClick( View v ) 
    {
      if ( v instanceof CheckBox ) {
        if ( mSurvey != null ) {
          mSurvey.setSelected( cbSelected.isChecked() );
        }
      }
    }

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
    SurveyChoice survey = (SurveyChoice)(getItem( pos ));

    ViewHolder holder; // = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.select_text_row, parent, false );
      holder = new ViewHolder( 
        (CheckBox)convertView.findViewById( R.id.selected ),
        (TextView)convertView.findViewById( R.id.desc ) );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.pos     = pos;
    if ( holder.mSurvey != null ) holder.mSurvey.setView( null );
    holder.mSurvey = survey;

    if ( survey != null ) {
      holder.cbSelected.setChecked( survey.isSelected() );
      holder.tvDesc.setText( survey.getName() );
      survey.setView( holder.cbSelected );
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

  /** @return the list of selected surveys
   */
  public List< String > getSelectedSurveys()
  {
    ArrayList< String > ret = new ArrayList<>();
    for ( SurveyChoice survey : mSurveys ) {
      if ( survey.isSelected() ) ret.add( survey.getName() );
    }
    return ret;
  }

  void toggleSelected()
  {
    for ( SurveyChoice survey : mSurveys ) {
      survey.toggleSelected();
    }
  }

}

