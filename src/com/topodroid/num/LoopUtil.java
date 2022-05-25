/* @file LoopUtil.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid loop closure utility functions
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

class LoopUtil
{

  /** matrix inverse: gauss pivoting method
   * @param a    matrix
   * @param nr   size of rows
   * @param nc   size of columns
   * @param nd   row-stride
   */
  static double computeInverse( double[] a, int nr, int nc, int nd)
  {
    double  det_val = 1.0;                /* determinant value */
    int     ij, jr, ki, kj;
    int ii  = 0;                          /* index of diagonal */
    int ir  = 0;                          /* index of row      */

    for (int i = 0; i < nr; ++i, ir += nd, ii = ir + i) {
      det_val *= a[ii];                 /* new value of determinant */
      /* ------------------------------------ */
      /* if value is zero, might be underflow */
      /* ------------------------------------ */
      if (det_val == 0.0f) {
        if (a[ii] == 0.0f) {
          break;                    /* error - exit now */
        } else {                    /* must be underflow */
          det_val = 1.0e-15f;
        }
      }
      double r = 1.0 / a[ii];   /* Calculate Pivot --------------- */
      a[ii] = 1.0f;
      ij = ir;                          /* index of pivot row */
      for (int j = 0; j < nc; ++j) {
        a[ij] = r * a[ij];
        ++ij;                         /* index of next row element */
      }
      ki = i;                           /* index of ith column */
      jr = 0;                           /* index of jth row    */
      for (int k = 0; k < nr; ++k, ki += nd, jr += nd) {
        if (i != k && a[ki] != 0.0f) {
          r = a[ki];                /* pivot target */
          a[ki] = 0.0f;
          ij = ir;                  /* index of pivot row */
          kj = jr;                  /* index of jth row   */
          for (int j = 0; j < nc; ++j) { /* subtract multiples of pivot row from jth row */
            a[kj] -= r * a[ij];
            ++ij;
            ++kj;
          }
        }
      }
    }
    return det_val;
  }

  /** swaps two columns in the given matrix
   * @param i1  index of the first column
   * @param i2  index of the second column
   * @param A   matrix
   * @param nc  number of rows
   * @param ne  number of columns
   */
  private static void swapColumns( int i1, int i2, int[] A, int nc, int ne )
  {
    for ( int j=0; j<nc; ++j ) {
      int t = A[ j*ne + i1 ];
      A[ j*ne + i1 ] = A[j*ne + i2];
      A[ j*ne + i2 ] = t;
    }
  }
  
  // identify a set of independent columns in the incidence matrix
  // and permute the columns of A
  // return the permutation ie the indices of first column, second, and so on
  private static void independentColumns( int[] A, int nc, int ne, int[] permutation )
  {
    for ( int k=0; k<ne; ++k ) permutation[k] = k;
    int k1 = nc;
    for ( int i=0; i<nc; ++i ) {
      int k = i;
      for ( ; k<ne; ++k ) {
        if ( A[ i*ne + k ] != 0 ) break;
      }
      for ( ; k<ne; ++k ) {
        int cnt = 1;
        for ( int j=i+1; j<nc; ++j ) if ( A[ j*ne + k ] == 0 ) ++cnt;
        if ( cnt == nc - i ) break;
      }
      if ( k != i ) {
        swapColumns( i, k, A, nc, ne );
        int a = permutation[i];
        permutation[i] = permutation[k];
        permutation[k] = a;
      }
    }
  }

  // compute cycle closure compensations
  // @param A    branch-cycle incidence matrix [ne X nc] accessed as A[ c * ne + b ]
  // @param CE   cycle closure errors [nc]
  // @param WE   branch weights [ne]
  // @param DE   branch corrections
  //
  // A * E = C is rewritten (after reordering columns)
  //   A1 * E1 + B1 * E2 = C
  // with A1 invertible: A2 = A1^-1. Therefore
  //   E1 = A2 * C - A2 * B1 * E2
  // The minimization fct is the sum of squared errors (possibly weighted)
  //   f(E1,E2) = w1 * E1 * E1 + w2 * E2 * E2
  // Substituting the expression of E1 this is a function of E2 only, f(E2) and
  //   1/2 df/dE2 =  w2 * E2 - (A2*B1)^t * w1 * ( A2*C - A2*B1 * E2 )
  // therefore we have the equation
  //   ( w2 * 1 + w1 * (A2*B1)^t * (A2*B1) ) * E2 = w1 * (A2*B1)^t * (A2*C)
  // i.e.
  //   D * E2 = F
  //   E2 = D^-1 * F
  // and
  //   E1 = A2 * C - (A2*B1) * D^-1 * F
  //
  static void correctCycles( int[] A, double[] CE, double[] WE, double[] DE, int ne, int nc )
  {
    int[] permutation = new int[ ne ];
    independentColumns( A, nc, ne, permutation );
    // print( "Incidence matrix:", A, nc, ne ); 
    // permutation[0] is the first independent column
    // permutation[1] is the second independent column
    // and so on
  
    // split incidence matrix in A-part and B-part
    int nb = ne - nc;
    double[] A0 = new double[ nc * nc ];
    double[] B0 = new double[ nb * nc ];
    for ( int i=0; i<nc; ++i ) {
      for ( int k=0; k<nc; ++k ) A0[i*nc + k] = A[ i*ne + k];      // row i, column k
      for ( int j=0; j<nb; ++j ) B0[i*nb + j] = A[ i*ne + nc + j]; // row i, column j
    }
    // print( "A0 matrix:", A0, nc, nc ); 
    // print( "B0 matrix:", B0, nc, nb ); 
  
    // inverse of A0
    double det = computeInverse( A0, nc, nc, nc );
    // printf( "det %.4f\n", det );
    // print( "A0 inverse:", A0, nc, nc ); 
  
    double[] A0B  = new double[ nc * nb ]; // A^-1 * B
    double[] A0CE = new double[ nc ];      // A^-1 * C
    for ( int i=0; i<nc; ++i ) {
      double sum = 0;
      for ( int k=0; k<nc; ++k ) sum += A0[i*nc + k] * CE[k];
      A0CE[ i ] = sum;

      for ( int j=0; j<nb; ++j ) {
        sum = 0;
        for ( int k=0; k<nc; ++k ) sum += A0[i*nc + k] * B0[k*nb + j];
        A0B[ i*nb + j ] = sum;
      }
    }
    // print( "A0B matrix:", A0B, nc, nb ); 
    // print( "A0C matrix:", A0C, nc, 1 ); 
  
    // D = I + AB^t AB
    double[] D = new double[ nb * nb ];
    for ( int i=0; i<nb; ++i ) {
      for ( int j=0; j<nb; ++j ) {
        double sum = (i==j)? WE[ permutation[nc+i] ] : 0;
        for ( int k=0; k<nc; ++k ) sum += A0B[k*nb + i] * WE[ permutation[k] ] * A0B[k*nb + j];
        D[ i*nb + j ] = sum;
      }
    }
    // print( "D matrix:", D, nb, nb ); 
  
    // F = AB^t AC
    double[] FE = new double[ nb ];
    for ( int i=0; i<nb; ++i ) {
      double sum = 0;
      for ( int k=0; k<nc; ++k ) sum += A0B[k*nb + i] * WE[ permutation[k] ] * A0CE[k];
      FE[ i ] = sum;
    }
    // print( "F matrix:", F, nb, 1 ); 
  
    det = computeInverse( D, nb, nb, nb );
    // printf( "det %.4f\n", det );
  
    // Eps_1 = D^-1 * E
    double[] D0 = new double[ ne ];
    for ( int i=0; i<nb; ++i ) { // second part
      double sum = 0;
      for ( int k=0; k<nb; ++k ) sum += D[i*nb + k] * FE[k];
      D0[ nc + i ] = sum;
    }
    for ( int i=0; i<nc; ++i ) { // first part
      double sum = A0CE[ i ];
      for ( int k=0; k<nb; ++k ) sum -= A0B[i*nb + k] * D0[nc + k];
      D0[ i ] = sum;
    }
    for ( int i=0; i<ne; ++i ) DE[ permutation[i] ] = - D0[i];
  }

}
