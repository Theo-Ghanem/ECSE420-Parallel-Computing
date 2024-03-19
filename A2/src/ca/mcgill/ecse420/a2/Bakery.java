package ca.mcgill.ecse420.a2;

//Based on Chapter 2 slide 108
public class Bakery {

  boolean[] flag;
  int[] label;

  public Bakery(int n) {
    flag = new boolean[n];
    label = new int[n];
    for (int i = 0; i < n; i++) {
      flag[i] = false;
      label[i] = 0;
    }
  }

  public void lock() {
    int i = ThreadID.get();
    flag[i] = true;
    label[i] = max(label) + 1;
    for (int k = 0; k < label.length; k++) {
      while ((k != i) && (flag[k] && ((label[k] < label[i]) || ((label[k] == label[i]) && (k
          < i))))) { //wait till the thread k is at a lower level and the victim is i
        // Busy waiting
      }
    }
  }

  public void unlock() {
    flag[ThreadID.get()] = false;
  }

  private int max(int[] array) {
    int max = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] > max) {
        max = array[i];
      }
    }
    return max;
  }

}


