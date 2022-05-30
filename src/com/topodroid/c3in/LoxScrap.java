/* @file LoxScrap.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch Scrap
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;


class LoxScrap
{
  private final int id;
  private final int sid;
  private final int nPts;
  private final int nIdx;
  private final double[] pts;
  private final int[] idx;

  LoxScrap( int _id, int _sid, int np, int ni, double[] p, int[] i )
  {
    id = _id;
    sid = _sid;
    nPts = np;
    nIdx = ni;
    pts = p;
    idx = i;
  }
    
  int Id()      { return id; }
  int Survey()  { return sid; }
  int NrPoint()  { return nPts; }
  int NrIndex()  { return nIdx; }

  double[] Point()   { return pts; }
  int[] Index() { return idx; }
  double[] Point( int k ) 
  { 
    double[] p = new double[3];
    p[0] = pts[3*k  ];
    p[1] = pts[3*k+1];
    p[2] = pts[3*k+2];
    return p;
  }
  int[] Index( int k ) 
  { 
    int[] p = new int[3];
    p[0] = idx[3*k  ];
    p[1] = idx[3*k+1];
    p[2] = idx[3*k+2];
    return p;
  }

}
