package Parser;

import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.regex.Pattern;
import java.lang.String;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import Parser.In;

public class DataLookup {
	
	private String filepath, delim;
	
	// constructor
	public DataLookup (String filepath, String delim){
		this.filepath = filepath;
		this.delim = delim;
	}
	
	
	// index of variable in dataset
	public int varNum(String varName){
		int varIndex = -1;
		if (varName.length()==0) return varIndex;
		In inputFile;
        inputFile = new In(filepath);	
        // search for key and value indexes in header record
        String headerline = inputFile.readLine();
        //System.out.println(headerline);
        String[] headers = headerline.split(Pattern.quote(delim));
        int nVar = headers.length;
        for (int i = 0; i < nVar; i++) {
            if (headers[i].toLowerCase().trim().replaceAll("\"","").equals(varName.toLowerCase().trim())) {
            	varIndex = i;
            	break;
            }
        }
        inputFile.close();
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
