/* @file AudioDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid audio dialog to register a comment for a shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.ui.MyStateBox;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

import java.io.File;
import java.io.IOException;

// import android.app.Dialog;
import android.os.Bundle;
// import android.os.Environment;

import android.content.Context;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.widget.LinearLayout;
import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.Window;
// import android.view.WindowManager;

import android.widget.Button;
import android.widget.TextView;

class AudioDialog extends MyDialog
                  implements View.OnClickListener
{
  private static final int ACTION_NONE = 0;
  private static final int ACTION_DELETE = 1;
  private static final int ACTION_OVERWRITE = 2;
  private int mAction = 0;

  private MediaPlayer   mMP;
  private MediaRecorder mMR;

  private MyStateBox mBtnPlay;
  private MyStateBox mBtnRec;
  private MyStateBox mBtnDelete;

  private Button mBtnConfirm;
  // private Button mBtnClose;

  private final IAudioInserter mParent;
  private final long mBid;
  private final String mFilepath;
  private boolean hasFile;
  private boolean canRec;
  private boolean canPlay;
  private DBlock  mBlk;
  // AudioInfo mAudio;

  // @param bid    block Id
  AudioDialog( Context ctx, IAudioInserter parent, long bid, DBlock blk )
  {
    super( ctx, R.string.AudioDialog );

    mParent = parent;
    mBid = bid;
    // mAudio = mApp.mData.getAudio( TDInstance.sid, mBid );
    mFilepath = TDPath.getSurveyAudioFile( TDInstance.survey, Long.toString(mBid) );
    // Log.v("DistoX", "audio dialog " + bid + " file: " + mFilepath );
    hasFile = (TDFile.getFile( mFilepath )).exists();
    mBlk    = blk;
  }


  @Override
  protected void onCreate( Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.audio_dialog, R.string.title_audio );

    LinearLayout layout2 = (LinearLayout) findViewById( R.id.layout2 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout2.setMinimumHeight( size + 40 );
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 10, 10, 20, 20 );

    // mBtnDelete = (Button) findViewById( R.id.audio_delete );
    // mBtnClose = (Button) findViewById( R.id.audio_close );
    // mBtnClose.setOnClickListener( this );
    ( (Button) findViewById( R.id.audio_close ) ).setOnClickListener( this );
    if ( mBlk != null ) {
      ( (TextView) findViewById( R.id.audio_id ) ).setText( String.format( mContext.getResources().getString( R.string.audio_id_shot ), mBlk.mFrom, mBlk.mTo ) );
    } else { 
      ( (TextView) findViewById( R.id.audio_id ) ).setText( String.format( mContext.getResources().getString( R.string.audio_id_plot ), mBid ) );
    }

    mBtnRec    = new MyStateBox( mContext, R.drawable.iz_audio_rec, R.drawable.iz_audio_rec_on );
    mBtnPlay   = new MyStateBox( mContext, R.drawable.iz_audio_play_off, R.drawable.iz_audio_play, R.drawable.iz_audio_stop );
    mBtnDelete = new MyStateBox( mContext, R.drawable.iz_audio_delete, R.drawable.iz_audio_delete );
    // mBtnConfirm = new Button( mContext );
    mBtnConfirm = (Button) findViewById( R.id.audio_confirm );

    mBtnRec.setOnClickListener( this );
    mBtnPlay.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );
    mBtnConfirm.setOnClickListener( this );
    mBtnConfirm.setText( R.string.audio_paused );
    mAction = ACTION_NONE;

    canRec  = true;
    canPlay = hasFile;
    mBtnRec.setState( 0 );
    mBtnPlay.setState( hasFile ? 1 : 0 );

    layout2.addView( mBtnPlay, lp );
    layout2.addView( mBtnRec, lp );
    layout2.addView( mBtnDelete, lp );
    // layout2.addView( mBtnConfirm, lp );
    layout2.invalidate();
  }

  public void onClick(View v) 
  {
    try {
      MyStateBox b = (MyStateBox)v;
      if ( b == mBtnDelete ) {
        if ( hasFile ) { // delete audio file
	  // Log.v("DistoX", "audio delete ask confirm");
          mAction = ACTION_DELETE;
          mBtnConfirm.setText(  R.string.audio_delete );
          return;
          // File file = TDFile.getFile( mFilepath );
          // file.delete();
          // mApp.mData.dropAudio( TDInstance.sid, mBid );
        }
	// Log.v("DistoX", "audio delete has no file");
      } else if ( b == mBtnPlay ) {
        mAction = ACTION_NONE;
        mBtnConfirm.setText( R.string.audio_paused );
        if ( canPlay ) {
          int sp = mBtnPlay.getState();
          if ( sp == 2 ) {
	    // Log.v("DistoX", "audio play stop");
            stopPlay();
          } else if ( sp == 1 ) {
	    // Log.v("DistoX", "audio play start");
            startPlay();
          }
        // } else {
	//   Log.v("DistoX", "audio play cannot play");
	}
        return;
      } else if ( b == mBtnRec ) {
        if ( canRec ) {
          int sr = mBtnRec.getState();
          if ( sr == 1 ) {
	    // Log.v("DistoX", "audio record stop");
            stopRec();
          } else if ( sr == 0 ) {
            if ( hasFile ) {
	      // Log.v("DistoX", "audio record ask overwrite");
              mAction = ACTION_OVERWRITE;
              mBtnConfirm.setText( R.string.audio_overwrite );
            } else {
	      // Log.v("DistoX", "audio record start");
              startRec();
            }
          }
	// } else {
	//   Log.v("DistoX", "audio record cannot record");
        }
        return;
      }
    } catch ( ClassCastException e ) { /* THIS IS OK */ }
    if ( mAction > ACTION_NONE ) {
      try {
        if ( (Button)v == mBtnConfirm ) {
          if ( mAction == ACTION_DELETE ) {
	    // Log.v("DistoX", "audio delete");
            deleteAudio();
          } else if ( mAction == ACTION_OVERWRITE ) {
	    // Log.v("DistoX", "audio overwrite start record");
            startRec();
            return;
          }
        // } else {
	//   Log.v("DistoX", "audio confirm undefined");
	}
      } catch ( ClassCastException e ) { /* THIS IS OK */ }
    }
    dismiss();
  }

  private void deleteAudio()
  {
    TDFile.deleteFile( mFilepath );
    TopoDroidApp.mData.deleteAudio( TDInstance.sid, mBid );
    if ( mParent != null ) mParent.deletedAudio( mBid );
  }

  private void startRec()
  {
    try {
      // if ( mParent != null ) mParent.startRecordAudio( mBid ); // startRecordAudio has empty implementation
      mMR = new MediaRecorder();
      mMR.setAudioSource(MediaRecorder.AudioSource.MIC);
      mMR.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      mMR.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT); // AMR_NB
      mMR.setOutputFile( mFilepath );
      mMR.prepare();
      canPlay = false;
      mBtnRec.setState( 1 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_recording );
      mMR.start();
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal State " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error("I/O error " + e.getMessage() );
    }
  }

  private void stopRec()
  {
    try {
      mMR.stop();
      mMR.release();
      mMR = null;
      mBtnRec.setState( 0 );
      mBtnPlay.setState( 1 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_paused );
      canPlay = true;
      hasFile = true;
      TopoDroidApp.mData.setAudio( TDInstance.sid, mBid, TDUtil.currentDateTime() );
      if ( mParent != null ) mParent.stopRecordAudio( mBid );
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    } catch ( RuntimeException e ) {
      TDLog.Error("Runtime error " + e.getMessage() );
    }
  }

  private void startPlay()
  {
    try {
      mMP = new MediaPlayer();
      mMP.setDataSource( mFilepath );
      mMP.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion( MediaPlayer mp ) 
        { 
          mp.release();
          canRec = true;
          mBtnPlay.setState( 1 );
          mBtnConfirm.setText(  R.string.audio_paused );
        }
      } );
      mMP.prepare();
      canRec = false;
      mBtnPlay.setState( 2 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText(  R.string.audio_playing );
      mMP.start();
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error("I/O error " + e.getMessage() );
    }
  }

  private void stopPlay()
  {
    try {
      canRec = true;
      if ( mMP != null ) {
        mMP.stop();
	mMP.release();
	mMP = null;
      }
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_paused );
      mBtnPlay.setState( 1 );
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    // } catch ( RuntimeException e ) {
    //   TDLog.Error("Runtime error " + e.getMessage() );
    }
  }

}

