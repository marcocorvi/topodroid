/* @file DrawingShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a survey shot: editing comment, extend and flag
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.CheckBox;

import android.text.InputType;
import android.inputmethodservice.KeyboardView;

public class DrawingShotDialog extends Dialog 
                               implements View.OnClickListener
{
  private Context mContext;

  private TextView mLabel;
  private Button mBtnOK;
  // private Button mBtnCancel;
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;

  private CheckBox mRBleft;
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private RadioButton mRBignore;

  // private RadioButton mRBsurvey;
  private CheckBox mRBduplicate;
  private CheckBox mRBsurface;
  // private CheckBox mRBbackshot;
  private Button mRBwalls;

  private DrawingActivity mParent;
  private DistoXDBlock mBlock;

  MyKeyboard mKeyboard = null;

  public DrawingShotDialog( Context context, DrawingActivity parent, DrawingPath shot )
  {
    super(context);
    mContext = context;
    mParent  = parent;
    mBlock   = shot.mBlock;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.drawing_shot_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mLabel     = (TextView) findViewById(R.id.shot_label);
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );

    mBtnOK     = (Button) findViewById(R.id.btn_ok);
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);

    mRBleft    = (CheckBox) findViewById( R.id.left );
    mRBvert    = (CheckBox) findViewById( R.id.vert );
    mRBright   = (CheckBox) findViewById( R.id.right );
    // mRBignore  = (RadioButton) findViewById( R.id.ignore );

    // mRBsurvey    = (RadioButton) findViewById( R.id.survey );
    mRBduplicate = (CheckBox) findViewById( R.id.duplicate );
    mRBsurface   = (CheckBox) findViewById( R.id.surface );
    // mRBbackshot  = (CheckBox) findViewById( R.id.backshot );
    mRBwalls  = (Button) findViewById( R.id.walls );

    // if ( ! TopoDroidApp.mLoopClosure ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( 0xff999999 );
    // }

    mLabel.setText( mBlock.dataString( mContext.getResources().getString(R.string.shot_data) ) );

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );

    mRBduplicate.setOnClickListener( this );
    mRBsurface.setOnClickListener( this );
    // mRBbackshot.setOnClickListener( this );
    if ( TopoDroidSetting.mWallsType != TopoDroidSetting.WALLS_NONE 
      && TopoDroidSetting.mLevelOverAdvanced 
      && ( mParent.getPlotType() == PlotInfo.PLOT_PLAN || mParent.getPlotType() == PlotInfo.PLOT_EXTENDED ) ) {
      mRBwalls.setOnClickListener( this );
    } else {
      mRBwalls.setVisibility( View.GONE );
    }

    mBtnOK.setOnClickListener( this );
    // mBtnCancel.setOnClickListener( this );

    if ( mBlock != null ) {
      mETfrom.setText( mBlock.mFrom );
      mETto.setText( mBlock.mTo );
      mETcomment.setText( mBlock.mComment );

      switch ( (int)mBlock.mExtend ) {
        case DistoXDBlock.EXTEND_LEFT:
          mRBleft.setChecked( true );
          break;
        case DistoXDBlock.EXTEND_VERT:
          mRBvert.setChecked( true );
          break;
        case DistoXDBlock.EXTEND_RIGHT:
          mRBright.setChecked( true );
          break;
        // case DistoXDBlock.EXTEND_IGNORE:
        //   mRBignore.setChecked( true );
        //   break;
      }
      switch ( (int)mBlock.mFlag ) {
        // case DistoXDBlock.BLOCK_SURVEY:
        //   mRBsurvey.setChecked( true );
        //   break;
        case DistoXDBlock.BLOCK_DUPLICATE:
          mRBduplicate.setChecked( true );
          break;
        case DistoXDBlock.BLOCK_SURFACE:
          mRBsurface.setChecked( true );
          break;
        // case DistoXDBlock.BLOCK_BACKSHOT:
        //   mRBbackshot.setChecked( true );
        //   break;
      }
    }
    setTitle( String.format( mContext.getResources().getString( R.string.shot_title ), mBlock.mFrom, mBlock.mTo ) );

    if ( TopoDroidSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TopoDroidSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom, flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,   flag );
      mKeyboard.hide();
    } else {
      mKeyboard.hide();
      if ( TopoDroidSetting.mStationNames == 1 ) {
        mETfrom.setInputType( TopoDroidConst.NUMBER_DECIMAL );
        mETto.setInputType( TopoDroidConst.NUMBER_DECIMAL );
      }
    }

  }

  public void onClick(View view)
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingShotDialog onClick() " + view.toString() );

    Button b = (Button)view;

    if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );

    } else if ( b == mRBsurface ) {
      mRBduplicate.setChecked( false );
      // mRBbackshot.setChecked( false );
    } else if ( b == mRBduplicate ) {
      mRBsurface.setChecked( false );
      // mRBbackshot.setChecked( false );
    // } else if ( b == mRBbackshot ) {
    //   mRBduplicate.setChecked( false );
    //   mRBsurface.setChecked( false );

    } else if ( b == mRBwalls ) {
      mParent.drawWallsAt( mBlock );
      dismiss();

    } else if ( b == mBtnOK ) {
      long extend = mBlock.mExtend;
      long flag   = mBlock.mFlag;

      if ( mRBleft.isChecked() ) {
        extend = DistoXDBlock.EXTEND_LEFT;
      } else if ( mRBvert.isChecked() ) {
        extend = DistoXDBlock.EXTEND_VERT;
      } else if ( mRBright.isChecked() ) {
        extend = DistoXDBlock.EXTEND_RIGHT;
      } else { // if ( mRBignore.isChecked() )
        extend = DistoXDBlock.EXTEND_IGNORE;
      }

      if ( mRBduplicate.isChecked() ) {
        flag = DistoXDBlock.BLOCK_DUPLICATE;
      } else if ( mRBsurface.isChecked() ) {
        flag = DistoXDBlock.BLOCK_SURFACE;
      // } else if ( mRBbackshot.isChecked() ) {
      //   flag = DistoXDBlock.BLOCK_BACKSHOT;
      } else { // if ( mRBsurvey.isChecked() )
        flag = DistoXDBlock.BLOCK_SURVEY;
      }

      mParent.updateBlockExtend( mBlock, extend ); // equal extend checked by the method
      mParent.updateBlockFlag( mBlock,flag ); // equal flag is checked by the method

      String from = mETfrom.getText().toString().trim();
      String to   = mETto.getText().toString().trim();
      String comment = mETcomment.getText().toString().trim();

      if ( ! from.equals( mBlock.mFrom ) || ! to.equals( mBlock.mTo ) ) { // FIXME revert equals
        mParent.updateBlockName( mBlock, from, to );
      }

      mParent.updateBlockComment( mBlock, comment ); // equal comment checked by the method

      // } else if (view.getId() == R.id.button_cancel ) {
      //   /* nothing */
 
      dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( TopoDroidSetting.mKeyboard ) {
      if ( mKeyboard.isVisible() ) {
        mKeyboard.hide();
        return;
      }
    }
    dismiss();
  }

}
        


