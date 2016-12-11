/*
*Name: Sit Soon Weng
*SID: 2228159S
*Assignemnt: MSA4_Coursework_2016
*
*/


import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class TagMatrix {
	private HashMap<String, ArrayList<String>> container = new HashMap<String, ArrayList<String>>();
	private HashSet<String> header = new HashSet<String>();

	public void reader() {
		String file = "photos_tags.csv";
		BufferedReader br = null;
		String line = "";
		String seperator = ",";

		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				ArrayList<String> list = new ArrayList<String>();
				String[] temp = line.split(seperator);
				// adding id and the list of tags related to the id
				if (container.isEmpty()) {
					list.add(temp[1]);
					container.put(temp[0], list);
				} else if (container.containsKey(temp[0])) {
					container.get(temp[0]).add(temp[1]);
				} else {
					list.add(temp[1]);
					container.put(temp[0], list);
				}
				// uses hashset to create a unique header
				header.add(temp[1]);
			}

		} catch (Exception e) {
			System.out.println("Errors found at reader: ");
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public HashMap<String, HashMap<String, Integer>> coMatrix(HashSet<String> header,
			HashMap<String, ArrayList<String>> container) {

		HashMap<String, HashMap<String, Integer>> OuterMap = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> InnerMap;
		ArrayList<String> AL = new ArrayList<String>();

		// This is to create a iterator whereby it will add the unique tags as
		// the header to the outerMap
		for (String headerA : header) {
			InnerMap = new HashMap<String, Integer>();
			for (String headerB : header) {
				InnerMap.put(headerB, 0);
			}
			OuterMap.put(headerA, InnerMap);
		}

		// This loop is use to compare all the co-related tags in the photo and
		// adding a value to the popularity
		for (String photosID : container.keySet()) {
			AL = container.get(photosID);
			for (int i = 0; i < AL.size(); i++) {
				String check = AL.get(i);
				for (int j = 0; j < AL.size(); j++) {
					String check2 = AL.get(j);
					if (check.equals(check2)) {
						OuterMap.get(check).put(check2, OuterMap.get(check).get(check2) - 1);
					} else {
						OuterMap.get(check).put(check2, OuterMap.get(check).get(check2) + 1);
					}

				}
			}
		}
		// Using this loop it will change all the key example a-a =-2 to a-a =
		// 0. By looping from the outer HashMap to inner HashMap
		for (HashMap.Entry<String, HashMap<String, Integer>> correctorLoop : OuterMap.entrySet()) {
			HashMap<String, Integer> hm = correctorLoop.getValue();
			for (HashMap.Entry<String, Integer> corrector : hm.entrySet()) {
				if (corrector.getValue() < 0) {
					String Key = corrector.getKey();
					hm.put(Key, 0);
				}
			}

		}
		return OuterMap;

	}

	public void writer(HashMap<String, HashMap<String, Integer>> matrix, HashSet<String> header) {

		// create filewriter to write to a file name "co-occurenceMatrix.csv"
		String filename = "co-occurenceMatrix.csv";
		FileWriter writer = null;

		try {
			writer = new FileWriter(filename);
			for (String ts : header) {
				writer.append("," + ts);
			}
			// leave a blank first so that matrices can be plotted correctly
			writer.append("\n");
			// loop through the hashset and write the values from the hashmap
			// into the CSV file
			for (String t : header) {
				HashMap<String, Integer> tempFile = matrix.get(t);
				writer.append(t);
				// if the value is null, change it 0
				for (String s : header) {
					if (tempFile.containsKey(s)) {
						writer.append("," + tempFile.get(s));
					} else {
						writer.append(",0");
					}
				}
				writer.append("\n");
			}
		} catch (Exception e) {
			System.out.println("Error in writing file.");
		} finally {
			try {// flush and close the writer
				writer.flush();
				writer.close();
			} catch (Exception e) {
				System.out.println("Error in closing writer");
			}
		}
	}

	// This method sort the HashMap by using Comparable collection from Java
	// itself, and return the HashMap of the sorted values.
	public <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> unsortMap) {

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			public int compare(Map.Entry<K, V> o2, Map.Entry<K, V> o1) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		HashMap<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;

	}

	public void calculateIDF(HashMap<String, Integer> innerMap, HashMap<String, Integer> tag, String text) {
		// create a replacement hashmap to store the updated value after
		// calculation from the formula
		HashMap<String, Double> replacement = new HashMap<String, Double>();
		// by implementing a counter, this allow the loop to break at 5.
		// Allowing top 5 to be printed.
		int counter = 1;
		for (HashMap.Entry<String, Integer> entry : innerMap.entrySet()) {
			double idf = Math.log(10000 / tag.get(entry.getKey()));
			double score = entry.getValue();
			replacement.put(entry.getKey(), score * idf);

		}
		// sort the HashMap to display the top 5.
		HashMap<String, Double> sorted = sortByValue(replacement);
		System.out.println("-----------------------------------------");
		System.out.println("Top 5 IDF for " + text + "\r\n");
		// loop to print out the top 5 IDF values.
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		for (HashMap.Entry<String, Double> entry : sorted.entrySet()) {
			System.out.println(counter + ": " + entry.getKey() + " " + df.format(entry.getValue()) + "\r\n");
			counter++;
			if (counter > 5) {
				break;
			}
		}
	}

	public HashMap<String, Integer> readPhotoTag() {
		String File = "tags.csv";
		BufferedReader br = null;
		String line = "";
		String seperator = ",";

		// read the tags from the tags.csv and store in a new hashmap
		HashMap<String, Integer> tags = new HashMap<String, Integer>();
		try {
			br = new BufferedReader(new FileReader(File));
			while ((line = br.readLine()) != null) {
				String temp[] = line.split(seperator);
				tags.put(temp[0], Integer.parseInt(temp[1]));
			}

		} catch (Exception e) {
			System.out.println("Errors found at reader: ");
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return tags;
	}

	public static void main(String[] args) {
		int counter = 1;
		TagMatrix tm = new TagMatrix();
		HashMap<String, HashMap<String, Integer>> matrix = new HashMap<String, HashMap<String, Integer>>();

		tm.reader();
		matrix = tm.coMatrix(tm.header, tm.container);
		tm.writer(matrix, tm.header);
		HashMap<String, Integer> tagsWoIDFWater = tm.sortByValue(matrix.get("water"));
		HashMap<String, Integer> tagsWoIDFPeople = tm.sortByValue(matrix.get("people"));
		HashMap<String, Integer> tagsWoIDFLondon = tm.sortByValue(matrix.get("london"));

		System.out.println("================ Task 2 =================");
		System.out.println("Top 5 without IDF: Water\r\n");
		for (HashMap.Entry<String, Integer> entry : tagsWoIDFWater.entrySet()) {
			System.out.println(counter + ": " + entry.getKey() + " " + entry.getValue() + "\r\n");
			counter++;
			if (counter > 5) {
				counter = 1;
				break;
			}
		}
		System.out.println("-----------------------------------------");
		System.out.println("Top 5 without IDF: People \r\n");
		for (HashMap.Entry<String, Integer> entry : tagsWoIDFPeople.entrySet()) {
			System.out.println(counter + ": " + entry.getKey() + " " + entry.getValue() + "\r\n");
			counter++;
			if (counter > 5) {
				counter = 1;
				break;
			}
		}
		System.out.println("-----------------------------------------");
		System.out.println("Top 5 without IDF: London \r\n");

		for (HashMap.Entry<String, Integer> entry : tagsWoIDFLondon.entrySet()) {
			System.out.println(counter + ": " + entry.getKey() + " " + entry.getValue() + "\r\n");
			counter++;
			if (counter > 5) {
				counter = 1;
				break;
			}
		}

		System.out.println("============== Task 3 ==============");

		tm.calculateIDF(matrix.get("water"), tm.readPhotoTag(), "water");
		tm.calculateIDF(matrix.get("people"), tm.readPhotoTag(), "people");
		tm.calculateIDF(matrix.get("london"), tm.readPhotoTag(), "london");

	}
}
#   s o o n w e n g  
 