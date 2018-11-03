package nachos.threads;
import java.util.*;
import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	public void timerInterrupt() {
		long currentTime = Machine.timer().getTime();
		System.out.println("Time: " + currentTime);
		Machine.interrupt().disable();
		Lib.assertTrue(Machine.interrupt().disabled());
		ThreadTimer nextThread;
		while((nextThread = waitQueue.peek()) != null && nextThread.wakeTime() <= Machine.timer().getTime()) {
			waitQueue.poll().thread().ready();
		}
		Machine.interrupt().enable();
		KThread.yield();
	}



	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		//while (wakeTime > Machine.timer().getTime()) {}
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;
		KThread kThread = KThread.currentThread();
		ThreadTimer threadTimer = new ThreadTimer(kThread, wakeTime);
		Machine.interrupt().disable();
		waitQueue.add(threadTimer);
		KThread.sleep();
		Machine.interrupt().enable();
	}
	//Implementing comparable for the Priority Queue 
	private class ThreadTimer implements Comparable<ThreadTimer>{
		private KThread thread;
		private long waketime;
		public ThreadTimer(KThread kThread, long wakeTime) {
			this.thread = kThread;
			this.waketime = wakeTime; 
		}
		public int compareTo(ThreadTimer threadTime) {
			if (this.waketime > threadTime.waketime) {
				return 1;
			}else if (this.waketime < threadTime.waketime) {
				return -1;
			}else 
				return 0;
		}	
		public long wakeTime() {
			return waketime;
		}
		public KThread thread() {
			return thread;
		}
	}
	//makes sure priority queue is in waketime order 
	PriorityQueue<ThreadTimer> waitQueue = new PriorityQueue<ThreadTimer>();
	
	// Add Alarm testing code to the Alarm class
	public static void alarmTest1() {
		int durations[] = {1000, 10*1000, 100*1000};
		long t0, t1;

		for (int d : durations) {
			t0 = Machine.timer().getTime();
			ThreadedKernel.alarm.waitUntil (d);
			t1 = Machine.timer().getTime();
			System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		}
	}

	// Implement more test methods here ...

	// Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
	public static void selfTest() {
		alarmTest1();

		// Invoke your other test methods here ...
	}	
}
