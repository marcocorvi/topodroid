/** @file TdManagerActivity.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief project manager main activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * Displays the list of tdconfig files
 * - long-pressing on a file opens it in the editor
 * - clicking on a file starts the TdmConfigActivity on it
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDRequest;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.HelpDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.TDandroid;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import java.util.ArrayList;

import android.content.res.Resources;
import android.content.pm.PackageManager;
// import android.app.Dialog;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
// import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.app.Activity;
import android.net.Uri;

import android.view.Menu;
// import android.view.SubMenu;
import android.view.MenuItem;
// import android.view.MenuInflater;

public class TdManagerActivity extends Activity
                       implements OnItemClickListener
                       // , OnItemLongClickListener
                       , OnClickListener

{
  TdmConfigAdapter mTdmConfigAdapter;

  MyHorizontalListView mListView;
  MyHorizontalButtonView mButtonView1;

  ListView mList;
  Button   mImage;
  ListView mMenu;
  ArrayAdapter<String> mMenuAdapter;
  Button[] mButton1;
  int mButtonSize = 42;

  private static final int HELP_PAGE = R.string.TdManager;

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.tdmanager_activity);
    // getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( R.string.tdm_title ); 
    
    mList = (ListView) findViewById(R.id.th_list);
    mList.setOnItemClickListener( this );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    mList.setDescendantFocusability( ViewGroup.FOCUS_BEFORE_DESCENDANTS );

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    resetButtonBar();

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( (TopoDroidApp)getApplication(), getResources(), R.drawable.iz_menu ) );

    mMenu = (ListView) findViewById( R.id.menu );
    mMenuAdapter = null;
    setMenuAdapter( getResources() );
    closeMenu();
    mMenu.setOnItemClickListener( this );

  }
  
  // -------------------------------------------------
  boolean onMenu = false;
  int mNrButton1 = 1;
  private static int[] izons = { 
    R.drawable.iz_plus,
  };
  private static final int[] help_icons = { 
    R.string.help_add_project,
  };

  int mNrMenus   = 2;
  private static int[] menus = { 
    R.string.menu_close,
    // R.string.menu_options,
    R.string.menu_help,
  };
  private static final int[] help_menus = {
    R.string.help_close,
    R.string.help_help,
  };

  private void resetButtonBar()
  {
    // mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, getResources(), R.drawable.iz_menu ) );

    if ( mNrButton1 > 0 ) {
      mButtonSize = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );
      // int size = TopoDroisApp.getScaledSize( this );
      // LinearLayout layout = (LinearLayout) findViewById( R.id.list_layout );
      // layout.setMinimumHeight( size + 40 );
      // LayoutParams lp = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
      // lp.setMargins( 10, 10, 10, 10 );
      // lp.width  = size;
      // lp.height = size;
      // // MyButton.resetCache( size );

      // FIXME TDMANAGER
      // mNrButton1 = 3 + ( TDSetting.mLevelOverAdvanced ? 2 : 0 );
      mButton1 = new Button[mNrButton1];

      for (int k=0; k<mNrButton1; ++k ) {
        mButton1[k] = MyButton.getButton( this, this, izons[k] );
        // layout.addView( mButton1[k], lp );
	// Log.v("DistoX-TdManager", "button size " + mButton1[k].getWidth() + "x" + mButton1[k].getHeight() );
      }

      mButtonView1 = new MyHorizontalButtonView( mButton1 );
      mListView.setAdapter( mButtonView1.mAdapter );
    }
  }

  private void setMenuAdapter( Resources res )
  {
    mMenuAdapter = new ArrayAdapter<String>( this, R.layout.menu );
    for ( int k=0; k<mNrMenus; ++k ) {
      mMenuAdapter.add( res.getString( menus[k] ) );  
    }
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  private void handleMenu( int pos ) 
  {
    closeMenu();
    int p = 0;
    // if ( p++ == pos ) {        // NEW
    //   (new TdmConfigDialog( this, this )).show();
    // } else if ( p++ == pos ) { // OPTIONS
    //   Intent intent = new Intent( this, TdManagerPreferences.class );
    //   startActivity( intent );
    // } else 
    if ( p++ == pos ) { // CLOSE
      finish();
    } else if ( p++ == pos ) { // HELP
      // new TdmHelpDialog( this ).show();
      new HelpDialog( this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE )).show();
    }
  }

  // ----------------------------------------------

  @Override
  public void onStart()
  {
    super.onStart();
    // Log.v("DistoX-TdManager", "TdManager on resume");
    updateTdmConfigList();
  }

  // @Override
  // public void onResume()
  // {
  //   super.onResume();
  // }
    
  void updateTdmConfigList()
  {
    mTdmConfigAdapter = new TdmConfigAdapter( this, R.layout.row, new ArrayList< TdmConfig >(),
      new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
          String survey = ((TextView)v).getText().toString();
	  // Log.v("DistoX-TdManager", "on click " + survey );
          TdmConfig tdconfig = mTdmConfigAdapter.get( survey );
	  if ( tdconfig != null ) startTdmConfigActivity( tdconfig );
	}
      }
    );
    File[] tdconfigs = TDPath.scanTdconfigDir();
    if ( tdconfigs != null && tdconfigs.length > 0 ) {
      for ( File file : tdconfigs ) {
        Log.v("DistoX-TdManager", "activity update: path <" + file.getAbsolutePath() + "> name <" + file.getName() + ">" );
        mTdmConfigAdapter.add( new TdmConfig( file.getAbsolutePath(), false ) ); // false: no save
      }
    } else {
      mTdmConfigAdapter.add( new TdmConfig( TDPath.getTdconfigFile( "test.tdconfig" ), false ) ); // false: no save
    }
    mList.setAdapter( mTdmConfigAdapter );
    // Log.v("DistoX-TdManager", "set adapter: size " + mTdmConfigAdapter.size() );
  }

  // @Override
  // public boolean onItemLongClick( AdapterView<?> parent, View view, int pos, long id )
  // {
  //   onItemClick( parent, view, pos, id );
  //   return true;
  // }

  @Override
  public void onItemClick( AdapterView<?> parent, View view, int pos, long id )
  {
    // CharSequence item = ((TextView) view).getText();
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }

    TdmConfig tdconfig = mTdmConfigAdapter.getItem( pos );
    // Log.v("DistoX-TdManager", "On Item Click: pos " + pos + " TdmConfig " + tdconfig.mFilepath );
    // TODO start TdmConfigActivity or Dialog
    startTdmConfigActivity( tdconfig );
  }

  void startTdmConfigActivity( TdmConfig tdconfig )
  {
    Intent intent = new Intent( this, TdmConfigActivity.class );
    intent.putExtra( TDRequest.TDCONFIG_PATH, tdconfig.getFilepath() );
    try {
      startActivityForResult( intent, TDRequest.REQUEST_TDCONFIG );
    } catch ( ActivityNotFoundException e ) {
      TDToast.make( R.string.no_editor );
    }
  }

  /** add a new tdconfig file
   * @param name    tdconfig name
   */
  void addTdmConfig( String name )
  {
    String filename = name;
    name = name.trim();
    if ( name == null || name.length() == 0 || name.startsWith(".") || name.startsWith("/") ) {
      TDToast.make( R.string.error_name_invalid );
      return;
    }
    if ( ! filename.endsWith(".tdconfig") ) filename = filename + ".tdconfig";
    String path = TDPath.getTdconfigFile( filename );
    if ( (new File(path)).exists() ) {
      TDToast.make( R.string.error_name_exists );
      return;
    }
    TdmConfig tdconfig = new TdmConfig( path, true ); // true: save
    // updateTdmConfigList();
    mTdmConfigAdapter.add( tdconfig );
    // Log.v("DistoX-TdManager", "path >" + path + "< size " + mTdmConfigAdapter.size() );
    // mList.setAdapter( mTdmConfigAdapter );
    mList.invalidate();
  }

  // deletes a tdconfig file
  //  @param filename tdconfig filename
  //
  // void deleteTdmConfig( String filepath )
  // {
  //   File file = new File( filepath );
  //   file.delete();
  //   updateTdmConfigList();
  // }
    
  public void onActivityResult( int request, int result, Intent intent ) 
  {
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case TDRequest.REQUEST_TDCONFIG:
        if ( result == TDRequest.RESULT_TDCONFIG_OK ) {
          // Log.v("DistoX-TdManager", "TdmConfig OK" );
          // nothing 
        } else if ( result == TDRequest.RESULT_TDCONFIG_DELETE ) {
          // get TdmConfig name and delete it
          String path = extras.getString( TDRequest.TDCONFIG_PATH );
          mTdmConfigAdapter.deleteTdmConfig( path );
          mList.invalidate();
          // updateTdmConfigList();
        } else if ( result == TDRequest.RESULT_TDCONFIG_NONE ) {
          Log.e("DistoX-TdManager", "TdmConfig NONE" );
          // nothing
        }
    }
  }


  // ---------------------------------------------------------------
  // OPTIONS MENU

  // private MenuItem mMInew;
  // private MenuItem mMIhelp;
  // private MenuItem mMIoptions;

  // @Override
  // public boolean onCreateOptionsMenu(Menu menu) 
  // {
  //   super.onCreateOptionsMenu( menu );

  //   mMInew     = menu.add( R.string.menu_new );
  //   mMIoptions = menu.add( R.string.menu_options );
  //   mMIhelp    = menu.add( R.string.menu_help );
  //   return true;
  // }

  // ---------------------------------------------------------------

  @Override
  public void onClick(View view)
  { 
    if ( onMenu ) {
      closeMenu();
      return;
    }
    Button b0 = (Button)view;

    if ( b0 == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }

    int k1 = 0;
    if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) { // TDCONFIG
      (new TdmConfigDialog( this, this )).show();
    // } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // OPTIONS
    //   Intent intent = new Intent( this, TdManagerPreferences.class );
    //   startActivity( intent );
    // } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) { // HELP
    //   new TdmHelpDialog( this ).show();
    // } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // EXIT
    //   finish();
    }
  }

}
