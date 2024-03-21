package ca.mcgill.ecse420.a2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This code was demonstrated in class by Prof. Denis Giannacopoulos During the lecture on January
 * 25, 2024 It has been adapted to work with the Bakery lock algorithm in Bakery.java
 *
 * @author dennisgiannacopoulos
 */
public class BakeryTest {

  // Number of threads that can use the Bakery lock is unlimited (in theory):
  private final static int NTHREADS = 8;

  // Number of attempts that will be made to acquire lock by each thread:
  private final static int PER_THREAD = 128;

  // Number of attempts that will be made, in total, to acquire lock:
  private final static int COUNT = NTHREADS * PER_THREAD;

  // Number of times the lock is acquired in total:
  static int lockCounter = 0;

  // Create an array to write the value of counter to each time it is incremented:
  static int[] counterArray = new int[COUNT];

  // Create an array to write the ID of thread incrementing counter:
  static int[] threadIDArray = new int[COUNT];

  // Create an instance of a Bakery lock object to test:
  private static Bakery lock = new Bakery(NTHREADS);

  // Runnable task that each thread will use to acquire lock:
  public static class BakeryThread implements Runnable {

    @Override
    public void run() {
      for (int i = 0; i < PER_THREAD; i++) {
        lock.lock();
        try {
          lockCounter = lockCounter + 1;
          counterArray[lockCounter - 1] = lockCounter;
          threadIDArray[lockCounter - 1] = ThreadID.get();
        } finally {
          lock.unlock();
        }
      }
    }
  }

  public static void main(String[] args) {
    ThreadID.reset();

    // Create a thread pool with NTHREADS to use:
    ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);

    // Create and execute runnable tasks to test lock object with:
    for (int i = 0; i < NTHREADS; i++) {
      executor.execute(new BakeryThread());
    }

    executor.shutdown();

    // Wait until all tasks are finished
    while (!executor.isTerminated()) {

    }

    // Display results to output:
    System.out.flush();
    for (int i = 0; i < COUNT; i++) {
      System.out.println("ID: " + threadIDArray[i] + " counter = " + counterArray[i]);
    }
    System.out.println("Final value of shared counter (should be: " + COUNT + ") = " + lockCounter);

  }


}