/* @file SensorListActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey sensor listing
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created from DistoX.java
 * 20120531 implemented sensor and 3D
 * 20120531 shot-numbering bugfix
 * 20120606 3D: implied therion export before 3D
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
// import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.app.Application;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
// import android.view.MenuInflater;
// import android.content.res.ColorStateList;

// import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;

// import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;
import android.view.View;
// import android.view.View.OnClickListener;
import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;
import android.preference.PreferenceManager;

import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

public class SensorListActivity extends Activity
                          // implements OnItemClickListener
{
  private TopoDroidApp app;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private SensorAdapter   mDataAdapter;
  private long mShotId = -1;   // id of the shot

  private String mSaveData = "";
  private TextView mSaveTextView = null;
  // private SensorInfo mSaveSensor = null;

  String mSensorComment;
  long   mSensorId;

  // -------------------------------------------------------------------
/*
  @Override
  public void refreshDisplay( int nr )
  {
    updateDisplay();
  }
*/

  public void updateDisplay( )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_SENSOR, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    DataHelper data = app.mData;
    if ( data != null && app.mSID >= 0 ) {
      List< SensorInfo > list = data.selectAllSensors( app.mSID, TopoDroidApp.STATUS_NORMAL );
      // TopoDroidLog.Log( TopoDroidLog.LOG_PHOTO, "update shot list size " + list.size() );
      updateSensorList( list );
      setTitle( app.mySurvey );
    // } else {
    //   Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
    }
  }

  private void updateSensorList( List< SensorInfo > list )
  {
    // TopoDroidLog.Log(TopoDroidLog.LOG_SENSOR, "updateSensorList size " + list.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_sensors, Toast.LENGTH_SHORT ).show();
      finish();
    }
    for ( SensorInfo item : list ) {
      mDataAdapter.add( item );
    }
  }

  // ---------------------------------------------------------------
  // list items click

/*
  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "SensorListActivity onItemClick id " + id);
    startSensorDialog( (TextView)view, position );
  }

  public void startSensorDialog( TextView tv, int pos )
  {
     mSaveSensor = mDataAdapter.get(pos);
     (new SensorEditDialog( this, this, mSaveSensor )).show();
  }
*/

  // ---------------------------------------------------------------
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.sensor_list_activity);
    app = (TopoDroidApp) getApplication();
    mDataAdapter = new SensorAdapter( this, R.layout.row, new ArrayList< SensorInfo >() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    updateDisplay( );
  }

  // ------------------------------------------------------------------

  public void dropSensor( SensorInfo sensor )
  {
    app.mData.deleteSensor( sensor.sid, sensor.id );
    updateDisplay( ); // FIXME
  }

  public void updateSensor( SensorInfo sensor, String comment )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_SENSOR, "updateSensor comment " + comment );
    if ( app.mData.updateSensor( sensor.sid, sensor.id, comment ) ) {
      // if ( app.mListRefresh ) {
      //   // This works but it refreshes the whole list
      //   mDataAdapter.notifyDataSetChanged();
      // } else {
      //   mSaveSensor.mComment = comment;
      // }
      updateDisplay(); // FIXME
    } else {
      Toast.makeText( this, R.string.no_db, Toast.LENGTH_SHORT ).show();
    }
  }
}
