/* @file PlotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid new-plot dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import java.util.Locale;

import android.os.Bundle;

import android.content.Context;
// import android.inputmethodservice.KeyboardView;

import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;

class PlotNewDialog extends MyDialog
                    implements View.OnClickListener
                    , View.OnLongClickListener
{
  private INewPlot mMaker;
  // private boolean notDone;

  private EditText mEditName;
  private EditText mEditStart;
  // private EditText mEditProject;

  private Button   mBtnOK;
  private Button   mBtnBack;
  private CheckBox mCBprojected;
  private CheckBox mCBdangling;
  private int mIndex;
  private MyKeyboard mKeyboard = null;

  /** cstr
   * @param context    context
   * @param app        application
   * @param maker      ...
   * @param index      ...
   */
  PlotNewDialog( Context context, TopoDroidApp app, INewPlot maker, int index )
  {
    super( context, app, R.string.PlotNewDialog );
    mMaker  = maker;
    mIndex  = index;
    // notDone = true;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_NO_TITLE);
    initLayout( R.layout.plot_new_dialog, R.string.plot_new );

    mEditName  = (EditText) findViewById(R.id.edit_plot_name);
    mEditStart = (EditText) findViewById(R.id.edit_plot_start);
    // mEditProject = (EditText) findViewById(R.id.plot_project);

    mEditName.setText( String.format(Locale.US, "%d", mIndex ) );
    // if current station is set:
    String station = null;
    if ( TDSetting.mFixedOrigin ) {
      station = mApp.getFirstPlotOrigin( );
      if ( station == null ) station = mApp.getFirstStation();
    } else {
      if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
        station = mApp.getFirstStation();
      } else {
	station = mApp.getCurrentOrLastStation();
      }
    }
    if ( station == null ) station = TDSetting.mInitStation;
    if ( station != null ) mEditStart.setText( station );
    mEditStart.setOnLongClickListener( this );

    mBtnOK = (Button) findViewById(R.id.btn_ok );
    mBtnBack = (Button) findViewById(R.id.btn_cancel );
    mBtnOK.setOnClickListener( this );
    mBtnOK.setOnLongClickListener( this );

    mBtnBack.setOnClickListener( this );

    mCBprojected = (CheckBox)findViewById( R.id.button_projected );
    if ( ! TDLevel.overAdvanced ) {
      mCBprojected.setVisibility( View.GONE );
    }
    mCBdangling = (CheckBox)findViewById( R.id.button_dangling );
    mCBdangling.setChecked( false );
    if ( ! TDLevel.overExpert ) {
      mCBdangling.setVisibility( View.GONE );
    }

    // mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    mKeyboard = MyKeyboard.getMyKeyboard( mContext, findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );

    if ( TDSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mEditName,  MyKeyboard.FLAG_POINT_LCASE_2ND );
      int flag = ( TDSetting.mStationNames == 1 ) ? MyKeyboard.FLAG_POINT : MyKeyboard.FLAG_POINT_LCASE_2ND;
      MyKeyboard.registerEditText( mKeyboard, mEditStart, flag);
      // MyKeyboard.registerEditText( mKeyboard, mEditProject, 0 ); // MyKeyboard.FLAG_POINT );
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mEditStart.setInputType( TDConst.NUMBER_DECIMAL );
      }
      // mEditProject.setInputType( TDConst.NUMBER );
    }
  }

  /** implements user long-tap
   * @param v tapped view
   * @return true if the tap has been handled
   */
  @Override
  public boolean onLongClick(View v) 
  {
    if ( v.getId() == R.id.edit_plot_start ) { // mEditStart
      CutNPaste.makePopup( mContext, (EditText)v );
      return true;
    }
    return false;
  }

  /** implements user tap - FIXME synchronized ?
   * @param v tapped view
   */
  @Override
  public void onClick(View v) 
  {
    CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "PlotDialog onClick() button " + b.getText().toString() ); 

    int vid = v.getId( );
    if ( vid == R.id.btn_ok ) {
      if ( ! handleOK( ) ) return;
    // } else if ( vid == R.id.btn_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }

  /** handle a press of the button "ok"
   * @return true if the tap has been handled
   */
  private boolean handleOK( )
  {
    String name  = mEditName.getText().toString().trim();
    String start = mEditStart.getText().toString().trim();
    // String view  = mEditView.getText().toString();
    // String view = null;

    // if ( name == null ) { // CANNOT HAPPEN
    //   mEditName.setError( mContext.getResources().getString( R.string.error_name_required ) );
    //   return false;
    // }
    name = TDUtil.toStationFromName( name );
    if ( name == null || name.length() == 0 ) {
      mEditName.setError( mContext.getResources().getString( R.string.error_name_required ) );
      return false;
    }
    if ( ! TDUtil.isStationName( name ) ) {
      mEditName.setError( mContext.getResources().getString( R.string.bad_station_name ) );
      return false;
    }
    if ( mApp.hasSurveyPlotName( name ) ) {
      mEditName.setError( mContext.getResources().getString( R.string.error_name_duplicate ) );
      return false;
    }

    // if ( start == null ) { // CANNOT HAPPEN
    //   mEditStart.setError( mContext.getResources().getString( R.string.error_start_required ) );
    //   return false;
    // }
    
    // start = TDUtil.toStationFromName( start );
    if ( start.length() == 0 ) {
      mEditStart.setError( mContext.getResources().getString( R.string.error_start_required ) );
      return false;
    } 
    if ( ! TDUtil.isStationName( start ) ) {
      mEditStart.setError( mContext.getResources().getString( R.string.bad_station_name ) );
      return false;
    }
    if ( mMaker.hasSurveyPlot( name ) ) {
      mEditName.setError( mContext.getResources().getString( R.string.plot_duplicate_name ) );
      return false;
    }
    boolean dangling = TDLevel.overExpert && mCBdangling.isChecked();
    if ( ! ( dangling || mMaker.hasSurveyStation( start ) ) ) {
      mEditStart.setError( mContext.getResources().getString( R.string.error_station_non_existing ) );
      return false;
    }

    boolean projected = false;
    if ( TDLevel.overAdvanced ) {
      projected = mCBprojected.isChecked();
    }

    if ( projected ) {
      mMaker.doProjectionDialog( name, start );
    } else {
      mMaker.makeNewPlot( name, start, true, 0, 0 ); // true = extended
    }
    return true;
  }

  /** handle a press on the BACK button
   */
  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }
}

