/* @file ShotEditDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog to enter FROM-TO stations etc.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDStatus;
// import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyStateBox;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MyColorPicker;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;



import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import java.util.regex.Pattern;

// import android.app.Dialog;
import android.os.Bundle;
// import android.widget.RadioButton;

import android.text.method.KeyListener;
import android.text.InputType;

import android.content.Context;
// import android.content.res.Resources;
import android.content.DialogInterface;
// import android.inputmethodservice.KeyboardView;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import android.view.View;
// import android.graphics.drawable.BitmapDrawable;

class ShotEditDialog extends MyDialog
                     implements View.OnClickListener
                     , View.OnLongClickListener
                     , MyColorPicker.IColorChanged
{
  private final ShotWindow mParent;
  private DBlock mBlk;
  private DBlock mPrevBlk;
  private DBlock mNextBlk;
  private int mPos; // item position in the parent' adapter list

  // private Pattern mPattern; // name pattern

  private EditText mETdistance; // distance | depth
  private EditText mETbearing;
  private EditText mETclino;    // clino | distance

  private TextView mTVextra;    // Magn Acc Dip
  private TextView mTVshotTime;

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;
  
  private MyCheckBox mRBdup   = null;
  private MyCheckBox mRBsurf  = null;
  private MyCheckBox mRBcmtd  = null;  // FIXME_COMMENTED 
  private MyCheckBox mRBbcks  = null;  // BACKSHOT 
  private MyStateBox mRBsplay = null;  // inhibit splay plan/profile display

  private MyCheckBox mCBlegPrev;  // adjoin to previous leg shot
  private MyCheckBox mCBlegNext;  // adjoin to next leg shot
  private MyCheckBox mCBrenumber; // renumber shots after this
  private MyCheckBox mCBallSplay;
  private MyCheckBox mCBxSplay = null;
  // private MyCheckBox mCBhighlight;
  private MyCheckBox mCBbackLeg = null;


  private CheckBox mRBleft;   // shot extend
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private CheckBox mRBignore;

  private Button   mButtonOK;
  private Button   mButtonSave;
  private Button   mButtonMore;
  private Button   mButtonBack;

  private Button   mButtonPrev;
  private Button   mButtonNext;
  private Button mButtonReverse;

  private String shot_from;
  private String shot_to;
  private String shot_distance;  // distance - depth
  private String shot_bearing;   // bearing
  private String shot_clino;     // clino    - distance
  // private boolean shot_manual;
  private boolean editable;      // whether the shot data are editable
  private float shot_stretch; // FIXME_STRETCH

  private String shot_extra;
  private int  shot_extend;
  private long shot_flag;
  private int  shot_mark; // cavway flag
  private String shot_time;
  private String shot_comment;

  private MyKeyboard mKeyboard = null;

  private KeyListener mKLdistance;
  private KeyListener mKLbearing;
  private KeyListener mKLclino;

  private static final int flagDistance = MyKeyboard.FLAG_POINT;
  private static final int flagBearing  = MyKeyboard.FLAG_POINT;
  private static final int flagClino    = MyKeyboard.FLAG_POINT | MyKeyboard.FLAG_SIGN;
  private static final int flagDepth    = MyKeyboard.FLAG_POINT;

  private boolean mFirst;
  private int splay_type; // 0 plain, 2 X, 4 H, 5 V see LegType

  private boolean hasMore = false;

  // @note used also by DrawingShohtDialog
  // 0: no_mark, ...
  final static int[] mShotMark= { R.string.mark_no_mark, R.string.mark_no_mark, R.string.mark_no_mark, R.string.mark_no_mark,
                                  R.string.mark_generic, R.string.mark_backsight, R.string.mark_ridge, R.string.mark_feature,
                                  R.string.mark_unknown };

  // MORE -------------------------------------------
  private LinearLayout mLayoutMore;
  // private RadioButton mRBfrom = null; // INTERMEDIATE_DATA
  // private RadioButton mRBto   = null;
  // private RadioButton mRBat   = null;
  // private EditText mETat;
  // private EditText mETleft;
  // private EditText mETright;
  // private EditText mETup;
  // private EditText mETdown;
  // private Button mBTlrud  = null;
  // private boolean mHasLRUD = false; // could be replaced by (mBTlrud != null)
  private CheckBox mCBleg = null;

  // private MyCheckBox mButtonPlot;
  private MyCheckBox mButtonPhoto  = null;
  private MyCheckBox mButtonAudio  = null;
  private MyCheckBox mButtonSensor = null;
  // private MyCheckBox mButtonShot   = null; // INTERMEDIATE_DATA
  private MyCheckBox mButtonSurvey = null;

  private MyCheckBox mButtonDelete = null;
  private MyCheckBox mButtonCheck  = null;

  private Button mBtnColor = null;  // user-set color (tester level)
  private Button mBtnRecalibrate = null; // recalibrate survey data from this shot onwards

  private int mColor;    // block color
  private boolean hideColorBtn = true;
  private boolean hasAudio = false;

  /** cstr
   */
  ShotEditDialog( Context context, ShotWindow parent, int pos, DBlock blk, DBlock prev, DBlock next
            )
  {
    super( context, null, R.string.ShotEditDialog ); // null app
    mParent = parent;
    mPos = pos;
    mFirst = true;
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    // loadDBlock( blk, prev, next );
    // TDLog.v("Shot edit dialog. Blk " + blk.mId + " roll " + blk.mRoll ); // mRoll is not set
    // TDLog.v( "Shot Dialog " + blk.toStringNormal(true) + " flag " + blk.getFlag() );
    if ( /* TDLevel.overAdvanced && */  mBlk.isSplay() ) {
      if ( mBlk.isXSplay() ) {
        splay_type = LegType.XSPLAY;
      } else if ( mBlk.isHSplay() ) {
        splay_type = LegType.HSPLAY;
      } else if ( mBlk.isVSplay() ) {
        splay_type = LegType.VSPLAY;
      } else { // if ( mBlk.isPlainSplay() ) 
        splay_type = LegType.NORMAL;
      }
    } else {
      splay_type = LegType.INVALID;
    } 
    // TDLog.v("splay type " + splay_type );
    mColor  = mBlk.getPaintColor();
    hasAudio = TDandroid.checkMicrophone( mContext );
  }

  /** load a shot and fill the fields of the interface
   * @param blk   shot to load
   * @param prev  previous shot
   * @param next  next shot
   */
  private void loadDBlock( DBlock blk, DBlock prev, DBlock next )
  {
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog LOAD " + blk.toString(true) );
    // TDLog.Log( TDLog.LOG_SHOT, "  prev " + ((prev != null)? prev.toString(true) : "null") );
    // TDLog.Log( TDLog.LOG_SHOT, "  next " + ((next != null)? next.toString(true) : "null") );

    shot_from    = blk.mFrom;
    shot_to      = blk.mTo;
    
    if ( blk.isTypeBlank() && prev != null && prev.isMainLeg() ) {
      if ( DistoXStationName.isLessOrEqual( prev.mFrom, prev.mTo ) ) {
        shot_from = prev.mTo;
	if ( mFirst ) {
          shot_to = DistoXStationName.incrementName( prev.mTo, mParent.getStationNames() );
	  mFirst  = false;
	} else {
          shot_to   = DistoXStationName.incrementName( prev.mTo );
	}
      } else {
        shot_to = prev.mFrom;
	if ( mFirst ) {
          shot_from = DistoXStationName.incrementName( prev.mFrom, mParent.getStationNames() );
	  mFirst  = false;
	} else {
          shot_from = DistoXStationName.incrementName( prev.mFrom );
	}
      }
    }
    
    // shot_data    = blk.dataString();
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
      shot_distance = blk.depthString();
      shot_bearing  = blk.bearingString();
      shot_clino    = blk.distanceString();
    } else { // SurveyInfo.DATAMODE_NORMAL
      shot_distance = blk.distanceString();
      shot_bearing  = blk.bearingString();
      shot_clino    = blk.clinoString();
    }
    // shot_manual = blk.isManual();
    editable    = blk.isManual() || TDSetting.mEditableShots;
    // TDLog.v( "shot " + blk.mId + " is editable " + editable + " length " + shot_distance );

    // shot_extra   = blk.extraString( mParent.mSurveyAccuracy );
    shot_extra   = mParent.getBlockExtraString( blk );
    shot_extend  = blk.getIntExtend();
    shot_stretch = blk.getStretch(); // FIXME_STRETCH
    shot_flag    = blk.getFlag();    // no cavway bits - not needed
    shot_mark    = blk.cavwayFlag(); // cavway flag
    shot_comment = blk.mComment;
    shot_time    = TDUtil.timestampToDateTime( blk.mTime );

    // shot_secleg  = blk.isSecLeg(); // DBlock.BLOCK_SEC_LEG;
    // shot_backleg = blk.isBackLeg();
    // shot_xsplay  = blk.isOtherSplay(); // DBlock.BLOCK_X_SPLAY | H_SPLAY | V_SPLAY
    // if ( blk.type() != DBlock.BLOCK_MAIN_LEG ) mCBdeleteLeg.setVisibility( View.GONE );

    updateView();
  }

  /** update the interface
   */
  private void updateView()
  {
    mETdistance.setText( shot_distance );
    mETbearing.setText( shot_bearing );
    mETclino.setText( shot_clino );

    mTVextra.setText( shot_extra );
    if ( shot_from.length() > 0 || shot_to.length() > 0 ) {
      mETfrom.setText( shot_from );
      mETto.setText( shot_to );
      // if ( shot_from.length() > 0 ) {
      //   mRBfrom.setText( shot_from );
      // } else {
      //   mRBfrom.setVisibility( View.GONE );
      // }
      // if ( shot_to.length() > 0 ) {
      //   mRBto.setText( shot_to );
      // } else {
      //   mRBto.setVisibility( View.GONE );
      // }
    }
    mTVshotTime.setText( shot_time );
    mETcomment.setText( ((shot_comment != null)? shot_comment : "") );
   
    // if ( DBlock.isSurvey(shot_flag) ) { mRBreg.setChecked( true ); }
    if ( TDLevel.overNormal ) {
      mRBdup.setState(  false );
      mRBsurf.setState( false );
      mRBcmtd.setState( false ); // FIXME_COMMENTED
      mRBbcks.setState( false ); // BACKSHOT
      if ( DBlock.isDuplicate(shot_flag) )      { mRBdup.setState(  true ); }
      else if ( DBlock.isSurface(shot_flag) )   { mRBsurf.setState( true ); }
      else if ( DBlock.isCommented(shot_flag) ) { mRBcmtd.setState( true ); } // FIXME_COMMENTED
      else if ( DBlock.isBackshot(shot_flag) )  { mRBbcks.setState( true ); } // BACKSHOT
      else if ( mRBsplay != null ) {
        if ( DBlock.isNoPlan(shot_flag) && DBlock.isNoProfile(shot_flag) ) { mRBsplay.setState( 3 ); }
        else if ( DBlock.isNoPlan(shot_flag) )        { mRBsplay.setState( 2 ); }
        else if ( DBlock.isNoProfile(shot_flag) )     { mRBsplay.setState( 1 ); }
        else                                          { mRBsplay.setState( 0 ); }
      }
      // else if ( DBlock.isBackshot(shot_flag) ) { mRBback.setChecked( true ); }
    }

    mCBlegPrev.setChecked( mBlk.isSecLeg() );
    if ( mCBbackLeg != null ) mCBbackLeg.setState( mBlk.isBackLeg() );

    mRBleft.setChecked(  false );
    mRBvert.setChecked(  false );
    mRBright.setChecked( false );
    // mRBignore.setChecked( false );
    if ( shot_extend == ExtendType.EXTEND_LEFT )       { mRBleft.setChecked(  true ); }
    else if ( shot_extend == ExtendType.EXTEND_VERT )  { mRBvert.setChecked(  true ); }
    else if ( shot_extend == ExtendType.EXTEND_RIGHT ) { mRBright.setChecked( true ); }
    // else if ( shot_extend == ExtendType.EXTEND_IGNORE ) { mRBignore.setChecked( true ); }

    // Spinner
    // switch ( shot_extend ) {
    //   case ExtendType.EXTEND_LEFT: break;
    //   case ExtendType.EXTEND_VERT: break;
    //   case ExtendType.EXTEND_RIGHT: break;
    //   case ExtendType.EXTEND_IGNORE: break;
    // }

    mButtonNext.setEnabled( mNextBlk != null );
    mButtonPrev.setEnabled( mPrevBlk != null );

    // do at the very end
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
      MyKeyboard.setEditable( mETdistance, mKeyboard, mKLdistance, editable, flagDepth );
      MyKeyboard.setEditable( mETbearing,  mKeyboard, mKLbearing,  editable, flagBearing );
      MyKeyboard.setEditable( mETclino,    mKeyboard, mKLclino,    editable, flagDistance );
    } else {
      MyKeyboard.setEditable( mETdistance, mKeyboard, mKLdistance, editable, flagDistance );
      MyKeyboard.setEditable( mETbearing,  mKeyboard, mKLbearing,  editable, flagBearing );
      MyKeyboard.setEditable( mETclino,    mKeyboard, mKLclino,    editable, flagClino );
    }

    // updateLayoutLRUD(); // INTERMEDIATE_DATA
    updateDeleteCheckButtons();

    TextView tv = (TextView)findViewById( R.id.shot_mark );
    if ( shot_mark > 0 ) {
      tv.setVisibility( View.VISIBLE );
      tv.setText( mShotMark[ (shot_mark < 8)? shot_mark : 8 ] );
    } else {
      tv.setVisibility( View.GONE );
    }

    mETcomment.requestFocus();
  }


