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
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import java.util.Locale;

import android.os.Bundle;

import android.content.Context;
import android.inputmethodservice.KeyboardView;

import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;

class PlotNewDialog extends MyDialog
                           implements View.OnClickListener
                           , View.OnLongClickListener
{
  private final TopoDroidApp mApp;
  private INewPlot mMaker;
  // private boolean notDone;

  private EditText mEditName;
  private EditText mEditStart;
  // private EditText mEditProject;

  private Button   mBtnOK;
  private Button   mBtnBack;
  private CheckBox mCBextended;
  private CheckBox mCBdangling;
  private int mIndex;
  private MyKeyboard mKeyboard = null;

  PlotNewDialog( Context context, TopoDroidApp app, INewPlot maker, int index )
  {
    super( context, R.string.PlotNewDialog );
    mApp    = app;
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

    mCBextended = (CheckBox)findViewById( R.id.button_extended );
    mCBextended.setChecked( true );
    mCBdangling = (CheckBox)findViewById( R.id.button_dangling );
    mCBdangling.setChecked( false );
    if ( ! TDLevel.overExpert ) {
      mCBdangling.setVisibility( View.GONE );
    }

    // mEditProject.setVisibility( View.INVISIBLE );
    // mCBextended.setOnClickListener( new View.OnClickListener() {
    //   public void onClick( View v ) {
    //     mEditProject.setVisibility( mCBextended.isChecked() ? View.INVISIBLE : View.VISIBLE );
    //   }
    // } );
         
    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), 
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
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

 
  @Override
  public boolean onLongClick(View v) 
  {
    if ( v.getId() == R.id.edit_plot_start ) { // mEditStart
      CutNPaste.makePopup( mContext, (EditText)v );
      return true;
    }
    return false;
  }

  // FIXME synchronized ?
  @Override
  public void onClick(View v) 
  {
    CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "PlotDialog onClick() button " + b.getText().toString() ); 

    switch ( v.getId( ) ) {
      case R.id.btn_ok:
        if ( ! handleOK( ) ) return;
        break;
      case R.id.btn_cancel:
        /* nothing */
        break;
    }
    dismiss();
  }

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
    name = TDUtil.noSpaces( name );
    if ( name.length() == 0 ) {
      mEditName.setError( mContext.getResources().getString( R.string.error_name_required ) );
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
    
    // start = TDUtil.noSpaces( start );
    start = start.trim();
    if ( start.length() == 0 ) {
      mEditStart.setError( mContext.getResources().getString( R.string.error_start_required ) );
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

    boolean extended = true;
    // int project = 0;
    if ( TDLevel.overAdvanced ) {
      extended = mCBextended.isChecked();
      // if ( ! extended ) {
      //   try {
      //     project = Integer.parseInt( mEditProject.getText().toString() );
      //   } catch ( NumberFormatException e ) {  }
      // }
    }

    if ( extended ) {
      mMaker.makeNewPlot( name, start, true, 0 ); // true = extended
    } else {
      mMaker.doProjectionDialog( name, start );
    }
    return true;
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }
}

