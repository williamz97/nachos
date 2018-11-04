package nachos.threads;

import java.util.ArrayList;
import java.util.NoSuchElementException;

class Node {
	int key;
	int value;
	Node next;

	// CONSTRUCTOR
	Node(int key, int value) {
		this.key = key;
		this.value = value;
	}
}

enum OperationType {
	INSERT, REMOVE, QUERY
}


class ThreadOperation {
	int k;
	OperationType op;
	int result;
};


public class HashTable {
	ArrayList<Node> bucketList;
	ArrayList<Node> itemList;
	int num_buckets;
	int num_items;
	double loadFactor;

	// CONSTRUCTOR
	HashTable(int n_buckets) {
		bucketList = new ArrayList<>();
		itemList = new ArrayList<>();
		num_buckets = n_buckets;
		num_items = 0;
		loadFactor = 0;
		// INITIALIZE EMPTY SPOT IN EACH BUCKET FOR CHAINING
		for (int i = 0; i < num_buckets; i++) {
			bucketList.add(null);
		}
	}

	// HASH FUNCTION
	int getBucketIndex(int key) {
		int hashCode = Integer.valueOf(key).hashCode();
		int index = hashCode % num_buckets;
		return index;
	}

	void insert(int k, int val) {

		// GET HEAD WITHIN INDEX BUCKET
		int index = getBucketIndex(k);
		Node head = bucketList.get(index);
		// ITERATE THROUGH NODE LIST
		while (head != null) {
			// IF KEY ALREADY ALREADY EXISTS
			if (head.key == k) {
				throw new java.lang.RuntimeException("Key Already Exists");
			}
			// LOOK AT NEXT NODE
			else {
				head = head.next;
			}
		}

		// GET HEAD WITHIN INDEX BUCKET
		head = bucketList.get(index);
		// ADD NEW NODE TO CHAIN WITH CHOSEN VALUE
		Node newNode = new Node(k, val);
		newNode.next = head;
		// INCREMENT ITEM COUNTER
		num_items += 1;
		// UPDATE NODE
		bucketList.set(index, newNode);
		// UPDATE ITEM LIST
		itemList.add(new Node(k, val));
		// UPDATE LOAD FACTOR AND REHASH IF REQURIED
		loadFactor = (double) num_items / num_buckets;
		if (loadFactor >= 0.75) {
			this.reHash();
		}
	}

	int getIndex(int k) {

		// GET HEAD WITHIN INDEX BUCKET
		int index = getBucketIndex(k);
		Node head = bucketList.get(index);

		// ITERATE THROUGH NODE LIST
		while (head != null) {
			//IF KEY IS FOUND RETURN INDEX
			if (head.key == k) {
				return index;
			}
			// LOOK AT NEXT NODE
			head = head.next;
		}
		if (head == null) {
			throw new NoSuchElementException("Key Not Found");
		}
		return -1;
	}

	void remove(int k) {

		// GETE HEAD WITHIN INDEX BUCKET
		int index = getBucketIndex(k);
		Node head = bucketList.get(index);

		// HOLD SPACE
		Node prev = null;

		// ITERATE THROUGH NODE LIST
		while (head != null) {
			// IF HEAD MATCHES , HOLD THE SPOT
			if (head.key == k) {
				break;
			}
			// KEEP LOOKING IN CHAIN
			else {
				prev = head;
				head = head.next;
			}
		}

		// EMPTY BUCKET
		if (head == null) {
			throw new java.lang.RuntimeException("Key Not Found");
		}

		// REMOVE KEY
		if (prev != null) {
			prev.next = head.next;
		} else {
			bucketList.set(index, head.next);
			num_items--;
			loadFactor = (double) num_items / num_buckets;

			// UPDATE ITEM LIST
			for (int i = 0; i < itemList.size(); i++) {
				if (itemList.get(i).key == k) {
					itemList.remove(i);
				}
			}
		}
	}

	int get(int k) {

		// GET HEAD WITHIN INDEX BUCKET
		int index = getBucketIndex(k);
		Node head = bucketList.get(index);

		// GET HEAD VALUE
		while (head != null) {
			if (head.key == k) {
				return head.value;
			}
			head = head.next;
		}
		if (head == null) {
			throw new NoSuchElementException("Key Not Found");
		}
		return -1;
	}

