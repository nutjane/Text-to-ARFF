import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

/**
 * Nut	  		Janekitiworapong	5688043		1	
 * Rata	  		Kittipol			5688076		1
 * Shotirose 	Poramesanaporn		5688112		1	
 * Kamon	  	Tuanghirunvimon 	5688172		1	
 */

public class TextToArffConverter {
	public static final String DELIM = "-";
	
	public static final HashSet<String> stopwords = new HashSet<String>();
	public static HashMap<String, HashMap<Integer, Integer>> mTerm = new HashMap<String, HashMap<Integer, Integer>>();
	public static ArrayList<Integer> mClassValue = new ArrayList<Integer>();
	
	TokenizerFactory tokenizerFactory = new RegExTokenizerFactory("[a-zA-Z0-9\\-]+|\\S");

	
	/**
	 * Initialize the converter by loading stopwords
	 * @param stopwordFilename
	 */
	public void initialize(String stopwordFilename)
	{
		//Loading stopword
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(new File(stopwordFilename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(String line: lines)
		{
			line = line.trim().toLowerCase();
			stopwords.add(line);
		}
		
	}
	
	/**
	 * Use this method to tokenize your string
	 * @param text
	 * @param removeStopwords
	 * @param stem
	 * @param N
	 * @return
	 */
	public String[] tokenize(String text, boolean R, boolean S, int N)
	{
		//lowercasing
		text = text.toLowerCase();
		
		//clean up punctuation
		String[] rawTerms = text.replaceAll("[^a-zA-Z0-9 ]", " ").split("\\s+");
		
		Vector<String> terms = new Vector<String>(Arrays.asList(rawTerms));
		
		//remove stopwords
		if(R)
		{
			Vector<String> temp = new Vector<String>();
			for(String term: terms)
			{
				if(!stopwords.contains(term))
				{
					temp.add(term);
				}
			}
			terms = temp;
		}
		
		//stem
		if(S)
		{
			Vector<String> temp = new Vector<String>();
			for(String term: terms)
			{
				term = PorterStemmerTokenizerFactory.stem(term);
				if(!term.isEmpty()) temp.add(term);
			}
			terms = temp;
		}
		
		//n-gram generation
		Vector<String> temp = new Vector<String>();
		for(int i = 0; i < terms.size(); i++)
		{	
			for(int n = 1; n <= N; n++)
			{
				if(i + n > terms.size()) break;
				String gram = "";
				for(int j = i; j < i+n; j++)
				{
					gram += terms.get(j) +" ";
				}
				
				gram = gram.trim().replace(" ", DELIM);
				if(gram.isEmpty()) continue;
				temp.add(gram);
			}
		}
		terms = temp;
		
		return terms.toArray(new String[terms.size()]);
	}
	
	
	/**
	 * Sort the HashMap
	 * @param  unsortMap
	 */
	public HashMap<String, HashMap<Integer, Integer>> sortHashMapByComparator(Map<String, HashMap<Integer, Integer>> unsortMap) {

		// Convert Map to List
		List<Map.Entry <String, HashMap<Integer, Integer>>> list = 
			new LinkedList<Map.Entry<String, HashMap<Integer, Integer>>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, HashMap<Integer, Integer>>>() {
			public int compare(Map.Entry<String, HashMap<Integer, Integer>> o1,
					Map.Entry<String, HashMap<Integer, Integer>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		// Convert sorted map back to a Map
		HashMap<String, HashMap<Integer, Integer>> sortedMap = new LinkedHashMap<String, HashMap<Integer, Integer>>();
		for (Iterator<Map.Entry<String, HashMap<Integer, Integer>>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, HashMap<Integer, Integer>> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	/**
	 * Convert an input data file into ARFF file format for further processing with weka. The inputdata has the format of 
	 * <LABEL><TAB><TEXT> 
	 * Where <LABEL> is either 1 or 0, <TAB> is the tab character, and <TEXT> is the textual content of the SMS message. 
	 * 
	 * The output ARFF file will be saved in the outArffFilename (make sure it has .arff extension).
	 * 
	 * Configurations:
	 * R = 	true 	--> Remove stopwords (defined in "stopwords.txt")
	 * 		false	--> Do not remove stopwords
	 * 
	 * S = 	true	--> Stem each term
	 * 		false 	--> Do not stem
	 * 
	 * N = {1,2,3, ...} --> Represent the document "up to" N-grams (default is 1)
	 * Example: "I like cats a lot"
	 * N = 1 => {i, like, cats, a, lot}
	 * N = 2 => {i, like, cats, a, lot, i-like, like-cats, cats-a, a-lot}
	 * N = 3 => {i, like, cats, a, lot, i-like, like-cats, cats-a, a-lot, i-like-cats, like-cats-a, cats-a-lot}
	 * 
	 * W = {"BINARY", "NORMFREQ", "NORMTFIDF"} --> Term weighting mode.
	 * 	BINARY(t,d) 	-> 1 if d contains t, 0 otherwise.
	 * 	NORMFREQ(t,d) 	-> (Number of occurrences of t in d)/(Max Frequency of any term in d)
	 * 	NORMTFIDF(t,d)	-> TFIDF(t,d)/(Max TFIDF(t,d))
	 * 
	 * For the TF and IDF value, we use the following formulas
	 * 	TF = sqrt(freq(t,d)) ; given that freq is the count of term t in character sequence d
	 * 	IDF = log(|D|/docfreq(t)+1) ; given that docfreq is the document frequency of term t,
	 * 								  that is, the number of documents in which the term t appears and,
	 * 								  D is an amount of documents in the collection.
	 * 
	 * @param inTextFilename
	 * @param outArffFilename
	 * @param R
	 * @param S
	 * @param N
	 * @param W
	 */
	public void convertTextToArff(String inTextFilename, String outArffFilename, boolean R, boolean S, int N, String W)
	{		
		StringBuilder report = new StringBuilder();
		TfIdfDistance idfGen = null;
		if(W.equals("NORMTFIDF")){
			idfGen = new TfIdfDistance(tokenizerFactory);
		}
		
		int docNumber = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inTextFilename));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	     String sCurrentLine;
	    try {
			while ((sCurrentLine = br.readLine()) != null) {
				
				//substring to delete class
				String[] readLine = sCurrentLine.split("	");
				
				mClassValue.add(Integer.parseInt(readLine[0]));
				String[] extractedTerm = tokenize(readLine[1], R, S, N);
				
				for(String term: extractedTerm){
					//System.out.println(term);
					HashMap<Integer, Integer> map;

					if(!mTerm.containsKey(term)){
						map = new HashMap<Integer, Integer>();
						map.put(docNumber, 1);
					} 
					else {
						map = mTerm.get(term);
						if(map.containsKey(docNumber)){
							int freq = map.get(docNumber);
							map.put(docNumber, freq+1);
						}
						else{
							map.put(docNumber, 1);
						}
					}
					mTerm.put(term, map);
				}
				docNumber++;
				
				//if it is tfidf, need to add to handle of TfIdfDistance object
				if(W.equals("NORMTFIDF")){
					StringBuilder builder = new StringBuilder();
					for(String t : extractedTerm) {
					    builder.append(t+" ");
					}
					idfGen.handle(builder.toString());
				}
				
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    mTerm = sortHashMapByComparator(mTerm);
	    
	    report.append("@RELATION SPAM_RSW:"+R+"_STEM:"+S+"_N:"+N+"_W:"+W+"\n");
    	for (Map.Entry<String, HashMap<Integer, Integer>> entry : mTerm.entrySet()) {
    	    String key = entry.getKey();
    	    report.append("@ATTRIBUTE "+key+" NUMERIC\n");
    	}
    	
    	report.append("@ATTRIBUTE [class] {0,1}"+"\n"+"@DATA"+"\n");
	    
	    for(int round=0; round<docNumber; round++){
	    	report.append("\n");
		    //start command
		    if(W.equals("BINARY")){
		    	for (Map.Entry<String, HashMap<Integer, Integer>> entry : mTerm.entrySet()) {
		    	    HashMap<Integer, Integer> value = entry.getValue();
		    	    if(value.containsKey(round)){
		    	    	report.append("1.0,");
		    	    }
		    	    else{
		    	    	report.append("0.0,");
		    	    }
		    	}
		    }
		    else if(W.equals("NORMFREQ")){
		    	double maxTermFreq = 0;
		    	//get Max Term Frequency
		    	for (Map.Entry<String, HashMap<Integer, Integer>> entry : mTerm.entrySet()) {
		    	    HashMap<Integer, Integer> value = entry.getValue();
		    	    if(value.containsKey(round) && value.get(round)>maxTermFreq){
		    	    	maxTermFreq = value.get(round);
		    	    }
		    	}
		    	for (Map.Entry<String, HashMap<Integer, Integer>> entry : mTerm.entrySet()) {
		    	    String key = entry.getKey();
		    	    HashMap<Integer, Integer> value = entry.getValue();
		    	    if(value.containsKey(round)){
		    	    	double v = value.get(round)/maxTermFreq;
		    	    	report.append(v+",");
		    	    }
		    	    else{
		    	    	report.append("0.0,");
		    	    }
		    	}
		    }
		    else if(W.equals("NORMTFIDF")){
		    	HashMap<String, Double> tfidfValue = new HashMap<String, Double>();
		    	
		    	double maxTfIdf = 0;
		    	//get Max TFIDF
		    	for (Map.Entry<String, HashMap<Integer, Integer>> entry : mTerm.entrySet()) {
		    	    String key = entry.getKey();
		    	    HashMap<Integer, Integer> value = entry.getValue();		    	    
		    	    if(value.containsKey(round)){
		    	    	double tf = Math.sqrt(value.get(round));
			    	    double idf = Math.log(idfGen.numDocuments()/(idfGen.docFrequency(key)+1.0));

			    	    double tfidf = tf*idf;
			    	    
			    	    tfidfValue.put(key, tfidf);
			    	    if(maxTfIdf < tfidf){
			    	    	maxTfIdf = tfidf;
			    	    }
		    	    } 
		    	}
		    			    	
		    	for (Map.Entry<String, HashMap<Integer, Integer>> entry : mTerm.entrySet()) {
		    	    String key = entry.getKey();
		    	    HashMap<Integer, Integer> value = entry.getValue();
		    	    if(value.containsKey(round)){
		    	    	double normTfIdf = tfidfValue.get(key)/maxTfIdf;
		    	    	report.append(normTfIdf+",");
		    	    }
		    	    else{
		    	    	report.append("0.0,");
		    	    }
		    	}
		    	
		    }
		    report.append(mClassValue.get(round));
	    }
	    
	    //preparing file name
	    //String filename = "sms1000.R"+R+"_S"+S+"_N"+N+"_W"+W+".arff";
	    Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outArffFilename), "utf-8"));
			writer.write(report.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	
	}
	
	public static void main(String[] args)
	{
	StopWatch clock = new StopWatch();
	clock.start();
		TextToArffConverter converter = new TextToArffConverter();
		converter.initialize("stopwords.txt");
		converter.convertTextToArff("sms1000.dat", "out.arff", true, true, 2, "NORMTFIDF"); //"BINARY", "NORMFREQ", "NORMTFIDF"
	clock.stop();
		System.out.println("Total time used: "+clock.toString()+" seconds");
		
	}
	
}
