package Geocoder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
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
    JLabel fileLabel, fileExplainationLabel;
    
    JTextField inputFileTextField, outputFileTextField;
    
    JComboBox <String> delimiterFileComboBox, buildingNumberColumnComboBox, streetNameColumnComboBox;
    JComboBox <String> zipCodeColumnComboBox, boroColumnComboBox, cityColumnComboBox;
    
    JButton chooseInputFileButton, refreshColumnsButton, startGeocodeButton;
    
    JFileChooser inputFileChooser;
    
    public GeocodeUI() {
        
    	// Set up input / output file fields and button
        inputFileLabel = new JLabel("Input file path:" );
        inputFileTextField = new JTextField();
        
        delimiterFileLabel = new JLabel("Input file delimiter:");
        // TODO Set max length of delimiter text field to 1 character
        String [] defaultDelimiters = {",", "\t", ":", "|", " "};
        delimiterFileComboBox = new JComboBox<String>(defaultDelimiters);
        delimiterFileComboBox.setEditable(true);
        
        outputFileLabel = new JLabel("Output file path:");
        outputFileTextField = new JTextField();
        
        inputFileChooser = new JFileChooser();
        chooseInputFileButton = new JButton("Choose an input file..");
        chooseInputFileButton.addActionListener(this);

        fileLabel = new JLabel("Input/Output files:");
        fileExplainationLabel = new JLabel(
        		"<html>Input file must be any valid pre-existing file or URL. <br>" + 
        		"Delimiter can only be one character in length. <br>" + 
        		"Output file may be any file name (existing or not), except temp.txt.</html>");
        
        
        // Setup input file columns fields and button
        refreshColumnsButton = new JButton("Refresh Input File Columns");
        refreshColumnsButton.addActionListener(this);

        buildingNumberColumnLabel = new JLabel("Building number:");
        buildingNumberColumnComboBox = new JComboBox<String>();
        buildingNumberColumnComboBox.setEditable(true);
        
        streetNameColumnLabel = new JLabel("Street name column:");
        streetNameColumnComboBox = new JComboBox<String>();
        streetNameColumnComboBox.setEditable(true);
        
        zipCodeColumnLabel = new JLabel("Zip Code:");
        zipCodeColumnComboBox = new JComboBox<String>();
        zipCodeColumnComboBox.setEditable(true);

        boroColumnLabel = new JLabel("Boro:");
        boroColumnComboBox = new JComboBox<String>();
        boroColumnComboBox.setEditable(true);

        cityColumnLabel = new JLabel("Neighborhood/Town/City:");
        cityColumnComboBox = new JComboBox<String>();
        cityColumnComboBox.setEditable(true);
        
        inputColumnLabel = new JLabel("Input file columns:");
        inputColumnExplanationLabel = new JLabel(
        		"<html>Choose the appropriate column names which contain the data for geocoding. <br>" + 
        		"The same column may be used multiple times. <br>" + 
        		"At least Building Number or Street Name field must be specified. <br>" + 
        		"All other fields can be left blank. </html>");

        
        // The button to start the geocode process 
        startGeocodeButton = new JButton("Start Geocoding");
        startGeocodeButton.addActionListener(this);
        
      
        // Layout for input/output file fields 
        JPanel filePane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        filePane.setLayout(gridbag);
        
        // Input File chooser 
        c.gridwidth = GridBagConstraints.REMAINDER; //last
        //c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0,0,10,0);
        c.weightx = 1.0;
        filePane.add(chooseInputFileButton, c);
        
        // Input File
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.insets = new Insets(0,0,0,0); // reset to default
        c.fill = GridBagConstraints.HORIZONTAL;      //reset to default
        c.weightx = 0.0;                       //reset to default
        filePane.add(inputFileLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        filePane.add(inputFileTextField, c);
        
        // Input Delimiter 
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        filePane.add(delimiterFileLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        filePane.add(delimiterFileComboBox, c);
        
        // Output File
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        filePane.add(outputFileLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        filePane.add(outputFileTextField, c);
        
        // File Explanation text and border title 
        c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10,0,0,0);
        c.weightx = 1.0;
        filePane.add(fileExplainationLabel, c);
        filePane.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Input/Outfile Files"),
                                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        
        // Layout for input column fields 
        JPanel columnsPane = new JPanel();
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        
        columnsPane.setLayout(gridbag);
        
        // Button to refresh column names
        c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.insets = new Insets(0,0,10,0);
        c.weightx = 1.0;
        columnsPane.add(refreshColumnsButton, c);
        
        // Building Number Column 
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.insets = new Insets(0,0,0,0);		   //reset to default
        c.fill = GridBagConstraints.HORIZONTAL;      //reset to default
        c.weightx = 0.0;                       //reset to default
        columnsPane.add(buildingNumberColumnLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        columnsPane.add(buildingNumberColumnComboBox, c);
        
        // Steet Name Column
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        columnsPane.add(streetNameColumnLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        columnsPane.add(streetNameColumnComboBox, c);
        
        // Zip Code Column
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        columnsPane.add(zipCodeColumnLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        columnsPane.add(zipCodeColumnComboBox, c);
        
        // Boro Column
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        columnsPane.add(boroColumnLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        columnsPane.add(boroColumnComboBox, c);
        
        // City Column
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        columnsPane.add(cityColumnLabel, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        columnsPane.add(cityColumnComboBox, c);
        
        // Columns Explanation and Border Title
        c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10,0,0,0);
        c.weightx = 1.0;
        columnsPane.add(inputColumnExplanationLabel, c);
        columnsPane.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Input File Columns"),
                                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        // Add the prepared panes to the window
        this.setLayout(new BorderLayout(0,1));
        
        this.add(filePane, BorderLayout.PAGE_START );
        this.add(columnsPane, BorderLayout.CENTER);
        this.add(startGeocodeButton, BorderLayout.PAGE_END);
        
        // This is the old layout using GridLayout - not too pretty
        /*this.setLayout(new GridLayout(0,2));
        add(chooseInputFileButton);
        add(refreshColumnsButton);
        
        add(inputFileLabel);
        add(inputFileTextField);
        add(delimiterFileLabel);
        add(delimiterFileComboBox);
        add(outputFileLabel);
        add(outputFileTextField);
        
        add(inputColumnLabel);
        add(inputColumnExplanationLabel);
        add(buildingNumberColumnLabel);
        add(buildingNumberColumnComboBox);
        add(streetNameColumnLabel);
        add(streetNameColumnComboBox);
        add(zipCodeColumnLabel);
        add(zipCodeColumnComboBox);
        add(boroColumnLabel);
        add(boroColumnComboBox);
        add(cityColumnLabel);
        add(cityColumnComboBox);
        
        add(startGeocodeButton);*/
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
	        // this will happen during a problem reading the file/URL
	    	try { reader.close(); } catch (Exception e) { }
	        return null;
	    } catch (NullPointerException nullPointerException) {
	    	// this will happen if the file or URL does not exist
	    	try { reader.close(); } catch (Exception e) { }
	    	return null;
	    }
	}
	
	private void setColumns(String fileName, String delimiter) {
		// Set the columns for each of the ComboBoxes
		String [] headers = getHeaders(fileName, delimiter);
		if (headers != null && headers.length != 0) {
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
			JOptionPane.showMessageDialog(null, "Could not find column names! Check input file path and delimiter.");
		}
			
	}
	
	private boolean inputExists(String fileName) {
		File file = new File(fileName );
        URL url = getClass().getResource(fileName);
        
        return (file.exists() || url != null);
	}
    
    public void actionPerformed(ActionEvent e) {

        //Run the geocoder after validating input.
        if (e.getSource() == chooseInputFileButton) {
        	int returnVal = inputFileChooser.showOpenDialog(null);
        	
        	if (returnVal == JFileChooser.APPROVE_OPTION ) {
        		String fileName = inputFileChooser.getSelectedFile().getAbsolutePath();
        		
        		// TODO Validate URL as well? Export logic to method.
        		if (inputExists(fileName)) {
        			inputFileTextField.setText(fileName);
        			setColumns(fileName,
        					(String)delimiterFileComboBox.getSelectedItem());
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
        	//File tfile = new File(inputFile);
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
        	// TODO Validate URL as well?
        	else if (!inputExists(inputFile)) { 
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
        frame.pack();
        // TODO Set Max Size doesn't work properly.. not a big deal though.
        /* frame.setMaximumSize(new Dimension(
        		(int)frame.getSize().width *2 ,
        		(int)frame.getSize().height )); */
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
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
