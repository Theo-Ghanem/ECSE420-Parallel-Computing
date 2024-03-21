package ca.mcgill.ecse420.a2;

public class ShakyLockTest1 {

  class ShakyLock {

    private int turn;
    private boolean busy = false;

    public void lock() {
//      System.out.println("Thread " + ThreadID.get() + " is trying to lock");
      int me = ThreadID.get(); // Simulates getting unique thread ID
      turn = me;
      do {
        busy = true;
      } while (turn == me || busy);
    }

    public void unlock() {
      System.out.println("Thread " + ThreadID.get() + " is unlocking");
      busy = false;
    }
  }

  public void runTest() {
    ShakyLock lock = new ShakyLock();
    Thread threadA = new Thread(() -> {
      lock.lock();
      System.out.println("Thread A in Critical Section");
      // Simulate work in critical section
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      lock.unlock();
    });
    Thread threadB = new Thread(() -> {
      lock.lock();
      System.out.println("Thread B in Critical Section");
      // Simulate work in critical section
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      lock.unlock();
    });

    threadA.start();
    threadB.start();

    try {
      threadA.join();
      threadB.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    ShakyLockTest1 test = new ShakyLockTest1();
    test.runTest();
  }
}

