package ca.mcgill.ecse420.a2;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
// This code is based on the code from the book "The Art of Multiprocessor Programming"
// In Chapter 2, Lecture slide 83

public class Filter implements Lock {

  int[] level; //level[i] for thread i
  int[] victim; //victim[L] for level L

  //*
  // * initializing Filter lock for n threads
  // * @param n number of threads
  // */
  public Filter(int n) {
    level = new int[n];
    victim = new int[n];
    for (int i = 1; i < n; i++) {
      level[i] = 0;
    }
  }

  public void lock() {
    int n = level.length;
    int i = ThreadID.get();
    for (int L = 1; L < n; L++) {
      level[i] = L; // Thread i is at level L
      victim[L] = i; // Thread i is the victim at level L
      for (int k = 0; k < n; k++) { // For all other threads
        if (k != i) { // If the thread is not i
          while (level[k] >= L
              && victim[L] == i) { //wait till the thread k is at a lower level and the victim is i
            // Busy waiting
          }
        }
      }
    }
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {

  }

  @Override
  public boolean tryLock() {
    return false;
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return false;
  }

  public void unlock() {
    level[ThreadID.get()] = 0;
  }

  @Override
  public Condition newCondition() {
    return null;
  }

}


class Counter {

  private int count;

  public void increment() {
    count++;
  }

  public int getCount() {
    return count;
  }
}


