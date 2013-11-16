package Parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

public class DataLookup {

    /** The path to the data-file */
	private String filepath;
	/** The delimiter string/character to split each line on */
	private String delim;
	
	// constructor
	public DataLookup (String filepath, String delim){
		this.filepath = filepath;
		this.delim = delim;
	}

	/**
	 * 
	 * @param headerName
	 * @return The header name, in lowercase, with quotes and extra whitespace
	 *     removed
	 */
	private String normalizeHeaderName(String headerName) {
	    return headerName.toLowerCase()
	            .replaceAll("\"", "")
	            .trim();
	}

	/**
	 * Parses the header-line to determine the index of the given column-name
	 * @param varName The name of the column
	 * @return The 0-based index of the column with the supplied name; -1 
	 * @throws IOException 
	 */
	public int varNum(String varName) {
	    if (varName == null || varName.length() == 0) {
	        return -1;
	    }

	    Reader reader = null;
	    int varIndex = -1;
	    try {
	        File file = new File(this.filepath);
	        if (file.exists()) {
	            reader = new FileReader(file);
	        } else {
	            URL url = getClass().getResource(this.filepath);

	            reader = new InputStreamReader(url.openStream());
	        }

	        CSVReader<String[]> csvParser = new CSVReaderBuilder<String[]>(reader)
	                .strategy(new CSVStrategy(this.delim.charAt(0), '\"', '#', false, true))
	                .entryParser(new CSVEntryParser<String[]>() {
	                    public String[] parseEntry(String... data) { return data; }
	                })
	                .build();
	        List<String> headers = csvParser.readHeader();

	        for (int i = 0; i < headers.size(); ++i) {
	            String header = headers.get(i);

	            if (this.normalizeHeaderName(header)
	                    .equals(this.normalizeHeaderName(varName))) {
	                varIndex = i;
	            }
	        }
	    } catch (IOException ioException) {
	        try { reader.close(); } catch (Exception e) { }
	        throw new RuntimeException(ioException);
	    }

	    return varIndex;
	}
	
	// number of observations
	public int nobs() {
		In inputFile = new In(filepath);
		int n = 0;
        inputFile.readLine();
        while (!inputFile.isEmpty()) {
        	inputFile.readLine();
        	n++;
        }
        inputFile.close();
		return n;
	}
	
	// create hash with duplicate key values
	public HashMap<String, ArrayList<String>> DupeKeyLookup(String keyvar, String valuevar) {
		
		HashMap <String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
		ArrayList<String> results = new ArrayList<String>();
		
		int keyIndex = varNum(keyvar);
		int valueIndex = varNum(valuevar);
		
		String key, value;
		
        // variable is not in the dataset
        if (keyIndex < 0) throw new RuntimeException(keyvar + " not found in file " + filepath);
        if (valueIndex < 0) throw new RuntimeException(valuevar + " not found in file " + filepath);

		In inputFile;
        inputFile = new In(filepath);
        
        String line = inputFile.readLine();
        while (!inputFile.isEmpty()) {
        	
        	key = "";
        	value = "";
        	
        	//read and parse data lines
            line = inputFile.readLine();
            String[] data = line.split(Pattern.quote(delim));
            
            if (data.length > keyIndex) key = data[keyIndex]; 
            if (data.length > valueIndex) value = data[valueIndex]; 

            //add values to key
            if (!hm.containsKey(key)) { results = new ArrayList<String>(); }
            else {results = hm.get(key); }
            results.add(value);
            hm.put(key, results);
        }		
        inputFile.close();
        return hm;
	}

