/** @file TdmEquateNewDialog.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid Manager new equate dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDString;
import com.topodroid.util.TDAnalytics;
import com.topodroid.ui.MyDialog;
import com.topodroid.TDX.R;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TDToast;

// import java.util.List;
import java.util.ArrayList;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
// import android.view.Window;

import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
// import android.widget.Spinner;
// import android.widget.ArrayAdapter;

import java.util.Locale;

class TdmEquateNewDialog extends MyDialog
                         implements OnClickListener
{
  TdmViewActivity mParent;
  ArrayList< TdmViewCommand > mCommands;
  // String[] mStation;
  // Spinner[] mSpinner;
  EditText[] mEdit;
  int mSize;
  int mCommandsSize;

  private Button mBTok;
  private Button mBTback;
  private Button mBTall; // HB EQ all
  private Button mBTsearch; // HB EQ all
  private Button mBTone; // HB EQ one
  int j0=0; // HB EQ all
  int l0=0; // HB EQ all

  TdmEquateNewDialog( Context context, TdmViewActivity parent, ArrayList< TdmViewCommand > commands )
  {
    super( context, null, R.string.TdmEquateNewDialog ); // null app
    mParent   = parent;
    mCommands = commands;
    mCommandsSize = mCommands.size();
    mSize = mCommandsSize;
    if ( mSize == 1 ) mSize = 2;
    // mStation = new String[ mSize ];
    mEdit = new EditText[ mSize ];
    // mSpinner = new Spinner[ mSize ];
    for ( int k = 0; k < mSize; ++k ) {
      // mStation[k] = null;
      mEdit[k] = null;
      // mSpinner[k] = null;
    }
    TopoDroidApp.updateAnalytic( TDAnalytics.TDM_EQ_DIALOG );
  }

  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.tdequate_new_dialog, R.string.title_equate_new );

    mBTok = (Button) findViewById( R.id.button_ok );
    mBTok.setOnClickListener( this );
    mBTback = (Button) findViewById( R.id.button_cancel );
    mBTback.setOnClickListener( this );
    mBTall = (Button) findViewById( R.id.button_all ); // HB EQ all
    mBTall.setOnClickListener( this ); // HB EQ all
    mBTsearch = (Button) findViewById( R.id.button_search ); // HB EQ all
    mBTsearch.setOnClickListener( this ); // HB EQ all
    mBTone = (Button) findViewById( R.id.button_one ); // HB EQ one
    mBTone.setOnClickListener( this ); // HB EQ one

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams( 
    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp1.setMargins( 0, 10, 20, 10 );
    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( 
    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp2.setMargins( 0, 10, 20, 10 );

    for ( int k=0; k<mSize; ++k ) {
      TdmViewCommand vc = mCommands.get( ( (mCommandsSize == 1)? 0 : k ) );
      // List< TdmViewStation > vs = vc.mStations;
      
      LinearLayout layout = new LinearLayout( mContext );
      TextView text = new TextView( mContext );
      text.setText( vc.name() ); // vc.fullname()
      mEdit[k] = new EditText( mContext );
      if ( vc.isParentRoot() ) {
        mEdit[k].setHint( R.string.ellipsis );
      } else {
        String name = vc.firstStation();
        if ( name == null ) {
          mEdit[k].setHint( R.string.ellipsis );
        } else {
          mEdit[k].setHint( name );
        }
      }
      
      // mSpinner[k] = new Spinner( mContext );
      // ArrayAdapter adapter = new ArrayAdapter<String>( mContext, R.layout.menu, mTypes );
      // spinner[k].setAdapter( adapter );
      layout.addView( text, lp1 );
      layout.addView( mEdit[k], lp2 );
      // layout.addView( spinner[k], lp );
      layout4.addView( layout, lp2 );
    }
    layout4.invalidate();
  }

  /* some thought about search/all :
   * The "search" button could show all possible equate (in yellow) and user choose which one to pick by tapping a station.
   * When the user picks an equate the program recomputes the possible equates and update the yellow equates.
   * When the user tap the "search" button, if there are possible equates it becomes "clear", otherwise there is a warning toast and the button is disabled.
   * When the user tap the "clear" button, the set of possible equates is cleared
   *
   * We need a local class for PossibleEquate with two survey stations (the two Surveys and the two Stations), even if the station names coincide.
   * Next we need a method to find all possible equates, and store them in an ArrayList of PossibleEquate.
   * A boolean function to check whether there are possible equates is useful: this could be a test whether ArrayList is nor empty
   * (the ArrayList could be instantiated when the dialog is created, and later filled/cleared).
   */ 
  @Override
  public void onClick(View v) 
  {
    // TDLog.v("Size " + mSize + " command Size " + mCommandsSize ); // mSize == mCommandsSize
    Button b = (Button) v;
    if ( b == mBTok ) {
      // String bad_station = null; // UNUSED
      ArrayList< String > sts = new ArrayList<>();
      for ( int k=0; k<mSize; ++k ) {
        TdmViewCommand vc = mCommands.get( ( (mCommandsSize == 1) ? 0 : k ) );
        String survey = vc.name();
        // int len = survey.length(); // UNUSED
        // while ( len > 0 && survey.charAt( len - 1 ) == '.' ) -- len;
        String station = mEdit[k].getText().toString();
        if ( ! station.equals("-") ) {// HB EQ all
          if ( station != null && station.length() > 0 ) {
            if ( vc.getViewStation( station ) != null ) {
              sts.add( station + "@" + survey ); // survey.substring(0,len) );
              // TDLog.v("added station: " + sts.size() + " survey <" + survey + ">" );
            } else {
              // bad_station = station + "@" + survey ); // survey.substring(0,len);
              // TDLog.v("Bad station: " + bad_station + " survey <" + survey + ">" );
              mEdit[k].setError( resString( R.string.bad_station_name ) );
              return;
            }
          } else {
            mEdit[k].setError( resString( R.string.error_name_required ) );
            return;
          }
        }
      }
      // if ( bad_station == null ) {
      //   mParent.makeEquate( sts ); // does nothing if sts.size() <= 1
      // } else {
      //   TDToast.makeWarn( String.format( mContext.getResources().getString( R.string.bad_station ), bad_station ) );
      //   return;
      // }
      mParent.makeEquate( sts ); // does nothing if sts.size() <= 1
    } else if ( b == mBTall ) { // HB EQ all
      ArrayList<String> stations = new ArrayList<>();
      if ( mSize > 1 ) {
        for ( int j = 0; j < ( mSize - 1 ) ; ++j) {
          // int good_station = 0; FIXME moved inside and replaced with a boolean
          TdmViewCommand vc0 = mCommands.get(j); // if ( vc0 == null ) continue;
          String survey0 = vc0.name();
          for ( TdmViewStation st0 : vc0.mStations ) {
            if ( st0.mEquated ) continue; // FIXME break or continue ? it depends on the semantics of "all"
            String station0 = st0.name();
            if ( TDString.isNullOrEmpty( station0 ) ) continue;
            if ( stations.contains( station0 ) ) continue;
            // TDLog.v("ALL station " + station0 + " survey-0 " + survey0 );
            ArrayList< String > sts = new ArrayList<>();
            // int len0 = survey0.length();
            // while (len0 > 0 && survey0.charAt(len0 - 1) == '.') --len0;
            // String name0 = station0 + "@" + survey0.substring(0, len0);
            String name0 = station0 + "@" + survey0;
            sts.add( name0 );
            for ( int k = ( j + 1 ); k < mSize; ++k) {
              TdmViewCommand vc1 = mCommands.get( k ); // if ( vc1 == null ) continue;
              String survey1 = vc1.name();
              // int len = survey1.length();
              // while (len > 0 && survey.charAt(len - 1) == '.') --len; // ?
              if ( vc1.getViewStation( station0 ) != null) {
                // String name1 = station0 + "@" + survey1.substring(0, len);
                String name1 = station0 + "@" + survey1;
                sts.add( name1 );
                // TDLog.v("add station " + station0 + " survey-1 " + survey1 );
              }
            }
            if ( sts.size() > 1 ) {
              mParent.makeEquate(sts); // does nothing if sts.size() <= 1
              stations.add( station0 );
            } else {
              TDLog.e("no good station for " + station0 );
            }
          }
        }
        //TDToast.makeWarn(String.format("size %d", mSize));
      }
    } else if ( b == mBTone ) { // HB EQ one equation - no loop
      ArrayList<String> stations = new ArrayList<>(); 
      int eq_group_nr = 0; // incremental value of equate group
      int eq_group_nr_max = 0; // debug: number of equate groups
      int[] eq_group = new int[mSize];
	  // FIXME eq_group should be initialized according to existing equates
      for (int j = 0; j < ( mSize ) ; ++j ) eq_group[j]=-1; 
      if ( mSize > 1 ) {
        for ( int j = 0; j < ( mSize - 1 ) ; ++j ) { 
          TdmViewCommand vc0 = mCommands.get(j);
          String survey0 = vc0.name();
          for ( TdmViewStation st0 : vc0.mStations ) { 
            if ( st0.mEquated ) break; 
            String station0 = st0.name(); 
            if ( TDString.isNullOrEmpty( station0 ) ) continue;
            if ( stations.contains( station0 ) ) continue;
            ArrayList<String> sts = new ArrayList<>();
            // int len0 = survey0.length(); 
            // while ( len0 > 0 && survey0.charAt(len0 - 1) == '.') --len0; // FIXME ! It should also be prohibited when creating the survey! It is allowed there.
            // sts.add( station0 + "@" + survey0.substring(0, len0));
            sts.add( station0 + "@" + survey0 );
            // TDLog.v("ONE add station " + station0 + " survey-0 " + survey0 + " eq-group " + eq_group[j] );
            for ( int k = ( j + 1 ); k < mSize; ++k ) {
              TdmViewCommand vc1 = mCommands.get( k );
              String survey1 = vc1.name();
              // int len = survey1.length();
              // while (len > 0 && survey.charAt(len - 1) == '.') --len; // ? FIXME ! It should also be prohibited when creating the survey! It is allowed there.
              if ( vc1.getViewStation( station0 ) != null ) { 
                if ((eq_group[j] == eq_group[k]) && (eq_group[j] != -1) ) {
                  // cycle: 
                  // scan survey1
                  //        survey1 --< station13 >-- survey3 ==> eq_group[1] = eq_group[3] := 1
                  //        survey1 --< station14 >-- survey4 ==>               eq_group[4] := eq_group[1] = 1
                  // equates 1--3 1--4
                  // scan survey2
                  //        survey2 --< station23 >-- survey3 : eq_group[2] := eq_group[3] ie eq_group[2] = 1
                  //        survey2 --< station25 >-- survey5 : eq_group[5] := eq_group[2] = 1
                  // equates 1--3 1--4 2--3 2--5
                  // scan survey3 
                  //        survey3 --< station35 >-- survey5 : eq_group[3] = 1 and eq_group[5] = 1 ==> cycle (2,3,5)
                  // equates 1--3 1--4 2--3 2--5 3--5
                  // TDLog.v("loop survey-1 " + survey1 );
                } else {
                  // sts.add( station0 + "@" + survey1.substring(0, len));
                  sts.add( station0 + "@" + survey1 );
                  // TDLog.v("add station " + station0 + " survey-1 " + survey1 + " eq-group " + eq_group[k] );
                  if ( eq_group[j] == -1 && eq_group[k] == -1 ) { // if no group
                    ++ eq_group_nr; // new equate-group
                    eq_group_nr_max++;
                    eq_group[j] = eq_group_nr;
                    eq_group[k] = eq_group_nr;
                  } else if (eq_group[j] == -1) { // j := k
                    eq_group[j] = eq_group[k];
                  } else if (eq_group[k] == -1) { // k := j
                    eq_group[k] = eq_group[j];
                  } else { // two group equate
                    // if ( eq_group[k] == eq_group[j] ) { // excluded by loop
                    //   // nothing to do
                    // } else 
                    if ( eq_group[k] > eq_group[j] ) {
                      for (int l = 0; l < ( mSize ) ; ++l ) if (eq_group[l]==eq_group[k]) eq_group[l]=eq_group[j]; // k -> j
                      eq_group_nr_max--;
                    } else { // eq_group[k] < eq_group[j]
                      for (int l = 0; l < ( mSize ) ; ++l ) if (eq_group[l]==eq_group[j]) eq_group[l]=eq_group[k]; // j -> k
                      eq_group_nr_max--;
                    }
                  }
                }
              }
            }
            if ( sts.size() > 1 ) {
              mParent.makeEquate( sts ); // does nothing if sts.size() <= 1
              stations.add( station0 ); // station exist equate
            }
          }
        }
        TDToast.makeWarn(String.format(Locale.US, "Group %d", eq_group_nr_max));
      }
    } else if ( b == mBTsearch ) {
      ArrayList<String> stations = new ArrayList<>();
      for ( int k = 0; k < mSize ; ++k) mEdit[k].setText("-");
      if ( mSize > 1 ) {
        for (int j = j0; j < ( mSize - 1 ) ; ++j) {
          TdmViewCommand vc0 = mCommands.get(j);
          String survey0 = vc0.name();
          for ( int l = l0; l < vc0.mStations.size(); ++l ){
            TdmViewStation st0 = vc0.mStations.get(l);
            if ( st0.mEquated) break; // FIXME break or continue ?
            String station0 = st0.name();
            if ( TDString.isNullOrEmpty( station0 ) ) continue;
            if ( stations.contains( station0 ) ) continue;
            // TDLog.v("SEARCH station " + station0 + " survey-0 " + survey0 );
            boolean good_station = false;
            ArrayList<String> sts = new ArrayList<>();
            // int len0 = survey0.length();
            // while (len0 > 0 && survey0.charAt(len0 - 1) == '.') --len0; // ?
            for (int k = j+1; k < mSize; ++k) {
              TdmViewCommand vc1 = mCommands.get( k );
              String survey1 = vc1.name();
              // int len = survey1.length();
              // while (len > 0 && survey1.charAt(len - 1) == '.') --len; // ?
              // TDLog.v("survey-1 <" + survey1 + ">" );
              if ( vc1.getViewStation( station0 ) != null) {
                // sts.add(station0 + "@" + survey1.substring(0, len)); //
                sts.add(station0 + "@" + survey1);
                mEdit[j].setText( station0 );
                mEdit[k].setText( station0 );
                good_station = true; // ++;
              } else {
                // TDLog.v("HBEQ Bad station: " + good_station + " survey-1 " + survey1);
              }
            }
            if (good_station ) { // if (good_station > 0) 
              j0 = j;
              l0 = l+1;
              if (l0 >= vc0.mStations.size() ) {
                j0 = j+1;
                l0 = 0;
              }
              // good_station = 0; // FIXME why reset the local variable ?
              return;
            }
          }
        }
        //TDToast.makeWarn(String.format("size %d", mSize));
        l0 = 0;
      }
    }
//-------------------------------------------------------------------------------HB EQ all
    dismiss();
  }
}
