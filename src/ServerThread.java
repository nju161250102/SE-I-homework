import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerThread extends Thread {
	private static ArrayList<String> userList = new ArrayList<String>();
	static int count = 0;
	
	Socket s = null;
	private PrintWriter out;
	private String user = "";
	private String lineSeparator = System.lineSeparator();
	private String[] fileName = new File("./").list();
	
	public ServerThread(Socket s) {
		this.s = s;
		count ++;
	}
	
	//update the list of filenames
	private void update() {fileName = new File("./").list();}
	
	//get the newest filename according to name
	private String getNewestFile(String name) {
		update();
		String reString = null;
		int max = 0;
		for (String s : fileName) {
			if (s.contains(name) && s.contains(user)) {
				String[] sArray = s.split("_");
				if (Integer.parseInt(sArray[1]) > max) {
					max = Integer.parseInt(sArray[1]);
					reString = s;
				}
			}
		}
		return reString;
	}
	
	//get all the files including filename and user's name
	private ArrayList<String> getFileList(String name) {
		update();
		ArrayList<String> reList = new ArrayList<String>();
		for (String s : fileName) {
			if (s.contains(name) && s.contains(user)) {
				reList.add(s);
			}
		}
		return reList;
	}
	
	//send one message to Client, use "over" as end
	private void sendToClient(String s) {
		out.println(s + lineSeparator + "over");
	}
	@SuppressWarnings("resource")
	
	//send content of file to client
	private void sendToClient(File f) throws IOException {
		BufferedReader fileBr = new BufferedReader(new FileReader(f));
		String tempString = null;
		String fileContent = "";
		do {
			tempString = fileBr.readLine();
			if (tempString != null) fileContent += (tempString + lineSeparator);
			else fileContent += ("over" + lineSeparator);
		} while (tempString != null);
		out.println(fileContent);
	}
	
	//compile
	private String compileBF(String s, String input, int plusNum, int subNum) throws Exception {
		//Machine initialize
		String output = "";
		char[] cell = new char[1025];
		int pointer = 513;
		int k = 0;
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '+') cell[pointer] ++;
			else if (c == '-') cell[pointer] --;
			else if (c == '>') pointer ++;
			else if (c == '<') pointer --;
			else if (c == ',') {
				cell[pointer] = input.charAt(k);
				k ++;
			}
			else if (c == '.') output += cell[pointer];
			else if (c == '[') {
				if (cell[pointer] == 0) {
					int n = 0;
					loop: while(true) {
						i ++;
						if (s.charAt(i) == '[') n++;
						if (s.charAt(i) == ']') {
							if (n == 0) break loop;
							else n --;
						}
					};
				}
			}
			else if (c == ']') {
				if (cell[pointer] != 0) {
					int n = 0;
					loop: while(true) {
						i --;
						if (s.charAt(i) == ']') n++;
						if (s.charAt(i) == '[') {
							if (n == 0) break loop;
							else n --;
						}
					};
				}
			}
		}
		return output;
	}
	
	//print info
	private void print(String s) {System.out.println("["+user+"] " + s);}
	
	@SuppressWarnings("deprecation")
	public void run(){
		String ip = s.getInetAddress().getHostAddress();
		System.out.println(ip + "  connected......");
		BufferedReader bufIn = null;
		
		try {
			bufIn= new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new PrintWriter(s.getOutputStream(),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String line = null;
		String input = "";
		
		try {
			while((line=bufIn.readLine())!=null){
				//System.out.println(line);
				
				if ("close".equals(line)) {
					print("Close");
					count --;
					userList.remove(user);
					break;
				}
				
				if ("Login".equals(line)) {
					user = bufIn.readLine();
					if (userList.contains(user)) {
						sendToClient("failed");
						count --;
						stop();
					}
					else {
						sendToClient("succeed");
						userList.add(user);
						print("Log in");
					}
				}
				
				else if ("Acquire".equals(line)) {
					List<String> re = new ArrayList<String>();
					for (String name: fileName) {
						if (name.contains(user)) re.add(name);
					}
					sendToClient(re.toString());
					print("Get fileList: " + re.toString());
				}
				
				else if ("New".equals(line)) {
					line = bufIn.readLine();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					Date date = new Date();
					File newFile = new File("./" + user + "_" + sdf.format(date) + "01_" + line);
					if (newFile.createNewFile()) {
						print("Build " + newFile.getName());
						sendToClient(line + "built succeed");
					}
				}
				
				else if ("Open".equals(line)) {
					String name = bufIn.readLine();
					sendToClient(new File("./" + getNewestFile(name)));
					print("Open " + name);
				}
				
				else if ("OpenByVersion".equals(line)) {
					String name = bufIn.readLine();
					sendToClient(new File("./" + user + "_" + name));
					print("Open " + name);
				}
				
				else if ("Save".equals(line)) {
					String name = bufIn.readLine();
					ArrayList<String> fileList = getFileList(name);
					String newestFileName = getNewestFile(name);
					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					
					String newFileName = null;
					if (newestFileName.split("_")[1].substring(0,8).equals(sdf.format(date))) newFileName = user + "_" + (Integer.parseInt(newestFileName.split("_")[1]) + 1) + "_" + name;
					else newFileName = user + "_" + sdf.format(date) + "01_" + name;
					fileList.add(newFileName);
					
					if (fileList.size() > 5) {
						String minName = fileList.get(0);
						for (String n : fileList) {
							if (Integer.parseInt(n.split("_")[1]) < Integer.parseInt(minName.split("_")[1])) minName = n;
						}
						new File("./" + minName).delete();
					}
					
					File newFile = new File("./" + newFileName);
					newFile.createNewFile();
					FileWriter fw = new FileWriter(newFile);
					do {
						line = bufIn.readLine();
						if (!"over".equals(line)) fw.write(line + lineSeparator);
					} while(!"over".equals(line));
					fw.close();
					
					print("Save " + newFileName);
					sendToClient(" saved successfully");
				}
				
				else if ("Version".equals(line)) {
					String name = bufIn.readLine();
					print("Get Version: " + getFileList(name).toString());
					sendToClient(getFileList(name).toString());
				}
				
				else if ("Run".equals(line)) {
					input = bufIn.readLine();
					print("Input" + input);
					line = bufIn.readLine();
					String code = "";
					int[] num = {0, 0, 0, 0, 0};//{>, <, [, ], ,}
					
					if (".bf".equals(line.substring(line.lastIndexOf("."), line.length()))) {
						line = bufIn.readLine();
						
						while (! line.equals("over")) {
							//create code
							for (int i = 0; i < line.length(); i++) {
								char c = line.charAt(i);
								if (c == ';') break;
								else if (c == '>') {num[0] ++; code += c;}
								else if (c == '<') {num[1] ++; code += c;}
								else if (c == '[') {num[2] ++; code += c;}
								else if (c == ']') {num[3] ++; code += c;}
								else if (c == ',') {num[4] ++; code += c;}
								else if (c == '.'|c == '+'|c == '-') code += c;
							 }
							 line = bufIn.readLine();
						 }
					}
					else {
						line = bufIn.readLine();
						while (! "over".equals(line)) {
							String[] codeArray = line.split(" ");
							for (int i = 0; i < codeArray.length; i ++) {
								if (i % 2 == 1) {
									String str = codeArray[i-1] + codeArray[i];
									if ("Ook.Ook?".equals(str)) {num[0] ++; code += '>';}
									else if ("Ook?Ook.".equals(str)) {num[1] ++; code += '<';}
									else if ("Ook!Ook?".equals(str)) {num[2] ++; code += '[';}
									else if ("Ook?Ook!".equals(str)) {num[3] ++; code += ']';}
									else if ("Ook.Ook!".equals(str)) {num[4] ++; code += ',';}
									else if ("Ook!Ook.".equals(str)) code += '.';
									else if ("Ook.Ook.".equals(str)) code += '+';
									else if ("Ook!Ook!".equals(str)) code += '-';
									else break;
								}
							}
							line = bufIn.readLine();
						}
					}
					print("Code:" + code);
					 
					if (num[4] > input.length()) sendToClient("Error: Insufficient input data");
					else if (num[2] < num[3]) sendToClient("Error: '[' not found");
					else if (num[2] > num[3]) sendToClient("Error: ']' not found");
					else {
						try {
							sendToClient("Output:" + compileBF(code, input, num[0], num[1]));
						} catch (Exception e) {
							e.printStackTrace();
							sendToClient("Error: Unkown Error in Code.");
						}
					}
				}
				if (!"over".equals(line)) line = bufIn.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
