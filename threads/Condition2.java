package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
    
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	
    	Machine.interrupt().disable();  // Disable interrupts
    	
    	conditionLock.release();  // Allow other threads to acquire
    	
    	// Adds currentThread to waitQueue to be accessed later
    	waitQueue.add(KThread.currentThread());
    
    	KThread.sleep(); // Sleep currentThread
    	
    	Machine.interrupt().enable(); // Enable interrupts
    	
    	conditionLock.acquire();  // Acquire lock
    
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    		
    	Machine.interrupt().disable(); // Disable Interrupts
    	
    	// Checks if nextThread in waitQueue is empty, if wake first thread
    	// and remove it from the waitQueue
    	
    	if(!waitQueue.isEmpty()) {
    		waitQueue.removeFirst().ready();
    	}
    	
    	Machine.interrupt().enable(); // Enable Interrupts
    	
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	
    	Machine.interrupt().disable(); // Disable Interrupts
    	
    	// While the waitQueue is not empty wake nextThread wake,
    	// nextThread will update in wake call
    	while(!waitQueue.isEmpty()) {
    		wake();
    	}
    	
    	Machine.interrupt().enable();  // Enable Interrupts
    	
    }
  
    private Lock conditionLock;
    private LinkedList<KThread> waitQueue = new LinkedList<KThread>();
    /**
     * Testing and debugging for Condition2
     */
   
    
    public static void selfTest(){
    	
    	System.out.println("Running selfTest");
    	new InterlockTest();
    	
    	final Lock testLock = new Lock();
    	final Condition2 test = new Condition2(testLock);
 
    	KThread sleepTest = new KThread(new Runnable () {
    		
    		public void run() {
    			testLock.acquire();  // Locks so other test can't run concurrently
    		    System.out.println("----------Testing sleep Method----------");
    		    test.sleep();
    		    System.out.println("----------sleep test passed----------");
    		    testLock.release();  // Release lock for another test to run
    		}
    	});
    	sleepTest.fork(); // Begin execution of sleepTest
    	
    	
    	KThread wakeTest = new KThread(new Runnable () {
    		
    		public void run() {
    			testLock.acquire();  // Locks so other test can't run concurrently
    		    System.out.println("----------Testing wake Method----------");
    		    test.wake();
    		    System.out.println("----------wake test passed----------");
    		    testLock.release();  // Release lock for another test to run
    		}
    	});
    	wakeTest.fork(); // Begin Execution of wakeTest
    
    	
    	KThread wakeAllTest = new KThread(new Runnable () {
    		
    		public void run() {
    			testLock.acquire();  // Locks so other test can't run concurrently
    		    System.out.println("----------Testing wakeAll Method----------");
    		    test.wakeAll();
    		    System.out.println("----------wakeAll test passed----------");
    		    testLock.release();  // Release lock for another test to run
    		}
    	});
    	wakeAllTest.fork();
    	
    } 
    

    // Place Condition2 test code inside of the Condition2 class.

    // Test programs should have exactly the same behavior with the
    // Condition and Condition2 classes.  You can first try a test with
    // Condition, which is already provided for you, and then try it
    // with Condition2, which you are implementing, and compare their
    // behavior.

    // Do not use this test program as your first Condition2 test.
    // First test it with more basic test programs to verify specific
    // functionality.

    public static void cvTest5() {
        final Lock lock = new Lock();
        //final Condition empty = new Condition(lock);
        final Condition2 empty = new Condition2(lock);
        final LinkedList<Integer> list = new LinkedList<>();

        KThread consumer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    while(list.isEmpty()){
                        empty.sleep();
                    }
                    Lib.assertTrue(list.size() == 5, "List should have 5 values.");
                    while(!list.isEmpty()) {
                        // context switch for the fun of it
                        KThread.currentThread().yield();
                        System.out.println("Removed " + list.removeFirst());
                    }
                    lock.release();
                }
            });

        KThread producer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    for (int i = 0; i < 5; i++) {
                        list.add(i);
                        System.out.println("Added " + i);
                        // context switch for the fun of it
                        KThread.currentThread().yield();
                    }
                    empty.wake();
                    lock.release();
                }
            });

        consumer.setName("Consumer");
        producer.setName("Producer");
        consumer.fork();
        producer.fork();

        // We need to wait for the consumer and producer to finish,
        // and the proper way to do so is to join on them.  For this
        // to work, join must be implemented.  If you have not
        // implemented join yet, then comment out the calls to join
        // and instead uncomment the loop with yield; the loop has the
        // same effect, but is a kludgy way to do it.
        consumer.join();
        producer.join();
        //for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
    }
    
    // Place Condition2 testing code in the Condition2 class.

    // Example of the "interlock" pattern where two threads strictly
    // alternate their execution with each other using a condition
    // variable.  (Also see the slide showing this pattern at the end
    // of Lecture 7.)

    private static class InterlockTest {
        private static Lock lock;
        private static Condition2 cv;

        private static class Interlocker implements Runnable {
            public void run () {
                lock.acquire();
                for (int i = 0; i < 10; i++) {
                    System.out.println(KThread.currentThread().getName());
                    cv.wake();   // signal
                    cv.sleep();  // wait
                }
                lock.release();
            }
        }

        public InterlockTest () {
            lock = new Lock();
            cv = new Condition2(lock);

            KThread ping = new KThread(new Interlocker());
            ping.setName("ping");
            KThread pong = new KThread(new Interlocker());
            pong.setName("pong");

            ping.fork();
            pong.fork();

            // We need to wait for ping to finish, and the proper way
            // to do so is to join on ping.  (Note that, when ping is
            // done, pong is sleeping on the condition variable; if we
            // were also to join on pong, we would block forever.)
            // For this to work, join must be implemented.  If you
            // have not implemented join yet, then comment out the
            // call to join and instead uncomment the loop with
            // yields; the loop has the same effect, but is a kludgy
            // way to do it.
            ping.join();
            //for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
        }
    }

    // Invoke Condition2.selfTest() from ThreadedKernel.selfTest()

}
