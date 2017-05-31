package org.ost.test.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueThreadTest {

  private static final String STR_Start_Thread = "Thread(%s/%s) is started" + System.lineSeparator();
  private static final String STR_Finish_Thread = "Thread(%s/%s) is finished" + System.lineSeparator();
  private static final String STR_poll_pattern = "Thread(%s/%s) Take %s" + System.lineSeparator();
  private static final String STR_add_pattern = "Thread(%s/%s) Add = %s" + System.lineSeparator();
  
  private static boolean isFinishAll = false;

  public static void main(String... args) {
    System.out.println("start");

    BlockingQueue<String> wrapQueue = new ArrayBlockingQueue<String>(1);

    Runnable testRun = new Runnable() {
      public void run() {
        Thread.currentThread().setName("Getter");
        System.out.printf(STR_Start_Thread, Thread.currentThread().getName(),Thread.currentThread().getId());
        while (!(isFinishAll)) {
          try {
            String element = wrapQueue.take();
            System.out.printf(STR_poll_pattern, Thread.currentThread().getName(),Thread.currentThread().getId(), element);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        System.out.printf(STR_Finish_Thread, Thread.currentThread().getName(),Thread.currentThread().getId());
      }
    };
    Thread t1;
    (t1 = new Thread(testRun)).start();
    Thread t2;
    (t2 = new Thread(testRun)).start();
    Runnable interruptRun = new Runnable() {
      @Override
      public void run() {
          t1.interrupt();
          t2.interrupt();
      }
    };
    
    new Thread(new Runnable() {
      private int sleep = 100;
      public void run() {
        Thread.currentThread().setName("Putter");
        System.out.printf(STR_Start_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        for (int i = 0; i < 10; i++) {
          String sToAdd = "Test" + i;
          System.out.printf(STR_add_pattern, Thread.currentThread().getName(),Thread.currentThread().getId(), sToAdd);
          wrapQueue.add(sToAdd);
          try {
            Thread.sleep(sleep);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        isFinishAll = true;
        while (wrapQueue.size() != 0){
        }
        new Thread(interruptRun).start();
        System.out.printf(STR_Finish_Thread, Thread.currentThread().getName(),Thread.currentThread().getId());
      }
    }).start();
    
    System.out.println("end");
  }
}

