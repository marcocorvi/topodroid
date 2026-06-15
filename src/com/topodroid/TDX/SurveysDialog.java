/* @file SurveysDialog.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid dialog for surveys wide actons
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDAnalytics;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;


import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.ListView;
import android.widget.CheckBox;
// import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;


class SurveysDialog extends MyDialog
                    implements View.OnClickListener
{
  public class SurveyChoice
  {
    final String name;
    boolean selected;
    CheckBox    view;
    
    public SurveyChoice( String n )
    { 
      name = n;
      selected = false;
      view     = null;
    }

    public void setSelected( boolean select ) { selected = select; }

    public boolean isSelected() { return selected; }

    public String getName() { return name; }

    public void toggleSelected() 
    { 
      selected = ! selected;
      if ( view != null ) {
        view.setChecked( selected );
      }
    }

    public void setView( CheckBox cb ) { view = cb; }

  }

  private final MainWindow mParent;
  private SurveysAdapter mAdapter = null;
  private List< SurveyChoice > mSurveys;

  /** cstr
   * @param context  context
   * @param coder    geomorphology coder
   * @param geocode     current geocode (in the coder)
   */
  SurveysDialog( Context context, MainWindow parent )
  {
    super( context, null, R.string.SurveysDialog ); // null app
    mParent  = parent;
    mSurveys = new ArrayList<>();
    List< String > names = TopoDroidApp.getSurveyNames();
    if ( names != null ) {
      for ( String name : names ) {
        mSurveys.add( new SurveyChoice( name ) );
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.surveys_dialog, R.string.title_surveys );

    ((Button) findViewById(R.id.button_delete)).setOnClickListener( this );
    ((Button) findViewById(R.id.button_export)).setOnClickListener( this );
    ((Button) findViewById(R.id.button_cancel)).setOnClickListener( this );
    ((Button) findViewById(R.id.button_toggle)).setOnClickListener( this );
    ((Button) findViewById(R.id.button_import)).setOnClickListener( this );

    ListView list = (ListView)findViewById(R.id.surveys_list );
    list.setDividerHeight( 2 );

    if ( mSurveys.size() == 0 ) {
      // TDToast
      dismiss();
    }

    mAdapter = new SurveysAdapter( mContext, R.layout.select_text_row, mSurveys );
    list.setAdapter( mAdapter );
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Photo Dialog onClick() " + view.toString() );
    if (view.getId() == R.id.button_toggle ) {
      mAdapter.toggleSelected();
      return;
    }
    dismiss();
    if (view.getId() == R.id.button_delete ) {
      List< String > surveys = mAdapter.getSelectedSurveys();
      if ( surveys.size() > 0 ) {
        mParent.deleteSurveys( surveys );
      }
    } else if (view.getId() == R.id.button_import ) {
      mParent.importSurveys();
    } else if (view.getId() == R.id.button_export ) {
      List< String > surveys = mAdapter.getSelectedSurveys();
      if ( surveys.size() > 0 ) {
        mParent.exportSurveys( surveys );
      }
    // } else if ( view.getId() == R.id.button_cancel ) {
    //   /* nothing */
    }
  }

}
        

