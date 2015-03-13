/** @file HorizontalListView.java
 *
 * @author marco corvi (adapted from 
 * http://sandyandroidtutorials.blogspot.it/2013/06/horizontal-listview-tutorial.html
 *
 * @date nov 2013
 *
 * @brief TopoDroid button bar buttons view
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 *
 */
package com.topodroid.DistoX;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AdapterView<ListAdapter> 
{
  public boolean mAlwaysOverrideTouch = true;
  protected ListAdapter mAdapter;              // data adapter
  private int mLeftViewIndex = -1;
  private int mRightViewIndex = 0;
  protected int mCurrentX;
  protected int mNextX;
  private int mMaxX = Integer.MAX_VALUE;
  private int mDisplayOffset = 0;
  protected Scroller mScroller;
  private GestureDetector mGesture;
  private Queue<View> mRemovedViewQueue = new LinkedList<View>();

  private OnItemSelectedListener  mOnItemSelected;    // listeners
  private OnItemClickListener     mOnItemClicked;
  private OnItemLongClickListener mOnItemLongClicked;

  private boolean mDataChanged = false;               // whether data have changed

  public HorizontalListView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    initView();
  }

  private synchronized void initView() 
  {
    mLeftViewIndex = -1;
    mRightViewIndex = 0;
    mDisplayOffset = 0;
    mCurrentX = 0;
    mNextX = 0;
    mMaxX = Integer.MAX_VALUE;
    mScroller = new Scroller(getContext());
    mGesture = new GestureDetector(getContext(), mOnGesture);
  }
  
  @Override
  public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener)
  {
    mOnItemSelected = listener;
  }
  
  @Override
  public void setOnItemClickListener(AdapterView.OnItemClickListener listener)
  {
    mOnItemClicked = listener;
  }
  
  @Override
  public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) 
  {
    mOnItemLongClicked = listener;
  }

  private DataSetObserver mDataObserver = new DataSetObserver() 
  {
    @Override
    public void onChanged() 
    {
      synchronized ( HorizontalListView.this ) {
        mDataChanged = true;
      }
      invalidate();
      requestLayout();
    }

    @Override
    public void onInvalidated() 
    {
      reset();
      invalidate();
      requestLayout();
    }
  };

  @Override
  public ListAdapter getAdapter()
  {
    return mAdapter;
  }

  @Override
  public View getSelectedView()
  {
    //TODO: implement
    return null;
  }

  @Override
  public void setAdapter(ListAdapter adapter)
  {
    if ( mAdapter != null ) {
      mAdapter.unregisterDataSetObserver(mDataObserver);
    }
    mAdapter = adapter;
    mAdapter.registerDataSetObserver(mDataObserver);
    reset();
  }
 
  private synchronized void reset()
  {
    initView();
    removeAllViewsInLayout();
    requestLayout();
  }

  @Override
  public void setSelection(int position)
  {
    //TODO: implement
  }

  private void addAndMeasureChild(final View child, int viewPos)
  {
    LayoutParams params = child.getLayoutParams();
    if(params == null) {
        params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    }

    addViewInLayout(child, viewPos, params, true);
    child.measure( MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                   MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
  }

  @Override
  protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom)
  {
    super.onLayout(changed, left, top, right, bottom);

    if ( mAdapter == null ) {
      return;
    }
     
    if ( mDataChanged ) {
      int oldCurrentX = mCurrentX;
      initView();
      removeAllViewsInLayout();
      mNextX = oldCurrentX;
      mDataChanged = false;
    }
    if ( mScroller.computeScrollOffset() ) {
      int scrollx = mScroller.getCurrX();
      mNextX = scrollx;
    }
   
    if ( mNextX <= 0 ) {
      mNextX = 0;
      mScroller.forceFinished(true);
    }
    if ( mNextX >= mMaxX ) {
      mNextX = mMaxX;
      mScroller.forceFinished(true);
    }
     
    int dx = mCurrentX - mNextX;
     
    removeNonVisibleItems( dx );
    fillList( dx );
    positionItems( dx );
     
    mCurrentX = mNextX;
     
    if ( ! mScroller.isFinished() ) {
      post(new Runnable(){
        @Override
        public void run() {
          requestLayout();
        }
      });
    }
    setBackgroundColor( 0x33ffffff );
  }
 
  private void fillList(final int dx) 
  {
    int edge = 0;
    View child = getChildAt(getChildCount()-1);
    if ( child != null ) {
      edge = child.getRight();
    }
    fillListRight(edge, dx);
     
    edge = 0;
    child = getChildAt(0);
    if ( child != null ) {
      edge = child.getLeft();
    }
    fillListLeft(edge, dx);
  }

  private void fillListRight(int rightEdge, final int dx)
  {
    while ( rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {
      View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
      addAndMeasureChild(child, -1);
      rightEdge += child.getMeasuredWidth();
         
      if ( mRightViewIndex == mAdapter.getCount()-1) {
        mMaxX = mCurrentX + rightEdge - getWidth();
      }
         
      if ( mMaxX < 0 ) {
        mMaxX = 0;
      }
      mRightViewIndex++;
    }
  }
 
  private void fillListLeft(int leftEdge, final int dx)
  {
    while(leftEdge + dx > 0 && mLeftViewIndex >= 0) {
      View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
      addAndMeasureChild(child, 0);
      leftEdge -= child.getMeasuredWidth();
      mLeftViewIndex--;
      mDisplayOffset -= child.getMeasuredWidth();
    }
  }

  private void removeNonVisibleItems(final int dx) 
  {
    View child = getChildAt(0);
    while ( child != null && child.getRight() + dx <= 0 ) {
      mDisplayOffset += child.getMeasuredWidth();
      mRemovedViewQueue.offer(child);
      removeViewInLayout(child);
      mLeftViewIndex++;
      child = getChildAt(0);
    }
     
    child = getChildAt(getChildCount()-1);
    while ( child != null && child.getLeft() + dx >= getWidth() ) {
      mRemovedViewQueue.offer(child);
      removeViewInLayout(child);
      mRightViewIndex--;
      child = getChildAt(getChildCount()-1);
    }
  }
 
  private void positionItems(final int dx)
  {
    if ( getChildCount() > 0 ) {
      mDisplayOffset += dx;
      int left = mDisplayOffset;
      for ( int i=0; i<getChildCount(); i++ ) {
        View child = getChildAt(i);
        int childWidth = child.getMeasuredWidth();
        child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
        left += childWidth + child.getPaddingRight();
      }
    }
  }

  public synchronized void scrollTo(int x) 
  {
    mScroller.startScroll(mNextX, 0, x - mNextX, 0);
    requestLayout();
  }
 
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev)
  {
    boolean handled = super.dispatchTouchEvent(ev);
    handled |= mGesture.onTouchEvent(ev);
    return handled;
  }
 
  protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
  {
    synchronized( HorizontalListView.this ) {
      mScroller.fling(mNextX, 0, (int)-velocityX, 0, 0, mMaxX, 0, 0);
    }
    requestLayout();
    return true;
  }
 
  protected boolean onDown(MotionEvent e) 
  {
    mScroller.forceFinished(true);
    return true;
  }

  private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener()
  {
    @Override
    public boolean onDown(MotionEvent e) {
      return HorizontalListView.this.onDown(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      synchronized(HorizontalListView.this){
        mNextX += (int)distanceX;
      }
      requestLayout();
      return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      for(int i=0;i<getChildCount();i++){
        View child = getChildAt(i);
        if (isEventWithinView(e, child)) {
          if(mOnItemClicked != null){
            mOnItemClicked.onItemClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ));
          }
          if(mOnItemSelected != null){
            mOnItemSelected.onItemSelected(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ));
          }
          break;
        }
             
      }
      return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
      int childCount = getChildCount();
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        if (isEventWithinView(e, child)) {
          if (mOnItemLongClicked != null) {
            mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
          }
          break;
        }
      }
    }

    private boolean isEventWithinView(MotionEvent e, View child) {
      Rect viewRect = new Rect();
      int[] childPosition = new int[2];
      child.getLocationOnScreen(childPosition);
      int left = childPosition[0];
      int right = left + child.getWidth();
      int top = childPosition[1];
      int bottom = top + child.getHeight();
      viewRect.set(left, top, right, bottom);
      return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
    }
  };
}

