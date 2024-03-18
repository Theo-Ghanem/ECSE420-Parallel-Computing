package ca.mcgill.ecse420.a2;

public class FilterLockTest {

  private final Filter lock;
  private final int n; // Number of threads
  private final int numIterations;

  public FilterLockTest(Filter lock, int n, int numIterations) {
    this.lock = lock;
    this.n = n;
    this.numIterations = numIterations;
  }

  public void runTest() throws InterruptedException {
    Counter counter = new Counter(); // Shared resource

    Thread[] threads = new Thread[n];
    for (int i = 0; i < n; i++) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < numIterations; j++) {
//          System.out.println("Thread " + ThreadID.get() + " is trying to lock");
          lock.lock();
//          synchronized (System.out) {
//            System.out.println("Thread " + ThreadID.get() + " has acquired the lock");
//          }
          counter.increment(); // Access shared resource
          lock.unlock();
          System.out.println("Thread " + ThreadID.get() + " has acquired the lock");
          System.out.println("Thread " + ThreadID.get() + " has unlocked");

        }
      });
      threads[i].start(); // Start threads
    }

    // Wait for threads to terminate
    for (Thread thread : threads) {
      thread.join();
    }

    // Check if the counter value is correct (should be n * iterations)
    if (counter.getCount() != n * numIterations) {
      System.out.println(
          "Error: Counter value is incorrect. Expected: " + (n * numIterations) + ", Actual: "
              + counter.getCount());
    } else {
      System.out.println("Test passed! Counter value is consistent.");
    }
  }

  public static void main(String[] args) throws InterruptedException {
    int numThreads = 4; // Adjust the number of threads
    int numIterations = 1000; // Adjust the number of iterations
    Filter lock = new Filter(numThreads); // Create Filter lock instance
    FilterLockTest test = new FilterLockTest(lock, numThreads, numIterations);
    test.runTest();
  }
}
