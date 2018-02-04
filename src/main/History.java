package main;

import javax.swing.*;

import org.jfree.data.time.Millisecond;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;


public class History extends JSplitPane{
    public static final int COLUMNS = 4;
    public static final int ROW_HEIGHT = 250;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int TOP_HEIGHT = 950;


    private Company[] companies;
    private JPanel calendar = new JPanel();
    private JPanel historyMain = new JPanel();
    private JLabel dateLabel = new JLabel("Date");
    private JTextField dateField = new JTextField();
    private JButton dateButton = new JButton("Show history");
    private JScrollPane scroll = new JScrollPane();
    private String historyDate;

    public class ThrLoad extends Thread {

        @Override
        public void run() {
            try {
                //loadHistory();
                loadHistoryFromOne();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }
    static ThrLoad thread = null;
    public History(int num){



        dateField.setColumns(12);

        dateButton.setEnabled(true);
        dateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                historyDate = dateField.getText();
                //dateButton.setEnabled(false);
                thread = new ThrLoad(); // should add in an executor
                thread.start();
                /*while(thread.getState()!=Thread.State.TERMINATED){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                dateButton.setEnabled(true);*/

            }
        });

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        Date now = Date.from(yesterday.toInstant(ZoneOffset.UTC));
        String strDate = sdfDate.format(now);
        dateField.setText(strDate);


        calendar.setLayout(new FlowLayout());
        calendar.setSize(WIDTH,HEIGHT-TOP_HEIGHT);
        calendar.add(dateLabel);
        calendar.add(dateField);
        calendar.add(dateButton);



        historyMain.setPreferredSize(new Dimension(0,(int)Math.ceil(num/(double)COLUMNS)*ROW_HEIGHT));
        historyMain.setLayout(new GridLayout((int)Math.ceil(num/(double)COLUMNS), COLUMNS));
        historyMain.setVisible(true);

        companies = new Company[num];
        for(int i = 0; i < num; i++){
            companies[i] = new Company("History " + i);
            historyMain.add(companies[i]);
        }

        scroll = new JScrollPane(historyMain, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setLayout(new ScrollPaneLayout());
        scroll.setVisible(true);

        setSize(WIDTH, HEIGHT);
        setDividerSize(1);
        setDividerLocation(TOP_HEIGHT);
        setOrientation(JSplitPane.VERTICAL_SPLIT);
        setTopComponent(scroll);
        setBottomComponent(calendar);

    }

    public void loadHistory() throws IOException, ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d HH:mm:ss.SSS z");
        File dir = new File("./data");
        File[] foundFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(historyDate);
            }
        });
        java.lang.String strLine;

        if(foundFiles.length == 0){
            JOptionPane.showMessageDialog(this,
                    "History from that day was not found.",
                    "WARNING",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (File file : foundFiles) {
            if (file.getName().startsWith(historyDate + ".txt")){
                continue;
            }
            String fileName = file.getName();
            String fileNumber = fileName.split("_")[1];
            fileNumber = fileNumber.substring(0, fileNumber.length() - 4);
            int i = Integer.parseInt(fileNumber);
            FileInputStream fstream = new FileInputStream(".\\data\\" + file.getName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            companies[i].serie.clear();
            while ((strLine = br.readLine()) != null)   {
                java.lang.String[] tokens = strLine.split(",");
                Date date = sdf.parse(tokens[0]);
                Millisecond time = new Millisecond(date);
                System.out.println(i);
                companies[i].serie.add(time, Float.parseFloat(tokens[1]));
            }
            in.close();
        }

    }

    private void loadHistoryFromOne() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d HH:mm:ss.SSS z");
        File dir = new File("./data");
        File[] foundFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(historyDate + ".txt");

            }
        });

        if(foundFiles.length == 0){
            JOptionPane.showMessageDialog(this,
                    "History from that day was not found.",
                    "WARNING",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.lang.String strLine;
        File file = foundFiles[0];
        FileInputStream fstream = new FileInputStream(".\\data\\" + file.getName());
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        while ((strLine = br.readLine()) != null)   {
            java.lang.String[] tokens = strLine.split(",");
            Date date = sdf.parse(tokens[0]);
            Millisecond time = new Millisecond(date);

            for(int i = 1; i < tokens.length; ++i){
                companies[i-1].serie.add(time, Float.parseFloat(tokens[i]));
            }
        }
        in.close();
    }


    public void renameChart(int i, String name){
        companies[i].renameChart(name);
    }
}