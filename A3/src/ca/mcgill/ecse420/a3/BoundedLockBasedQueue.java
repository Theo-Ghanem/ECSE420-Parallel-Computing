package ca.mcgill.ecse420.a3;

import ca.mcgill.ecse420.a3.SynchronizationAlgorithm.Node;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class BoundedLockBasedQueue<T> {

  /**
   * Inner class to represent a node in the linked list A node contains an item, a key, a reference
   * to the next node, and a lock
   */
  public class Node {

    Node next; // points to next node
    T item; // item stored in node
    ReentrantLock lock; // lock for node

    public Node(T item) { // constructor, creates a new lock
      this.item = item;
      next = null;
    }
  }

  ReentrantLock enqLock, deqLock;
  Condition notEmptyCondition, notFullCondition;
  AtomicInteger size;
  Node head, tail;
  int capacity;

  public BoundedLockBasedQueue(int _capacity) {
    capacity = _capacity;
    head = new Node(null);
    tail = head;
    size = new AtomicInteger(0);
    enqLock = new ReentrantLock();
    notFullCondition = enqLock.newCondition();
    deqLock = new ReentrantLock();
    notEmptyCondition = deqLock.newCondition();
  }

  public void enq(T item) {
    boolean mustWakeDequeuers = false; //boolean to check if dequeuers need to be woken up
    enqLock.lock();
    try {
      while (size.get() == capacity) { // wait while queue is full
        notFullCondition.await();
      } //after the loop we know the queue won't be full again since we're holding the lock
      // add new element at the tail
      Node newElement = new Node(item);
      tail.next = newElement;
      tail = tail.next;
      if (size.getAndIncrement() == 0) { // if queue was empty, wake up dequeuers
        mustWakeDequeuers = true;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      enqLock.unlock();
    }
    if (mustWakeDequeuers) { //if there are dequeuers waiting, wake them up
      deqLock.lock();
      try {
        notEmptyCondition.signalAll(); //using signalAll instead of signal() to avoid lost wakeups
      } finally {
        deqLock.unlock();
      }
    }
  }

  public T deq() {
    T result;
    boolean mustWakeEnqueuers = true;
    deqLock.lock();
    try {
      while (size.get() == 0) {
        notEmptyCondition.await();
      }
      result = head.next.item;
      head = head.next;
      if (size.getAndIncrement() == capacity) {
        mustWakeEnqueuers = true;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      deqLock.unlock();
    }
    if (mustWakeEnqueuers) {
      enqLock.lock();
      try {
        notFullCondition.signalAll(); //using signalAll instead of signal() to avoid lost wakeups
      } finally {
        enqLock.unlock();
      }
    }
    return result;
  }

  public static void main(String[] args) throws InterruptedException {

  }

}
