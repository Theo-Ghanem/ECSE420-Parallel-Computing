package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock-free bounded queue implementation inspired from "The Art of Multiprocessor Programming" Chap
 * 10 page 230-231 but adapted to make it bounded
 *
 * @param <T> Type of elements in the queue
 */
public class LockFreeBoundedQueue<T> {

  // Note these are all made final to prevent accidental re-assignment by any other thread
  // And they're private to prevent other classes from accessing them
  private final T[] items;
  private final int capacity;
  private final AtomicInteger head; // points to the first element (inclusive)
  private final AtomicInteger tail; // points to the slot after the last element (exclusive)

  /**
   * Constructor for the LockFreeBoundedQueue
   *
   * @param capacity The capacity of the bounded queue
   */
  public LockFreeBoundedQueue(int capacity) {
    this.capacity = capacity;
    this.items = (T[]) new Object[capacity];
    // using AtomicInteger to keep track of head and tail to make it atomic since must be lock free
    this.head = new AtomicInteger(0);
    this.tail = new AtomicInteger(0);
  }

  /**
   * Method to enqueue an item into the queue
   *
   * @param item The item to enqueue
   * @return true if the item was enqueued, false if the queue is full
   */
  public boolean enqueue(T item) {
    int expectedTail, newTail; // variables to keep track of the tail
    do {
      expectedTail = tail.get(); // get the current tail (atomically)
      newTail =
          (expectedTail + 1) % capacity; // calculate the new tail and make sure we wrap around
      // Check if full
      if (newTail == head.get()) {
        return false; // Queue is full, can't enqueue item
      }
    } while (!tail.compareAndSet(expectedTail,
        newTail)); // If the tail is trailing behind, keep updating it
    items[expectedTail] = item; // Add the item to the queue
    return true;
  }

  /**
   * Method to dequeue an item from the queue
   *
   * @return The item dequeued from the queue, or null if the queue is empty
   */
  public T dequeue() {
    int expectedHead, newHead; // variables to keep track of the head
    do {
      expectedHead = head.get(); // get the current head (atomically)
      newHead =
          (expectedHead + 1) % capacity; // calculate the new head and make sure we wrap around
      // Check if empty
      if (expectedHead == tail.get()) { // if head and tail are the same, the queue is empty
        return null; // Queue is empty we can't dequeue anything
      }
    } while (!head.compareAndSet(expectedHead,
        newHead)); // If the head is trailing behind, keep updating it
    return items[expectedHead]; // Return the dequeued item at the head
  }

  public static void main(String[] args) {

  }
}

