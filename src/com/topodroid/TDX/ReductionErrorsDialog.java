/* @file ReductionErrorsDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration coefficients display dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDColor;
import com.topodroid.utils.StringPair;
import com.topodroid.ui.MyDialog;
import com.topodroid.num.TDNum;
import com.topodroid.num.NumCycle;
// import com.topodroid.prefs.TDSetting;

// import java.lang.ref.WeakReference;
// import java.util.Locale;
import java.util.List;

import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
// import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.view.View;

// import android.graphics.Bitmap;

public class ReductionErrorsDialog extends MyDialog
                       implements View.OnClickListener
{
  // private final WeakReference<DrawingActivity> mParent; 

  // private Button mButtonBack;

  private List< StringPair >  mMultiBad;
  private int mMagneticBad;
  private boolean mExtendBad;
  private boolean mAttachedBad;
  private int mNrBadLoops;
  private List< NumCycle > mBadLoops;

  public ReductionErrorsDialog( Context context, // DrawingWindow parent,
                    List< StringPair > multi_bad, int magnetic_bad, TDNum num )
  {
    super( context, null, R.string.ReductionErrorsDialog ); // null app
    // mParent = new WeakReference<GMActivity>( parent );

    mMultiBad    = multi_bad;
    mMagneticBad = magnetic_bad;
    mExtendBad   = ! num.surveyExtend; // extend_bad;
    mAttachedBad = ! num.surveyAttached; // attached_bad;
    mNrBadLoops  = num.nrInaccurateLoops;
    mBadLoops    = num.getBadLoops();
    
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.reduction_errors_dialog, R.string.title_reduction_errors );

    TextView textMulti  = (TextView) findViewById(R.id.multi_bad);
    if ( mMultiBad.size() == 0 ) {
      textMulti.setVisibility( View.GONE );
    } else {
      StringBuilder msg = new StringBuilder();
      for (StringPair sp : mMultiBad ) {
        msg.append(" ");
        msg.append( sp.toString() );
        // TDLog.v("Bad sibling " + sp.toString() );
      }
      textMulti.setText( String.format( mContext.getResources().getString( R.string.survey_bad_siblings ), msg.toString() ) );
    }
      
    TextView textMagn   = (TextView) findViewById(R.id.magnetic_bad);
    if ( mMagneticBad == 0 ) {
      textMagn.setVisibility( View.GONE );
    }

    TextView textExtend = (TextView) findViewById(R.id.extend_bad);
    if ( ! mExtendBad ) {
      textExtend.setVisibility( View.GONE );
    }

    TextView textAttach = (TextView) findViewById(R.id.attached_bad);
    if ( ! mAttachedBad ) {
      textAttach.setVisibility( View.GONE );
    }

    TextView badLoops = (TextView) findViewById( R.id.loops_bad );
    ListView loopList = (ListView) findViewById( R.id.loop_list );
    if ( mNrBadLoops == 0 ) {
      badLoops.setVisibility( View.GONE );
      loopList.setVisibility( View.GONE );
    } else {
      badLoops.setText( String.format( mContext.getResources().getString( R.string.loops_bad ), mNrBadLoops ) );
      ArrayAdapter<String> adapter = new ArrayAdapter<>( mContext, R.layout.message );
      for ( NumCycle cl : mBadLoops ) adapter.add( cl.toString() );
      loopList.setAdapter( adapter );
    }

    Button button_back  = (Button) findViewById( R.id.button_back );
    button_back.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    // int id = v.getId();
    dismiss();
  }

}