	int getBucketSize(int k) {

		// GET HEAD WITHIN INDEX BUCKET
		int index = k;
		Node head = bucketList.get(index);

		int size = 0;

		while (head != null) {
			size += 1;
			head = head.next;
		}
		return size;
	}

	HashTable reHash() {

		// ADD NEW AND EMPTY BUCKETS
		this.bucketList = new ArrayList();
		this.num_buckets = num_buckets * 2;

		// COPY ITEM LIST
		ArrayList<Node> temp = itemList;

		// RESET TABLE VARIABLES
		itemList = new ArrayList<>(); // RESET TO AVOID DUPLICATES IN ITEM LIST
		num_items = 0;
		loadFactor = 0;

		// INITIALIZE EMPTY BUCKETS
		for (int i = 0; i < num_buckets; i++) {
			bucketList.add(null);
		}

		// POPULATE BUCKETS USING ITEM LIST
		for (int i = 0; i < temp.size(); i++) {
			//System.out.println(itemList.get(i).key + " ---> " + itemList.get(i).value);
			int k = temp.get(i).key;
			int val = temp.get(i).value;
			this.insert(k, val);
		}
		return this;
	}


	void batch(int n_ops, ThreadOperation[] ops) {

		ArrayList<KThread> ThreadList = new ArrayList();   // HOLD THREADS
		ArrayList<Integer> ThreadKeys = new ArrayList();   // HOLD KEYS
		HashTable HTable = this;

		for (int i = 0; i < ops.length; i++) {

			int index = ops[i].k;    // INDEX CORRESPONDING TO EACH PAIR ITERATION OF OPERATION AND THRED
			ThreadKeys.add(index);   // ADD KEY TO WATCH LIST

			boolean isInList = false; // CONDITIONAL LOCK

			for (int j = 0; j < ThreadKeys.size(); j++) { // CHECK IF IN WATCH LIST
				if (ThreadKeys.get(j) == index) {
					isInList = true;
				}
			}

			while (!isInList) {      // FALSE BUSY WAITING TO HOLD THREAD

			}

			ThreadKeys.add(index);  // ADD CURRENT KEY TO WATCH LIST

			// IF STATEMENT TO CHOOSE CORRECT OPERATION

			if (ops[i].op == OperationType.INSERT) {
				final int k = ops[i].k;
				final int val = ops[i].result;
				final int thread_num = i;
				KThread thread = new KThread(new Runnable() {
					public void run() {
						/*
						System.out.println("This Thread is Going to Run: " + OperationType.INSERT);
						System.out.println("Inserting: " + k+":"+val);
						HTable.insert(k,val);
						System.out.println(".Get k Returns: " + HTable.get(k));
						*/
						System.out.println("Thread " + thread_num + "  has completed");
					}
				});
				thread.fork();
				ThreadList.add(thread);
			}

			if (ops[i].op == OperationType.REMOVE) {
				final int k = ops[i].k;
				final int thread_num = i;
				// MAKE THREADS
				KThread thread = new KThread(new Runnable() {
					public void run() {
						/*
						System.out.println("This Thread is Going to Run: " + OperationType.REMOVE);
						System.out.println("Removing k: " + k);
						HTable.remove(k);
						System.out.println(".Get k Returns: " + HTable.get(k));
						*/
						System.out.println("Thread " + thread_num + "  has completed");
					}
				});
				thread.fork();
				ThreadList.add(thread);
			}


			if (ops[i].op == OperationType.QUERY) {
				final int k = ops[i].k;
				final int val = ops[i].result;
				final int thread_num = i;
				// MAKE THREADS
				KThread thread = new KThread(new Runnable() {
					public void run() {
						/*
						System.out.println("This Thread is Going to Run: " + OperationType.QUERY);
						System.out.println("Inserting k: " + k+":"+val);
						HTable.insert(k,val);
						HTable.get(k);
						System.out.println(".Get k Returns: " + HTable.get(k));
						*/
						System.out.println("Thread " + thread_num + "  has completed");
					}
				});
				thread.fork();
				ThreadList.add(thread);
			}

			// REMOVE INDEX FROM WATCH LIST
			for (int j = 0; j < ThreadKeys.size(); j++) {
				if (ThreadKeys.get(j) == index) {
					ThreadKeys.remove(j);
				}
			}

		}
	}