// -------------------------------------------------------------------

  private void setCBxSplay( int type )
  {
    if ( mCBxSplay == null ) return;
    splay_type = type;
    int res_splay = -1;
    switch ( splay_type ) {
      case LegType.NORMAL:
        res_splay = R.drawable.iz_ysplays_plain;
        break;
      case LegType.XSPLAY:
        res_splay = R.drawable.iz_ysplays_no;
        break;
      case LegType.HSPLAY:
        res_splay = R.drawable.iz_ysplays_horz;
        break;
      case LegType.VSPLAY:
        res_splay = R.drawable.iz_ysplays_vert;
        break;
    }
    if ( res_splay >= 0 ) {
      TDandroid.setButtonBackground( mCBxSplay, MyButton.getButtonBackground( mContext, mContext.getResources(), res_splay ) );
        // TDLog.v("create splay checkbox");
    } else {
      mCBxSplay.setVisibility( View.GONE );
    }
  }

  // @Override
  // public void onRestoreInstanceState( Bundle icicle )
  // {
  //   // FIXME DIALOG mKeyboard.hide();
  // }
 
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    // boolean photoCheck = TDandroid.checkCamera( mContext );

    initLayout( R.layout.shot_dialog, null );

    mETdistance = (EditText) findViewById(R.id.shot_distance);
    mETbearing  = (EditText) findViewById(R.id.shot_bearing);
    mETclino    = (EditText) findViewById(R.id.shot_clino);

    mKLdistance = mETdistance.getKeyListener();
    mKLbearing  = mETbearing .getKeyListener();
    mKLclino    = mETclino   .getKeyListener();

    mTVextra   = (TextView) findViewById(R.id.shot_extra );
    // mETname = (EditText) findViewById(R.id.shot_name );
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );
    mTVshotTime = (TextView) findViewById(R.id.shot_time );

    // mRBfrom = (RadioButton) findViewById( R.id.station_from );
    // mRBto   = (RadioButton) findViewById( R.id.station_to );

    // mButtonLRUD = {Button} findViewById( R.id.btn_lrud );
    // mETleft  = (EditText) findViewById(R.id.shot_left );
    // mETright = (EditText) findViewById(R.id.shot_right);
    // mETup    = (EditText) findViewById(R.id.shot_up   );
    // mETdown  = (EditText) findViewById(R.id.shot_down );
   
    mETfrom.setOnLongClickListener( this );
    mETto.setOnLongClickListener( this );

    // mCBdeleteLeg = (CheckBox) findViewById(R.id.delete_leg );
    // mButtonLRUD.setOnClickListener( this );

    // mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    mKeyboard = MyKeyboard.getMyKeyboard( mContext, findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );

    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom,  flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,    flag );
      // MyKeyboard.registerEditText( mKeyboard, mETleft,  MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETright, MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETup,    MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETdown,  MyKeyboard.FLAG_POINT );
      // mKeyboard.hide();
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( InputType.TYPE_CLASS_NUMBER );
        mETto.setInputType( InputType.TYPE_CLASS_NUMBER );
      }
      // mETleft.setInputType( InputType.TYPE_CLASS_NUMBER );
      // mETright.setInputType( InputType.TYPE_CLASS_NUMBER );
      // mETup.setInputType( InputType.TYPE_CLASS_NUMBER );
      // mETdown.setInputType( InputType.TYPE_CLASS_NUMBER );
    }

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    // LinearLayout layout9 = (LinearLayout) findViewById( R.id.layout9 );
    layout4.setMinimumHeight( size + 20 );
    // layout9.setMinimumHeight( size + 20 );
    
    mCBlegPrev   = new MyCheckBox( mContext, size, R.drawable.iz_leg2_ok, R.drawable.iz_leg2_no );
    mCBlegNext   = new MyCheckBox( mContext, size, R.drawable.iz_legnext_ok, R.drawable.iz_legnext_no );
    mCBrenumber  = new MyCheckBox( mContext, size, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_no );
    mCBallSplay  = new MyCheckBox( mContext, size, R.drawable.iz_splays_ok, R.drawable.iz_splays_no );
    int nr_buttons = 4;

    if ( TDLevel.overNormal ) {
      mRBdup  = new MyCheckBox( mContext, size, R.drawable.iz_dup_ok, R.drawable.iz_dup_no );
      mRBsurf = new MyCheckBox( mContext, size, R.drawable.iz_surface_ok, R.drawable.iz_surface_no );
      mRBcmtd = new MyCheckBox( mContext, size, R.drawable.iz_comment_ok, R.drawable.iz_comment_no );
      mRBbcks = new MyCheckBox( mContext, size, R.drawable.iz_backsight_ok, R.drawable.iz_backsight_no );
      mRBdup.setOnClickListener( this );
      mRBsurf.setOnClickListener( this );
      mRBcmtd.setOnClickListener( this );
      mRBbcks.setOnClickListener( this );
      nr_buttons += 4;
    }

    // FIXME_X2_SPLAY
    // if ( TDLevel.overAdvanced ) {
    //   if ( shot_xsplay ) {
    //     mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_xsplays_ok, R.drawable.iz_ysplays_no );
    //   } else {
    //     mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_ysplays_ok, R.drawable.iz_xsplays_no );
    //   }
    //   mCBxSplay.setOnClickListener( this );
    // }
    if ( TDLevel.overAdvanced && TDSetting.mSplayClasses ) {
      mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_xsplays_ok, R.drawable.iz_ysplays_plain );
      mCBxSplay.setOnClickListener( this );
      nr_buttons ++;
    }
    setCBxSplay( splay_type );

    if ( TDLevel.overBasic ) {
      mCBbackLeg = new MyCheckBox( mContext, size, R.drawable.iz_backleg_ok, R.drawable.iz_backleg_no );
      mCBbackLeg.setOnClickListener( this );
      nr_buttons ++;
    }

    if ( TDLevel.overExpert && TDSetting.mSplayClasses ) {
      mRBsplay = new MyStateBox( mContext, R.drawable.iz_plan_profile, R.drawable.iz_plan, R.drawable.iz_extended, R.drawable.iz_none );
      mRBsplay.setOnClickListener( this );
      nr_buttons ++;
    } // else mRBsplay = null;
    // mCBhighlight = new MyCheckBox( mContext, size, R.drawable.iz_highlight_ok, R.drawable.iz_highlight_no );

    Button[] mButton = new Button[nr_buttons];
    // TDLog.v( "nr buttons " + nr_buttons );

    int k = 0;
    if ( mRBdup  != null ) mButton[k++] = mRBdup;
    if ( mRBsurf != null ) mButton[k++] = mRBsurf;
    if ( mRBcmtd != null ) mButton[k++] = mRBcmtd;
    if ( mRBbcks != null ) mButton[k++] = mRBbcks;
    mButton[k++] = mCBlegPrev;
    mButton[k++] = mCBlegNext;
    if ( mCBbackLeg != null ) mButton[k++] = mCBbackLeg;
    mButton[k++] = mCBrenumber;
    mButton[k++] = mCBallSplay;
    if ( mRBsplay  != null ) mButton[k++] = mRBsplay;
    if ( mCBxSplay != null ) mButton[k++] = mCBxSplay;

    MyHorizontalListView mListView = (MyHorizontalListView) findViewById(R.id.listview);
    // mListView.setEmptyPlacholder( true );
    /* size = */ TopoDroidApp.setListViewHeight( mContext, mListView );
    MyHorizontalButtonView mButtonView = new MyHorizontalButtonView( mButton );
    mListView.setAdapter( mButtonView.mAdapter );

    layout4.invalidate();

    mCBlegPrev.setOnClickListener( this );
    mCBlegNext.setOnClickListener( this );
    mCBallSplay.setOnClickListener( this );

    LinearLayout mLayoutReverse = (LinearLayout) findViewById(R.id.layout_reverse );
    mButtonReverse = new MyButton( mContext, this, size, R.drawable.iz_swap );
    mLayoutReverse.addView( mButtonReverse );

    mRBleft   = (CheckBox) findViewById(R.id.left );
    mRBvert   = (CheckBox) findViewById(R.id.vert );
    mRBright  = (CheckBox) findViewById(R.id.right );
    // mRBignore = (CheckBox) findViewById(R.id.ignore );

    // if ( TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( TDColor.MID_GRAY );
    // }

    mButtonPrev = (Button) findViewById(R.id.btn_prev );
    mButtonNext = (Button) findViewById(R.id.btn_next );

    mButtonSave = (Button) findViewById(R.id.btn_save );
    mButtonOK   = (Button) findViewById(R.id.btn_ok );
    mButtonMore = (Button) findViewById(R.id.btn_more );
    mButtonBack = (Button) findViewById(R.id.btn_back );

    mLayoutMore = (LinearLayout)findViewById( R.id.layout_more );
    mLayoutMore.setVisibility( View.GONE );

    // mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETfrom.setKeyListener( NumberKeyListener );
    // mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    if ( TDSetting.mPrevNext ) {
      mButtonPrev.setOnClickListener( this );
      mButtonNext.setOnClickListener( this );
      mButtonSave.setOnClickListener( this );
    } else {
      mButtonPrev.setVisibility( View.INVISIBLE );
      mButtonNext.setVisibility( View.INVISIBLE );
      mButtonSave.setVisibility( View.GONE );
    }
    mButtonOK.setOnClickListener( this );
    if ( TDLevel.overNormal ) {
      mButtonMore.setOnClickListener( this );
      // createLayoutLRUD( size ); // INTERMEDIATE_DATA
      createMoreButtons( size );
      createDeleteCheckButtons( size );
    } else {
      mButtonMore.setVisibility( View.GONE );
    }
    mButtonBack.setOnClickListener( this );

    // mButtonReverse.setOnClickListener( this );

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );
    // mRBignore.setOnClickListener( this );

    loadDBlock( mBlk, mPrevBlk, mNextBlk );

    // updateView();
    mParent.mOnOpenDialog = false;


    // tv_stations = (TextView) findViewById( R.id.photo_shot_stations );
    // tv_data = (TextView) findViewById( R.id.photo_shot_data );
    // tv_stations.setText( String.format( mContext.getResources().getString( R.string.shot_name ), mBlk.Name() ) );
    // if ( TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL ) {
    //   tv_data.setText( mBlk.dataStringNormal( mContext.getResources().getString(R.string.shot_data) ) );
    // } else { // SurveyInfo.DATAMODE_DIVING
    //   tv_data.setText( mBlk.dataStringDiving( mContext.getResources().getString(R.string.shot_data) ) );
    // }

  }

  /** save the changes to the DBlock
   */
  private boolean saveDBlock()
  {
    // add LRUD at station if any is checked and data have been entered
    // String station = null;
    // if ( mRBfrom.isChecked() ) {
    //   station = mBlk.mFrom;
    // } else if ( mRBto.isChecked() ) {
    //   station = mBlk.mTo;
    // }
    // if ( station != null ) {
    //   // check the data
    //   mParent.insertLRUDatStation( station, mBlk.mBearing, mBlk.mClino, 
    //     mETleft.getText().toString().replace(',','.') ,
    //     mETright.getText().toString().replace(',','.') ,
    //     mETup.getText().toString().replace(',','.') ,
    //     mETdown.getText().toString().replace(',','.') 
    //   );
    // }
    // TDLog.v("SAVE block");

    boolean can_do_backleg = false;
    boolean backleg_val = mCBbackLeg != null && mCBbackLeg.isChecked();
    boolean all_splay = mCBallSplay.isChecked();
    // FIXME_X2_SPLAY
    int set_xsplay = -1;
    if ( splay_type >= 0 ) {
      // [1] if splay_type > 0 and now mCBxSplay is plain change splay class (possibly "all" splays of the same type) to plain
      // [0]                   otherwise change splay class to all plain splays only if "all" is set
      // [-1] else case
      set_xsplay = splay_type;
    }
    // boolean leg_next = false;
    // boolean shot_secleg = false;
    if ( mCBlegPrev.isChecked() ) {
      // secondary leg shot have empty FROM, TO, ignore EXTEND, no STRETCH, survey FLAG, extra LEG_TYPE, no COMMENT
      mBlk.setTypeSecLeg();
      mParent.updateShotNameAndFlags( "", "", ExtendType.EXTEND_IGNORE, 0, 0, LegType.EXTRA, "", mBlk, false );
      return true;
      // shot_from = "";
      // shot_to   = "";
      // shot_secleg  = true;
      // // can_do_backleg = false;
      // all_splay = false;
      // set_xsplay = -1;
    } else if ( mCBlegNext.isChecked() ) {
      long id = mParent.mergeToNextLeg( mBlk );
      if ( id >= 0 ) {
        shot_from = mBlk.mFrom;
        shot_to   = mBlk.mTo;
      }
      return true;
      // leg_next  = true;
      // all_splay = false;
      // set_xsplay = -1;
    } else {
      shot_from = TDUtil.toStationFromName( mETfrom.getText().toString() ); // NOSPACES this replaces all specials
      if ( ! TDUtil.isStationName( shot_from ) ) {
        mETfrom.setError( mContext.getResources().getString( R.string.bad_station_name ) );
        return false;
      }
      // if ( shot_from == null ) { shot_from = ""; }
      shot_to = TDUtil.toStationToName( mETto.getText().toString() ); // NOSPACES this replaces all specials
      if ( ! TDUtil.isStationName( shot_to ) ) {
        mETto.setError( mContext.getResources().getString( R.string.bad_station_name ) );
        return false;
      }
      can_do_backleg = ( shot_from.length() > 0 ) && ( shot_to.length() > 0 );
    }
    // TDLog.v( "<" + shot_from + "-" + shot_to + "> do backleg " + can_do_backleg + " value " + backleg_val );

    long flag = DBlock.FLAG_SURVEY;
    if ( TDLevel.overNormal ) {
      if ( mRBdup.isChecked() )       { flag = DBlock.FLAG_DUPLICATE; }
      else if ( mRBsurf.isChecked() ) { flag = DBlock.FLAG_SURFACE; }
      else if ( mRBcmtd.isChecked() ) { flag = DBlock.FLAG_COMMENTED; } // FIXME_COMMENTED
      else if ( mRBbcks.isChecked() ) { flag = DBlock.FLAG_BACKSHOT; } // BACKSHOT
      else if ( mRBsplay != null ) {
        if ( mRBsplay.getState() == 1 )      { flag = DBlock.FLAG_NO_PROFILE; }
        else if ( mRBsplay.getState() == 2 ) { flag = DBlock.FLAG_NO_PLAN; }
        else if ( mRBsplay.getState() == 3 ) { flag = DBlock.FLAG_NO_PLAN | DBlock.FLAG_NO_PROFILE; }
        // FIXME TODO add another state for both NO_PLAN and NO_PROFILE
      }
    }
    // else if ( mRBback.isChecked() ) { flag = DBlock.FLAG_BACKSHOT; } // old
    // else                            { flag = DBlock.FLAG_SURVEY; }
    if ( mBlk.isTampered() ) flag |= DBlock.FLAG_TAMPERED;
    // flag |= mBlk.cavwayBits(); // cavway bits are restored by resetFlag()
    shot_flag = mBlk.resetFlag( flag );
    // TDLog.v("shot flag " + shot_flag );

    shot_extend = mBlk.getIntExtend();
    if ( mRBleft.isChecked() )       { shot_extend = ExtendType.EXTEND_LEFT; }
    else if ( mRBvert.isChecked() )  { shot_extend = ExtendType.EXTEND_VERT; }
    else if ( mRBright.isChecked() ) { shot_extend = ExtendType.EXTEND_RIGHT; }
    else                             { shot_extend = ExtendType.EXTEND_IGNORE; }

    // TDLog.v("clr xsplay " + set_xsplay
    //                     + " all splay " + all_splay 
    //                     + " can_do_backleg " + can_do_backleg
    //                     + " backleg val " + backleg_val 
    //                     + " leg_next " + leg_next
    //                     + " secleg " + shot_secleg
    //                     + " F " + shot_from
    //                     + " T " + shot_to
    //                     + " flag " + shot_flag
    //                     + " extend " + shot_extend );


    // if ( shot_secleg ) {
    //   // TDLog.v( "block set sec-leg type ");
    //   mBlk.setTypeSecLeg();
    // } else if ( leg_next ) { // FIXME this can go immediately after the test of the checkbox
    //   // can_do_backleg = false; // not necessary
    //   long id = mParent.mergeToNextLeg( mBlk );
    //   if ( id >= 0 ) {
    //     shot_from = mBlk.mFrom;
    //     shot_to   = mBlk.mTo;
    //   }
    //   return;
    // }
 
    int extend = shot_extend;
    boolean sf_len = shot_from.length() > 0; // 20230118 local var
    boolean st_len = shot_to.length() > 0;
    if ( mBlk.getIntExtend() != shot_extend ) {
      if ( /* leg_next || */ ( sf_len && st_len ) ) { // leg
        mBlk.setExtend( extend, ExtendType.STRETCH_NONE ); // FIXME_STRETCH
      } else if ( ( sf_len /* && ! st_len */ ) || ( st_len /* && ! sf_len */ ) ) { // splay
        // extend = shot_extend + ExtendType.EXTEND_FVERT;
        mBlk.setExtend( extend, ExtendType.STRETCH_NONE ); // FIXME_STRETCH
      }
    }

    String comment = mETcomment.getText().toString();  // COMMENT empty comment is OK
    if ( comment != null ) mBlk.mComment = comment.trim();

    boolean renumber  = false;
    boolean splay_classes = false;
    // boolean highlight = false;
    if ( shot_from.length() > 0 ) {
      if ( shot_to.length() > 0 ) {
        renumber = mCBrenumber.isChecked();
        if ( TDSetting.mSplayClasses ) splay_classes = all_splay;
        all_splay = false;
        set_xsplay = -1;
      // } else { // this is useless: replaced by long-tap on shot list 
      //   if ( mCBhighlight.isChecked() ) {
      //     // TDLog.v("SHOT " + "parent to highlight " + mBlk.mFrom + " " + mBlk.mTo );
      //     mParent.highlightBlock( mBlk );
      //   }
      }
    }

    // TDLog.v("renumber " + renumber + " comment " + comment );

    // TDLog.v( "all_splay " + all_splay + " set_xsplay " + set_xsplay + " splay_type " + splay_type );

    if ( all_splay ) {
      if ( set_xsplay >= 0 ) {
        long leg0 = mBlk.getLegType(); // old leg_type: 0 plain, 2 X, 4 H, 5 V
        long leg1 = LegType.NORMAL;    // new leg_type
        if ( set_xsplay > 0 ) {
          leg0 = leg1;
          leg1 = set_xsplay;
        }
        // TDLog.v("[1] all leg type " + leg0 + " -> " + leg1 + " " + set_xsplay );
        mParent.updateSplayShots( shot_from, shot_to, extend, shot_flag, leg1, comment, mBlk );
        mParent.updateSplayLeg( mPos, leg0, leg1 );
      }
    } else if ( splay_classes ) {
      mParent.setSplayClasses( mPos );

    } else if ( set_xsplay == 0 ) {
      long leg = LegType.NORMAL;
      // TDLog.v("[2] leg type " + leg + " " + set_xsplay );
      // mParent.updateSplayLegType( mBlk, leg );
      mParent.updateShotNameAndFlags( shot_from, shot_to, extend, shot_stretch, shot_flag, leg, comment, mBlk, renumber );
    } else {
      // TDLog.v("other " + set_xsplay );
      // mBlk.setName( shot_from, shot_to ); // done by parent.updateShot
      long leg = LegType.NORMAL;
      if ( set_xsplay > 0 && mBlk.isSplay() ) {
        leg = set_xsplay;
      }
      // TDLog.v("[3] leg type " + leg + " " + set_xsplay );
      if ( can_do_backleg && backleg_val ) {
        leg = LegType.BACK;
      }
      // TDLog.v( "Block is splay " + mBlk.isSplay() + " leg " + leg + " blk type " + mBlk.getBlockType() );
      // TDLog.v("from <" + shot_from + "> to <" + shot_to + ">" );
      mParent.updateShotNameAndFlags( shot_from, shot_to, extend, shot_stretch, shot_flag, leg, comment, mBlk, renumber );
    }
    // mParent.scrollTo( mPos );

    if ( editable ) {
      try {
	if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
          float p = Float.parseFloat( mETdistance.getText().toString() ) / TDSetting.mUnitLength;
	  float b = Float.parseFloat( mETbearing.getText().toString() )  / TDSetting.mUnitAngle;
          float d = Float.parseFloat( mETclino.getText().toString() )    / TDSetting.mUnitLength;
          mParent.updateShotDepthBearingDistance( p, b, d, mBlk );
	} else { // SurveyInfo.DATAMODE_DEPTH
          float d = Float.parseFloat( mETdistance.getText().toString() ) / TDSetting.mUnitLength;
	  float b = Float.parseFloat( mETbearing.getText().toString() )  / TDSetting.mUnitAngle;
          float c = Float.parseFloat( mETclino.getText().toString() )    / TDSetting.mUnitAngle;
          mParent.updateShotDistanceBearingClino( d, b, c, mBlk );
	}
      } catch (NumberFormatException e ) {
        TDLog.e( e.getMessage() );
        return false;
      }
    }

    // if ( renumber ) {
    //   TDLog.v( "renumber shots after block id " + mBlk.mId );
    //   mParent.renumberShotsAfter( mBlk );
    // }

    return true;
  }


  /** display the CutCopyPaste popup for the given edit text
   * @param v   edit text view
   * 
   * This is used for the stations edit texts.
   * FIXME The popup remains open when the user change focus to the other station.
   */
  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.dismissPopup();
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }
    

  @Override
  public void onClick(View v) 
  {
    CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    Button b = (Button) v;
    if ( b == mButtonBack ) {
      CutNPaste.dismissPopup();
      dismiss();
    }

    if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
      // shot_extend = mRBleft.isChecked() ? ExtendType.EXTEND_LEFT : ExtendType.EXTEND_IGNORE;
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
      // shot_extend = mRBvert.isChecked() ? ExtendType.EXTEND_VERT : ExtendType.EXTEND_IGNORE;
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );
      // shot_extend = mRBright.isChecked() ? ExtendType.EXTEND_RIGHT : ExtendType.EXTEND_IGNORE;
    }
    if ( b == mButtonPrev ) {
      mCBallSplay.setVisibility( View.GONE );
      setCBxSplay( -1 );
      // shift:
      //               prev -- blk -- next
      // prevOfPrev -- prev -- blk
      //
      // saveDBlock();
      if ( mPrevBlk != null ) {
        DBlock prevBlock = mParent.getPreviousLegShot( mPrevBlk, true );
        // TDLog.Log( TDLog.LOG_SHOT, "PREV " + mPrevBlk.toString(true ) );
        loadDBlock( mPrevBlk, prevBlock, mBlk );
        // updateView();
      // } else {
        // TDLog.v( "PREV is null" );
      }
    } else if ( b == mButtonNext ) {
      mCBallSplay.setVisibility( View.GONE );
      setCBxSplay( -1 );
      // shift:
      //        prev -- blk -- next
      //                blk -- next -- nextOfNext
      // saveDBlock();
      if ( mNextBlk != null ) {
        DBlock next = mParent.getNextLegShot( mNextBlk, true );
        // TDLog.Log( TDLog.LOG_SHOT, "NEXT " + mNextBlk.toString(true ) );
        loadDBlock( mNextBlk, mBlk, next );
        // updateView();
      // } else {
        // TDLog.v( "NEXT is null" );
      }
    }

    if ( ! TDInstance.isSurveyMutable ) { // IMMUTABLE
      TDToast.makeWarn("Immutable survey");
      return;
    }

    if ( b == mCBlegPrev ) {
      // TDLog.v( "CB leg clicked ");
      if ( mCBlegPrev.toggleState() ) {
        mCBallSplay.setState( false );
        setCBxSplay( -1 );
        mCBlegNext.setState( false );
      }
    } else if ( b == mCBallSplay ) {
      // TDLog.v( "CB all_splay clicked ");
      if ( mCBallSplay.toggleState() ) {
        // if ( mCBxSplay != null ) mCBxSplay.setState( false );
        mCBlegPrev.setState( false );
        mCBlegNext.setState( false );
      }
    } else if ( mCBbackLeg != null && b == mCBbackLeg ) {
      mCBbackLeg.toggleState();
    } else if ( mCBxSplay != null && b == mCBxSplay ) {
      setCBxSplay( LegType.nextSplayClass( splay_type ) );
      mCBallSplay.setState( false );
      mCBlegPrev.setState( false );
      mCBlegNext.setState( false );
    } else if ( b == mCBlegNext ) {
      if ( mCBlegNext.toggleState() ) {
        mCBlegPrev.setState( false );
        mCBallSplay.setState( false );
        setCBxSplay( -1 ); 
      }

    } else if ( mRBdup != null  && b == mRBdup ) {
      if ( mRBdup.toggleState() ) {
        mRBsurf.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
        mRBbcks.setState( false ); // BACKSHOT
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBsurf != null  && b == mRBsurf ) {
      if ( mRBsurf.toggleState() ) {
        mRBdup.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
        mRBbcks.setState( false ); // BACKSHOT
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBcmtd != null  && b == mRBcmtd ) { // FIXME_COMMENTED
      if ( mRBcmtd.toggleState() ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
        mRBbcks.setState( false ); // BACKSHOT
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBbcks != null  && b == mRBbcks ) { // BACKSHOT
      if ( mRBbcks.toggleState() ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBsplay != null && b == mRBsplay ) {
      mRBsplay.setState( ( mRBsplay.getState() + 1 ) % mRBsplay.getNrStates() );
      if ( mRBsplay.getState() > 0 ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
        mRBbcks.setState( false ); // BACKSHOT
      }

    } else if ( b == mButtonMore ) {
      CutNPaste.dismissPopup();
      // dismiss();
      if ( TDLevel.overNormal ) {
        // mParent.onBlockLongClick( mBlk );
        if ( hasMore ) {
          mButtonMore.setText( R.string.button_more );
          mLayoutMore.setVisibility( View.GONE );
          hasMore = false;
        } else {
          mButtonMore.setText( R.string.button_less );
          mLayoutMore.setVisibility( View.VISIBLE );
          hasMore = true;
        }
      }
    } else if ( b == mButtonOK ) { // OK and SAVE close the keyboard
      if ( saveDBlock() ) {
        dismiss();
      } else {
        TDLog.e("OK failed to save block");
      }
    } else if ( b == mButtonSave ) {
      if ( ! saveDBlock() ) {
        TDToast.makeWarn( R.string.shot_not_saved );
      }

    } else if ( b == mButtonReverse ) {
      shot_from = TDUtil.toStationFromName( mETfrom.getText().toString() );
      if ( ! TDUtil.isStationName( shot_from ) ) {
        mETfrom.setError( mContext.getResources().getString( R.string.bad_station_name ) );
        return;
      }
      shot_to = TDUtil.toStationToName( mETto.getText().toString() );
      if ( ! TDUtil.isStationName( shot_to ) ) {
        mETto.setError( mContext.getResources().getString( R.string.bad_station_name ) );
        return;
      }
      if ( shot_to.length() > 0 && shot_from.length() > 0 ) { // TODO REVERSE SPLAY ?
        String temp = shot_from; // new String( shot_from );
        shot_from = shot_to;
        shot_to = temp;
        mETfrom.setText( shot_from );
        mETto.setText( shot_to );
	// FIXME_EXTEND swap extend if set
        if ( mRBleft.isChecked() ) {
	  mRBleft.setChecked( false );
	  mRBright.setChecked( true );
          // shot_extend = ExtendType.EXTEND_RIGHT;
	} else if ( mRBright.isChecked() ) {
	  mRBleft.setChecked( true );
	  mRBright.setChecked( false );
          // shot_extend = ExtendType.EXTEND_LEFT;
	}
       }
    } else if ( (! hideColorBtn) && mBtnColor != null && b == mBtnColor ) {
      new MyColorPicker( mContext, this, mColor ).show();
      return; // dismiss only after the color change
    } else if ( mBtnRecalibrate != null && b == mBtnRecalibrate ) { // RECALIBRATE
      mParent.recalibrate( mBlk.mId );
      dismiss();
    // } else if ( mHasLRUD && b == mBTlrud ) { // AT-STATION LRUD // INTERMEDIATE_DATA
    //   float d = -1;
    //   long at = mBlk.mId;
    //   String station = null;
    //   String from = null;
    //   if ( mRBto.isChecked() ) { // TO
    //     station = mBlk.mTo;
    //   } else if ( mRBfrom.isChecked() ) { // FROM
    //     station = mBlk.mFrom;
    //   } else { 
    //     String dstr = mETat.getText().toString().replace(',','.');
    //     try { d = Float.parseFloat( dstr ); } catch ( NumberFormatException e ) {
    //       TDLog.e("Non-number value");
    //     }
    //     // add a duplicate leg d, mBlk.mBearing, mBlk.mClino
    //     from = mBlk.mFrom;
    //     station = from + "-" + dstr;
    //     // at should be -1L in this case
    //     at = -1L;
    //   }
    //   if ( station != null ) {
    //     // try insert intermediate LRUD
    //     if ( mParent.insertLRUDatStation( at, station, mBlk.mBearing, mBlk.mClino, 
    //       mETleft.getText().toString().replace(',','.') ,
    //       mETright.getText().toString().replace(',','.') ,
    //       mETup.getText().toString().replace(',','.') ,
    //       mETdown.getText().toString().replace(',','.') 
    //       ) ) {
    //       if ( from != null ) {
    //         // TDLog.v("LRUD " + "insert dup leg from " + from + " station " + station ); 
    //         mParent.insertDuplicateLeg( from, station, d, mBlk.mBearing, mBlk.mClino, mBlk.getIntExtend() );
    //       }
    //     }
    //   }
    //   dismiss();
    // } else if ( b == mButtonPlot ) {       // PHOTO
    //   mParent.highlightBlock( mBlk );
    //   dismiss();
    } else if ( mButtonPhoto != null && b == mButtonPhoto ) {  // PHOTO
      mParent.askPhotoComment( mBlk );
      dismiss();
    } else if ( mButtonAudio != null && b == mButtonAudio ) {  // AUDIO
      mParent.startAudio( null, mBlk );
      dismiss();
    } else if ( mButtonSensor != null && b == mButtonSensor ) { // SENSOR
      mParent.askSensor( mBlk );
      dismiss();
    // } else if ( b == mButtonExternal ) {
    //   mParent.askExternal( );
    // } else if ( b == mButtonShot ) {  // INSERT NEW SHOT // INTERMEDIATE_DATA
    //   mParent.dialogInsertShotAt( mBlk );
    //   dismiss();
    } else if ( mButtonSurvey != null && b == mButtonSurvey ) { // SPLIT
      if ( TDLevel.overExpert ) {
        mParent.doSplitOrMoveDialog( mBlk.mId );
        dismiss();
      } else {
        TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.survey_split,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int btn ) {
              mParent.doSplitOrMoveSurvey( mBlk.mId, null );  // null: split
              dismiss();
            }
          } );
        // mParent.askSurvey( );
      }
    } else if ( mButtonCheck != null && b == mButtonCheck ) { // CHECK
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.shot_check,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doDeleteShot( mBlk.mId, mBlk, TDStatus.CHECK, true );
            dismiss();
          }
        } );
    } else if ( b == mButtonDelete ) { // DELETE
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.shot_delete,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doDeleteShot( mBlk.mId, mBlk, TDStatus.DELETED, (mCBleg != null && mCBleg.isChecked()) );
            dismiss();
          }
        } );
      // mParent.doDeleteShot( mBlk.mId );

    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    //   dismiss();
    }
    // } else if ( b == mButtonDrop ) {
    //   mParent.dropShot( mBlk );
    //   onBackPressed();
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }



