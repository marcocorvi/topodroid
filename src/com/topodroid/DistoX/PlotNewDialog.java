/* @file PlotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid new-plot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Toast;

// import android.util.Log;

public class PlotNewDialog extends MyDialog
                           implements View.OnClickListener
                           , View.OnLongClickListener
{
  private TopoDroidApp mApp;
  private INewPlot mMaker;
  // private boolean notDone;

  private EditText mEditName;
  private EditText mEditStart;
  // private EditText mEditProject;

  private Button   mBtnOK;
  private Button   mBtnBack;
  private CheckBox mCBextended;
  private int mIndex;
  private MyKeyboard mKeyboard = null;

  public PlotNewDialog( Context context, TopoDroidApp app, INewPlot maker, int index )
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

    mEditName.setText( Integer.toString( mIndex ) );
    // if current station is set:
    String station = mApp.getCurrentOrLastStation();
    if ( station != null ) mEditStart.setText( station );
    mEditStart.setOnLongClickListener( this );

    mBtnOK = (Button) findViewById(R.id.button_ok );
    mBtnBack = (Button) findViewById(R.id.button_back );
    mBtnOK.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );
    mCBextended = (CheckBox)findViewById( R.id.button_extended );
    mCBextended.setChecked( true );

    // mEditProject.setVisibility( View.INVISIBLE );
    // mCBextended.setOnClickListener( new View.OnClickListener() {
    //   public void onClick( View v ) {
    //     mEditProject.setVisibility( mCBextended.isChecked() ? View.INVISIBLE : View.VISIBLE );
    //   }
    // } );
         
    if ( ! TDLevel.overAdvanced ) {
      LinearLayout layout3 = (LinearLayout) findViewById( R.id.layout3 );
      layout3.setVisibility( View.GONE );
    }

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
        mMaker.makeNewPlot( name, start, extended, 0 );
      } else {
        mMaker.doProjectionDialog( name, start );
      }
    } else if ( b == mBtnBack ) {
      /* nothing */
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

