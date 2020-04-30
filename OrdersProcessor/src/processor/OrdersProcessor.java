package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class OrdersProcessor {

	public static void main(String[] args) {
		/*
		 * Primary Map shared by all threads Of client iD to item and quantity within
		 * order
		 */
		TreeMap<Integer, TreeMap<String, Integer>> bigMap = new TreeMap<Integer, TreeMap<String, Integer>>();

		/*
		 * Scanner prompting and input collection
		 */
		Scanner scanner = new Scanner(System.in);

		// store items data file name
		System.out.println("Enter item's data file name");
		String fileName = scanner.nextLine();

		// store option for single or multiple
		System.out.println("Enter 'y' for multiple threads, any other character otherwise");
		String multiple = scanner.next();

		// store number of orders
		System.out.println("Enter number of orders to process");
		int orders = Integer.parseInt(scanner.next());

		// store order's base name
		System.out.println("Enter order's base filename");
		String baseName = scanner.next(); // next vs. next line?

		// store results file name
		System.out.println("Enter result's filename");
		String resultsFile = scanner.next();

		// begin timing
		long startTime = System.currentTimeMillis();
		scanner.close();

		/*
		 * Creating map of items and prices Used for reference in writing results
		 */
		HashMap<String, Double> itemsInfo = new HashMap<String, Double>();
		File itemsFile = new File(fileName);
		Scanner itemScanner = null;
		try {
			itemScanner = new Scanner(itemsFile);
		} catch (FileNotFoundException e1) {
			System.out.println("item file not found");
		}

		while (itemScanner.hasNext()) {

			String line = itemScanner.nextLine();
			String item = line.replaceAll("[^a-zA-Z-]", "");
			Double price = Double.parseDouble(line.replaceAll("[^0-9\\.]", ""));
			itemsInfo.put(item, price);
		}
		itemScanner.close();

		/*
		 * Threading
		 */

		if (!(multiple.equals("y"))) {
			// single threading approach just creates new SingleThread object and calls run
			SingleThread single = new SingleThread(bigMap, baseName, orders);
			single.run();
		} else {
			/*
			 * For multithreading create array list of threads for each MultiThread object
			 * run all
			 */
			ArrayList<Thread> threads = new ArrayList<Thread>();

			for (int i = 1; i <= orders; i++) {
				threads.add(new Thread(new MultiThread(bigMap, baseName, i)));
			}
			// start all
			for (Thread thread : threads) {
				thread.start();
			}
			// join all
			// each object has own lock
			try {
				for (Thread thread : threads) {
					thread.join();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("interruption occured");
			}
		}

		/*
		 * making the report
		 */
		TreeMap<String, Integer> summaryMap = new TreeMap<String, Integer>();
		String results = "";
		// for each order id
		for (Integer iD : bigMap.keySet()) {
			Double orderCost = 0.0;
			results = results + "----- Order details for client with Id: " + iD + " -----" + "\n";
			// for each item
			for (String item : bigMap.get(iD).keySet()) {
				int quantity = bigMap.get(iD).get(item);
				// placing into summary map
				if (summaryMap.containsKey(item)) {
					summaryMap.put(item, summaryMap.get(item) + quantity);
				} else {
					summaryMap.put(item, quantity);
				}
				/*
				 * variables to be input into results pulled from relevant maps
				 */
				Double price = itemsInfo.get(item);
				String costPer = NumberFormat.getCurrencyInstance().format(price);
				Double costTot = quantity * price;
				orderCost = orderCost + costTot;
				String costTotStr = NumberFormat.getCurrencyInstance().format(costTot);

				results = results + "Item's name: " + item + ", Cost per item: " + costPer + ", Quantity: " + quantity
						+ ", Cost: " + costTotStr + "\n";
			}
			results = results + "Order Total: " + NumberFormat.getCurrencyInstance().format(orderCost) + "\n";
		}

		// adding summary map to results report
		results = results + "***** Summary of all orders *****" + "\n";
		double grandTotal = 0;
		for (String item : summaryMap.keySet()) {
			Double costPer = itemsInfo.get(item);
			String costPerStr = NumberFormat.getCurrencyInstance().format(costPer);
			int num = summaryMap.get(item);
			Double total = costPer * num;
			grandTotal = grandTotal + total;
			String totalStr = NumberFormat.getCurrencyInstance().format(total);

			results = results + "Summary - Item's name: " + item + ", Cost per item: " + costPerStr + ", Number sold: "
					+ num + ", Item's Total: " + totalStr + "\n";

		}
		// grand total
		results = results + "Summary Grand Total: " + NumberFormat.getCurrencyInstance().format(grandTotal) + "\n";

		/*
		 * Writing results to the file
		 */

		Writer wr;
		try {
			wr = new FileWriter(resultsFile);
			wr.write(results);
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("writer issue");
		}
		// end timing
		long endTime = System.currentTimeMillis();
		System.out.println("Processing time (msec): " + (endTime - startTime));

	}
}
