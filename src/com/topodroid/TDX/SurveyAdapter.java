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
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;

import com.topodroid.ui.MyColorPicker;
import com.topodroid.ui.MyButton;

import android.content.Context;

import android.widget.ArrayAdapter;
// import android.widget.CheckBox;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.graphics.drawable.BitmapDrawable;

import java.util.List;

class SurveyAdapter extends ArrayAdapter< Cave3DSurvey >
{
  // TODO button size = TopoDroidApp.widthPixels / 40 instead of 27
  static BitmapDrawable mEyeOn  = null;
  static BitmapDrawable mEyeOff = null;

  private Context mContext;
  private final TopoGL mParent;
  List< Cave3DSurvey > mItems;
  private final LayoutInflater mLayoutInflater;

  SurveyAdapter( Context ctx, TopoGL parent, int res_id, List< Cave3DSurvey > items )
  {
    super( ctx, res_id, items );
    if ( mEyeOn == null ) {
      mEyeOn  = MyButton.getButtonBackground( TDInstance.context, (int)(TopoDroidApp.mDisplayWidth/40), R.drawable.eye_on );
      mEyeOff = MyButton.getButtonBackground( TDInstance.context, (int)(TopoDroidApp.mDisplayWidth/40), R.drawable.eye_off );
    }
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
                                  , MyColorPicker.IColorChanged
  { 
    int      pos;
    Cave3DSurvey mSurvey;   // used to make sure blocks do not hold ref to a view, that does not belong to them REVISE_RECENT
    Button   cbShow;
    Button   colorBtn;
    TextView cbText;

    ViewHolder( Button show, Button color, TextView text )
    {
      pos      = 0;
      mSurvey  = null; 
      cbShow   = show;
      cbShow.setOnClickListener( this );
      colorBtn = color;
      cbText   = text;
    }

    // IColorChanged
    public void colorChanged( int color )
    {
      if ( mSurvey != null ) mSurvey.setTmpColor( color );
      colorBtn.setBackgroundColor( color );
    }

    private void setCbShow( boolean checked )
    {
      cbShow.setBackground( (checked ? mEyeOn : mEyeOff) );
    }

    @Override
    public void onClick( View v )
    {
      if ( mSurvey == null ) return;
      if ( v.getId() == R.id.cb_show ) {
        setCbShow( mSurvey.switchVisible() );
      } else if ( v.getId() == R.id.btn_color ){
        (new MyColorPicker( mContext, this, mSurvey.getTmpColor() )).show();
      }
    }

    void setSurvey( Cave3DSurvey b, int p )
    {
      // TDLog.v("holder set survey " + b.mName + " pos " + p );
      mSurvey = b;
      pos     = p;
      setCbShow( b.visible );
      cbText.setText( b.name );
      colorBtn.setBackgroundColor( b.getTmpColor() );
    }
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    // TDLog.v("sketch adapter get view " + pos );
    Cave3DSurvey b = mItems.get( pos );
    ViewHolder holder = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.survey_color_row, parent, false );
      holder = new ViewHolder( 
        (Button)convertView.findViewById( R.id.cb_show ),
        (Button)convertView.findViewById( R.id.btn_color ),
        (TextView)convertView.findViewById( R.id.cb_text )
      );
      // holder.colorBtn = (Button) convertView.findViewById( R.id.btn_color );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.setSurvey( b, pos );
    // holder.colorBtn.setBackgroundColor( b.getTmpColor() );
    holder.colorBtn.setOnClickListener( holder );
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

