/* @file SketchAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief Cave3D adapter for survey sketches
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.List;
// import java.util.ArrayList;

class SketchAdapter extends ArrayAdapter< GlSketch >
{
  private Context mContext;
  private final TopoGL mParent;
  List< GlSketch > mItems;
  private final LayoutInflater mLayoutInflater;

  /** cstr
   * @param ctx     context
   * @param parent  parent window
   * @param res_id  id of the resource used to present the sketches
   * @param items   list of sketches
   */
  SketchAdapter( Context ctx, TopoGL parent, int res_id, List< GlSketch > items )
  {
    super( ctx, res_id, items );
    mContext = ctx;
    mParent  = parent;
    mItems   = items;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  /** @return the sketch at the given position
   * @param pos   position in the list of sketches
   */
  public GlSketch get( int pos ) 
  { 
    return ( pos < 0 || pos >= mItems.size() )? null : mItems.get(pos);
  }

  /** sketch view holder
   */
  private class ViewHolder implements OnClickListener
  { 
    int      pos;
    GlSketch mSketch;   // used to make sure blocks do not hold ref to a view, that does not belog to them REVISE_RECENT
    TextView tvName;
    CheckBox cbShow;
    CheckBox cbDelete;

    ViewHolder( TextView name, CheckBox show, CheckBox delete )
    {
      pos      = 0;
      mSketch  = null; 
      tvName   = name;
      cbShow   = show;
      cbDelete = delete;
      cbShow.setOnClickListener( this );
      cbDelete.setOnClickListener( this );
    }

    @Override
    public void onClick( View v )
    {
      if ( (CheckBox)v == cbShow ) {
        mSketch.mShow = cbShow.isChecked();
      } else if ( (CheckBox)v == cbDelete ) {
        mSketch.mDelete = cbDelete.isChecked();
      }
    }

    void setSketch( GlSketch b, int p )
    {
      // TDLog.v("TopoGL holder set sketch " + b.mName + " pos " + p );
      mSketch = b;
      pos     = p;
      tvName.setText( b.mName );
      cbShow.setChecked( b.mShow );
      cbDelete.setChecked( b.mDelete );
    }
  }

  /** @return the view used to present a sketch
   * @param pos   sketch position
   * @param convertView convertible view (if not null)
   * @param parent      view-group parent
   */
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    // TDLog.v("TopoGL sketch adapter get view " + pos );
    GlSketch b = mItems.get( pos );
    ViewHolder holder = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.sketch_row, parent, false );
      holder = new ViewHolder( 
        (TextView)convertView.findViewById( R.id.name ),
        (CheckBox)convertView.findViewById( R.id.show ),
        (CheckBox)convertView.findViewById( R.id.delete )
      );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.setSketch( b, pos );
    // b.mView = convertView;
    return convertView;
  }

  /** @return the number of sketches in the list
   */ 
  @Override
  public int getCount() { return mItems.size(); }

  /** @return the number of sketches in the list
   */ 
  public int size() { return mItems.size(); }

  /** @return the type of a sketch view, always IGNORE
   * @param pos   sketch position
   */ 
  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }

  /** add a sketch to the list
   * @param sketch   sketch to add
   * @return true if successful
   * @note it can fail if the sketch is null, or has a null name, or the name is already in the list
   */
  public boolean addSketch( GlSketch sketch ) 
  { 
    if ( sketch == null || sketch.mName == null ) return false;
    for ( GlSketch item : mItems ) if ( item.mName.equals( sketch.mName ) ) return false;
    mItems.add( sketch );
    return true;
  }
 
}

