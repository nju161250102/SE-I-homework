import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class OpenWindow extends JFrame {
	private ClientWindow window;
	private Set<String> fileNameSet = new HashSet<String>();
	private boolean lock = true; // true is open; false is new
	@SuppressWarnings("rawtypes")
	private JList list = new JList();
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JComboBox comboBox = new JComboBox(new String[]{".bf", ".ook"});
	private JTextField textField = new JTextField("");
	
	@SuppressWarnings("unchecked")
	void setWindow(String s) {
		setTitle(s);
		lock = "New".equals(s);
		textField.setEditable(lock);
		comboBox.setEnabled(lock);
		setVisible(true);

		//get information from server
		try {
			window.terminal.send("Acquire", "");
			String fileNameList = window.terminal.get();
			String[] fileNameArray = fileNameList.substring(1, fileNameList.length() - 1).split(",");
			for (String name : fileNameArray) fileNameSet.add(name.split("_")[2]);
			list.setListData(fileNameSet.toArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	OpenWindow(ClientWindow window) {
		this.window = window;
		//size and location
		Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width / 5, screenSize.height / 2);
		setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
		setResizable(false);
		
		//layout
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		setBackground(Color.white);
		
		//component
		list.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setAutoscrolls(true);
		JButton button = new JButton("Yes");
		
		//add
		add(list, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(3, 1, 0, 0));
		bottomPanel.add(textField);
		bottomPanel.add(comboBox);
		bottomPanel.add(button);
		add(bottomPanel, BorderLayout.SOUTH);
		
		//Listener
		addWindowListener(new WindowAdapter(){ 
			public void windowClosing(WindowEvent e){   
				setVisible(false);
			}   
		});
		list.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				textField.setText((String) list.getSelectedValue());
			}
		});
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (lock) {  // New operation
						if (fileNameSet.contains(textField.getText())) {
							textField.setText("Name Existed");
						}
						else {
							window.terminal.send("New", textField.getText() + comboBox.getSelectedItem());
							window.openSuccess(textField.getText() + comboBox.getSelectedItem());
						}
					}
					else {
						window.terminal.send("Open", textField.getText());
						window.setContent(window.terminal.get());
						window.openSuccess(textField.getText());
					}
					
				}catch(IOException e1) {
					e1.printStackTrace();
				}
				setVisible(false);
			}	
		});
		
	}
}
