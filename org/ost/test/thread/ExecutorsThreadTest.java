package org.ost.test.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorsThreadTest {
  private static final String STR_Start_Thread = "Thread(%s/%s) is started" + System.lineSeparator();
  private static final String STR_Finish_Thread = "Thread(%s/%s) is finished" + System.lineSeparator();
  private static final String STR_poll_pattern = "Thread(%s/%s) Take %s" + System.lineSeparator();
  private static final String STR_add_pattern = "Thread(%s/%s) Add = %s" + System.lineSeparator();
  
  private static final String STR_finish = "finish";

  public static void main(String... args) {
    System.out.println("start");
    ExecutorService executor = Executors.newFixedThreadPool(10);
    BlockingQueue<String> wrapQueue = new ArrayBlockingQueue<String>(1);
    BlockingQueue<String> finish = new SynchronousQueue<String>();
    class CallableAdd<T> implements Callable<T>{
      private int sleep = 100;
      @Override
      public T call() throws Exception {
        Thread.currentThread().setName("Putter");
        System.out.printf(STR_Start_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        for (int i = 0; i < 10; i++) {
          String sToAdd = "Test" + i;
          System.out.printf(STR_add_pattern, Thread.currentThread().getName(), Thread.currentThread().getId(), sToAdd);
          wrapQueue.add(sToAdd);
          try {
            Thread.sleep(sleep);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        System.out.printf(STR_Finish_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        while (wrapQueue.size() != 0){
        }
        finish.add(STR_finish);
        return null;
      }
    }
    
    class CallableTake<T> implements Callable<T>{
      @Override
      public T call() throws Exception {
        Thread.currentThread().setName("Getter");
        System.out.printf(STR_Start_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        try {
          while (true) {
            try {
              String element = wrapQueue.take();
              System.out.printf(STR_poll_pattern, Thread.currentThread().getName(), Thread.currentThread().getId(), element);
            } catch (InterruptedException e) {
              e.printStackTrace();
              return null;
            }
          }
        } finally {
          System.out.printf(STR_Finish_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        }
      }
    }
    
    Future<String> futureAdd = executor.submit(new CallableAdd<String>());
    Future<String> futureTake1 = executor.submit(new CallableTake<String>());
    Future<String> futureTake2 = executor.submit(new CallableTake<String>());
    
    class CallableFinish<T> implements Callable<T>{
      @Override
      public T call() throws Exception {
        Thread.currentThread().setName("Finish");
        System.out.printf(STR_Start_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        if(finish.take().endsWith(STR_finish)){
          futureTake1.cancel(true);
          futureTake2.cancel(true);
        }
        System.out.printf(STR_Finish_Thread, Thread.currentThread().getName(), Thread.currentThread().getId());
        return null;
      }
    }
    Future<String> futureFinish = executor.submit(new CallableFinish<String>());
    
    System.out.println("end");
  }
}
