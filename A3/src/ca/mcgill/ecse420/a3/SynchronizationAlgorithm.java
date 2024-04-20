package ca.mcgill.ecse420.a3;

import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

/**
 * SynchronizationAlgorithm class that implements a linked list with add, remove, and contains
 * operations. The class uses fine-grained synchronization to ensure thread safety.
 *
 * @param <T>
 * @author From lecture slides and textbook "Art of Multiprocessor Programming" Chap 9 Page 202-203
 */
public class SynchronizationAlgorithm<T> {

  /**
   * Inner class to represent a node in the linked list A node contains an item, a key, a reference
   * to the next node, and a lock
   */
  public class Node {

    int key; // unique hashcode
    Node next; // points to next node
    T item; // item stored in node
    ReentrantLock lock; // lock for node

    public Node(T item) { // constructor, creates a new lock
      this.item = item;
      if (item == null) // if item is null, set key to 0
      {
        this.key = 0;
      } else // otherwise, set key to hashcode of item (unique identifier
      {
        this.key = item.hashCode();
      }
      lock = new ReentrantLock();
    }

    public void lock() { // tries to acquire lock for node
      lock.lock();
    }

    public void unlock() { // releases lock for node
      lock.unlock();
    }
  }

  Node head; // head of linked list
  Node tail; // tail of linked list

  /**
   * Constructor for SynchronizationAlgorithm
   */
  public SynchronizationAlgorithm() { // constructor
    head = new Node(null); // create head node
    head.key = Integer.MIN_VALUE; // set key to min value
    tail = new Node(null); // create tail node
    tail.key = Integer.MAX_VALUE; // set key to max value
    head.next = tail; // head points to tail
  }

  /**
   * Method to add an item to the list
   *
   * @param item to add
   * @return True if the item was added, False if the item already exists
   * @author From textbook "Art of Multiprocessor Programming" Chapter 9, page 202
   */
  public boolean add(T item) {
    int key = item.hashCode();
    head.lock();
    Node pred = head;
    try {
      Node curr = pred.next;
      curr.lock();
      try {
        while (curr.key < key) {
          pred.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock();
        }
        if (curr.key == key) {
          return false;
        }
        Node newNode = new Node(item);
        newNode.next = curr;
        pred.next = newNode;
        return true;
      } finally {
        curr.unlock();
      }
    } finally {
      pred.unlock();
    }
  }

  /**
   * Method to remove an item from the list
   *
   * @param item to remove
   * @return True if the item was removed, False if the item does not exist
   * @author From textbook "Art of Multiprocessor Programming" Chapter 9, page 203
   */
  public boolean remove(T item) {
    Node pred = null, curr = null;
    int key = item.hashCode();
    head.lock();
    try {
      pred = head;
      curr = pred.next;
      curr.lock();
      try {
        while (curr.key < key) {
          pred.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock();
        }
        if (curr.key == key) {
          pred.next = curr.next;
          return true;
        }
        return false;
      } finally {
        curr.unlock();
      }
    } finally {
      pred.unlock();
    }
  }

  /**
   * Method to check if the list contains an item
   *
   * @param item to check if contained
   * @return True if the item is contained, False if the item does not exist
   */
  public boolean contains(T item) {
    int key = item.hashCode();
    head.lock();
    Node pred = null;
    Node current = null;
    try {
      pred = head;
      current = pred.next;
      current.lock();
      try {
        while (current.key < key) {
          pred.unlock();
          pred = current;
          current = current.next;
          current.lock();
        }
        return current.key == key;
      } finally {
        current.unlock();
      }
    } finally {
      pred.unlock();
    }
  }

  /**
   * Helper Method to print out the list
   */
  public void printList() {
    Node current = head.next;
    System.out.print("List contains: ");
    while (current != tail) {
      System.out.print(current.item + ", ");
      current = current.next;
    }
    System.out.println();
  }

  /**
   * Helper method to get the size of the list
   *
   * @return size of the list
   */
  public int size() {
    int count = 0;
    Node current = head.next;
    while (current != tail) {
      count++;
      current = current.next;
    }
    return count;
  }

  /**
   * Main method to test the SynchronizationAlgorithm Change numThreads to test with different
   * number of threads
   *
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException {
    int numThreads = 10; // Number of threads

    final SynchronizationAlgorithm<Integer> testList = new SynchronizationAlgorithm<>();
    final int[] successfulAdds = new int[1]; // Use an array to hold the counter
    final int[] successfulRemoves = new int[1];

    // Create and start threads
    Thread[] threads = new Thread[numThreads];
    for (int i = 0; i < numThreads; i++) {
      threads[i] = new Thread(() -> {
        Random random = new Random(); // Random number generator
        for (int j = 0; j < 100; j++) { // Perform 100 operations per thread
          // Randomly add, remove, or check if an element is contained in the list
          int operation = random.nextInt(3); // 0: Add, 1: Remove, 2: Contains
          int value = random.nextInt(100); // Generate random value

          if (operation == 0) { // Add operation
            if (testList.add(value)) {
              System.out.println(
                  "Thread " + ThreadID.get() + " is adding " + value);
              successfulAdds[0]++; // Increment counter within the array
            }
          } else if (operation == 1) { // Remove operation
            if (testList.remove(value)) {
              System.out.println(
                  "Thread " + ThreadID.get() + " is removing " + value);
              successfulRemoves[0]++; // Increment counter within the array
            }
          } else {
            // perform contains operation (doesn't affect expected size)
            Boolean isContained = testList.contains(value);
            System.out.println(
                "Thread " + ThreadID.get() + " is checking if " + value
                    + " is contained: " + isContained);
          }

          try {
            Thread.sleep(random.nextInt(10)); // Introduce random delays for concurrency
          } catch (InterruptedException e) {
            System.err.println("Thread interrupted during sleep!");
          }
        }
      });
      threads[i].start(); // Start the thread
    }

    // Wait for all threads to finish
    for (Thread thread : threads) {
      thread.join();
    }

    int expectedSize =
        successfulAdds[0] - successfulRemoves[0]; // Calculate the expected size of the list

    System.out.println("Test completed!");
    testList.printList();

    if (testList.size() != expectedSize) {
      System.err.println("Error: Inconsistent list size!");
    }
  }
}