	//create regular hash 
	public HashMap<String, String> KeyLookup(String keyvar, String valuevar) {
		
		HashMap <String, String> hm = new HashMap<String, String>();

		int keyIndex = varNum(keyvar);
		int valueIndex = varNum(valuevar);
		
		String key, value;
		
        // variable is not in the dataset
        if (keyIndex < 0) throw new RuntimeException(keyvar + " not found in file " + filepath);
        if (valueIndex < 0 && !valuevar.equals("")) throw new RuntimeException(valuevar + " not found in file " + filepath);
        

        try  {
	        BufferedReader inputFile = new BufferedReader( new InputStreamReader(getClass().getResourceAsStream(filepath)));
	        
	        String line = inputFile.readLine();
	        while ((line = inputFile.readLine()) != null) {
	        	
	        	key = "";
	        	value = "";
	        	
	        	//read and parse data lines
	            String[] data = line.split(Pattern.quote(delim));
	            if (data.length > keyIndex) key = data[keyIndex];
	            if (data.length > valueIndex && !valuevar.equals("")) value = data[valueIndex];
	            
	            //add values to key
	            hm.put(key, value);
	            
	        }		
	        inputFile.close();
	        return hm;
        } catch(Exception e) { 
        	System.out.println("Exception while reading csv file: " + e);
        	return hm;
        }                  
	}		
	//create regular hash with array result - BROKEN DO NOT USE
	/*public HashMap<String, String[]> KeyLookup(String keyvar, String[] valuevars) {
		
		HashMap <String, String[]> hm = new HashMap<String, String[]>();

		int keyIndex = varNum(keyvar);

        // variable is not in the dataset
        if (keyIndex < 0) throw new RuntimeException(keyvar + " not found in file " + filepath);

		int[] valueIndexes = new int[valuevars.length];
		for (int i=0; i<valueIndexes.length; i++) {
			valueIndexes[i]=varNum(valuevars[i]);
			if (valueIndexes[i] < 0) throw new RuntimeException(valuevars[i] + " not found in file " + filepath);
		}

		In inputFile = new In(filepath);
    	String key = "";
    	String[] values = new String[valuevars.length];
    	
        String line = inputFile.readLine();
        while (!inputFile.isEmpty()) {

        	//read and parse data lines
            line = inputFile.readLine();
            String[] data = line.split(Pattern.quote(delim));
           
            if (data.length > keyIndex) key = data[keyIndex]; 
            else key = "";
            
            for (int i=0; i<valuevars.length; i++) {
            	if (data.length > valueIndexes[i]) values[i] = data[valueIndexes[i]]; 
            	else values[i] = "";
            }

            //add values to key
            if (!hm.containsKey(key) && !key.equals("")) {
            	hm.put(key, values);
            	//StdOut.println(Arrays.toString(values));
        		if (key.equals("1 11111201")) {
        			String[] res = hm.get("1 11111201");
        			System.out.println(res[0]);   
        			return hm;
        		}
            }

        }		
        inputFile.close();
        return hm;
	}	*/

	
	//get all values for variable
	public ArrayList<String> GetVals(String var) {
		ArrayList<String> results = new ArrayList<String>();
		
		int varIndex = varNum(var);

		String value;
		
        // variable is not in the dataset
        if (varIndex < 0) throw new RuntimeException(var + " not found in file " + filepath);

		In inputFile;
        inputFile = new In(filepath);
        
        String line = inputFile.readLine();
        while (!inputFile.isEmpty()) {
        	value = "";
        	
        	//read and parse data lines
            line = inputFile.readLine();
            String[] data = line.split(Pattern.quote(delim));
            
            if (data.length > varIndex) value = data[varIndex]; 

            //add values to key
            results.add(value);
        }		
        inputFile.close();
        return results;		
	}
	
	public static HashMap KeyLookup(String filename, String delim, String keyvar, String valuevar) {
		DataLookup dl = new DataLookup(filename,delim);
		HashMap m = dl.KeyLookup(keyvar, valuevar);		
		return m;
	}
	
	public static HashMap DupeKeyLookup(String filename, String delim, String keyvar, String valuevar) {
		DataLookup dl = new DataLookup(filename,delim);
		HashMap m = dl.DupeKeyLookup(keyvar, valuevar);		
		return m;
	}	
	
	public static void main(String[] args) {
		DataLookup lu = new DataLookup("Z:\\NBAT\\gis_sample.csv",",");
		int v = lu.varNum("NBATID");
		System.out.println(v);/*
		DataLookup zipFile = new DataLookup("Geocoder/zip_to_boro.csv",",");
		HashMap<String,String>  zipMatch = zipFile.KeyLookup("zip", "boronum");		
		
		DataLookup addressFile = new DataLookup("/Geocoder/valid_addresses.csv",",");
		HashMap<String,String> addressMatch = addressFile.KeyLookup("address", "ids");
		String res = addressMatch.get("1 11111001");
		System.out.println(res);
		
		DataLookup dl = new DataLookup("/Parser/edit_streetnames.csv",",");

		// create hash with duplicate key values
		HashMap<String, ArrayList<String>> dupkey = dl.DupeKeyLookup("boronum_g", "streetname_g");
		ArrayList<String> boro_5_vals = dupkey.get("5");
		StdOut.println(boro_5_vals.size());
		
		//create regular hash 
		HashMap<String, String> hm = dl.KeyLookup("boronum_g", "streetname_g");
		String last_boro_5 =  (String) hm.get("6");
		StdOut.println(last_boro_5);		
		
		//get all values for variable
		ArrayList<String> boro = dl.GetVals("boronum_g");
		StdOut.println(boro.size());

		// number of observations
		StdOut.println(dl.nobs());
*/
	}

}
