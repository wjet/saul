/**
 * 
 */
package edu.illinois.cs.cogcomp.saulexamples.data;

import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Vivek Srikumar
 * 
 */
public class SentimentDataReader implements Parser {

	private List<Document> docs;
	private int current;

	public SentimentDataReader(String dir, boolean train) {

		docs = new ArrayList<>();
		try {
			if (train) {
				String positiveFile = dir + File.separator + "positive.review";

				read(positiveFile);

				String negativeFile = dir + File.separator + "negative.review";

				read(negativeFile);
			} else {
				String testFile = dir + File.separator + "unlabeled.review";
				read(testFile);
			}
			Collections.shuffle(docs);
			current = 0;

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @param negativeFile
	 * @param string
	 * @throws java.io.FileNotFoundException
	 */
	private void read(String file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = br.readLine();

		while (line != null) {
			String[] parts = line.split("\\s+");

			String label = parts[parts.length - 1].split(":")[1].trim();

			List<String> words = new ArrayList<>();
			for (int i = 0; i < parts.length - 1; i++) {
				String[] p = parts[i].split(":");

				for (int j = 0; j < Integer.parseInt(p[1]); j++)
					words.add(p[0].trim());
			}

			Collections.shuffle(words);

			docs.add(new Document(words, label));
			line = br.readLine();
		}

		br.close();
	}

	public void close() {
	}

	public Object next() {
		if (current < docs.size())
			return docs.get(current++);
		else
			return null;
	}

	public void reset() {
		current = 0;
	}

}
