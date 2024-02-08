package ca.mcgill.ecse420.a1;

public class Deadlock {

  // Code inspired from https://www.javatpoint.com/deadlock-in-java
// and lecture notes
  public static void main(String[] args) {
    // Two resources that the threads will try to lock
    final String resource1 = "resource1";
    final String resource2 = "resource2";

    // The first thread (t1) that will try to lock resource1 then resource2
    Thread t1 = new Thread() {
      public void run() {
        // Try to lock resource1
        synchronized (resource1) {
          System.out.println("Thread 1: locked resource 1");

          // Sleep for a short time to allow the other thread to start running
          try {
            Thread.sleep(50);
          } catch (Exception ignored) {
          }

          // Try to lock resource2
          synchronized (resource2) {
            System.out.println("Thread 1: locked resource 2");
          }
        }
      }
    };

    // The second thread (t2) that will try to lock resource2 then resource1
    Thread t2 = new Thread() {
      public void run() {
        // Try to lock resource2
        synchronized (resource2) {
          System.out.println("Thread 2: locked resource 2");

          // Sleep for a short time to allow the other thread to start running
          try {
            Thread.sleep(50);
          } catch (Exception ignored) {
          }

          // Try to lock resource1
          synchronized (resource1) {
            System.out.println("Thread 2: locked resource 1");
          }
        }
      }
    };

    // Start the two threads
    t1.start();
    t2.start();
  }
}