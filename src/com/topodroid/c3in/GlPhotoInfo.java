

package com.topodroid.c3in;

import com.topodroid.math.Vector3D;
import com.topodroid.TDX.Cave3DStation;

public class GlPhotoInfo extends Vector3D
{
  public long mId;
  public long mItemId;
  public String from;
  public String to;
  public float mIntercept;
  public String mFilename;

  GlPhotoInfo( long id, String filename, long item_id, float intercept )
  {
    mId = id;
    mFilename = filename;
    mItemId = item_id;
    mIntercept = intercept;
  }

  boolean interpolate( Cave3DStation st_from, Cave3DStation st_to ) 
  {
    if ( st_from == null ) {
      if ( st_to == null ) {
        return false;
      }
      x = st_to.x;
      y = st_to.y;
      z = st_to.z;
    } else {
      if ( st_to == null ) {
        x = st_from.x;
        y = st_from.y;
        z = st_from.z;
      } else {
        float vt = mIntercept;
        float vf = 1 - vt;
        x = st_from.x * vf + st_to.x * vt;
        y = st_from.y * vf + st_to.y * vt;
        z = st_from.z * vf + st_to.z * vt;
      }
    }
    return true;
  }
  

}
