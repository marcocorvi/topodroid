/* @file NumCycle.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction cycle of the survey net
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

// import com.topodroid.utils.TDLog;

import java.util.ArrayList;

public class NumCycle
{
  static class NumCycleBranch
  {
    NumBranch mBranch;
    // NumNode   mNode;
    int       mDir;    // branch direction in the cycle

    /** cstr
     * @param br   network branch
     * @param nd   network node (unused)
     * @param dir  branch direction
     */
    NumCycleBranch( NumBranch br, NumNode nd, int dir )
    {
      mBranch = br;
      // mNode = nd;
      mDir = dir;
    }

    /** copy cstr
     * @param ncb   cycle branch
     */
    NumCycleBranch( NumCycleBranch ncb )
    {
      mBranch = ncb.mBranch;
      // mNode = ncb.mNode;
      mDir    = ncb.mDir;
    }

    /** @return the length of the branch
     */
    double length() { return mBranch.len; }

  }

  // private int mMax;
  // int mSize;
  private ArrayList< NumCycleBranch > branches;
  // private ArrayList< NumNode >   nodes;
  // int[]       dirs; // branch direction in the cycle
  double e, s, v;    // displacement = closure error
  double ce, cs, cv; // compensation corrections
  boolean applyCorrection; // whether to apply loop correction in the compensation (SELECTIVE case)

  /** @return the length of the cycle, sum of the lengths of the branches
   */
  double length()
  {
    double len = 0;
    for ( NumCycleBranch ncb : branches ) {
      len += ncb.length();
    }
    return len;
  }

  /** @return the absolute value of the loop mis-closure
   */
  double error() 
  { 
    double ret = e*e + s*s + v*v;
    return ( ret > 0 )? Math.sqrt( ret ) : 0;
  }

  // /** @return the square error
  //  */
  // double squareError() 
  // { 
  //   return ( e*e + s*s + v*v );
  // }

  /** reset the loop correction
   */
  void resetCorrections()
  {
    ce = 0;
    cs = 0;
    cv = 0;
  }

  /** @return the number of branches in this cycle
   */
  int size() { return branches.size(); }

  /** cstr
   * @param sz   not-used
   */
  NumCycle( int sz )
  {
    // mMax = sz;
    // mSize = 0;
    branches = new ArrayList< NumCycleBranch >();
    // nodes = new NumNode[mMax];
    // dirs  = new int[mMax];
    applyCorrection = true;
  }

  /** @return the cycle (algebraically) composed of this cycle and another one (with sign)
   * @param cy    the other cycle
   * @param sign  sign of the other cycle in the composition
   */
  NumCycle compose( NumCycle cy, int sign )
  {
    NumCycle ret = new NumCycle( 0 ); // sz = 0 (it is unused)
    ArrayList< NumCycleBranch > branches2 = new ArrayList<>();
    branches2.addAll( cy.branches ); // for ( NumCycleBranch cb2 : cy.branches ) branches2.add( cb2 );
    for ( NumCycleBranch cb1 : branches ) {
      NumBranch b1 = cb1.mBranch;
      int d1 = cb1.mDir;
      boolean skip = false;
      for ( NumCycleBranch cb2 : branches2 ) {
        NumBranch b2 = cb2.mBranch;
        if ( b2 == b1 ) {
          d1 += cb2.mDir;
          branches2.remove( cb2 );
          break;
        }
      }
      if ( d1 != 0 ) {
        ret.addBranch( new NumCycleBranch( b1, null, d1 ) );
      }
    }
    for ( NumCycleBranch cb2 : branches2 ) {
      ret.addBranch( new NumCycleBranch( cb2 ) );
    }
    return ret;   
  }

  /** @return the length this cycle partially overlaps with another cycle
   * @param cy    the other cycle
   */
  double overlapLength( NumCycle cy )
  {
    double ret = 0;
    for ( NumCycleBranch cb1 : branches ) {
      NumBranch b1 = cb1.mBranch;
      for ( NumCycleBranch cb2 : cy.branches ) {
        if ( cb2.mBranch == b1 ) {
          ret += b1.len * cb1.mDir * cb2.mDir;
          break;
        }
      }
    }
    return ret;
  }

  /** @return the (signed) number of branches this cycle partially overlaps with another cycle
   * @param cy    the other cycle
   */
  int overlapCount( NumCycle cy )
  {
    int ret = 0;
    for ( NumCycleBranch cb1 : branches ) {
      NumBranch b1 = cb1.mBranch;
      for ( NumCycleBranch cb2 : cy.branches ) {
        if ( cb2.mBranch == b1 ) {
          ret += cb1.mDir * cb2.mDir;
          break;
        }
      }
    }
    return ret;
  }

  /** @return true if this cycle partially overlaps with another cycle
   * @param cy    the other cycle
   */
  boolean overlap( NumCycle cy )
  {
    for ( NumCycleBranch cb1 : branches ) {
      NumBranch b1 = cb1.mBranch;
      for ( NumCycleBranch cb2 : cy.branches ) {
        if ( cb2.mBranch == b1 ) {
          return true;
        }
      }
    }
    return false;
  }

  /** add a branch to this cycle
   * @param branch   branch to add
   * @param node     node to determine the direction of the branch in the cycle
   */
  void addBranch( NumBranch branch, NumNode node )
  {
    branches.add( new NumCycleBranch( branch, node, ( (node == branch.n2)? 1 : -1 ) ) );
    // if ( mSize < mMax ) {
    //   branches[mSize] = branch;
    //   // nodes[mSize] = node;
    //   dirs[mSize] = ( node == branch.n2 )? 1 : -1;
    //   // TDLog.Log( TDLog.LOG_NUM, "C add branch dir " + dirs[mSize] );
    //   // branch.dump();
    //   ++mSize;
    // }
  }

  /** add a branch
   * @param ncb   cycle-branch
   * @note this is used to make composite cycles (combinations of cycles with integer coefficients)
   */
  void addBranch( NumCycleBranch ncb ) { branches.add( ncb ); }

  // NumNode getNode( int k ) 
  // {
  //   if ( k >= 0 && k < mSize ) return nodes[k];
  //   return null;
  // }

  /** assumes that branches displacement-vector (e,s,v) have been computed
   *  computes the total displacement along the cycle (= loop mis-closure)
   */
  void computeError()
  {
    e = 0;
    s = 0;
    v = 0;
    // for ( int k=0; k<mSize; ++k ) {
    for ( NumCycleBranch branch : branches ) {
      NumBranch br = branch.mBranch;
      int dir      = branch.mDir;
      e += br.e * dir;
      s += br.s * dir;
      v += br.v * dir;
    }
  }

  /** get the branch direction in this cycle
   * @param br   num branch
   * @return  the direction of the branch in this cycle or 0 if the branch is not in this cycle
   */
  int getBranchDir( NumBranch br )
  {
    for ( NumCycleBranch branch : branches ) {
      if ( br == branch.mBranch ) return branch.mDir;
    }
    return 0;
  }

  /** @return true if every branch in this cycle is equal to a branch of one of the cycles
   * @param cycles    array of cycles
   */
  boolean isBranchCovered( ArrayList< NumCycle > cycles )
  {
    for ( NumCycleBranch branch : branches ) {
      NumBranch br = branch.mBranch;
      boolean found = false;
      for ( NumCycle cy : cycles ) {
        if ( cy == this ) continue;
        for ( NumCycleBranch branch1 : cy.branches ) {
          if ( br == branch1.mBranch ) {
            found = true;
            break;
          }
        }
        if ( found ) break;
      }
      if ( ! found ) return false; // the branch has not been found among the cycles
    }
    return true;
  }

  /** @return string description of the loop
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for ( NumCycleBranch branch : branches ) {
      // sb.append( branch.mBranch.toString() ).append(" (").append( branch.mDir ).append(") ");
      sb.append( branch.mBranch.toString( branch.mDir) ).append(" ");
    }
    return sb.toString();
  }

  /** set the bad-loop flag to the cycle shots
   */
  void setBadLoopShots()
  {
    for ( NumCycleBranch branch : branches ) {
      branch.mBranch.setBadLoopShots();
    }
  }

}

