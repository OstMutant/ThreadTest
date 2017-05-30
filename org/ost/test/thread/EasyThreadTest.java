package org.ost.test.thread;

import java.util.PriorityQueue;
import java.util.Queue;

public class EasyThreadTest {

  private static final String STR_Start_Thread = "Thread(%s/%s) is started" + System.lineSeparator();
  private static final String STR_Finish_Thread = "Thread(%s/%s) is finished" + System.lineSeparator();
  private static final String STR_poll_pattern = "Thread(%s/%s) Poll %s" + System.lineSeparator();
  private static final String STR_add_pattern = "Thread(%s/%s) Add = %s" + System.lineSeparator();
  
  private static boolean isFinishAll = false;

  public static void main(String... args) {
    System.out.println("start");

    class WrapQueue {
      
      private Queue<String> queue = new PriorityQueue<String>();

      public void add(String val) {
        synchronized (queue) {
          queue.add(val);
        }
      }

      public String poll() {
        synchronized (queue) {
          return queue.poll();
        }
      }
      
      public int size() {
        return queue.size();
      }
    }

    WrapQueue wrapQueue = new WrapQueue();
    Object monitor = new Object();

    Runnable testRun = new Runnable() {
      public void run() {
        Thread.currentThread().setName("Getter");
        System.out.printf(STR_Start_Thread, Thread.currentThread().getName(),Thread.currentThread().getId());
        while (!(isFinishAll && wrapQueue.size() == 0)) {
          String element = wrapQueue.poll();
          System.out.printf(STR_poll_pattern, Thread.currentThread().getName(),Thread.currentThread().getId(), element);
          synchronized (monitor) {
            try {
              if(!isFinishAll){
                monitor.wait();
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
        System.out.printf(STR_Finish_Thread, Thread.currentThread().getName(),Thread.currentThread().getId());
      }
    };

    new Thread(new Runnable() {
      private int sleep = 0;
      public void run() {
        Thread.currentThread().setName("Putter");
        System.out.printf(STR_Start_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        for (int i = 0; i < 10; i++) {
          String sToAdd = "Test" + i;
          System.out.printf(STR_add_pattern, Thread.currentThread().getName(),Thread.currentThread().getId(), sToAdd);
          wrapQueue.add(sToAdd);
          synchronized (monitor) {
            monitor.notify();
          }
          try {
            Thread.sleep(sleep);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        synchronized (monitor) {
          isFinishAll = true;
          monitor.notifyAll();
        }
        System.out.printf(STR_Finish_Thread, Thread.currentThread().getName(),Thread.currentThread().getId());
      }
    }).start();
    new Thread(testRun).start();
    new Thread(testRun).start();
    
    System.out.println("end");
  }
}