// -------------------------------------------------------------------

  // private void createLayoutLRUD( int size ) // INTERMEDIATE_DATA
  // {
  //   mHasLRUD = true;
  //   mRBfrom  = (RadioButton)findViewById( R.id.station_from );
  //   mRBto    = (RadioButton)findViewById( R.id.station_to );
  //   mRBat    = (RadioButton)findViewById( R.id.station_at );
  //   mETat    = (EditText)findViewById( R.id.station_distance );
  //   mETleft  = (EditText)findViewById( R.id.shot_left );
  //   mETright = (EditText)findViewById( R.id.shot_right );
  //   mETup    = (EditText)findViewById( R.id.shot_up );
  //   mETdown  = (EditText)findViewById( R.id.shot_down );
  //   mBTlrud  = (Button)findViewById( R.id.lrud_ok );
  // }

  // private void updateLayoutLRUD() // INTERMEDIATE_DATA
  // {
  //   boolean hide_lrud = true;
  //   if ( mBlk.mFrom.length() > 0 ) {
  //     if ( mBlk.mTo.length() > 0 ) {
  //       hide_lrud = false;
  //     // } else {
  //     //   mRBto.setVisibility( View.GONE );
  //     //   mRBat.setVisibility( View.GONE );
  //     //   mETat.setVisibility( View.GONE );
  //     }
  //   } 
  //   if ( hide_lrud ) {
  //     ((LinearLayout)findViewById( R.id.layout_lrud )).setVisibility( View.GONE );
  //     ((LinearLayout)findViewById( R.id.layout_lrud_data )).setVisibility( View.GONE );
  //   } else if ( mHasLRUD ) {
  //     mRBfrom.setText( mBlk.mFrom );
  //     mRBfrom.setChecked( true );
  //     mRBto.setText( mBlk.mTo );
  //     mETat.setText( TDString.ZERO );
  //     mBTlrud.setOnClickListener( this );
  //   }
  // }

  private void createMoreButtons( int size )
  {
    int nr_buttons = 5; // ( mBlk.type() == DBlock.BLOCK_MAIN_LEG )? 7 : 6;

    // mButtonPlot   = new MyCheckBox( mContext, size, R.drawable.iz_plot, R.drawable.iz_plot ); 
    // mButtonPlot.setOnClickListener( this );
    
    // if ( photoCheck ) {
      mButtonPhoto  = new MyCheckBox( mContext, size, R.drawable.iz_camera, R.drawable.iz_camera ); 
      mButtonPhoto.setOnClickListener( this );
    // } else {
    //   mButtonPhoto = null;
    //   -- nr_buttons;
    // }

    if ( hasAudio ) {
      mButtonAudio = new MyCheckBox( mContext, size, R.drawable.iz_audio, R.drawable.iz_audio ); 
      mButtonAudio.setOnClickListener( this );
    } else {
      mButtonAudio = null;
      -- nr_buttons;
    }

    if ( TDSetting.mWithSensors ) {
      mButtonSensor = new MyCheckBox( mContext, size, R.drawable.iz_sensor, R.drawable.iz_sensor ); 
      mButtonSensor.setOnClickListener( this );
    } else {
      mButtonSensor = null;
      -- nr_buttons;
    }

    // mButtonShot   = new MyCheckBox( mContext, size, R.drawable.iz_add_leg, R.drawable.iz_add_leg ); // INTERMEDIATE_DATA
    // mButtonShot.setOnClickListener( this );

    if ( TDLevel.overAdvanced ) {
      mButtonSurvey = new MyCheckBox( mContext, size, R.drawable.iz_split, R.drawable.iz_split );
      mButtonSurvey.setOnClickListener( this );
    } else {
      mButtonSurvey = null;
      -- nr_buttons;
    }
    Button[] mButtonX = new Button[nr_buttons];
    int pos = 0;
    if ( mButtonPhoto  != null ) mButtonX[pos++] = mButtonPhoto;
    if ( mButtonAudio  != null ) mButtonX[pos++] = mButtonAudio;
    if ( mButtonSensor != null ) mButtonX[pos++] = mButtonSensor;
    // if ( mButtonShot   != null ) mButtonX[pos++] = mButtonShot; // INTERMEDIATE_DATA
    if ( mButtonSurvey != null ) mButtonX[pos++] = mButtonSurvey;

    LinearLayout layout4x = (LinearLayout) findViewById( R.id.layout4x );
    layout4x.setMinimumHeight( size + 20 );

    MyHorizontalListView mListViewX = (MyHorizontalListView) findViewById(R.id.listviewx);
    // mListView.setEmptyPlaceholder( true );
    /* size = */ TopoDroidApp.setListViewHeight( mContext, mListViewX );
    MyHorizontalButtonView mButtonViewX = new MyHorizontalButtonView( mButtonX );
    mListViewX.setAdapter( mButtonViewX.mAdapter );
    layout4x.invalidate();
  }

  private void createDeleteCheckButtons( int size )
  {
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );
    LinearLayout layout4b = (LinearLayout) findViewById( R.id.layout4b );
    layout4b.setMinimumHeight( size + 20 );
    mBtnColor = (Button) findViewById( R.id.btn_color );

    mButtonCheck  = new MyCheckBox( mContext, size, R.drawable.iz_compute_transp, R.drawable.iz_compute_transp );
    mButtonCheck.setOnClickListener( this );
    layout4b.addView( mButtonCheck );
    mButtonCheck.setLayoutParams( lp );

    if ( ! TDInstance.isDivingMode() ) {
      mBtnRecalibrate  = new MyCheckBox( mContext, size, R.drawable.iz_compute, R.drawable.iz_compute );
      mBtnRecalibrate.setOnClickListener( this );
      layout4b.addView( mBtnRecalibrate );
      mBtnRecalibrate.setLayoutParams( lp );
    }

    mButtonDelete = new MyCheckBox( mContext, size, R.drawable.iz_delete_transp, R.drawable.iz_delete_transp );
    mButtonDelete.setOnClickListener( this );
    // mCBleg = (CheckBox) findViewById( R.id.leg ); // delete whole leg
    layout4b.addView( mButtonDelete );
    mButtonDelete.setLayoutParams( lp );

    mCBleg = new CheckBox( mContext );
    mCBleg.setText( R.string.delete_whole_leg );
    layout4b.addView( mCBleg );
    mCBleg.setLayoutParams( lp );
  }

  private void updateDeleteCheckButtons()
  {
    if ( mBtnColor != null ) {
      if ( TDLevel.overExpert ) {
        if ( mBlk.isSplay() ) {
          if ( TDSetting.mSplayColor ) {
            mBtnColor.setBackgroundColor( mColor ); 
            mBtnColor.setOnClickListener( this );
            hideColorBtn = false;
          }
        }
      }
      if ( hideColorBtn ) {
        mBtnColor.setVisibility( View.GONE );
      } else {
        mBtnColor.setVisibility( View.VISIBLE );
      }
    }

    if ( mCBleg != null ) {
      if ( mBlk.isMainLeg() ) {
        mCBleg.setChecked( false );
        mCBleg.setVisibility( View.VISIBLE );
      } else {
        mCBleg.setVisibility( View.GONE );
      }
    }

    if ( mButtonCheck != null ) {
      if ( mBlk.isMainLeg() ) {
        if ( TDLevel.overAdvanced && mBlk.isDistoX() ) {
          mButtonCheck.setVisibility( View.VISIBLE );
        } else {
          mButtonCheck.setVisibility( View.GONE );
        }
      } else {
        mButtonCheck.setVisibility( View.GONE );
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
    if ( color != mColor ) {
      mParent.updateBlockColor( mBlk, color );
      // mColor = color;
      // mBtnColor.setBackgroundColor( mColor );
      dismiss();
    } else {
      // TDLog.v("color not changed");
    }
  }

}

