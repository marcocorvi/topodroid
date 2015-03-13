/* @file ShotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid dialog for a new (manually entered) shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130621 set hint for FROM field
 * 20130910 added "at" field
 * 20131022 dismiss only to the "close" button
 * 20140220 N.B. insertManualShot keeps into account current units
 * 20140221 auto update of stations names: three buttons, OK, Save, Back
 * 20140416 setError for required EditText inputs
 */
package com.topodroid.DistoX;

// import java.Thread;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioButton;

// import android.widget.Spinner;
// import android.widget.ArrayAdapter;

import android.content.Context;
import android.text.InputType;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

// import android.util.Log;


public class ShotNewDialog extends Dialog
                           implements View.OnClickListener, IBearingAndClino
{
  // private ShotActivity mParent;
  private Context mContext;
  private TopoDroidApp mApp;
  private ILister mLister;
  private DistoXDBlock mPrevBlk;
  private boolean  notDone;
  private long mAt; // id of the shot where to add new shot (-1 end)

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETdistance;
  private EditText mETbearing;
  private EditText mETclino;
  // private Spinner  mExtend;
  private RadioButton mRadioLeft;
  private RadioButton mRadioVert;
  private RadioButton mRadioRight;

  private EditText mETleft;
  private EditText mETright;
  private EditText mETup;
  private EditText mETdown;
  private CheckBox mCBsplayAtTo;
  private Button   mBtnOk;
  private Button   mBtnSave;
  // private Button   mBtnBack;
  private Button   mBtnSensor;

  TimerTask mTimer;

  public ShotNewDialog( Context context, TopoDroidApp app, ILister lister, DistoXDBlock last_blk, long at )
  {
    super( context );
    mContext = context;
    mApp  = app;
    mLister  = lister;
    mPrevBlk = last_blk;
    notDone  = true;
    mAt      = at;
    mTimer   = null;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "ShotNewDialog onCreate" );
    setContentView(R.layout.shot_new_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mETfrom = (EditText) findViewById(R.id.shot_from );
    mETto   = (EditText) findViewById(R.id.shot_to );
    mETdistance = (EditText) findViewById(R.id.shot_distance );
    mETbearing  = (EditText) findViewById(R.id.shot_bearing );
    mETclino    = (EditText) findViewById(R.id.shot_clino );
    mETleft     = (EditText) findViewById(R.id.shot_left );
    mETright    = (EditText) findViewById(R.id.shot_right );
    mETup       = (EditText) findViewById(R.id.shot_up );
    mETdown     = (EditText) findViewById(R.id.shot_down );
    mCBsplayAtTo = (CheckBox) findViewById( R.id.splay_at_to );

    mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    if ( mPrevBlk != null ) {
      mETfrom.setHint( mPrevBlk.mTo );
      mETto.setHint( DistoXStationName.increment( mPrevBlk.mTo ) );
    }
    String current_name = mApp.getCurrentStationName();
    if ( current_name != null ) {
      if ( TopoDroidSetting.isSurveyForward() ) {
        mETfrom.setHint( current_name );
      } else {
        mETto.setHint( current_name );
      }
    }

    mBtnOk    = (Button) findViewById(R.id.button_ok_shot_name );
    mBtnSave  = (Button) findViewById(R.id.button_save_shot_name );
    // mBtnBack  = (Button) findViewById(R.id.button_back_shot_name );
    mBtnSensor = (Button) findViewById(R.id.button_sensor );

    mRadioLeft  = (RadioButton) findViewById(R.id.radio_left );
    mRadioVert  = (RadioButton) findViewById(R.id.radio_vert );
    mRadioRight = (RadioButton) findViewById(R.id.radio_right );
    // mExtend = (Spinner) findViewById( R.id.extend );
    // ArrayAdapter< CharSequence > adapter =
    //   ArrayAdapter.createFromResource( mContext, R.array.extend_name, android.R.layout.simple_spinner_item );   
    // adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
    // mExtend.setAdapter( adapter );

    mBtnOk.setOnClickListener( this );
    mBtnSave.setOnClickListener( this );
    // mBtnBack.setOnClickListener( this );
    mBtnSensor.setOnClickListener( this );

    setTitle( R.string.shot_info );
  }

  private void resetData( String from )
  {
    String to = DistoXStationName.increment( from );
    mETfrom.setText( from );
    mETto.setText(to);
    mETdistance.setText("");
    mETbearing.setText("");
    mETclino.setText("");
    mETleft.setText("");
    mETright.setText("");
    mETup.setText("");
    mETdown.setText("");
  }

  // implements
  public void setBearingAndClino( float b, float c )
  {
    StringWriter compassStr = new StringWriter();
    StringWriter clinoStr = new StringWriter();
    PrintWriter pwb = new PrintWriter( compassStr );
    PrintWriter pwc = new PrintWriter( clinoStr );
    pwb.format(Locale.ENGLISH, "%.1f", b );
    pwc.format(Locale.ENGLISH, "%.1f", c );
    mETbearing.setText( compassStr.getBuffer().toString() );
    mETclino.setText( clinoStr.getBuffer().toString() );
  }

  // FIXME synchronized ?
  public void onClick(View v) 
  {
    if ( mTimer != null ) mTimer.mRun = false;

    Button b = (Button) v;
    String val;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "ShotNewDialog onClick button " + b.getText().toString() );

    if ( b == mBtnOk || b == mBtnSave ) {
      if ( notDone && mETfrom.getText() != null ) {
        String shot_from = mETfrom.getText().toString();
        if ( shot_from == null || shot_from.length() == 0 ) {
          shot_from = mETfrom.getHint().toString();
        }
        String shot_to   = mETto.getText().toString();
        if ( shot_to == null || shot_to.length() == 0 ) {
          shot_to = mETto.getHint().toString();
        }

        if ( shot_to == null ) {
          shot_to = "";
        } else {
          shot_to = TopoDroidApp.noSpaces( shot_to );
        }

        shot_from = TopoDroidApp.noSpaces( shot_from );
        if ( shot_from.length() == 0 ) {
          mETfrom.setError( mContext.getResources().getString( R.string.error_from_required ) );
          return;
        }

        String distance = mETdistance.getText().toString().trim();
        if ( distance.length() == 0 ) { 
          mETdistance.setError( mContext.getResources().getString( R.string.error_length_required ) );
          return;
        }
        String bearing = mETbearing.getText().toString().trim();
        if ( bearing.length() == 0 ) {
          mETbearing.setError( mContext.getResources().getString( R.string.error_azimuth_required ) );
          return;
        }
        String clino = mETclino.getText().toString().trim();
        if ( clino.length() == 0 ) {
          mETclino.setError( mContext.getResources().getString( R.string.error_clino_required ) );
          return;
        }

        notDone = false;
        // Log.v( TopoDroidApp.TAG, "data " + distance + " " + bearing + " " + clino );

        long shot_extend = 1;
        if ( mRadioLeft.isChecked() ) { shot_extend = -1; }
        else if ( mRadioVert.isChecked() ) { shot_extend = 0; }

        // switch ( mExtend.getSelectedItemPosition() ) {
        //   case 0: shot_extend = -1; break;
        //   case 1: shot_extend = 0;  break;
        //   case 2: shot_extend = 1;  break;
        //   // FIXME case 3: shot_extend = 2;  break;
        // }

        DistoXDBlock blk = null;
        try {
          if ( shot_to.length() > 0 ) {
            String splay_station = mCBsplayAtTo.isChecked() ? shot_to : shot_from;
            blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                               Float.parseFloat( distance.replace(',','.') ),
                               Float.parseFloat( bearing.replace(',','.') ),
                               Float.parseFloat( clino.replace(',','.') ),
                               shot_extend,
                               mETleft.getText().toString().replace(',','.') ,
                               mETright.getText().toString().replace(',','.') ,
                               mETup.getText().toString().replace(',','.') ,
                               mETdown.getText().toString().replace(',','.') ,
                               splay_station );
          } else {
            blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                               Float.parseFloat(distance.replace(',','.') ),
                               Float.parseFloat(bearing.replace(',','.') ),
                               Float.parseFloat(clino.replace(',','.') ),
                               shot_extend,
                               null, null, null, null, null );
          }
          mApp.setCurrentStationName( null );
        } catch ( NumberFormatException e ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR,
             "parse Float error: distance " + distance + " bearing " + bearing + " clino " + clino );
        }
        if ( blk != null ) {
          resetData( shot_to );
          if ( mLister !=  null ) {
            mLister.refreshDisplay( 1, false );
          }
          notDone = true;
        }
      }
      if ( b == mBtnOk ) {
        dismiss();
      }
    } else if ( b == mBtnSensor ) {
      mTimer = new TimerTask( mContext, this );
      mTimer.execute();
    // } else if ( b == mBtnBack ) {
    //   dismiss();
    }
  }
}