/* sample
HorizontalListViewDemo.java

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class HorizontalListViewDemo extends Activity
{
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    
      setContentView(R.layout.listviewdemo);
    
      HorizontalListView listview = (HorizontalListView) findViewById(R.id.listview);
      listview.setAdapter(mAdapter);
    
  }

  private static String[] dataObjects = new String[]{ "Text #1",
      "Text #2",
      "Text #3","Text #4","Text #5","Text #6","Text #7","Text #8","Text #9","Text #10" };
 
  private BaseAdapter mAdapter = new BaseAdapter() {

      private OnClickListener mOnButtonClicked = new OnClickListener() {
        
          @Override
          public void onClick(View v) {
          }
      };

      @Override
      public int getCount() {
          return dataObjects.length;
      }

      @Override
      public Object getItem(int position) {
          return null;
      }

      @Override
      public long getItemId(int position) {
          return 0;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
          View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, null);
          TextView title = (TextView) retval.findViewById(R.id.title);
          Button button = (Button) retval.findViewById(R.id.clickbutton);
          button.setOnClickListener(mOnButtonClicked);
          title.setText(dataObjects[position]);
        
          return retval;
      }
    
  };

}

main.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    >
    <TextView 
       android:layout_width="fill_parent"
       android:layout_height="wrap_content"
       android:text="@string/hello"
    />
</LinearLayout> 

listviewdemo.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  android:orientation="vertical"
  android:layout_width="fill_parent"
  android:layout_height="fill_parent"
  android:background="#fff"
  >
  <com.sandy.demo.HorizontalListView
      android:id="@+id/listview"
      android:layout_width="fill_parent"
      android:layout_height="wrap_content"
      android:background="#ddd"
  />
</LinearLayout>

viewitem.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  android:orientation="vertical"
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  android:background="#fff"
  >
  <ImageView
      android:id="@+id/image"
      android:layout_width="150dip"
      android:layout_height="150dip"
      android:scaleType="centerCrop"
      android:src="@drawable/icon"
      />
  <TextView
      android:id="@+id/title"
      android:layout_width="fill_parent"
      android:layout_height="wrap_content"
      android:textColor="#000"
      android:gravity="center_horizontal"
      />
  <Button
      android:id="@+id/clickbutton"
      android:layout_width="fill_parent"
      android:layout_height="wrap_content"
      android:textColor="#000"
      android:text="Click Me"
      />
 
</LinearLayout>

*/
