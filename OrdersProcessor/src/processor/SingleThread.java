package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeMap;

public class SingleThread implements Runnable {

	/*
	 * Single Thread Class Handles Orders Sequentially using one thread
	 */
	String baseName;
	int orders;
	private TreeMap<Integer, TreeMap<String, Integer>> bigMap;

	/*
	 * constructor takes big map, file base name, and amount of orders
	 */
	public SingleThread(TreeMap<Integer, TreeMap<String, Integer>> bigMap, String baseName, int orders) {
		this.baseName = baseName;
		this.orders = orders;
		this.bigMap = bigMap; // Can I do this??
	}

	public void run() {
		// goes through each order sequentially
		for (int i = 1; i <= orders; i++) {
			// create new file to scan
			File orderFile = new File(baseName + i + ".txt");
			Scanner scanner = null;
			try {
				scanner = new Scanner(orderFile);
			} catch (FileNotFoundException e) {
				System.out.println("File not found");
			}

			// create map of items to quantity from order file
			TreeMap<String, Integer> orderMap = new TreeMap<String, Integer>();

			String text = scanner.nextLine();
			// method for extracting digits found on stack exchange
			// used to extract client Id from first line
			String id = text.replaceAll("\\D+", "");
			int clientId = Integer.parseInt(id);

			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();
				// found on stack exchange
				// method to extract item string
				String item = line.replaceAll("[^a-zA-Z-]", "");

				// put item and quantity in map
				if (orderMap.containsKey(item)) {
					orderMap.put(item, orderMap.get(item) + 1);
				} else {
					orderMap.put(item, 1);
				}
			}

			// add to big map and ensure single thread has a lock
			synchronized (bigMap) {
				bigMap.put(clientId, orderMap);
			}

		}

	}
}
