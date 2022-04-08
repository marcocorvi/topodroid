/* @file ShotDialog.java
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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
// import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyStateBox;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;

import java.util.regex.Pattern;

// import android.app.Dialog;
import android.os.Bundle;
// import android.widget.RadioButton;

import android.text.method.KeyListener;
import android.text.InputType;

import android.content.Context;
// import android.content.res.Resources;
// import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import android.view.View;
// import android.graphics.drawable.BitmapDrawable;

class ShotDialog extends MyDialog
                 implements View.OnClickListener
                          , View.OnLongClickListener
{
  private final ShotWindow mParent;
  private DBlock mBlk;
  private DBlock mPrevBlk;
  private DBlock mNextBlk;
  private int mPos; // item position in the parent' adapter list

  private Pattern mPattern; // name pattern

  // private TextView mTVdata;
  private EditText mETdistance; // distance | depth
  private EditText mETbearing;
  private EditText mETclino;    // clino | distance

  private TextView mTVextra;

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;
  
  private MyCheckBox mRBdup   = null;
  private MyCheckBox mRBsurf  = null;
  private MyCheckBox mRBcmtd  = null;  // FIXME_COMMENTED 
  private MyStateBox mRBsplay = null;  // inhibit splay plan/profile display

  private MyCheckBox mCBlegPrev;
  private MyCheckBox mCBlegNext;
  private MyCheckBox mCBrenumber;
  private MyCheckBox mCBallSplay;
  private MyCheckBox mCBxSplay = null;
  // private MyCheckBox mCBhighlight;
  private MyCheckBox mCBbackLeg = null;

  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView;
  private Button[] mButton;

  private CheckBox mRBleft;
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
  private boolean shot_manual;
  private float shot_stretch; // FIXME_STRETCH

  private int set_xsplay = -1;

  private String shot_extra;
  private int  shot_extend;
  private long shot_flag;
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

  ShotDialog( Context context, ShotWindow parent, int pos, DBlock blk,
              DBlock prev, DBlock next
            )
  {
    super( context, R.string.ShotDialog );
    mParent = parent;
    mPos = pos;
    mFirst = true;
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    // loadDBlock( blk, prev, next );
    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog " + blk.toString(true) );
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
  }


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
    shot_manual   = blk.isManual();

    // TDLog.v( "shot is manual " + shot_manual + " length " + shot_distance );

    // shot_extra   = blk.extraString( mParent.mSurveyAccuracy );
    shot_extra   = mParent.getBlockExtraString( blk );
    shot_extend  = blk.getIntExtend();
    shot_stretch = blk.getStretch(); // FIXME_STRETCH
    shot_flag    = blk.getFlag();
    shot_comment = blk.mComment;

    // shot_secleg  = blk.isSecLeg(); // DBlock.BLOCK_SEC_LEG;
    // shot_backleg = blk.isBackLeg();
    // shot_xsplay  = blk.isOtherSplay(); // DBlock.BLOCK_X_SPLAY | H_SPLAY | V_SPLAY
    // if ( blk.type() != DBlock.BLOCK_MAIN_LEG ) mCBdeleteLeg.setVisibility( View.GONE );

    updateView();
  }

  private void updateView()
  {
    // mTVdata.setText( shot_data );
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
    mETcomment.setText( ((shot_comment != null)? shot_comment : "") );
   
    // if ( DBlock.isSurvey(shot_flag) ) { mRBreg.setChecked( true ); }
    if ( TDLevel.overNormal ) {
      mRBdup.setState(  false );
      mRBsurf.setState( false );
      mRBcmtd.setState( false ); // FIXME_COMMENTED
      if ( DBlock.isDuplicate(shot_flag) )      { mRBdup.setState(  true ); }
      else if ( DBlock.isSurface(shot_flag) )   { mRBsurf.setState( true ); }
      else if ( DBlock.isCommented(shot_flag) ) { mRBcmtd.setState( true ); } // FIXME_COMMENTED
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
      MyKeyboard.setEditable( mETdistance, mKeyboard, mKLdistance, shot_manual, flagDepth );
      MyKeyboard.setEditable( mETbearing,  mKeyboard, mKLbearing,  shot_manual, flagBearing );
      MyKeyboard.setEditable( mETclino,    mKeyboard, mKLclino,    shot_manual, flagDistance );
    } else {
      MyKeyboard.setEditable( mETdistance, mKeyboard, mKLdistance, shot_manual, flagDistance );
      MyKeyboard.setEditable( mETbearing,  mKeyboard, mKLbearing,  shot_manual, flagBearing );
      MyKeyboard.setEditable( mETclino,    mKeyboard, mKLclino,    shot_manual, flagClino );
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

    initLayout( R.layout.shot_dialog, null );

    // mTVdata    = (TextView) findViewById(R.id.shot_data );
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

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
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

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    // TDToast.make( "SIZE " + size );
    
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
      mRBdup.setOnClickListener( this );
      mRBsurf.setOnClickListener( this );
      mRBcmtd.setOnClickListener( this );
      nr_buttons += 3;
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

    mButton = new Button[nr_buttons];
    // TDLog.v( "nr buttons " + nr_buttons );

    int k = 0;
    if ( mRBdup  != null ) mButton[k++] = mRBdup;
    if ( mRBsurf != null ) mButton[k++] = mRBsurf;
    if ( mRBcmtd != null ) mButton[k++] = mRBcmtd;
    mButton[k++] = mCBlegPrev;
    mButton[k++] = mCBlegNext;
    if ( mCBbackLeg != null ) mButton[k++] = mCBbackLeg;
    mButton[k++] = mCBrenumber;
    mButton[k++] = mCBallSplay;
    if ( mRBsplay  != null ) mButton[k++] = mRBsplay;
    if ( mCBxSplay != null ) mButton[k++] = mCBxSplay;

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    // mListView.setEmptyPlacholder( true );
    /* size = */ TopoDroidApp.setListViewHeight( mContext, mListView );
    mButtonView = new MyHorizontalButtonView( mButton );
    mListView.setAdapter( mButtonView.mAdapter );

    layout4.invalidate();

    mCBlegPrev.setOnClickListener( this );
    mCBlegNext.setOnClickListener( this );
    mCBallSplay.setOnClickListener( this );

    mButtonReverse = (Button)  findViewById(R.id.shot_reverse );

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
    } else {
      mButtonMore.setVisibility( View.GONE );
    }
    mButtonBack.setOnClickListener( this );


    mButtonReverse.setOnClickListener( this );

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );
    // mRBignore.setOnClickListener( this );

    loadDBlock( mBlk, mPrevBlk, mNextBlk );
    // updateView();
    mParent.mOnOpenDialog = false;
  }

  private void saveDBlock()
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

    boolean do_backleg = false;
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
      mBlk.setTypeSecLeg();
      mParent.updateShotNameAndFlags( "", "", ExtendType.EXTEND_IGNORE, 0, 0, LegType.EXTRA, "", mBlk );
      return;
      // shot_from = "";
      // shot_to   = "";
      // shot_secleg  = true;
      // // do_backleg = false;
      // all_splay = false;
      // set_xsplay = -1;
    } else if ( mCBlegNext.isChecked() ) {
      long id = mParent.mergeToNextLeg( mBlk );
      if ( id >= 0 ) {
        shot_from = mBlk.mFrom;
        shot_to   = mBlk.mTo;
      }
      return;
      // leg_next  = true;
      // all_splay = false;
      // set_xsplay = -1;
    } else {
      shot_from = TDUtil.noSpaces( mETfrom.getText().toString() );
      // if ( shot_from == null ) { shot_from = ""; }
      shot_to = TDUtil.noSpaces( mETto.getText().toString() );
      do_backleg = ( shot_from.length() > 0 ) && ( shot_to.length() > 0 );
    }
    // TDLog.v( "<" + shot_from + "-" + shot_to + "> do backleg " + do_backleg + " value " + backleg_val );

    shot_flag = DBlock.FLAG_SURVEY;
    if ( TDLevel.overNormal ) {
      if ( mRBdup.isChecked() )       { shot_flag = DBlock.FLAG_DUPLICATE; }
      else if ( mRBsurf.isChecked() ) { shot_flag = DBlock.FLAG_SURFACE; }
      else if ( mRBcmtd.isChecked() ) { shot_flag = DBlock.FLAG_COMMENTED; } // FIXME_COMMENTED
      else if ( mRBsplay != null ) {
        if ( mRBsplay.getState() == 1 )      { shot_flag = DBlock.FLAG_NO_PROFILE; }
        else if ( mRBsplay.getState() == 2 ) { shot_flag = DBlock.FLAG_NO_PLAN; }
        else if ( mRBsplay.getState() == 3 ) { shot_flag = DBlock.FLAG_NO_PLAN | DBlock.FLAG_NO_PROFILE; }
        // FIXME TODO add another state for both NO_PLAN and NO_PROFILE
      }
    }
    // else if ( mRBback.isChecked() ) { shot_flag = DBlock.FLAG_BACKSHOT; }
    // else                            { shot_flag = DBlock.FLAG_SURVEY; }
    // TDLog.v("shot flag " + shot_flag );

    shot_extend = mBlk.getIntExtend();
    if ( mRBleft.isChecked() )       { shot_extend = ExtendType.EXTEND_LEFT; }
    else if ( mRBvert.isChecked() )  { shot_extend = ExtendType.EXTEND_VERT; }
    else if ( mRBright.isChecked() ) { shot_extend = ExtendType.EXTEND_RIGHT; }
    else                             { shot_extend = ExtendType.EXTEND_IGNORE; }

    // TDLog.v("clr xsplay " + set_xsplay
    //                     + " all splay " + all_splay 
    //                     + " do_backleg " + do_backleg
    //                     + " backleg val " + backleg_val 
    //                     + " leg_next " + leg_next
    //                     + " secleg " + shot_secleg
    //                     + " F " + shot_from
    //                     + " T " + shot_to
    //                     + " flag " + shot_flag
    //                     + " extend " + shot_extend );

    mBlk.resetFlag( shot_flag );

    // if ( shot_secleg ) {
    //   // TDLog.v( "block set sec-leg type ");
    //   mBlk.setTypeSecLeg();
    // } else if ( leg_next ) { // FIXME this can go immediately after the test of the checkbox
    //   // do_backleg = false; // not neceessary
    //   long id = mParent.mergeToNextLeg( mBlk );
    //   if ( id >= 0 ) {
    //     shot_from = mBlk.mFrom;
    //     shot_to   = mBlk.mTo;
    //   }
    //   return;
    // }
 
    int extend = shot_extend;
    boolean sflen = shot_from.length() > 0;
    boolean stlen = shot_to.length() > 0;
    if ( mBlk.getIntExtend() != shot_extend ) {
      if ( /* leg_next || */ ( sflen && stlen ) ) { // leg
        mBlk.setExtend( extend, ExtendType.STRETCH_NONE ); // FIXME_STRETCH
      } else if ( ( sflen /* && ! stlen */ ) || ( stlen /* && ! sflen */ ) ) { // splay
        // extend = shot_extend + ExtendType.EXTEND_FVERT;
        mBlk.setExtend( extend, ExtendType.STRETCH_NONE ); // FIXME_STRETCH
      }
    }

    String comment = mETcomment.getText().toString();
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
      //     TDLog.v("SHOT " + "parent to highlight " + mBlk.mFrom + " " + mBlk.mTo );
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
      mParent.updateShotNameAndFlags( shot_from, shot_to, extend, shot_stretch, shot_flag, leg, comment, mBlk );
    } else {
      // TDLog.v("other " + set_xsplay );
      // mBlk.setName( shot_from, shot_to ); // done by parent.updateShot
      long leg = LegType.NORMAL;
      if ( set_xsplay > 0 && mBlk.isSplay() ) {
        leg = set_xsplay;
      }
      // TDLog.v("[3] leg type " + leg + " " + set_xsplay );
      if ( do_backleg && backleg_val ) {
        leg = LegType.BACK;
      }
      // TDLog.v( "Block is splay " + mBlk.isSplay() + " leg " + leg + " blk type " + mBlk.getBlockType() );
      // TDLog.v("from <" + shot_from + "> to <" + shot_to + ">" );
      mParent.updateShotNameAndFlags( shot_from, shot_to, extend, shot_stretch, shot_flag, leg, comment, mBlk );
    }
    // mParent.scrollTo( mPos );

    if ( shot_manual ) {
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
      } catch (NumberFormatException e ) { }
    }

    if ( renumber ) {
      // TDLog.v( "renumber shots after block id " + mBlk.mId );
      mParent.renumberShotsAfter( mBlk );
    }
  }


  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }
    

  @Override
  public void onClick(View v) 
  {
    CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "ShotDialog onClick button " + b.getText().toString() );

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

    } else if ( b == mCBlegPrev ) {
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
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBsurf != null  && b == mRBsurf ) {
      if ( mRBsurf.toggleState() ) {
        mRBdup.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBcmtd != null  && b == mRBcmtd ) { // FIXME_COMMENTED
      if ( mRBcmtd.toggleState() ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBsplay != null && b == mRBsplay ) {
      mRBsplay.setState( ( mRBsplay.getState() + 1 ) % mRBsplay.getNrStates() );
      if ( mRBsplay.getState() > 0 ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
      }

    } else if ( b == mButtonBack ) {
      CutNPaste.dismissPopup();
      dismiss();
    } else if ( b == mButtonMore ) {
      CutNPaste.dismissPopup();
      dismiss();
      if ( TDLevel.overNormal ) mParent.onBlockLongClick( mBlk );
    } else if ( b == mButtonOK ) { // OK and SAVE close the keyboard
      saveDBlock();
      dismiss();
    } else if ( b == mButtonSave ) {
      saveDBlock();

    } else if ( b == mButtonPrev ) {
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
      } else {
        TDLog.Log( TDLog.LOG_SHOT, "PREV is null" );
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
      } else {
        TDLog.Log( TDLog.LOG_SHOT, "NEXT is null" );
      }

    } else if ( b == mButtonReverse ) {
      shot_from = mETfrom.getText().toString();
      shot_from = TDUtil.noSpaces( shot_from );
      shot_to = mETto.getText().toString();
      shot_to = TDUtil.noSpaces( shot_to );
      if ( shot_to.length() > 0 && shot_from.length() > 0 ) {
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
    // } else if ( b == mButtonDrop ) {
    //   mParent.dropShot( mBlk );
    //   onBackPressed();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}

