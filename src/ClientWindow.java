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

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ClientWindow extends JFrame {
	/*
	┌──────────────────────────────────────┐
	│Menu                                  │
	├──────────────────────────────────────┥
    │		                               │
	│inputArea                             │
	│ 		                               │
	├──────────────────────────────────────┥
	│dataInput                             │
	├──────────────────────────────────────┥
	│bottomLabel                           │
	└──────────────────────────────────────┘
	*/
	private JTextArea inputArea = new JTextArea();
	private JTextField dataInput = new JTextField();
	private JLabel bottomLabel = new JLabel();
	//Menu and MenuItem
	private JMenu menuVersion = new JMenu("Version");
	private JMenuItem menuFileNew = new JMenuItem("New");
	private JMenuItem menuFileOpen = new JMenuItem("Open");
	private JMenuItem menuFileSave = new JMenuItem("Save");
	private JMenuItem menuEditRun = new JMenuItem("Run");
	private JMenuItem[] menuVersionItem = new JMenuItem[5];
	private JMenuItem menuLogIn = new JMenuItem("Log in");
	private JMenuItem menuLogOut = new JMenuItem("Log out");
	//two child windows
	private LogWindow logWindow = new LogWindow(this);
	private OpenWindow openWindow = new OpenWindow(this);
	ClientTerminal terminal = new ClientTerminal();
	protected String name = ""; // the name of user
	//get and set
	public String getInput() { return inputArea.getText();}
	public String getData() {return dataInput.getText();}
	public void setContent(String s) {inputArea.setText(s);}
	public void setInfo(String s) {bottomLabel.setText(s);}
	//file opened successfully
	public void openSuccess(String s) {
		setTitle("编译器客户端  --------" + s);

		menuEditRun.setEnabled(true);
		name = s;
		setVersion();
		menuFileSave.setEnabled(true);
	}
	//set the Version Menu
	protected void setVersion() {
		String str = null;
		String[] list = null;
		try {
			terminal.send("Version", name);
			str = terminal.get();
			list = str.substring(1, str.length() - 1).split(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < list.length; i ++) {
			menuVersionItem[i].setText(list[i].split("_")[1]);
			menuVersion.add(menuVersionItem[i]);
		}
		for (int i = list.length; i < 5; i ++) menuVersion.remove(menuVersionItem[i]);
	}
	//
	ClientWindow() {
		super("编译器客户端");
		
		//size and location
		Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (screenSize.width * 0.618), screenSize.height / 2);
		setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
		setResizable(false);
		
		//close button and layout
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.setBackground(Color.white);
		
		//other menus
		JMenu menuFile = new JMenu("File");
		JMenu menuEdit = new JMenu("Edit");
		JMenu menuLog = new JMenu("Log");
		JMenuItem menuFileExit = new JMenuItem("Exit");
		JMenuItem menuEditUndo = new JMenuItem("Undo");
		JMenuItem menuEditRedo = new JMenuItem("Redo");
		JMenuBar menuBar = new JMenuBar();
		menuFileSave.setEnabled(false);
		//add Menu and MenuItem
		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		menuBar.add(menuVersion);
		menuBar.add(menuLog);
		menuFile.add(menuFileNew);
		menuFile.add(menuFileOpen);
		menuFile.add(menuFileSave);
		menuFile.addSeparator();
		menuFile.add(menuFileExit);
		menuEdit.add(menuEditUndo);
		menuEdit.add(menuEditRedo);
		menuEdit.addSeparator();
		menuEdit.add(menuEditRun);
		for (int i = 0; i < menuVersionItem.length; i++) menuVersionItem[i] = new JMenuItem("i");
		menuLog.add(menuLogIn);
		menuLog.add(menuLogOut);

		//text area
		inputArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		inputArea.setLineWrap(true);
		inputArea.setWrapStyleWord(true);
		JScrollPane jsp = new JScrollPane(inputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		//data area
		dataInput.setText("please type the input here...");
		
		//bottom bar
		setInfo("Please log in first");
		
		//add component
		add(menuBar, BorderLayout.NORTH);
		add(jsp, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2, 1, 0, 2));
		bottomPanel.add(dataInput);
		bottomPanel.add(bottomLabel);
		add(bottomPanel, BorderLayout.SOUTH);
		
		//Menu Action Listener
		addWindowListener(new WindowAdapter(){ 
			public void windowClosing(WindowEvent e){ 
				try {
					if (menuLogOut.isEnabled()) terminal.send("close", "");
					System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				setVisible(false);
			}   
		}); 
		menuFileNew.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				openWindow.setWindow("New");
			}
		});
		menuFileOpen.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openWindow.setWindow("Open");
			}
		});
		menuFileSave.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					terminal.send("Save", name + System.lineSeparator() + getInput());
					setInfo(name + terminal.get());
					setVersion();
				} catch (IOException e1) {
					setInfo("Wrong: Save Failed");
				}
			}
		});
		menuFileExit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					terminal.send("close", "");
				} catch (IOException e1) {
					setInfo("Wrong: Log Out Failed");
				}
				System.exit(1);
			}
		});
		menuEditRun.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					terminal.send("Run", getData() + System.lineSeparator() + name + System.lineSeparator() + getInput());
					String[] s = terminal.get().split(":");
					
					if ("Error".equals(s[0])) setInfo("Error : " + s[1]);
					else if ("Output".equals(s[0])) setInfo("The output:  " + s[1]);
				} catch (IOException e) {
					setInfo("Wrong: May Not Connected to Server");
				}
			}
			
		});
		for (int i = 0; i < menuVersionItem.length; i ++) {
			menuVersionItem[i].addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						terminal.send("OpenByVersion", e.getActionCommand() + "_" + name);
						setContent(terminal.get());
						setInfo("VersionID: " + e.getActionCommand());
					} catch (IOException e1) {
						setInfo("Wrong: Not connected to Server");
					}
				}
			});
		}
		menuLogIn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				logWindow.setVisible(true);
			}
		});
		menuLogOut.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					terminal.close();
				} catch (IOException e1) {
					setInfo("Wrong: Log Out Failed");
				}
				lockMenu(false);		
				menuFileSave.setEnabled(false);
			}
		});
		lockMenu(false);
		
		logWindow.setVisible(false);
		setVisible(true);
	}
	
	public void lockMenu(boolean b) {
		menuVersion.setEnabled(b);
		menuFileNew.setEnabled(b);
		menuFileOpen.setEnabled(b);
		menuLogIn.setEnabled(!b);
		menuLogOut.setEnabled(b);
	}
	//
	
	public static void main(String[] args) {
		new ClientWindow();
	}
	
}