/* @file DrawingShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a survey shot: editing comment, extend and flag
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyColorPicker;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.ExtendType;
// import com.topodroid.common.PlotType;

import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
// import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

// import android.text.InputType;
import android.inputmethodservice.KeyboardView;

class DrawingShotDialog extends MyDialog
                        implements View.OnClickListener
                        , View.OnLongClickListener
                        , MyColorPicker.IColorChanged
{
  // private TextView mLabel;
  private Button mBtnOK;
  private Button mBtnCancel;
  private Button mBtnColor;  // user-set color (tester level)
  // private Button mBtnLeg = null;    // leg-inclined projection
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;

  // private boolean hideLegBtn     = true;
  private boolean hideColorBtn   = true;
  private boolean hideStretchBar = true;

  private CheckBox mRBleft;
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private RadioButton mRBignore;
  private SeekBar mStretchBar;

  // private RadioButton mRBsurvey;
  private MyCheckBox mRBdup  = null;
  private MyCheckBox mRBsurf = null;
  private MyCheckBox mRBcmtd = null;
  private MyCheckBox mCBxSplay = null;

  private CheckBox mCBfrom   = null;
  private CheckBox mCBto     = null;
  // private CheckBox mRBbackshot;
  // private Button mRBwalls;

  private final DrawingWindow mParent;
  private DBlock mBlock;
  private int mColor;    // bock color
  private DrawingPath mPath;
  private int mFlag; // can barrier/hidden FROM and TO
  private int mIntExtend; // used to set the progress in the Stretch Bar
  private float mStretch; // FIXME_STRETCH use a slider

  // 0x01 can barrier FROM
  // 0x02 can hidden  FROM
  // 0x04 can barrier TO
  // 0x08 can hidden  TO

  private MyKeyboard mKeyboard = null;

  /** cstr
   * @param context  context
   * @param parent   parent window
   * @param shot     drawing path of this dialog
   * @param flag     ...
   */
  DrawingShotDialog( Context context, DrawingWindow parent, DrawingPath shot, int flag )
  {
    super(context, null, R.string.DrawingShotDialog ); // null app
    mParent  = parent;
    mBlock   = shot.mBlock;
    mColor   = mBlock.getPaintColor();
    mPath    = shot;
    mFlag    = flag;
    mIntExtend = mBlock.getReducedIntExtend();
    mStretch = mBlock.getStretch();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.drawing_shot_dialog,
      String.format( mContext.getResources().getString( R.string.shot_title ), mBlock.mFrom, mBlock.mTo ) );

    TextView mLabel     = (TextView) findViewById(R.id.shot_label);
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );
    TextView tv_type    = (TextView) findViewById(R.id.shot_type ); // 20230118 local var "tv_type"
    if (mBlock.isBacksight() ) {
      tv_type.setText( R.string.type_b );
    } else if (mBlock.isForesight() ) {
      tv_type.setText( R.string.type_d );
    } else if (mBlock.isManual() ) {
      tv_type.setText( R.string.type_m );
    }
    if (mBlock.isMultiBad() ) {
      tv_type.setTextColor( TDColor.DARK_ORANGE );
    } else if ( TopoDroidApp.mShotWindow != null && TopoDroidApp.mShotWindow.isBlockMagneticBad( mBlock ) ) {
      tv_type.setTextColor( TDColor.FIXED_RED );
    }

    mETfrom.setOnLongClickListener( this );
    mETto.setOnLongClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );

    mBtnOK     = (Button) findViewById(R.id.btn_ok);
    mBtnCancel = (Button) findViewById(R.id.btn_cancel);

    mRBleft    = (CheckBox) findViewById( R.id.left );
    mRBvert    = (CheckBox) findViewById( R.id.vert );
    mRBright   = (CheckBox) findViewById( R.id.right );
    // mRBignore  = (RadioButton) findViewById( R.id.ignore );
    mStretchBar = (SeekBar) findViewById(R.id.stretchbar );

    // mRBsurvey    = (RadioButton) findViewById( R.id.survey );
    // mRBdup  = (CheckBox) findViewById( R.id.duplicate );
    // mRBsurf = (CheckBox) findViewById( R.id.surface );
    // mRBbackshot  = (CheckBox) findViewById( R.id.backshot );

    // hideStretchBar = true;
    // hideColorBtn   = true;
    // hideLegBtn     = true;
    mBtnColor = (Button) findViewById( R.id.btn_color );
    if ( TDLevel.overExpert ) {
      if ( mBlock.isSplay() ) {
	if ( TDSetting.mSplayColor ) {
          mBtnColor.setBackgroundColor( mColor ); 
          mBtnColor.setOnClickListener( this );
          hideColorBtn = false;
	}
      } else if ( mBlock.isMainLeg() ) {
        if ( mParent.isExtendedProfile() ) {
          if ( TDSetting.mExtendFrac ) {
            Bitmap bitmap =  MyButton.getLVRseekbarBackGround( mContext, (int)(TopoDroidApp.mDisplayWidth), 20 );
            if ( bitmap != null ) {
              BitmapDrawable background = new BitmapDrawable( mContext.getResources(), bitmap );
              TDandroid.setSeekBarBackground( mStretchBar, background ); 
            }
            hideStretchBar = false;
            mStretchBar.setProgress( (int)(150 + 100 * mIntExtend + 100 * mStretch ) );
            mStretchBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
              public void onProgressChanged( SeekBar stretchbar, int progress, boolean fromUser) {
                if ( fromUser ) {
                  if ( progress < 100 ) { 
                    mIntExtend = -1;
                    mRBleft.setChecked(  true );
                    mRBvert.setChecked(  false );
                    mRBright.setChecked( false );
                    mStretch = (progress- 50)/100.0f; 
                  } else if ( progress > 200 ) {
                    mIntExtend = 1;
                    mRBleft.setChecked(  false );
                    mRBvert.setChecked(  false );
                    mRBright.setChecked( true );
                    mStretch = (progress-250)/100.0f;
                  } else { 
                    mIntExtend = 0;
                    mRBleft.setChecked(  false );
                    mRBvert.setChecked(  true );
                    mRBright.setChecked( false );
                    mStretch = (progress-150)/100.0f;
                  }
                  // mStretch = (progress - 100)/200.0f;
                  if ( mStretch < -0.5f ) mStretch = -0.5f;
                  if ( mStretch >  0.5f ) mStretch =  0.5f;
                }
              }
              public void onStartTrackingTouch(SeekBar stretchbar) { }
              public void onStopTrackingTouch(SeekBar stretchbar) { }
            } );
            mStretchBar.setEnabled( true );
          }
        }
      }
    }

    if ( hideColorBtn )   mBtnColor.setVisibility( View.GONE );
    if ( hideStretchBar ) mStretchBar.setVisibility( View.GONE );

    LinearLayout layout3  = (LinearLayout) findViewById( R.id.layout3 );
    LinearLayout layout3b = (LinearLayout) findViewById( R.id.layout3b );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout3.setMinimumHeight( size + 20 );
    layout3b.setMinimumHeight( size + 20 );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    if ( TDLevel.overNormal ) {
      mRBdup      = new MyCheckBox( mContext, size, R.drawable.iz_dup_ok, R.drawable.iz_dup_no );
      mRBsurf     = new MyCheckBox( mContext, size, R.drawable.iz_surface_ok, R.drawable.iz_surface_no );
      mRBcmtd     = new MyCheckBox( mContext, size, R.drawable.iz_comment_ok, R.drawable.iz_comment_no );
      layout3.addView( mRBdup,  lp );
      layout3.addView( mRBsurf, lp );
      layout3.addView( mRBcmtd, lp );
      mRBdup.setOnClickListener( this );
      mRBsurf.setOnClickListener( this );
      mRBcmtd.setOnClickListener( this );
      if ( TDLevel.overAdvanced && mBlock.isOtherSplay() ) {
        mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_xsplays_ok, R.drawable.iz_ysplays_no );
        mCBxSplay.setChecked( false ); // ??? setState( false );
        layout3.addView( mCBxSplay, lp );
        mCBxSplay.setOnClickListener( this );
      }
      // if ( TDLevel.overExpert && mBlock.isMainLeg() ) {
      //   if ( TDSetting.mLegProjection ) {
      //     mBtnLeg = MyButton.getButton( mContext, this, R.drawable.iz_inclined );
      //     layout3.addView( mBtnLeg, lp );
      //     hideLegBtn = false;
      //   }
      // }
    } else {
      layout3.setVisibility( View.GONE );
    }

    boolean hide3b = true;
    if ( TDLevel.overAdvanced ) {
      if ( mBlock.isMainLeg() ) {
        if ( ( mFlag & 0x03 ) != 0 ) { // FROM can be barrier/hidden
          mCBfrom = new CheckBox( mContext );
          mCBfrom.setText( mBlock.mFrom );
          mCBfrom.setChecked( false );
          layout3b.addView( mCBfrom, lp );
          hide3b = false;
        }
        if ( ( mFlag & 0x0c ) != 0 ) { // TO can be barrier/hidden
          mCBto   = new CheckBox( mContext );
          mCBto.setText( mBlock.mTo );
          mCBto.setChecked( false );
          layout3b.addView( mCBto, lp );
          hide3b = false;
        }
      }
    }
    if ( hide3b ) layout3b.setVisibility( View.GONE );

    // mRBwalls  = (Button) findViewById( R.id.walls ); // AUTOWALLS

    // if ( TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( TDColor.MID_GRAY );
    // }
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL ) {
      mLabel.setText( mBlock.dataStringNormal( mContext.getResources().getString(R.string.shot_data) ) );
    } else { // SurveyInfo.DATAMODE_DIVING
      mLabel.setText( mBlock.dataStringDiving( mContext.getResources().getString(R.string.shot_data) ) );
    }

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );

    // mRBbackshot.setOnClickListener( this );

    // AUTOWALLS
    // if ( TDSetting.mWallsType != TDSetting.WALLS_NONE 
    //   && TDLevel.overExpert 
    //   && mBlock.isMainLeg()
    //   && ( PlotType.isSketch2D( mParent.getPlotType() ) ) ) {
    //   mRBwalls.setOnClickListener( this );
    // } else {
    //   mRBwalls.setVisibility( View.GONE );
    // }

    mBtnOK.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    if ( mBlock != null ) { // block cannot be null 
      mETfrom.setText( mBlock.mFrom );
      mETto.setText( mBlock.mTo );
      if ( mBlock.mComment != null && mBlock.mComment.length() > 0 ) {
        mETcomment.setText( mBlock.mComment );
      } else {
        mETcomment.setHint( R.string.comment );
      }

      switch ( mBlock.getIntExtend() ) {
        case ExtendType.EXTEND_LEFT:
          mRBleft.setChecked( true );
          break;
        case ExtendType.EXTEND_VERT:
          mRBvert.setChecked( true );
          break;
        case ExtendType.EXTEND_RIGHT:
          mRBright.setChecked( true );
          break;
        // case ExtendType.EXTEND_IGNORE:
        //   mRBignore.setChecked( true );
        //   break;
      }
      // if ( mBlock.isSurvey() ) {
      //   mRBsurvey.setChecked( true );
      if ( TDLevel.overNormal ) {
        if ( mBlock.isDuplicate() ) {
          mRBdup.setState( true );
        } else if ( mBlock.isSurface() ) {
          mRBsurf.setState( true );
        } else if ( mBlock.isCommented() ) { // FIXME_COMMENTED
          mRBcmtd.setState( true );
        // } else if ( mBlock.isBackshot() ) {
        //   mRBbackshot.setChecked( true );
        }
      }
    }
    
    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom, flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,   flag );
      mKeyboard.hide();
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( TDConst.NUMBER_DECIMAL );
        mETto.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }
  }

  /** update after a color change
   * @param color    new color
   */
  @Override
  public void colorChanged( int color )
  {
    if ( hideColorBtn ) return;
    mColor = color;
    mParent.updateBlockColor( mBlock, mColor );
    mBtnColor.setBackgroundColor( mColor );
  }

  /** implements user long-taps
   * @param view   tapped view
   * @return true if the tap has been handled
   */
  @Override
  public boolean onLongClick(View view)
  {
    CutNPaste.makePopup( mContext, (EditText)view );
    return true;
  }

  /** implements user taps
   * @param view   tapped view
   */
  @Override
  public void onClick(View view)
  {
    // TDLog.v( "Drawing Shot Dialog onClick() " + view.toString() );
    CutNPaste.dismissPopup();

    Button b = (Button)view;

    if ( (! hideColorBtn) && b == mBtnColor ) {
      new MyColorPicker( mContext, this, mColor ).show();
      return;
    // } else if ( (! hideLegBtn) && b == mBtnLeg ) {
    //   // TODO
    //   return;
    } else if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
      if ( ! hideStretchBar ) {
        mIntExtend = mRBleft.isChecked() ? -1 : 0;
        mStretch = 0;
        mStretchBar.setProgress(  150+100*mIntExtend );
      }
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
      if ( ! hideStretchBar ) {
        mIntExtend = 0;
        mStretch = 0;
        mStretchBar.setProgress( 150 );
      }
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );
      if ( ! hideStretchBar ) {
        mIntExtend = mRBright.isChecked() ? 1 : 0;
        mStretch = 0;
        mStretchBar.setProgress(  150+100*mIntExtend );
      }

    } else if ( TDLevel.overNormal && b == mRBdup ) {
      mRBdup.toggleState();
      if ( mRBdup.isChecked() ) {
        mRBsurf.setState( false );
        mRBcmtd.setState( false );
      }
    } else if ( TDLevel.overNormal && b == mRBsurf ) {
      mRBsurf.toggleState();
      if ( mRBsurf.isChecked() ) {
        mRBdup.setState( false );
        mRBcmtd.setState( false );
      }
    } else if ( TDLevel.overNormal && b == mRBcmtd ) {
      mRBcmtd.toggleState();
      if ( mRBcmtd.isChecked() ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
      }
    } else if ( TDLevel.overNormal && b == mCBxSplay ) {
      mCBxSplay.toggleState();

    // } else if ( b == mRBbackshot ) {
    //   mRBdup.setChecked( false );
    //   mRBsurf.setChecked( false );

    // } else if ( TDLevel.overExpert && b == mRBwalls ) { // AUTOWALLS
    //   mParent.drawWallsAt( mBlock );
    //   dismiss();

    } else if ( b == mBtnCancel ) {
      dismiss();
    } else if ( b == mBtnOK ) {
      MyKeyboard.close( mKeyboard );

      if ( TDLevel.overAdvanced ) {
        if ( mCBfrom != null && mCBfrom.isChecked() ) {
          if ( ( mFlag & 0x01 ) == 0x01 ) { // can barrier FROM
            mParent.toggleStationBarrier( mBlock.mFrom, false );
          } else if ( ( mFlag & 0x02 ) == 0x02 ) { // can hidden FROM
            mParent.toggleStationHidden( mBlock.mFrom, false );
          }
        }
        if ( mCBto != null && mCBto.isChecked() ) {
          if ( ( mFlag & 0x04 ) == 0x04 ) { // can barrier TO
            mParent.toggleStationBarrier( mBlock.mTo, false );
          } else if ( ( mFlag & 0x08 ) == 0x08 ) { // can hidden TO
            mParent.toggleStationHidden( mBlock.mTo, false );
          }
        }
      }

      // int extend = mBlock.getIntExtend();
      int extend = ExtendType.EXTEND_IGNORE;
      if ( mRBleft.isChecked() )       { extend = ExtendType.EXTEND_LEFT; }
      else if ( mRBvert.isChecked() )  { extend = ExtendType.EXTEND_VERT; }
      else if ( mRBright.isChecked() ) { extend = ExtendType.EXTEND_RIGHT; }
      // TDLog.v( "Extend " + extend + " Stretch " + mStretch );
      mParent.updateBlockExtend( mBlock, extend, mStretch ); // FIXME_STRETCH equal extend checked by the method

      if ( TDLevel.overNormal ) {
        mBlock.clearFlagDuplicateSurfaceCommented();
        long flag  = mBlock.getFlag();
        if ( mRBdup.isChecked() )       { flag |= DBlock.FLAG_DUPLICATE; }
        else if ( mRBsurf.isChecked() ) { flag |= DBlock.FLAG_SURFACE; }
        else if ( mRBcmtd.isChecked() ) { flag |= DBlock.FLAG_COMMENTED; }
        // // else if ( mRBbackshot.isChecked() ) { flag = DBlock.FLAG_BACKSHOT; }
        // else /* if ( mRBsurvey.isChecked() ) */ { flag = DBlock.FLAG_SURVEY; }
        mParent.updateBlockFlag( mBlock, flag, mPath ); // equal flag is checked by the method
      }

      if ( TDLevel.overAdvanced && mCBxSplay != null ) {
	if ( mCBxSplay.isChecked() ) {
          mParent.clearBlockSplayLeg( mBlock, mPath );
	}
      }

      String from = mETfrom.getText().toString().trim();
      String to   = mETto.getText().toString().trim();

      if ( ! from.equals( mBlock.mFrom ) || ! to.equals( mBlock.mTo ) ) { // FIXME revert equals
        mParent.updateBlockName( mBlock, from, to );
      }

      if ( mETcomment.getText() != null ) {
        String comment = mETcomment.getText().toString().trim();
        mParent.updateBlockComment( mBlock, comment ); // equal comment checked by the method
      }

      // } else if (view.getId() == R.id.button_cancel ) {
      //   /* nothing */
 
      dismiss();
    }
  }

  /** implements a user BACK press
   */
  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}
        


