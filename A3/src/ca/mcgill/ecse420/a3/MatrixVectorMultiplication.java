package ca.mcgill.ecse420.a3;

import java.util.Vector;
import java.util.concurrent.*;

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

    public void printMatrix() {
      for (int i = 0; i < dim; i++) {
        for (int j = 0; j < dim; j++) {
          System.out.print(data[rowDisplace + i][colDisplace + j] + " ");
        }
        System.out.println();
      }
    }

    Matrix[][] split() {
      Matrix[][] result = new Matrix[2][2];
      int newDim = this.dim / 2;
      result[0][0] =
          new Matrix(this.data, this.rowDisplace, this.colDisplace, newDim);
      result[0][1] =
          new Matrix(this.data, this.rowDisplace, this.colDisplace + newDim, newDim);
      result[1][0] =
          new Matrix(this.data, this.rowDisplace + newDim, this.colDisplace, newDim);
      result[1][1] =
          new Matrix(this.data, this.rowDisplace + newDim, this.colDisplace + newDim, newDim);
      return result;
    }
  }

  public class Vector {

    int dim;
    double[] data;
    int displace;

    public Vector(int d) {
      dim = d;
      data = new double[d];
    }

    private Vector(double[] vector, int x, int d) {
      data = vector;
      displace = x;
      dim = d;
    }

    public double get(int i) {
      return data[i + displace];
    }

    public void set(int i, double value) {
      data[i + displace] = value;
    }

    public int getDim() {
      return dim;
    }

    Vector[] split() {
      Vector[] result = new Vector[2];
      int newDim = this.dim / 2;
      result[0] = new Vector(this.data, this.displace, newDim);
      result[1] = new Vector(this.data, this.displace + newDim, newDim);
      return result;
    }

    public void printVector() {
      for (int i = 0; i < dim; i++) {
        System.out.print(data[displace + i] + " ");
      }
      System.out.println();
    }
  }

  public class MatrixTask {

    // initialize the cached thread pool executor with 10 threads using the Executors factory
    int corePoolSize = 5;  // Minimum number of threads in the pool
    int maximumPoolSize = 10;  // Maximum number of threads in the pool
    long keepAliveTime = 1L;  // Keep alive time for idle threads
    TimeUnit unit = TimeUnit.SECONDS;
    BlockingQueue<Runnable> workQueue = new SynchronousQueue<Runnable>();  // Workqueue for holding tasks

    ExecutorService exec = new ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        new ThreadPoolExecutor.CallerRunsPolicy()  // Rejection policy
    );


    Matrix add(Matrix a, Matrix b) throws ExecutionException, InterruptedException {
      int n = a.getDim();
      Matrix c = new Matrix(n);
      Future<?> future = exec.submit(new AddTask(a, b, c));
      future.get();
      return c;
    }

    class AddTask implements Runnable {

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

    class AddVectorTask implements Runnable {

      Vector a, b, c;

      public AddVectorTask(Vector myA, Vector myB, Vector myC) {
        a = myA;
        b = myB;
        c = myC;
      }

      public void run() {
        try {
          int n = a.getDim();
          if (n == 1) {
            c.set(0, a.get(0) + b.get(0));
          } else {
            Vector[] aa = a.split(), bb = b.split(), cc = c.split();
            Future<?>[] future = (Future<?>[]) new Future[2];
            for (int i = 0; i < 2; i++) {
              future[i] = exec.submit(new AddVectorTask(aa[i], bb[i], cc[i]));
            }
            for (int i = 0; i < 2; i++) {
              future[i].get();
            }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
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
            Matrix[][] aa = a.split(), bb = b.split();
            Matrix[][] ll = lhs.split(), rr = rhs.split();
            Future<?>[][][] future = (Future<?>[][][]) new Future[2][2][2];
            for (int i = 0; i < 2; i++) {
              for (int j = 0; j < 2; j++) {
                future[i][j][0] =
                    exec.submit(new MulTask(aa[i][0], bb[0][j], ll[i][j]));
                future[i][j][1] =
                    exec.submit(new MulTask(aa[i][1], bb[1][j], rr[i][j]));
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

    class MulVectorTask implements Runnable {

      Matrix a;
      Vector b, c, lhs, rhs;

      public MulVectorTask(Matrix myA, Vector myB, Vector myC) {
        a = myA;
        b = myB;
        c = myC;
        lhs = new Vector(a.getDim());
        rhs = new Vector(a.getDim());
      }

      public void run() {
        try {
          if (a.getDim() == 1) {
            c.set(0, a.get(0, 0) * b.get(0));
          } else {
            Matrix[][] aa = a.split();
            Vector[] bb = b.split();
            Vector[] ll = lhs.split(), rr = rhs.split();
            Future<?>[][] future = (Future<?>[][]) new Future[2][2];
            for (int i = 0; i < 2; i++) {
              future[i][0] =
                  exec.submit(new MulVectorTask(aa[i][0], bb[0], ll[i]));
              future[i][1] =
                  exec.submit(new MulVectorTask(aa[i][1], bb[1], rr[i]));
            }
            for (int i = 0; i < 2; i++) {
              for (int k = 0; k < 2; k++) {
                future[i][k].get();
              }
            }
            Future<?> done = exec.submit(new AddVectorTask(lhs, rhs, c));
            done.get();
          }
        } catch (
            Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  public static void testMultiplyMatrixMatrix(int n, boolean verbose) {
    // write a test for the parallel matrix-vector multiplication:
    MatrixVectorMultiplication mvm = new MatrixVectorMultiplication();
    MatrixTask mt = mvm.new MatrixTask();
    Matrix a = mvm.new Matrix(n);
    Matrix b = mvm.new Matrix(n);

    //fill int the matrices with random integer values:
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        a.set(i, j, (int) (Math.random() * 10));
        b.set(i, j, (int) (Math.random() * 10));
      }
    }

    //multiply the matrices:
    try {
      Matrix c = mvm.new Matrix(n);
      if (verbose) {
        System.out.println("Matrix a:");
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < n; j++) {
            System.out.print(a.get(i, j) + " ");
          }
          System.out.println();
        }
        System.out.println("Matrix b:");
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < n; j++) {
            System.out.print(b.get(i, j) + " ");
          }
          System.out.println();
        }
      }
      // measure the time it takes to multiply the matrices
      long startTime = System.currentTimeMillis();
      mt.new MulTask(a, b, c).run();
      long endTime = System.currentTimeMillis();

      if (verbose) {
        System.out.println("Matrix c:");
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < n; j++) {
            System.out.print(c.get(i, j) + " ");
          }
          System.out.println();
        }
      }

      // calculate expected result sequentially and measure the time taken
      long startTimeSeq = System.currentTimeMillis();
      double[][] expected = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          expected[i][j] = 0;
          for (int k = 0; k < n; k++) {
            expected[i][j] += a.get(i, k) * b.get(k, j);
          }
        }
      }
      long endTimeSeq = System.currentTimeMillis();

      // compare the expected result with the actual result
      boolean correct = true;
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          if (expected[i][j] != c.get(i, j)) {
            correct = false;
            break;
          }
        }
      }
      if (correct) {
        System.out.println("The result is correct!");
        System.out.println("Parallel Time Taken: " + (endTime - startTime) + "ms");
        System.out.println("Sequential Time Taken: " + (endTimeSeq - startTimeSeq) + "ms");
      } else {
        System.out.println("\nThe result is incorrect!");
        System.out.println("Parallel Time Taken: " + (endTime - startTime) + "ms");
        System.out.println("Sequential Time Taken: " + (endTimeSeq - startTimeSeq) + "ms");
        if (verbose) {
          System.out.println("Expected result:");
          for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
              System.out.print(expected[i][j] + " ");
            }
            System.out.println();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void testAddMatrixMatrix(int n) {
    // write a test for the parallel matrix-vector multiplication:
    MatrixVectorMultiplication mvm = new MatrixVectorMultiplication();
    MatrixTask mt = mvm.new MatrixTask();
    Matrix a = mvm.new Matrix(n);
    Matrix b = mvm.new Matrix(n);

    //fill int the matrices with random integer values:
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        a.set(i, j, (int) (Math.random() * 10));
        b.set(i, j, (int) (Math.random() * 10));
      }
    }

    //multiply the matrices:
    try {
      Matrix c = mvm.new Matrix(n);

      mt.new AddTask(a, b, c).run();
      System.out.println("Matrix a:");
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          System.out.print(a.get(i, j) + " ");
        }
        System.out.println();
      }
      System.out.println("Matrix b:");
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          System.out.print(b.get(i, j) + " ");
        }
        System.out.println();
      }
      System.out.println("Matrix c:");
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          System.out.print(c.get(i, j) + " ");
        }
        System.out.println();
      }

      // calculate expected result
      double[][] expected = new double[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          expected[i][j] = a.get(i, j) + b.get(i, j);
        }
      }

      // compare the expected result with the actual result
      boolean correct = true;
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          if (expected[i][j] != c.get(i, j)) {
            correct = false;
            break;
          }
        }
      }
      if (correct) {
        System.out.println("The result is correct!");
      } else {
        System.out.println("\nThe result is incorrect!");
        System.out.println("Expected result:");
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < n; j++) {
            System.out.print(expected[i][j] + " ");
          }
          System.out.println();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void testAddMatrixVector(int n) {
    // write a test for the parallel matrix-vector multiplication:
    MatrixVectorMultiplication mvm = new MatrixVectorMultiplication();
    MatrixTask mt = mvm.new MatrixTask();
    Vector a = mvm.new Vector(n);
    Vector b = mvm.new Vector(n);

    //fill int the matrices with random integer values:
    for (int i = 0; i < n; i++) {
      b.set(i, (int) (Math.random() * 10));
      a.set(i, (int) (Math.random() * 10));
    }

    //add the matrices:
    try {
      Vector c = mvm.new Vector(n);

      mt.new AddVectorTask(a, b, c).run();
      System.out.println("Matrix a:");
      for (int i = 0; i < n; i++) {
        System.out.print(a.get(i) + " ");
      }
      System.out.println();
      System.out.println("Vector b:");
      for (int i = 0; i < n; i++) {
        System.out.print(b.get(i) + " ");
      }
      System.out.println();
      System.out.println("Vector c:");
      for (int i = 0; i < n; i++) {
        System.out.print(c.get(i) + " ");
      }

      // calculate expected result
      double[] expected = new double[n];
      for (int i = 0; i < n; i++) {
        expected[i] = a.get(i) + b.get(i);
      }

      // compare the expected result with the actual result
      boolean correct = true;
      for (int i = 0; i < n; i++) {
        if (expected[i] != c.get(i)) {
          correct = false;
          break;
        }
      }
      if (correct) {
        System.out.println("The result is correct!");
      } else {
        System.out.println("\nThe result is incorrect!");
        System.out.println("Expected result:");
        for (int i = 0; i < n; i++) {
          System.out.print(expected[i] + " ");
        }
        System.out.println();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void testMultiplyMatrixVector(int n, boolean verbose) {
    // write a test for the parallel matrix-vector multiplication:
    MatrixVectorMultiplication mvm = new MatrixVectorMultiplication();
    MatrixTask mt = mvm.new MatrixTask();
    Matrix a = mvm.new Matrix(n);
    Vector b = mvm.new Vector(n);

    //fill int the matrices with random integer values:
    for (int i = 0; i < n; i++) {
      b.set(i, (int) (Math.random() * 10));
      for (int j = 0; j < n; j++) {
        a.set(i, j, (int) (Math.random() * 10));
      }
    }

    //multiply the matrices:
    try {
      Vector c = mvm.new Vector(n);

      if (verbose) {
        System.out.println("Matrix a:");
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < n; j++) {
            System.out.print(a.get(i, j) + " ");
          }
          System.out.println();
        }

        System.out.println("Vector b:");
        for (int i = 0; i < n; i++) {
          System.out.print(b.get(i) + " ");
        }
        System.out.println();
      }

      // measure the time it takes to multiply the matrices
      long startTime = System.currentTimeMillis();
      mt.new MulVectorTask(a, b, c).run();
      long endTime = System.currentTimeMillis();

      if (verbose) {
        System.out.println("Vector c:");
        for (int i = 0; i < n; i++) {
          System.out.print(c.get(i) + " ");
        }
      }

      // calculate expected result
      long startTimeSeq = System.currentTimeMillis();
      double[] expected = new double[n];
      for (int i = 0; i < n; i++) {
        expected[i] = 0;
        for (int j = 0; j < n; j++) {
          expected[i] += a.get(i, j) * b.get(j);
        }
      }
      long endTimeSeq = System.currentTimeMillis();

      // compare the expected result with the actual result
      boolean correct = true;
      for (int i = 0; i < n; i++) {
        if (expected[i] != c.get(i)) {
          correct = false;
          break;
        }
      }
      if (correct) {
        System.out.println("The result is correct!");
        System.out.println("Parallel Time Taken: " + (endTime - startTime) + "ms");
        System.out.println("Sequential Time Taken: " + (endTimeSeq - startTimeSeq) + "ms");
      } else {
        System.out.println("\nThe result is incorrect!");
        System.out.println("Parallel Time Taken: " + (endTime - startTime) + "ms");
        System.out.println("Sequential Time Taken: " + (endTimeSeq - startTimeSeq) + "ms");
        if (verbose) {
          System.out.println("Expected result:");
          for (int i = 0; i < n; i++) {
            System.out.print(expected[i] + " ");
          }
          System.out.println();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    testMultiplyMatrixVector(4096, false);
  }

}
