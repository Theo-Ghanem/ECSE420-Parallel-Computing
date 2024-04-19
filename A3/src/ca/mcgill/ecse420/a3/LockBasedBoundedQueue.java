package ca.mcgill.ecse420.a3;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
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
    this.tail = this.head = 0; // head and tail point to the first element
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
        System.out.println("Queue is full, waiting for items to be dequeued");
        notFullCondition.await();
      } //after the loop we know the queue won't be full again since we're holding the lock
      // add new element at the tail
      items[tail] = item;
      tail = (tail + 1) % capacity; // update tail and make sure we wrap around the array
      if (size.getAndIncrement() == 0) { // if queue was empty, wake up dequeuers
//        System.out.println("Queue is no longer empty, waking up dequeuers");
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
        System.out.println("Queue is empty, waiting for items to be enqueued");
        notEmptyCondition.await();
      }
      result = items[head];
      head = (head + 1) % capacity; // update head and make sure we wrap around the array
      if (size.getAndDecrement() == capacity) { // if queue is full, wake up enqueuers
        mustWakeEnqueuers = true;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      deqLock.unlock();
    }
    if (mustWakeEnqueuers) {
//      System.out.println("Queue is no longer full, waking up enqueuers");
      enqLock.lock();
      try {
        notFullCondition.signalAll(); //using signalAll instead of signal() to avoid lost wakeups
      } finally {
        enqLock.unlock();
      }
    }
    return result;
  }

  //make printQueue method atomic:
  public void printQueue() {
    enqLock.lock();
    deqLock.lock();
    try {
      System.out.print("Queue contains: ");
      for (int i = head; i != tail; i = (i + 1) % capacity) {
        System.out.print(items[i] + ", ");
      }
      System.out.println();
    } finally {
      enqLock.unlock();
      deqLock.unlock();
    }
  }

  /**
   * Main method to test the LockBasedBoundedQueue
   */
  public static void main(String[] args) throws InterruptedException {
    testQueue(new LockBasedBoundedQueue<>(3), 10);
  }

  /**
   * Method to test the LockBasedBoundedQueue Was not asked to implement this method but this is
   * just a quick check to see if the queue works
   *
   * @param queue      The queue to test
   * @param numThreads The number of threads to spawn
   * @throws InterruptedException If the executor service is interrupted
   */
  public static void testQueue(LockBasedBoundedQueue<Integer> queue, int numThreads)
      throws InterruptedException {
    final int[] successfulEnqueues = new int[1]; // Track successful enqueues
    final int[] successfulDequeues = new int[1]; // Track successful dequeues

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    // Spawn threads for enqueue and dequeue operations
    for (int i = 0; i < numThreads / 2; i++) {
      executor.submit(() -> {
        int value = new Random().nextInt(100); // Enqueue random integer
        try {
          queue.enq(value);
          successfulEnqueues[0]++;
        } catch (IllegalStateException e) {
          System.err.println("Enqueue failed: Queue is full");
        }
      });
    }

    for (int i = numThreads / 2; i < numThreads; i++) {
      executor.submit(() -> {
        if (queue.deq() != null) {
          successfulDequeues[0]++;
        }
      });
    }

    // Shutdown the executor service after all tasks are submitted
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    // Print the queue size and success counters
    System.out.println("Successful enqueues: " + successfulEnqueues[0]);
    System.out.println("Successful dequeues: " + successfulDequeues[0]);

    // Verify queue size based on success counters
    int expectedSize = successfulEnqueues[0] - successfulDequeues[0];
    if (expectedSize == queue.size.get()) {
      System.out.println("Queue size matches");
    } else {
      System.out.println("Queue size does not match");
    }
  }


}



