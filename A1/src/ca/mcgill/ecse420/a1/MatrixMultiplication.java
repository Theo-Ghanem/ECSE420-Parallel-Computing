package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class MatrixMultiplication {

  private static int NUMBER_THREADS = 16;
  private static int MATRIX_SIZE = 2000; //change back to 2000

  public static void main(String[] args) {

    // Generate two random matrices, same size
    double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);

//    //Q1.1
//    long sequentialDuration =
//        measureExecutionTime(MatrixMultiplication::sequentialMultiplyMatrix, a, b) / 1_000_000_000;
//    System.out.println("Sequential: " + sequentialDuration + " s");
//
//    //Q1.2
//    long parallelDuration =
//        measureExecutionTime(MatrixMultiplication::parallelMultiplyMatrix, a, b) / 1_000_000_000;
//    System.out.println("Parallel: " + parallelDuration + " s");
//
//    System.out.println("parallel execution is " + sequentialDuration / parallelDuration
//        + " times faster than sequential execution");
//
//    //Q1.1
//    boolean matricesAreEqual = compareMatrices(a, b, parallelMultiplyMatrix(a, b));
//    if (matricesAreEqual) {
//      System.out.println("The matrices are equal.");
//    } else {
//      System.out.println("The matrices are not equal.");
//    }
//
//    //Q1.4
    executeAndPlot(a, b, NUMBER_THREADS);

//    //Q1.5
    int[] matrixSizes = {100, 200, 500, 1000, 2000, 3000, 4000};
//    int[] matrixSizes = {100, 200, 500, 1000};
//    executeAndPlotMatrixSize(NUMBER_THREADS, matrixSizes);
  }

  /**
   * Returns the result of a sequential matrix multiplication The two matrices are randomly
   * generated
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
    double[][] resultMatrix = new double[MATRIX_SIZE][MATRIX_SIZE];
    for (int rowMatrixA = 0; rowMatrixA < MATRIX_SIZE; rowMatrixA++) {
      for (int colMatrixB = 0; colMatrixB < MATRIX_SIZE; colMatrixB++) {
        for (int colMatrixA = 0; colMatrixA < MATRIX_SIZE; colMatrixA++) {
          resultMatrix[rowMatrixA][colMatrixB] +=
              a[rowMatrixA][colMatrixA] * b[colMatrixA][colMatrixB];
        }
      }
    }
    return resultMatrix;
  }

  /**
   * Returns the result of a concurrent matrix multiplication The two matrices are randomly
   * generated
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
    double[][] resultMatrix = new double[MATRIX_SIZE][MATRIX_SIZE];

    ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
    for (int rowMatrixA = 0; rowMatrixA < MATRIX_SIZE; rowMatrixA++) {
      for (int colMatrixB = 0; colMatrixB < MATRIX_SIZE; colMatrixB++) {
        final int finalRowMatrixA = rowMatrixA;
        final int finalColMatrixB = colMatrixB;
        executorService.submit(() -> {
          for (int colMatrixA = 0; colMatrixA < MATRIX_SIZE; colMatrixA++) {
            resultMatrix[finalRowMatrixA][finalColMatrixB] +=
                a[finalRowMatrixA][colMatrixA] * b[colMatrixA][finalColMatrixB];
          }
        });
      }
    }
    executorService.shutdown();

    // Wait until all tasks are finished
    try {
      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    while (!executorService.isTerminated())
      ;
    return resultMatrix;
  }

  /**
   * Populates a matrix of given size with randomly generated integers between 0-10.
   *
   * @param numRows number of rows
   * @param numCols number of cols
   * @return matrix
   */
  private static double[][] generateRandomMatrix(int numRows, int numCols) {
    double[][] matrix = new double[numRows][numCols];
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        matrix[row][col] = (double) ((int) (Math.random() * 10.0));
      }
    }
    return matrix;
  }

  /**
   * Question 1.3 Measures the execution time of a method in nanoseconds and prints it to the
   * console.
   *
   * @param methodToMeasure
   * @param matrix1
   * @param matrix2
   * @return execution time in nanoseconds
   */
  public static long measureExecutionTime(
      BiFunction<double[][], double[][], double[][]> methodToMeasure, double[][] matrix1,
      double[][] matrix2) {
    long startTime = System.nanoTime();
    methodToMeasure.apply(matrix1, matrix2);
    long endTime = System.nanoTime();
    long duration = endTime - startTime;
//    System.out.println("Execution time in nanoseconds: " + duration);
//    System.out.println("Execution time in milliseconds: " + duration / 1_000_000);
    System.out.println("Execution time in seconds: " + duration / 1_000_000_000);
    return duration;
  }

  /**
   * Helper method to print out matrices
   *
   * @param matrix you want to print
   */
  public static void printMatrix(double[][] matrix) {
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        System.out.print(matrix[i][j] + " ");
      }
      System.out.println();
    }
  }


  /**
   * Helper method to compare two matrices and check if they are equal using Apache Commons Math
   * library
   *
   * @param a
   * @param b
   * @param matrixToBeChecked
   * @return
   */
  public static boolean compareMatrices(double[][] a, double[][] b, double[][] matrixToBeChecked) {
    RealMatrix matrixA = MatrixUtils.createRealMatrix(a);
    RealMatrix matrixB = MatrixUtils.createRealMatrix(b);
    RealMatrix correctMatrix = matrixA.multiply(matrixB);
    double[][] correctMatrixArray = correctMatrix.getData();
    if (matrixToBeChecked.length != correctMatrixArray.length
        || matrixToBeChecked[0].length != correctMatrixArray[0].length) {
      return false; // Matrices don't have the same dimensions
    }

    for (int i = 0; i < matrixToBeChecked.length; i++) {
      for (int j = 0; j < matrixToBeChecked[i].length; j++) {
        if (matrixToBeChecked[i][j] != correctMatrixArray[i][j]) {
          return false; // Found a mismatch
        }
      }
    }
    return true; // Matrices are equal
  }

  /**
   * Helper method to execute and plot the execution time of the parallel and sequential methods
   *
   * @param a          Matrix 1
   * @param b          Matrix 2
   * @param numThreads number of threads to use
   */
  public static void executeAndPlot(double[][] a, double[][] b, int numThreads) {
    System.out.println("Executing and plotting up to " + numThreads + " threads:");
    XYSeries seriesParallel = new XYSeries("Parallel Execution Time");
    XYSeries seriesSequential = new XYSeries("Sequential Execution Time");

    System.out.println("Running sequential execution...");
    long sequentialDuration = measureExecutionTime(MatrixMultiplication::sequentialMultiplyMatrix,
        a, b);
    seriesSequential.add(1,
        sequentialDuration / 1_000_000_000); // Add to the first index as it's only executed once
    System.out.println("Sequential execution done!");

    for (int i = 1; i <= numThreads; i++) {
      NUMBER_THREADS = i;
      seriesSequential.add(i,
          sequentialDuration / 1_000_000_000); // this ensures we have a cst line for the sequential

      System.out.println("Running parallel execution with " + i + " threads:");
      long parallelDuration = measureExecutionTime(MatrixMultiplication::parallelMultiplyMatrix, a,
          b);
      seriesParallel.add(i, parallelDuration / 1_000_000_000);
//      System.out.println(parallelDuration / 1_000_000_000 + " seconds");
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(seriesSequential);
    dataset.addSeries(seriesParallel);

    JFreeChart chart = ChartFactory.createXYLineChart(
        "Execution Time vs Number of Threads",
        "Number of Threads",
        "Execution Time (seconds)",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    );
    setPlotSettings(chart);
  }

  /**
   * Method to execute and plot the execution time of the parallel and sequential methods based on
   * the matrix size
   *
   * @param numThreads  number of threads to use
   * @param matrixSizes sizes of the matrices to use
   */

  public static void executeAndPlotMatrixSize(int numThreads, int[] matrixSizes) {
    System.out.println(
        "Executing and plotting different matrix sizes with " + numThreads + " threads:");
    XYSeries seriesParallel = new XYSeries("Parallel Execution Time");
    XYSeries seriesSequential = new XYSeries("Sequential Execution Time");

    for (int i : matrixSizes) {
      System.out.println("Matrix size: " + i);
      MATRIX_SIZE = i;
      double[][] a = generateRandomMatrix(i, i);
      double[][] b = generateRandomMatrix(i, i);

      System.out.println("Running sequential execution...");
      long sequentialDuration = measureExecutionTime(MatrixMultiplication::sequentialMultiplyMatrix,
          a, b);
      seriesSequential.add(i, sequentialDuration / 1_000_000_000);

      System.out.println("Running parallel execution...");
      long parallelDuration = measureExecutionTime(MatrixMultiplication::parallelMultiplyMatrix, a,
          b);
      seriesParallel.add(i, parallelDuration / 1_000_000_000);
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(seriesSequential);
    dataset.addSeries(seriesParallel);

    JFreeChart chart = ChartFactory.createXYLineChart(
        "Execution Time vs Matrix Size",
        "Matrix Size",
        "Execution Time (seconds)",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    );
    setPlotSettings(chart);
  }

  /**
   * Helper method to set the plot settings
   *
   * @param chart you want to set the settings for
   */
  private static void setPlotSettings(JFreeChart chart) {
    // Set the X-axis to only allow integer tick units
//    NumberAxis xAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
//    xAxis.setTickUnit(new NumberTickUnit(1)); // Set custom tick unit
    ChartPanel panel = new ChartPanel(chart);
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }


}
