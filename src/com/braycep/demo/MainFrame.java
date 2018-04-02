package com.braycep.demo;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JFrame mainFrame = null;
	private static JPanel contentPane = null;
	private static JTextField words = null;
	private static JButton search = null;
	protected static JTable table = null;
	private static JButton stop = null;
	protected static JLabel resulInfo = null;
	protected static DefaultTableModel model = null;

	private static JPopupMenu menu = null;
	private static int focusedRowIndex = 0;

	public static void main(String[] args) {
		JFrame frame = new MainFrame();
		frame.setResizable(false);
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		mainFrame = this;
		setTitle("酷狗音乐下载链接获取");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(240, 100, 910, 580);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		words = new JTextField();
		words.setBounds(102, 34, 747, 21);
		contentPane.add(words);
		words.setColumns(10);

		JLabel label = new JLabel("\u6B4C\u66F2\u540D\uFF1A");
		label.setBounds(40, 37, 54, 15);
		contentPane.add(label);

		search = new JButton("\u67E5\u627E");
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		search.setBounds(756, 65, 93, 23);
		contentPane.add(search);
		// contentPane.add(textArea);

		resulInfo = new JLabel("\u7ED3\u679C\uFF1A");
		resulInfo.setBounds(40, 97, 203, 15);
		contentPane.add(resulInfo);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(40, 118, 809, 396);
		contentPane.add(scrollPane);

		String[] cols = new String[] { "歌名", "歌手", "专辑", "文件名", "时长", "文件大小", "波特率", "下载链接" };
		String[][] datas = new String[][] {};
		model = new DefaultTableModel(datas, cols);
		table = new JTable(model);
		scrollPane.setViewportView(table);

		stop = new JButton("停止");
		stop.setBounds(636, 65, 93, 23);
		contentPane.add(stop);

		events();

		contentPane.updateUI();
	}

	private void events() {
		search.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (words.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(mainFrame, "请输入一个有效的歌曲名", "提示：", JOptionPane.INFORMATION_MESSAGE);
				} else {
					model.setRowCount(0);
					resulInfo.setText("结果：");
					new Thread(()->{
						Utils.stop = false;
						getMusicInfo();
					}).start();
				}
			}
		});

		stop.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				Utils.stop = true;
			}
		});

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					focusedRowIndex = table.rowAtPoint(e.getPoint());
					if (focusedRowIndex != -1) {
						table.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
						createMouseMenu();
						menu.show(table, e.getX(), e.getY());
					}
				}
			}
		});
	}

	private static void createMouseMenu() {
		menu = new JPopupMenu();
		JMenuItem downloadItem = new JMenuItem();
		JMenuItem downloadByBrowserItem = new JMenuItem();
		JMenuItem playByDefaultPlayerItem = new JMenuItem();
		JMenuItem playItem = new JMenuItem();
		JMenuItem openFolder = new JMenuItem();
		menu.add(initItem(downloadItem,"下载"));
		menu.add(initItem(playItem, "播放"));
		menu.add(initItem(downloadByBrowserItem, "浏览器下载"));
		menu.add(initItem(playByDefaultPlayerItem, "默认播放器播放"));
		menu.add(initItem(openFolder, "打开下载位置"));
		
	}

	private static JMenuItem initItem(JMenuItem item,String text) {
		item.setText(text);
		switch (text) {
		case "浏览器下载":
			String url = (String)table.getValueAt(focusedRowIndex, 7);
			System.out.println(url);
			item.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					downloadMusicByDefaultBrowser(url);
				}
			});
			break;
		case "默认播放器播放":
			item.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (String)table.getValueAt(focusedRowIndex, 7);
					String songName = (String)table.getValueAt(focusedRowIndex, 0);
					String[] tmp = fileName.split("/");
					fileName = tmp[tmp.length-1];
					String parentPath = System.getProperties().getProperty("user.home")+"\\Downloads\\";
					String fileNames[] = new String[2];
					fileNames[0] = songName+".mp3";
					fileNames[1] = fileName;
					System.out.println(fileNames);
					try {
						playLoaclMusic(parentPath, fileNames);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			break;
		case "打开下载位置":
			item.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Runtime.getRuntime().exec("explorer /e,/root,"+System.getProperties().getProperty("user.home")+"\\Downloads\\");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			break;
		default:
			break;
		}
		return item;
	}

	private static void playLoaclMusic(String parentPath,String[] fileNames) throws Exception{
		File mp3File = new File(parentPath+fileNames[0]);
		File hashFile = new File(parentPath+fileNames[1]);
		hashFile.renameTo(mp3File);
		if (mp3File.exists()) {
			Runtime.getRuntime().exec("explorer "+mp3File.getAbsolutePath());
			System.out.println("explorer /c "+mp3File.getAbsolutePath());
		}else if (hashFile.exists()) {
			Runtime.getRuntime().exec("explorer /c "+hashFile.getAbsolutePath());
		}else {
			JOptionPane.showMessageDialog(mainFrame, "文件未找到", "提示：",JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private static void getMusicInfo() {
		String musicName = words.getText().trim();
		try {
			musicName = URLEncoder.encode(musicName, "UTF-8");
			Utils.getMusicList(Utils.query(musicName).toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private static void downloadMusicByDefaultBrowser(String url){
		if(Desktop.isDesktopSupported()){
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URL(url).toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