	public static void selfTest() {

		HashTable HTable = new HashTable(18);

		HTable.insert(1, 2);

		HTable.insert(3, 4);

		HTable.insert(5, 6);

		HTable.insert(7, 8);

		HTable.insert(9, 10);

		HTable.insert(11, 12);

		HTable.insert(13, 14);

		HTable.insert(15, 16);

		HTable.insert(17, 18);

		HTable.insert(19, 20);

		HTable.insert(21, 22);

		HTable.insert(23, 24);

		HTable.insert(25, 26);

		HTable.insert(27, 28);

		HTable.insert(29, 30);

		HTable.insert(31, 32);

		HTable.insert(33, 34);

		HTable.insert(35, 36);


		/*
		 System.out.println("Exception Test");

		  System.out.println(" - - - - - - - - - -");
		 System.out.println(".remove() Non Existent Key where Key=300: ");
		 HTable.remove(300); System.out.println();
		 System.out.println(".insert() Duplicate Key where Key=1: ");
		 HTable.insert(1,10); System.out.println();
		 System.out.println(".get() Non Existent Key where Key=100: ");
		 System.out.println(HTable.get(100)); System.out.println();
		 System.out.println(".getIndex() Non Existent Key where Key=200: ");
		 System.out.println(HTable.getIndex(200)); System.out.println();
		*/


		/*
		 System.out.println(".getBucketSize Test");

		 System.out.println(" - - - - - - - - - -");
		 System.out.println("Total Items: " + HTable.num_items);
		 System.out.println(HTable.getBucketSize(0));
		 System.out.println(HTable.getBucketSize(1));
		 System.out.println(HTable.getBucketSize(2));
		 System.out.println(HTable.getBucketSize(3));
		 System.out.println(HTable.getBucketSize(4));
		 System.out.println(HTable.getBucketSize(5));
		 System.out.println(HTable.getBucketSize(6));
		 System.out.println(HTable.getBucketSize(7));
		 System.out.println(HTable.getBucketSize(8));
		 System.out.println(HTable.getBucketSize(9)); System.out.println();
		 System.out.println();
		*/


		/*
		 System.out.println(".reHash Test");

		 System.out.println(" - - - - - - - - - -");
		 System.out.println("Initial Load Factor: " + HTable.loadFactor);
		 System.out.println("Num_Buckets: " + HTable.num_buckets);
		 System.out.println(); System.out.println("Inserted 13:14");
		 HTable.insert(13, 14); System.out.println("Load Factor: " +
		 HTable.loadFactor); System.out.println("Num_Buckets: " +
		 HTable.num_buckets); System.out.println();
		 System.out.println("Inserted 15:16"); HTable.insert(15, 16);
		 System.out.println("Load Factor: " + HTable.loadFactor);
		 System.out.println("Num_Buckets: " + HTable.num_buckets);
		 System.out.println(); System.out.println("Inserted 17:18");
		 HTable.insert(17, 18); System.out.println("Load Factor: " +
		 HTable.loadFactor); System.out.println("Num_Buckets: " +
		 HTable.num_buckets); System.out.println(); System.out.println();
		*/


		/*
		 System.out.println("LoadFactor Test");

		 System.out.println(" - - - - - - - - - -");
		 System.out.println("Num_Items: " + HTable.num_items);
		 System.out.println("Num_Buckets: " + HTable.num_buckets);
		 System.out.println("Load Factor: " + HTable.loadFactor);
		 System.out.println(); System.out.println();
		*/


		/*
		 System.out.println(".Get() Test");

		 System.out.println(" - - - - - - - - - -");
		 System.out.println(".Get(1)"); System.out.println(HTable.get(1));
		 System.out.println(".Get(3)"); System.out.println(HTable.get(3));
		 System.out.println(".Get(5)"); System.out.println(HTable.get(5));
		 System.out.println(".Get(7)"); System.out.println(HTable.get(7));
		 System.out.println(".Get(9)"); System.out.println(HTable.get(9));
		 System.out.println(".Get(11)"); System.out.println(HTable.get(11));
		 System.out.println(); System.out.println();
		*/


		/*
		 System.out.println(".GetIndex() Test");

		 System.out.println(" - - - - - - - - - -");
		 System.out.println(".GetIndex(1)");
		 System.out.println(HTable.getIndex(1));
		 System.out.println(".GetIndex(3)");
		 System.out.println(HTable.getIndex(3));
		 System.out.println(".GetIndex(5)");
		 System.out.println(HTable.getIndex(5));
		 System.out.println(".GetIndex(7)");
		 System.out.println(HTable.getIndex(7));
		 System.out.println(".GetIndex(9)");
		 System.out.println(HTable.getIndex(9));
		 System.out.println(".GetIndex(11)");
		 System.out.println(HTable.getIndex(11)); System.out.println();
		 System.out.println();
		*/


		/*
		 System.out.println(".remove() Test");

		 System.out.println(" - - - - - - - - - -"); HTable.remove(1);
		 System.out.println(".remove(1)");
		 System.out.println(".getIndex(1) = " + HTable.getIndex(1));
		 HTable.remove(3); System.out.println(".remove(3)");
		 System.out.println(".getIndex(3) = " + HTable.getIndex(3));
		 HTable.remove(5); System.out.println(".remove(5)");
		 System.out.println(".getIndex(5) = " + HTable.getIndex(5));
		 HTable.remove(7); System.out.println(".remove(7)");
		 System.out.println(".getIndex(7) = " + HTable.getIndex(7));
		 HTable.remove(9); System.out.println(".remove(9)");
		 System.out.println( ".getIndex(9) = " + HTable.getIndex(9));
		 HTable.remove(11); System.out.println(".remove(11)");
		 System.out.println( ".getIndex(11) = " + HTable.getIndex(11));
		 */


		System.out.println();
		System.out.println(".batch Test");
		System.out.println(" - - - - - - - - - -");


		ThreadOperation op0 = new ThreadOperation();
		op0.op = OperationType.INSERT;
		op0.k = 100;
		op0.result = 101;

		ThreadOperation op1 = new ThreadOperation();
		op1.op = OperationType.REMOVE;
		op1.k = 100;

		ThreadOperation op2 = new ThreadOperation();
		op2.op = OperationType.QUERY;
		op2.k = 1;

		ThreadOperation op3 = new ThreadOperation();
		op3.op = OperationType.REMOVE;
		op3.k = 3;

		ThreadOperation op4 = new ThreadOperation();
		op4.op = OperationType.INSERT;
		op4.k = 102;
		op4.result = 103;

		ThreadOperation op5 = new ThreadOperation();
		op5.op = OperationType.INSERT;
		op5.k = 104;
		op5.result = 105;

		ThreadOperation op6 = new ThreadOperation();
		op6.op = OperationType.REMOVE;
		op6.k = 102;

		ThreadOperation op7= new ThreadOperation();
		op7.op = OperationType.QUERY;
		op7.k = 104;

		ThreadOperation op8 = new ThreadOperation();
		op8.op = OperationType.REMOVE;
		op8.k = 104;

		ThreadOperation op9 = new ThreadOperation();
		op9.op = OperationType.INSERT;
		op9.k = 106;
		op9.result = 107;

		ThreadOperation op10 = new ThreadOperation();
		op10.op = OperationType.INSERT;
		op10.k = 108;
		op10.result = 109;

		ThreadOperation op11 = new ThreadOperation();
		op11.op = OperationType.REMOVE;
		op11.k = 108;

		ThreadOperation op12 = new ThreadOperation();
		op12.op = OperationType.QUERY;
		op12.k = 106;

		ThreadOperation op13 = new ThreadOperation();
		op13.op = OperationType.REMOVE;
		op13.k = 106;

		ThreadOperation op14 = new ThreadOperation();
		op14.op = OperationType.INSERT;
		op14.k = 110;
		op14.result = 111;

		ThreadOperation op15 = new ThreadOperation();
		op15.op = OperationType.INSERT;
		op15.k = 112;
		op15.result = 113;

		ThreadOperation op16 = new ThreadOperation();
		op16.op = OperationType.REMOVE;
		op16.k = 112;


		ThreadOperation op17 = new ThreadOperation();
		op17.op = OperationType.QUERY;
		op17.k = 110;

		ThreadOperation op18 = new ThreadOperation();
		op18.op = OperationType.REMOVE;
		op18.k = 110;


		ThreadOperation[] thread_ops = new ThreadOperation[]{op0, op1, op2, op3, op4, op5, op6, op7, op8, op9, op10, op11, op12, op13, op14, op15 ,op16,op17, op18};
		HTable.batch(18, thread_ops);
	}
}