package ca.mcgill.ecse420.a1;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Deadlock {
    public static void main(String[] args) {
        final Lock lock1 = new ReentrantLock();
        final Lock lock2 = new ReentrantLock();

        // The first thread (t1) that will try to acquire lock1 then lock2
        Thread t1 = new Thread(() -> {
            // Try to acquire lock1
            lock1.lock();
            System.out.println("Thread 1: Acquired lock 1");

            // Sleep for a short time to allow the other thread to start running
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }

            // Try to acquire lock2
            lock2.lock();
            System.out.println("Thread 1: Acquired lock 2");

            // Release the locks
            lock2.unlock();
            System.out.println("Thread 1: Released lock 2");
            lock1.unlock();
            System.out.println("Thread 1: Released lock 1");
        });

        // The second thread (t2) that will try to acquire lock2 then lock1
        Thread t2 = new Thread(() -> {
            // Try to acquire lock2
            lock2.lock();
            System.out.println("Thread 2: Acquired lock 2");

            // Sleep for a short time to allow the other thread to start running
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }

            // Try to acquire lock1
            lock1.lock();
            System.out.println("Thread 2: Acquired lock 1");

            // Release the locks
            lock1.unlock();
            System.out.println("Thread 2: Released lock 1");
            lock2.unlock();
            System.out.println("Thread 2: Released lock 2");
        });

        // Start the two threads
        while (true) {
            try {
                t1.start();
                t2.start();
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                System.out.println("Interrupted thread. Restarting...");
            }
        }
    }
}
