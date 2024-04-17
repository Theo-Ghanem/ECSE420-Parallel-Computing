package ca.mcgill.ecse420.a3;

import static ca.mcgill.ecse420.a3.MatrixVectorMultiplication.MatrixTask.exec;

import ca.mcgill.ecse420.a3.MatrixVectorMultiplication.MatrixTask.AddTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixVectorMultiplication {

  /**
   * @author inspired from "The Art of Multiprocessor Programming" Chap 16 page 372 but adapted
   */
  public class Matrix {

    int dim;
    double[][] data;
    int rowDisplace, colDisplace;

    public Matrix(int d) {
      dim = d;
      rowDisplace = colDisplace = 0;
      data = new double[d][d];
    }

    private Matrix(double[][] matrix, int x, int y, int d) {
      data = matrix;
      rowDisplace = x;
      colDisplace = y;
      dim = d;
    }

    public double get(int row, int col) {
      return data[row + rowDisplace][col + colDisplace];
    }

    public void set(int row, int col, double value) {
      data[row + rowDisplace][col + colDisplace] = value;
    }

    public int getDim() {
      return dim;
    }

    Matrix[][] split() {
      Matrix[][] result = new Matrix[2][2];
      int newDim = dim / 2;
      result[0][0] =
          new Matrix(data, rowDisplace, colDisplace, newDim);
      result[0][1] =
          new Matrix(data, rowDisplace, colDisplace + newDim, newDim);
      result[1][0] =
          new Matrix(data, rowDisplace + newDim, colDisplace, newDim);
      result[1][1] =
          new Matrix(data, rowDisplace + newDim, colDisplace + newDim, newDim);
      return result;
    }
  }

  public class MatrixTask {

    static ExecutorService exec = Executors.newCachedThreadPool();

    //...
    Matrix add(Matrix a, Matrix b) throws ExecutionException, InterruptedException {
      int n = a.getDim();
      Matrix c = new Matrix(n);
      Future<?> future = exec.submit(new AddTask(a, b, c));
      future.get();
      return c;
    }

    static class AddTask implements Runnable {

      Matrix a, b, c;

      public AddTask(Matrix myA, Matrix myB, Matrix myC) {
        a = myA;
        b = myB;
        c = myC;
      }

      public void run() {
        try {
          int n = a.getDim();
          if (n == 1) {
            c.set(0, 0, a.get(0, 0) + b.get(0, 0));
          } else {
            Matrix[][] aa = a.split(), bb = b.split(), cc = c.split();
            Future<?>[][] future = (Future<?>[][]) new Future[2][2];
            for (int i = 0; i < 2; i++) {
              for (int j = 0; j < 2; j++) {
                future[i][j] =
                    exec.submit(new AddTask(aa[i][j], bb[i][j], cc[i][j]));
              }
            }
            for (int i = 0; i < 2; i++) {
              for (int j = 0; j < 2; j++) {
                future[i][j].get();
              }
            }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  class MulTask implements Runnable {

    Matrix a, b, c, lhs, rhs;

    public MulTask(Matrix myA, Matrix myB, Matrix myC) {
      a = myA;
      b = myB;
      c = myC;
      lhs = new Matrix(a.getDim());
      rhs = new Matrix(a.getDim());
    }

    public void run() {
      try {
        if (a.getDim() == 1) {
          c.set(0, 0, a.get(0, 0) * b.get(0, 0));
        } else {
          Matrix[][] aa = a.split(), bb = b.split(), cc = c.split();
          Matrix[][] ll = lhs.split(), rr = rhs.split();
          Future<?>[][][] future = (Future<?>[][][]) new Future[2][2][2];
          for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
              future[i][j][0] =
                  exec.submit(new MulTask(aa[i][0], bb[0][i], ll[i][j]));
              future[i][j][1] =
                  exec.submit(new MulTask(aa[1][i], bb[i][1], rr[i][j]));
            }
          }
          for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
              for (int k = 0; k < 2; k++) {
                future[i][j][k].get();
              }
            }
          }
          Future<?> done = exec.submit(new AddTask(lhs, rhs, c));
          done.get();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Returns the result of a sequential matrix multiplication The two matrices are randomly
   * generated
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
//  public static double[][] sequentialMultiplyMatrixVector(double[][] a, double[][] b) {
//    double[][] resultMatrix = new double[MATRIX_SIZE][MATRIX_SIZE];
//    for (int rowMatrixA = 0; rowMatrixA < MATRIX_SIZE; rowMatrixA++) {
//      for (int colMatrixB = 0; colMatrixB < MATRIX_SIZE; colMatrixB++) {
//        for (int colMatrixA = 0; colMatrixA < MATRIX_SIZE; colMatrixA++) {
//          resultMatrix[rowMatrixA][colMatrixB] +=
//              a[rowMatrixA][colMatrixA] * b[colMatrixA][colMatrixB];
//        }
//      }
//    }
//    return resultMatrix;
//  }
  public static void main(String[] args) {
    // write a test for the parallel matrix-vector multiplication:
    MatrixVectorMultiplication mvm = new MatrixVectorMultiplication();
    Matrix a = mvm.new Matrix(4);
    Matrix b = mvm.new Matrix(4);

    //fill int the matrices with random integer values:
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        a.set(i, j, (int) (Math.random() * 10));
        b.set(i, j, (int) (Math.random() * 10));
      }
    }

    //multiply the matrices:
    try {
      Matrix c = mvm.new Matrix(4);
      mvm.new MulTask(a, b, c).run();
      System.out.println("Matrix a:");
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          System.out.print(a.get(i, j) + " ");
        }
        System.out.println();
      }
      System.out.println("Matrix b:");
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          System.out.print(b.get(i, j) + " ");
        }
        System.out.println();
      }
      System.out.println("Matrix c:");
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          System.out.print(c.get(i, j) + " ");
        }
        System.out.println();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
