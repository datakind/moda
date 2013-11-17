package Geocoder;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GeocodeUI extends JPanel
                             implements ActionListener {
    private static final long serialVersionUID = 3610491923112278696L;

    JLabel inputFileLabel, outputFileLabel, delimiterFileLabel, buildingNumberColumnLabel, streetNameColumnLabel;
    JLabel zipCodeColumnLabel, boroColumnLabel, cityColumnLabel;
    
    JTextField inputFileTextField, outputFileTextField, delimiterFileTextField, buildingNumberColumnTextField;
    JTextField steetNameColumnTextField, zipCodeColumnTextField, boroColumnTextField, cityColumnTextField;
    
    JButton startGeocodeButton;
    
    public GeocodeUI() {
        super(new GridLayout(0,2));
 
        
    	
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
        delimiterFileTextField = new JTextField(1);
        add(delimiterFileTextField);
        
        buildingNumberColumnLabel = new JLabel("Building number column:");
        add(buildingNumberColumnLabel);
        buildingNumberColumnTextField = new JTextField();
        add(buildingNumberColumnTextField);
        
        streetNameColumnLabel = new JLabel("Street name column:");
        add(streetNameColumnLabel);
        steetNameColumnTextField = new JTextField();
        add(steetNameColumnTextField);
        
        zipCodeColumnLabel = new JLabel("Zip Code column:");
        add(zipCodeColumnLabel);
        zipCodeColumnTextField = new JTextField();
        add(zipCodeColumnTextField);
        
        boroColumnLabel = new JLabel("Boro column:");
        add(boroColumnLabel);
        boroColumnTextField = new JTextField();
        add(boroColumnTextField);
        
        cityColumnLabel = new JLabel("Neighborhood/Town/City column:");
        add(cityColumnLabel);
        cityColumnTextField = new JTextField();
        add(cityColumnTextField);
        
        startGeocodeButton = new JButton("Start Geocoding");
        add(startGeocodeButton);
        startGeocodeButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == startGeocodeButton) {
        	/* String filenameIn,
        	 * String filenameOut, 
        	 * String splitchar,
        	 * String houseNumberVar
        	 * String streetNameVar
        	 * String zipCodeVar
        	 * String boroVar
        	 * String townVar */
        	new Geocode(
        				inputFileTextField.getText(),
        				outputFileTextField.getText(),
        				null,
        				null,
        				delimiterFileTextField.getText(),
        				buildingNumberColumnTextField.getText(),
        				steetNameColumnTextField.getText(),
        				zipCodeColumnTextField.getText(),
        				boroColumnTextField.getText(),
        				cityColumnTextField.getText());
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
