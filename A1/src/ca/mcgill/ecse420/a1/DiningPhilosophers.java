package ca.mcgill.ecse420.a1;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
    private static int NUMBER_PHILOSOPHERS = 5;
    private static final Philosopher[] philosophers = new Philosopher[NUMBER_PHILOSOPHERS];
    private static final Chopstick[] chopsticks = new Chopstick[NUMBER_PHILOSOPHERS];

    public static void main(String[] args) {
        try {
            // ask user which type of scenario they would like to run
            System.out.println("Which solution would you like to run?");
            System.out.println("\t1. Deadlock and Starvation Prone");
            System.out.println("\t2. Starvation Prone");
            System.out.println("\t3. Deadlock and Starvation Safe");

            // get user input from terminal
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter solution number: ");
            int solution = scanner.nextInt();

            // ask the user for the number of philosophers
            System.out.print("Enter the number of philosophers: ");
            NUMBER_PHILOSOPHERS = scanner.nextInt();

            // ask user if they would like to force scenarios
            System.out.println("Would you like to force a specific scenario?");
            System.out.println("\t1. No specific scenario");
            System.out.println("\t2. Force deadlock");
            System.out.println("\t3. Force starvation");
            System.out.println("\t4. Force deadlock and starvation");

            // get user input from terminal
            System.out.print("Enter scenario number: ");
            int forceScenario = scanner.nextInt();
            scanner.close();

            // set default parameters
            int eatBoundTime = 1000;
            int[] eatBoundTimeArray = new int[NUMBER_PHILOSOPHERS];
            int thinkBoundTime = 1000;
            int[] thinkBoundTimeArray = new int[NUMBER_PHILOSOPHERS];
            int pickUpDelay = 0;
            int[] pickUpDelayArray = new int[NUMBER_PHILOSOPHERS];

            // set parameters based on user selected scenario
            switch (forceScenario) {
                case 1:
                    for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
                        eatBoundTimeArray[i] = eatBoundTime;
                        thinkBoundTimeArray[i] = thinkBoundTime;
                        pickUpDelayArray[i] = pickUpDelay;
                    }
                    break;
                case 2:
                    // add delay to pick up to allow for collisions
                    pickUpDelay = 1500;
                    for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
                        eatBoundTimeArray[i] = eatBoundTime;
                        thinkBoundTimeArray[i] = thinkBoundTime;
                        pickUpDelayArray[i] = pickUpDelay;
                    }
                    break;
                case 3:
                    // make one philosopher eat and think for a longer time
                    for (int i = 0; i < NUMBER_PHILOSOPHERS - 1; i++) {
                        eatBoundTimeArray[i] = eatBoundTime;
                        thinkBoundTimeArray[i] = thinkBoundTime;
                        pickUpDelayArray[i] = pickUpDelay;
                    }
                    eatBoundTimeArray[NUMBER_PHILOSOPHERS - 1] = 5000;
                    thinkBoundTimeArray[NUMBER_PHILOSOPHERS - 1] = 5000;
                    pickUpDelayArray[NUMBER_PHILOSOPHERS - 1] = 5000;

                    break;
                case 4:
                    // make one philosopher eat and think for a longer time and add delay to pick up to allow for collisions
                    pickUpDelay = 1500;
                    for (int i = 0; i < NUMBER_PHILOSOPHERS - 1; i++) {
                        eatBoundTimeArray[i] = eatBoundTime;
                        thinkBoundTimeArray[i] = thinkBoundTime;
                        pickUpDelayArray[i] = pickUpDelay;
                    }
                    eatBoundTimeArray[NUMBER_PHILOSOPHERS - 1] = 5000;
                    thinkBoundTimeArray[NUMBER_PHILOSOPHERS - 1] = 5000;
                    pickUpDelayArray[NUMBER_PHILOSOPHERS - 1] = 5 * pickUpDelay;
                    break;
                default:
                    System.out.println("Invalid input");
                    return;
            }

            // run the selected solution
            switch (solution) {
                case 1:
                    runPhilosophersDeadlockStarvation(eatBoundTimeArray, thinkBoundTimeArray, pickUpDelayArray);
                    break;
                case 2:
                    runPhilosophersStarvation(eatBoundTimeArray, thinkBoundTimeArray, pickUpDelayArray);
                    break;
                case 3:
                    runPhilosophers(eatBoundTimeArray, thinkBoundTimeArray, pickUpDelayArray);
                    break;
                default:
                    System.out.println("Invalid input");
                    return;
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input");
            return;
        }
    }

    public static void runPhilosophersDeadlockStarvation(int[] eatBoundTime, int[] thinkBoundTime, int[] pickUpDelay) {
        System.out.println("Running deadlock and starvation scenario...\n");
        // check if arrays are of same length as number of philosophers
        if (validateArrays(eatBoundTime, thinkBoundTime, pickUpDelay)) return;
        // init chopsticks
        for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
            chopsticks[i] = new ChopstickUnfair(i);
        }
        // Create philosophers (threads)
        for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
            philosophers[i] = new PhilosopherDeadlockStarvation(i, eatBoundTime[i], thinkBoundTime[i], pickUpDelay[i]);
            new Thread(philosophers[i]).start();
        }
    }

    public static void runPhilosophersStarvation(int[] eatBoundTime, int[] thinkBoundTime, int[] pickUpDelay) {
        System.out.println("Running starvation scenario...\n");
        // check if arrays are of same length as number of philosophers
        if (validateArrays(eatBoundTime, thinkBoundTime, pickUpDelay)) return;
        // init chopsticks
        for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
            chopsticks[i] = new ChopstickUnfair(i);
        }
        // Create philosophers (threads)
        for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
            philosophers[i] = new PhilosopherStarvation(i, eatBoundTime[i], thinkBoundTime[i], pickUpDelay[i]);
            new Thread(philosophers[i]).start();
        }
    }

    public static void runPhilosophers(int[] eatBoundTime, int[] thinkBoundTime, int[] pickUpDelay) {
        System.out.println("Running no deadlock or starvation scenario...\n");
        // check if arrays are of same length as number of philosophers
        if (validateArrays(eatBoundTime, thinkBoundTime, pickUpDelay)) return;
        // init chopsticks
        for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
            chopsticks[i] = new ChopstickFair(i);
        }
        // Create philosophers (threads)
        for (int i = 0; i < NUMBER_PHILOSOPHERS; i++) {
            philosophers[i] = new PhilosopherNoDeadlockStarvation(i, eatBoundTime[i], thinkBoundTime[i], pickUpDelay[i]);
            new Thread(philosophers[i]).start();
        }

    }

    private static boolean validateArrays(int[] eatBoundTime, int[] thinkBoundTime, int[] pickUpDelay) {
        if (eatBoundTime.length != NUMBER_PHILOSOPHERS) {
            System.out.println("Invalid input");
            return true;
        }
        if (thinkBoundTime.length != NUMBER_PHILOSOPHERS) {
            System.out.println("Invalid input");
            return true;
        }
        if (pickUpDelay.length != NUMBER_PHILOSOPHERS) {
            System.out.println("Invalid input");
            return true;
        }
        return false;
    }

    public interface Philosopher extends Runnable {
        @Override
        void run();

        int id();

        int eatBoundTime();

        int thinkBoundTime();

        int pickUpDelay();

        default long eat() {
            long currentTime = System.currentTimeMillis();
            try {
                System.out.println("Philosopher " + id() + " is eating");
                Thread.sleep(((new Random()).nextInt(this.eatBoundTime())));
            } catch (InterruptedException ignored) {
            }
            // return current time for logging
            return currentTime;
        }

        default long think() {
            try {
                Thread.sleep(((new Random()).nextInt(this.thinkBoundTime())));
            } catch (InterruptedException ignored) {
            }
            // return current time for logging
            return System.currentTimeMillis();
        }

        default long delayPickUp() {
            if (this.pickUpDelay() > 0) {
                try {
                    long delay = (new Random()).nextInt(this.pickUpDelay());
                    Thread.sleep(delay);
                    return delay;
                } catch (InterruptedException ignored) {
                }
            }
            return 0;
        }
    }

    public record PhilosopherDeadlockStarvation(int id, int eatBoundTime, int thinkBoundTime,
                                                int pickUpDelay) implements Philosopher {

        @Override
        public void run() {
            // define left and right chopstick
            Chopstick leftChopstick = chopsticks[id];
            Chopstick rightChopstick = chopsticks[(id + 1) % NUMBER_PHILOSOPHERS];
            ArrayList<Long> waitTimes = new ArrayList<>();

            // add shutdown hook to log wait times
            Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Philosopher " + id + " wait times: " + waitTimes)));

            while (true) {
                long startWait = think();

                // randomly pick up chopsticks
                leftChopstick.pickUp(id);
                long delay = delayPickUp();
                rightChopstick.pickUp(id);

                long endWait = eat();
                System.out.println("Philosopher " + id + " waited " + (endWait - startWait - delay) + "ms");

                // put down left and right chopstick
                leftChopstick.putDown(id);
                rightChopstick.putDown(id);

                // log the time spent waiting
                waitTimes.add(endWait - startWait - delay);

            }

        }
    }

    public record PhilosopherStarvation(int id, int eatBoundTime, int thinkBoundTime,
                                        int pickUpDelay) implements Philosopher {

        @Override
        public void run() {
            // define left and right chopstick
            Chopstick leftChopstick = chopsticks[id];
            Chopstick rightChopstick = chopsticks[(id + 1) % NUMBER_PHILOSOPHERS];
            ArrayList<Long> waitTimes = new ArrayList<>();

            // add shutdown hook to log wait times
            Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Philosopher " + id + " wait times: " + waitTimes)));

            while (true) {
                long delay = 0;
                long endWait;
                long startWait = endWait = think();

                if (id % 2 == 0) {
                    leftChopstick.pickUp(id);
                    delay = delayPickUp();
                    rightChopstick.pickUp(id);
                } else {
                    rightChopstick.pickUp(id);
                    delay = delayPickUp();
                    leftChopstick.pickUp(id);
                }

                endWait = eat();
                System.out.println("Philosopher " + id + " waited " + (endWait - startWait - delay) + "ms");
                leftChopstick.putDown(id);
                rightChopstick.putDown(id);

                // log the time spent waiting
                waitTimes.add(endWait - startWait - delay);
            }

        }
    }

    public record PhilosopherNoDeadlockStarvation(int id, int eatBoundTime, int thinkBoundTime,
                                                  int pickUpDelay) implements Philosopher {

        @Override
        public void run() {
            // define left and right chopstick
            Chopstick leftChopstick = chopsticks[id];
            Chopstick rightChopstick = chopsticks[(id + 1) % NUMBER_PHILOSOPHERS];
            ArrayList<Long> waitTimes = new ArrayList<>();

            // add shutdown hook to log wait times
            Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Philosopher " + id + " wait times: " + waitTimes)));

            while (true) {
                long delay = 0;
                long endWait;
                long startWait = endWait = think();

                if (id % 2 == 0) {
                    leftChopstick.pickUp(id);
                    delay = delayPickUp();
                    rightChopstick.pickUp(id);
                } else {
                    rightChopstick.pickUp(id);
                    delay = delayPickUp();
                    leftChopstick.pickUp(id);
                }

                endWait = eat();
                System.out.println("Philosopher " + id + " waited " + (endWait - startWait - delay) + "ms");
                leftChopstick.putDown(id);
                rightChopstick.putDown(id);

                // log the time spent waiting
                waitTimes.add(endWait - startWait - delay);
            }
        }
    }

    public interface Chopstick {
        boolean pickUp(int philosopherId);

        void putDown(int philosopherId);
    }

    public static class ChopstickUnfair implements Chopstick {
        private boolean inUse = false;
        private final int id;

        public ChopstickUnfair(int id) {
            this.id = id;
        }

        public synchronized boolean pickUp(int philosopherId) {
            while (inUse) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("ERROR: Philosopher " + philosopherId + " was interrupted while waiting to pick up chopstick " + this.id);
                }
            }
            System.out.println("Philosopher " + philosopherId + " is picking up chopstick " + this.id);
            return inUse = true;
        }

        public synchronized void putDown(int philosopherId) {
            System.out.println("Philosopher " + philosopherId + " is putting down chopstick " + this.id);
            inUse = false;
            notify();
        }
    }

    public static class ChopstickFair implements Chopstick {
        private boolean inUse = false;
        private final ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(1, true);
        private final Lock lock = new ReentrantLock(true);
        private final int id;

        public ChopstickFair(int id) {
            this.id = id;
        }

        public boolean pickUp(int philosopherId) {
            lock.lock();
            System.out.println("Philosopher " + philosopherId + " is picking up chopstick " + this.id);
            return true;
        }

        public void putDown(int philosopherId) {
            lock.unlock();
            System.out.println("Philosopher " + philosopherId + " is putting down chopstick " + this.id);
        }
    }
}
