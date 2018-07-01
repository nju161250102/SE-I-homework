package client;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class LogWindow extends JFrame {

	public LogWindow(ClientWindow window){
		super("登录");
		
		//size and location
		Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (screenSize.width * 0.309), screenSize.height / 4);
		setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
		setResizable(false);
		int w = getWidth();
		int h = getHeight();
		
		//layout
		Container c = getContentPane();
		c.setLayout(null);
		setBackground(Color.white);
		
		//component and its location
		JLabel labelA = new JLabel("Enter IP:  ");
		JLabel labelB = new JLabel("User name: ");
		JTextField textA = new JTextField("127.0.0.1");
		JTextField textB = new JTextField("qian");
		JButton buttonA = new JButton("Yes");
		JButton buttonB = new JButton("Quit");
		labelA.setBounds(w / 8, h / 7, w / 4, h / 7);
		textA.setBounds(w * 3 / 8, h / 7, w / 2, h / 7);
		labelB.setBounds(w / 8, h * 3 / 7, w / 4, h / 7);
		textB.setBounds(w * 3 / 8, h * 3 / 7, w / 2, h / 7);
		buttonA.setBounds(w / 8, h * 5 / 7, w / 4, h / 7);
		buttonB.setBounds(w * 5 / 8, h * 5 / 7, w / 4, h / 7);
		
		//add component
		add(labelA);
		add(textA);
		add(labelB);
		add(textB);
		add(buttonA);
		add(buttonB);
		
		//Listener
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				setVisible(false);
			}
		});
		buttonA.addActionListener(l -> {
			if (window.terminal.connect(textA.getText(), 10080, textB.getText())) {
				try {
					if ("succeed".equals(window.terminal.get())) {
						System.out.println("chenggong");
						window.setInfo("Welcome! " + textB.getText());
						window.lockMenu(true);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			else window.setInfo("server.Server Connection Error!");
			setVisible(false);
		});
		buttonB.addActionListener(l -> setVisible(false));
	}
	
}
