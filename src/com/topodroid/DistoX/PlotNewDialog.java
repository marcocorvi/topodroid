/* @file PlotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid new-plot dialog
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120521 using INewPlot interface for the maker
 * 20140416 setError for required EditText inputs
 */
package com.topodroid.DistoX;


import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
// import android.text.InputType;
import android.inputmethodservice.KeyboardView;

import android.view.Window;

import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Toast;

// import android.util.Log;

public class PlotNewDialog extends Dialog
                           implements View.OnClickListener
                           , View.OnLongClickListener
{
  private Context mContext;
  private INewPlot mMaker;
  // private boolean notDone;

  private EditText mEditName;
  private EditText mEditStart;
  // private EditText mEditView;

  // private RadioButton mBtnPlan;
  // private RadioButton mBtnExtended;

  // private RadioButton mBtnVSection;
  // private RadioButton mBtnHSection;
  // private RadioGroup  mBtns;

  private Button   mBtnOK;
  // private Button   mBtnBack;
  // private Button   mBtnCancel;
  private int mIndex;
  private MyKeyboard mKeyboard = null;

  public PlotNewDialog( Context context, INewPlot maker, int index )
  {
    super( context );
    mContext = context;
    mMaker  = maker;
    mIndex  = index;
    // notDone = true;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.plot_new_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mEditName  = (EditText) findViewById(R.id.edit_plot_name);
    mEditStart = (EditText) findViewById(R.id.edit_plot_start);
    // mEditView  = (EditText) findViewById(R.id.edit_plot_view);

    mEditName.setText( Integer.toString( mIndex ) );

    mEditStart.setOnLongClickListener( this );

    // mBtnPlan     = (RadioButton) findViewById( R.id.btn_plot_plan );
    // mBtnExtended = (RadioButton) findViewById( R.id.btn_plot_ext );
    // mBtnVSection = (RadioButton) findViewById( R.id.btn_plot_vcross );
    // mBtnHSection = (RadioButton) findViewById( R.id.btn_plot_hcross );

    // mEditName.setHint( R.string.scrap_name );
    // mEditStart.setHint( R.string.station_base );
    // mEditView.setHint(  R.string.station_viewed );
    // mBtnPlan.setChecked( true ); // default is plan

    mBtnOK = (Button) findViewById(R.id.button_ok );
    mBtnOK.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_back );
    // mBtnBack.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
   
    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), 
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mEditName,  MyKeyboard.FLAG_POINT_LCASE_2ND );
      int flag = ( TDSetting.mStationNames == 1 ) ? MyKeyboard.FLAG_POINT : MyKeyboard.FLAG_POINT_LCASE_2ND;
      MyKeyboard.registerEditText( mKeyboard, mEditStart, flag);
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mEditStart.setInputType( TopoDroidConst.NUMBER_DECIMAL );
      }
    }
  }

 
  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  // FIXME synchronized ?
  @Override
  public void onClick(View v) 
  {
    CutNPaste.dismissPopup();

    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "PlotDialog onClick() button " + b.getText().toString() ); 

    if ( /* notDone && */ b == mBtnOK ) {
      // notDone = false;
      String name  = mEditName.getText().toString().trim();
      String start = mEditStart.getText().toString().trim();
      // String view  = mEditView.getText().toString();
      // String view = null;

      if ( name == null ) {
        String error = mContext.getResources().getString( R.string.error_name_required );
        mEditName.setError( error );
        return;
      } 
      name = TopoDroidUtil.noSpaces( name );
      if ( name.length() == 0 ) {
        String error = mContext.getResources().getString( R.string.error_name_required );
        mEditName.setError( error );
        return;
      } 
      if ( start == null ) {
        String error = mContext.getResources().getString( R.string.error_start_required );
        mEditStart.setError( error );
        return;
      } 
      // start = TopoDroidUtil.noSpaces( start );
      start = start.trim();
      if ( start.length() == 0 ) {
        String error = mContext.getResources().getString( R.string.error_start_required );
        mEditStart.setError( error );
        return;
      } 
      if ( mMaker.hasSurveyPlot( name ) ) {
        String error = mContext.getResources().getString( R.string.plot_duplicate_name );
        mEditName.setError( error );
        return;
      }
      if ( ! mMaker.hasSurveyStation( start ) ) {
        String error = mContext.getResources().getString( R.string.error_station_non_existing );
        mEditStart.setError( error );
        return;
      }

      // long type = PlotInfo.PLOT_PLAN;
      // if ( mBtnPlan.isChecked() )          { type = PlotInfo.PLOT_PLAN; }
      // else if ( mBtnExtended.isChecked() ) { type = PlotInfo.PLOT_EXTENDED; }
      // else if ( mBtnVSection.isChecked() ) { type = PlotInfo.PLOT_V_SECTION; }
      // else if ( mBtnHSection.isChecked() ) { type = PlotInfo.PLOT_H_SECTION; }
      // view = TopoDroidUtil.noSpaces( view );
      // mMaker.makeNewPlot( name, type, start, view );
      mMaker.makeNewPlot( name, start );
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;

    if ( TDSetting.mKeyboard ) {
      if ( mKeyboard.isVisible() ) {
        mKeyboard.hide();
        return;
      }
    }
    dismiss();
  }
}

