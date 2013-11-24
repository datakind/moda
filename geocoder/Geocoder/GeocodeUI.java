package Geocoder;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

public class GeocodeUI extends JPanel
                             implements ActionListener {
    private static final long serialVersionUID = 3610491923112278696L;

    JLabel inputFileLabel, outputFileLabel, delimiterFileLabel, buildingNumberColumnLabel, streetNameColumnLabel;
    JLabel zipCodeColumnLabel, boroColumnLabel, cityColumnLabel, inputColumnLabel, inputColumnExplanationLabel;
    
    JTextField inputFileTextField, outputFileTextField;
    //JTextField steetNameColumnTextField, zipCodeColumnTextField, boroColumnTextField, cityColumnTextField, delimiterFileTextField, buildingNumberColumnTextField;
    
    JComboBox <String> delimiterFileComboBox, buildingNumberColumnComboBox, streetNameColumnComboBox;
    JComboBox <String> zipCodeColumnComboBox, boroColumnComboBox, cityColumnComboBox;
    
    JButton chooseInputFileButton, refreshColumnsButton, startGeocodeButton;
    
    JFileChooser inputFileChooser;
    
    public GeocodeUI() {
    	// TODO Improve layout design
        super(new GridLayout(0,2));
 
        inputFileChooser = new JFileChooser();
        chooseInputFileButton = new JButton("Choose an input file..");
        chooseInputFileButton.addActionListener(this);
        add(chooseInputFileButton);
        
        refreshColumnsButton = new JButton("Refresh Input File Columns");
        refreshColumnsButton.addActionListener(this);
        add(refreshColumnsButton);
    	
        inputFileLabel = new JLabel("Input file path:");
        add(inputFileLabel);
        inputFileTextField = new JTextField();
        add(inputFileTextField);
        
        outputFileLabel = new JLabel("Output file path:");
        add(outputFileLabel);
        outputFileTextField = new JTextField();
        add(outputFileTextField);
        
        delimiterFileLabel = new JLabel("Input file delimiter:");
        add(delimiterFileLabel);
        
        // TODO Set max length of delimiter text field to 1 character
        String [] defaultDelimiters = {",", "\t", ":", "|", " "};
        delimiterFileComboBox = new JComboBox<String>(defaultDelimiters);
        delimiterFileComboBox.setEditable(true);
        add(delimiterFileComboBox);
        
        //delimiterFileTextField = new JTextField(",",1);
        //add(delimiterFileTextField);
        
        
        inputColumnLabel = new JLabel("Input file columns:");
        add(inputColumnLabel);
        inputColumnExplanationLabel = new JLabel("The same column may be used multiple times!");
        add(inputColumnExplanationLabel);
        
        
        buildingNumberColumnLabel = new JLabel("Building number column:");
        add(buildingNumberColumnLabel);
        buildingNumberColumnComboBox = new JComboBox<String>();
        buildingNumberColumnComboBox.setEditable(true);
        add(buildingNumberColumnComboBox);
        
        //buildingNumberColumnTextField = new JTextField();
        //add(buildingNumberColumnTextField);
        
        streetNameColumnLabel = new JLabel("Street name column:");
        add(streetNameColumnLabel);
        streetNameColumnComboBox = new JComboBox<String>();
        streetNameColumnComboBox.setEditable(true);
        add(streetNameColumnComboBox);
        
        //steetNameColumnTextField = new JTextField();
        //add(steetNameColumnTextField);
        
        zipCodeColumnLabel = new JLabel("Zip Code column:");
        add(zipCodeColumnLabel);
        zipCodeColumnComboBox = new JComboBox<String>();
        zipCodeColumnComboBox.setEditable(true);
        add(zipCodeColumnComboBox);
        
        //zipCodeColumnTextField = new JTextField();
        //add(zipCodeColumnTextField);
        
        boroColumnLabel = new JLabel("Boro column:");
        add(boroColumnLabel);
        boroColumnComboBox = new JComboBox<String>();
        boroColumnComboBox.setEditable(true);
        add(boroColumnComboBox);
        
        //boroColumnTextField = new JTextField();
        //add(boroColumnTextField);
        
        cityColumnLabel = new JLabel("Neighborhood/Town/City column:");
        add(cityColumnLabel);
        cityColumnComboBox = new JComboBox<String>();
        cityColumnComboBox.setEditable(true);
        add(cityColumnComboBox);
        
        //cityColumnTextField = new JTextField();
        //add(cityColumnTextField);
        
        startGeocodeButton = new JButton("Start Geocoding");
        add(startGeocodeButton);
        startGeocodeButton.addActionListener(this);
    }

	public String [] getHeaders(String fileName, String delimiter) {
	    if (delimiter == null || delimiter.length() == 0) {
	        return null;
	    }

	    Reader reader = null;
	    try {
	        File file = new File(fileName );
	        if (file.exists()) {
	            reader = new FileReader(file);
	        } else {
	            URL url = getClass().getResource(fileName);

	            reader = new InputStreamReader(url.openStream());
	        }

	        CSVReader<String[]> csvParser = new CSVReaderBuilder<String[]>(reader)
	                .strategy(new CSVStrategy(delimiter.charAt(0), '\"', '#', false, true))
	                .entryParser(new CSVEntryParser<String[]>() {
	                    public String[] parseEntry(String... data) { return data; }
	                })
	                .build();
	        List<String> headerList = csvParser.readHeader();
	        
	        String [] headers = headerList.toArray(new String[headerList.size()]);
	        
	        return headers;
	        
	    } catch (IOException ioException) {
	        try { reader.close(); } catch (Exception e) { }
	        return null;
	    }  
	}
	
	private void setColumns(String fileName, String delimiter) {
		// Set the columns for each of the ComboBoxes
		String [] headers = getHeaders(fileName, delimiter);
		if (headers.length != 0) {
			// Remove the current selection options
			buildingNumberColumnComboBox.removeAllItems();
			streetNameColumnComboBox.removeAllItems();
		    zipCodeColumnComboBox.removeAllItems();
		    boroColumnComboBox.removeAllItems();
		    cityColumnComboBox.removeAllItems();
		    
		    // Add the newly parsed selection options
			for (String col : headers) {
				buildingNumberColumnComboBox.addItem(col);
				streetNameColumnComboBox.addItem(col);
			    zipCodeColumnComboBox.addItem(col);
			    boroColumnComboBox.addItem(col);
			    cityColumnComboBox.addItem(col);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Could not find column names!");
		}
			
	}
    
    public void actionPerformed(ActionEvent e) {

        //Run the geocoder after validating input.
        if (e.getSource() == chooseInputFileButton) {
        	int returnVal = inputFileChooser.showOpenDialog(null);
        	
        	if (returnVal == JFileChooser.APPROVE_OPTION ) {
        		File file = inputFileChooser.getSelectedFile();
        		
        		if (file.exists()) {
        			inputFileTextField.setText(file.getAbsolutePath());
        			setColumns(file.getAbsolutePath(),
        					(String)delimiterFileComboBox.getSelectedItem());
        			/*
        			// Set the columns for each of the ComboBoxes
        			String [] headers = getHeaders(file.getAbsolutePath(),
        						(String)delimiterFileComboBox.getSelectedItem());
        			if (headers.length != 0) {
        				// Remove the current selection options
        				buildingNumberColumnComboBox.removeAllItems();
        				streetNameColumnComboBox.removeAllItems();
        			    zipCodeColumnComboBox.removeAllItems();
        			    boroColumnComboBox.removeAllItems();
        			    cityColumnComboBox.removeAllItems();
        			    
        			    // Add the newly parsed selection options
        				for (String col : headers) {
        					buildingNumberColumnComboBox.addItem(col);
        					streetNameColumnComboBox.addItem(col);
            			    zipCodeColumnComboBox.addItem(col);
            			    boroColumnComboBox.addItem(col);
            			    cityColumnComboBox.addItem(col);
        				}
        			} else {
        				JOptionPane.showMessageDialog(null, "Could not find column names!");
        			}*/

        		} else {
        			JOptionPane.showMessageDialog(null,
            			    "Input File must be a valid file!");
        		}
        	}
        }
        else if (e.getSource() == refreshColumnsButton) {
        	setColumns(inputFileTextField.getText().trim(), (String)delimiterFileComboBox.getSelectedItem() );
        }
    	else if (e.getSource() == startGeocodeButton) {
       	        	
        	// Get the inputs 
        	String inputFile = inputFileTextField.getText().trim();
        	File tfile = new File(inputFile);
        	String outputFile = outputFileTextField.getText().trim();
			String delimiterFile = (String)delimiterFileComboBox.getSelectedItem(); // delimiterFileTextField.getText().trim();
			String buildingNumberColumn = (String)buildingNumberColumnComboBox.getSelectedItem(); // buildingNumberColumnTextField.getText().trim();
			String steetNameColumn = (String)streetNameColumnComboBox.getSelectedItem(); // steetNameColumnTextField.getText().trim();
			String zipCodeColumn = (String) zipCodeColumnComboBox.getSelectedItem(); // zipCodeColumnTextField.getText().trim();
			String boroColumn = (String) boroColumnComboBox.getSelectedItem(); // boroColumnTextField.getText().trim();
			String cityColumn = (String) cityColumnComboBox.getSelectedItem(); // cityColumnTextField.getText().trim();
			
			// validate those inputs (more validation done in Geocoder, this is the bare minimum
        	if (outputFile.equals("temp.txt") ) {
        		JOptionPane.showMessageDialog(null,
        			    "Out File cannot be temp.txt! This file is used by the Geocoder application.");
        	}
        	else if (!tfile.exists()) { 
        		JOptionPane.showMessageDialog(null,
        			    "Input File must be a valid file!");
        	}
        	else if (delimiterFile.length() > 1) {
        		JOptionPane.showMessageDialog(null,
        			    "Delimiter not be more than one character in length.");
        	}
        	else if (buildingNumberColumn.equals("") && steetNameColumn.equals("")) {
        		JOptionPane.showMessageDialog(null,
        			    "Either Building Number Column or Street Name Column must be specfied.");
        	}
        	else {
        		Geocode geocode = new Geocode(
        				inputFile,
        				outputFile,
//        				"", "", // TODO error-log file and unmatched-address file
        				delimiterFile,
        				buildingNumberColumn,
        				steetNameColumn,
        				zipCodeColumn,
        				boroColumn,
        				cityColumn);
        	}

        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("GeocodeUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Add content to the window.
        frame.add(new GeocodeUI());

        //Display the window.
        frame.getContentPane().setPreferredSize(new Dimension(500, 300));
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                    createAndShowGUI();
                }
            });
        }
    }
