package de.zmt.kitt.gui;

import java.awt.BorderLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelListener;

import de.zmt.kitt.sim.io.ModelParams;
import de.zmt.kitt.sim.params.*;


public class EnvironmentDefinitionView extends JPanel{
	public JTable table;
	private EnvironmentEdit model;
	public JButton btnSave;
	public JButton btnLoad;

	public EnvironmentDefinitionView(EnvironmentDefinition def, ActionListener listener)  {
		super();

		this.setLayout(new BorderLayout());
		
		// Create table 
		model = new EnvironmentEdit(def,7);
		if( table != null){
			table.setModel(model);
		}
		else{
			table = new JTable(model) {
				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return (columnIndex == 1) ? true : false;
				}
			};
		}
		table.revalidate();
		table.repaint();					
		model.addTableModelListener((TableModelListener)listener);
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		JPanel controlPanel = new JPanel();
		
		btnLoad = new JButton("load set");
		controlPanel.add(btnLoad);
		btnLoad.addActionListener( listener);
		
		JTextField nameInput= new JTextField("environment", 10);
		nameInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				TextField nameField= (TextField)e.getSource();
				//model.def.speciesName=nameField.getText();
			}
		});
		nameInput.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                JTextField nameField = (JTextField) e.getSource();
                //model.def.speciesName=nameField.getText();
            }
        });
		btnSave = new JButton("save set");
		controlPanel.add(btnSave);
		btnSave.addActionListener( listener);
		
			
		controlPanel.add(nameInput);
		
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);

		this.setVisible(true);
	}
	
	
	public void setParameters(ModelParams cfg){
		cfg.setEnvironmentDefinition( model.def);
		table.revalidate();
		table.repaint();	
	}
	
	
	public EnvironmentDefinition getEditedParameters( ){
		return model.def;		
	}
}
