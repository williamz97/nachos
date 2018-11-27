package nachos.threads;
import java.util.ArrayList;


public class Bridge {
	int num_cars_on_bridge;
	int num_cars_waiting;
	int curr_dir;
	Lock myLock = new Lock();

	ArrayList<Condition> condArray = new ArrayList<>();
	Condition left_waiting = new Condition(myLock);
	Condition right_waiting = new Condition(myLock);
	ArrayList<Integer> num_waiting = new ArrayList<>();

	Bridge(){
		condArray.add(left_waiting);
		condArray.add(right_waiting);
		num_waiting.add(0);
		num_waiting.add(0);
	}


	void ArriveBridge(int dir) {
		myLock.acquire();
		num_cars_waiting ++;
		while ( num_cars_on_bridge == 3 || curr_dir != dir) {
			condArray.get(dir).sleep();
		}
		num_cars_waiting --;
		num_waiting.set(dir, (num_waiting.get(dir) + 1));
		num_cars_on_bridge ++;
		curr_dir = dir;
		myLock.release();
	}

	void CrossingBridge(int dir){
		myLock.acquire();
		System.out.println(" I'm crossing the bridge in " + dir + " direction");
		myLock.release();
	}

	void ExitBridge(int dir) {
		myLock.acquire();
		num_waiting.set(dir, (num_waiting.get(dir) - 1));
		num_cars_on_bridge --;
		if(num_waiting.get(dir) != 0) {
			condArray.get(dir).wake();
		}
		else{
			if ( num_waiting.get( (dir + 1) % 2)!= 0) {
				condArray.get(dir).wake();
			}
		}
		myLock.release();
	}

	void BridgeCollisionTest() {

		// 	TWO TRAVELING IN ONE DIRECTION AND ONE ATTEMPTING TO CROSS IN THE OPPOSITE DIRECTION

		KThread thread_a = new KThread(new Runnable() {
			public void run() {
				ArriveBridge(0);
			}
		});
		KThread thread_b = new KThread(new Runnable() {
			public void run() {
				ArriveBridge(1);
			}
		});

		KThread thread_d = new KThread(new Runnable() {
			public void run() {
				ArriveBridge(0);
			}
		});

	}

	void BridgeCapacityTest(){

		// THREE TRAVELING IN ONE DIRECTION AND A FOURTH ATTEMPTING TO CROSS THE SAME DIRECTION

		KThread thread_e = new KThread(new Runnable() {
			public void run() {
				ArriveBridge(0);
			}
		});
		KThread thread_f = new KThread(new Runnable() {
			public void run() {
				ArriveBridge(0);
			}
		});
		KThread thread_g = new KThread(new Runnable() {
			public void run() {
				ArriveBridge(0);
			}
		});
		KThread thread_h = new KThread(new Runnable() {
			public void run() {
				ArriveBridge(0);
			}
		});

	}

}
