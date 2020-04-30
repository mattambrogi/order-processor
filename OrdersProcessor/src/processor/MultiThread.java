package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeMap;

public class MultiThread implements Runnable {

	/*
	 * MultiThread class handles multithreading by creating a thread for each file
	 */
	private TreeMap<Integer, TreeMap<String, Integer>> bigMap;
	String baseName;
	int order;

	// constructor takes bipMap, order base name, and the individual order number
	public MultiThread(TreeMap<Integer, TreeMap<String, Integer>> bigMap, String baseName, int order) {
		this.bigMap = bigMap;
		this.baseName = baseName;
		this.order = order;
	}

	public void run() {

		// scan this order file
		File orderFile = new File(baseName + order + ".txt");
		Scanner scanner = null;
		try {
			scanner = new Scanner(orderFile);
		} catch (FileNotFoundException e) {
			System.out.println("invalid file");
		}

		// create map of items to quantity from order
		TreeMap<String, Integer> orderMap = new TreeMap<String, Integer>();

		String text = scanner.nextLine();
		// method for extracting digits found on stack exchange
		// used to extract client Id from first line
		String id = text.replaceAll("\\D+", "");
		int clientId = Integer.parseInt(id);
		System.out.println("Reading order for client with id: " + clientId);
		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();
			// found on stack exchange
			String item = line.replaceAll("[^a-zA-Z-]", "");

			// put item and quantity in map
			if (orderMap.containsKey(item)) {
				orderMap.put(item, orderMap.get(item) + 1);
			} else {
				orderMap.put(item, 1);
			}
		}

		synchronized (bigMap) {
			bigMap.put(clientId, orderMap);
		}

	}

}
