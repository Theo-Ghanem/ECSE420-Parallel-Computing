package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * Lock-based bounded queue implementation based on slides from lecture and "The Art of
 * Multiprocessor Programming" Chap 10, page 225-227
 *
 * @param <T> Type of elements in the queue
 */
public class LockBasedBoundedQueue<T> {

  private ReentrantLock enqLock, deqLock;
  private Condition notEmptyCondition, notFullCondition;
  private AtomicInteger size;
  private int head, tail;
  private int capacity;
  private T[] items; // array to store items (acts as queue)

  /**
   * Constructor for the LockBasedBoundedQueue
   *
   * @param capacity The capacity of the bounded queue
   */
  public LockBasedBoundedQueue(int capacity) {
    this.items = (T[]) new Object[capacity];
    this.tail = this.head = 0;
    this.size = new AtomicInteger(0);
    this.enqLock = new ReentrantLock();
    this.notFullCondition = enqLock.newCondition();
    this.deqLock = new ReentrantLock();
    this.notEmptyCondition = deqLock.newCondition();
    this.capacity = capacity;
  }

  /**
   * Method to enqueue an item into the queue If the queue is full, the method will wake up
   * dequeuers and wait until there is space in the queue
   *
   * @param item The item to enqueue
   */
  public void enq(T item) {
    boolean mustWakeDequeuers = false; //boolean to check if dequeuers need to be woken up
    enqLock.lock();
    try {
      while (size.get() == capacity) { // wait while queue is full
        notFullCondition.await();
      } //after the loop we know the queue won't be full again since we're holding the lock
      // add new element at the tail
      items[tail] = item;
      tail = (tail + 1) % capacity; // update tail and make sure we wrap around the array
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

  /**
   * Method to dequeue an item from the queue If the queue is empty, the method will wait until
   * there is an item to dequeue
   *
   * @return The item dequeued from the queue
   */
  public T deq() {
    T result;
    boolean mustWakeEnqueuers = true;
    deqLock.lock();
    try {
      while (size.get() == 0) {
        notEmptyCondition.await();
      }
      result = items[head];
      head = (head + 1) % capacity; // update head and make sure we wrap around the array
      if (size.getAndIncrement() == capacity) { // if queue is full, wake up enqueuers
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

  /**
   * Main method to test the LockBasedBoundedQueue Change numThreads to test with different number
   * of threads
   */
  public static void main(String[] args) throws InterruptedException {
    //TODO: Implement test
  }

}
