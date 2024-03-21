package ca.mcgill.ecse420.a2;

public class ShakyLockTest2 {

  class ShakyLock {

    private int turn;
    private boolean busy = false;

    public void lock() {
      int me = ThreadID.get(); // Simulates getting unique thread ID
      turn = me;
      do {
        busy = true;
      } while (turn == me || busy);
    }

    public void unlock() {
      busy = false;
    }
  }

  public void runTest() {
    ShakyLock lock = new ShakyLock();
    int numThreads = 3;

    Thread[] threads = new Thread[numThreads];
    for (int i = 0; i < numThreads; i++) {
      int finalI = i;
      threads[i] = new Thread(() -> {
        lock.lock();
        System.out.println("Thread " + (finalI + 1) + " in Critical Section");
        // Simulate work in critical section
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        lock.unlock();
      });
      threads[i].start();
    }

    try {
      for (Thread thread : threads) {
        thread.join();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    ShakyLockTest2 test = new ShakyLockTest2();
    test.runTest();
  }
}

