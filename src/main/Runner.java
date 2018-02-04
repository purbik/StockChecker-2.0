/**
 * Created by Alena on 26. 1. 2017.
 */

import java.awt.Dimension;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.sun.org.apache.xerces.internal.impl.dv.xs.BooleanDV;
import com.sun.org.apache.xpath.internal.operations.Bool;
import main.Company;
import main.CompanySetting;
import main.History;


import main.Settings;
import org.jfree.data.time.Millisecond;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;


public class Runner {
    public static final int COLUMNS = 4;
    public static final int ROW_HEIGHT = 250;
    public static final int MAX_NUM = 39;
    static Runner r;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int TOP_HEIGHT = 950;
    private WebDriver driver;
    public static int num;
    private boolean loop;
    private boolean peep;
    private int delay;
    private int wealth = 10000;     //v EURECH mozna lepe nacitat ze souboru a umoznit ukladani

    private static String dateToday;
    private JFrame mainFrame;

    private JTextField wealthTF;
    private JLabel wealthL;
    private JButton startButton;
    private JButton stopButton;
    private JButton settingsButton;



    private JTextField[] buyPrices;
    private JTextField[] amounts;
    private JTextField[] profits;

    private Company[] companies;
    private CompanySetting[] companiesSettings;
    private static java.lang.String[] links = new java.lang.String[MAX_NUM];
    private static float[] lowerBounds = new float[MAX_NUM];
    private static float[] upperBounds = new float[MAX_NUM];
    private static Boolean[] checkBoxes = new Boolean[MAX_NUM];
    private static java.lang.String[] stockNames = new java.lang.String[MAX_NUM];



    private JPanel[] items = new JPanel[MAX_NUM];
    private JPanel[] wholes = new JPanel[MAX_NUM];
    private JPanel control;
    private JPanel view;
    private JScrollPane scroll;
    private History historyPane;

    private JLabel usa = new JLabel();
    private JTextField usaValue = new JTextField();
    private JLabel ger = new JLabel();
    private JTextField gerValue = new JTextField();



    List<PrintWriter> writers = new ArrayList<PrintWriter>();
    PrintWriter mW;
    private Boolean load = true;


    public Runner() {
        initUI();
    }

    private static java.lang.String getDateString() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private void changeGlobalVars(int num) {

        for(int i = 0; i < num; i++){
            checkBoxes[i] = companiesSettings[i].getCheck();
            links[i] = companiesSettings[i].getLink();
            lowerBounds[i] = companiesSettings[i].getLowerBound();
            upperBounds[i] = companiesSettings[i].getUpperBound();
        }
    }
    //DONE
    private static void saveCurrentConfig(int num) {
        try{
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("config.txt"), "utf-8"));
            bw.write(Integer.toString(num));
            bw.newLine();
            for(int i = 0; i < num; i++) {
                if(checkBoxes[i]){
                    bw.write("True ");
                }
                else {
                    bw.write("False ");
                }

                bw.write(links[i] + " " + lowerBounds[i] + " " + upperBounds[i]);
                bw.newLine();
            }
            bw.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }
    //DONE
    private static void loadConfigFile() {
        int i = 0;
        try{
            FileInputStream fstream = new FileInputStream("config.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            java.lang.String strLine;
            num = Integer.parseInt(br.readLine());
            if(num > MAX_NUM) {
                System.out.println("Too many chosen companies - " + num);
            }
            while ((strLine = br.readLine()) != null)   {
                java.lang.String[] tokens = strLine.split(" ");

                checkBoxes[i] = Boolean.valueOf(tokens[0]);
                links[i] = tokens[1];
                lowerBounds[i] = Float.parseFloat(tokens[2]);
                upperBounds[i] = Float.parseFloat(tokens[3]);
                i++;
            }
            in.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void loadDataFiles() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d HH:mm:ss.SSS z");
        File dir = new File("./data");
        File[] foundFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(dateToday);
            }
        });
        java.lang.String strLine;

        for (File file : foundFiles) {
            if (file.getName().startsWith(dateToday + ".txt")){
                continue;
            }
            String fileName = file.getName();
            String fileNumber = fileName.split("_")[1];
            fileNumber = fileNumber.substring(0, fileNumber.length() - 4);
            int i = Integer.parseInt(fileNumber);
            FileInputStream fstream = new FileInputStream(".\\data\\" + file.getName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((strLine = br.readLine()) != null)   {
                java.lang.String[] tokens = strLine.split(",");
                Date date = sdf.parse(tokens[0]);
                Millisecond time = new Millisecond(date);


                companies[i].serie.add(time, Float.parseFloat(tokens[1]));


            }
            in.close();
        }
    }
    private void loadAllFromOne() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d HH:mm:ss.SSS z");
        File dir = new File("./data");
        File[] foundFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(dateToday + ".txt");

            }
        });
        java.lang.String strLine;
        File file = foundFiles[0];
        FileInputStream fstream = new FileInputStream(".\\data\\" + file.getName());
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        while ((strLine = br.readLine()) != null)   {
            java.lang.String[] tokens = strLine.split(",");
            Date date = sdf.parse(tokens[0]);
            Millisecond time = new Millisecond(date);

            for(int i = 1; i <= num; ++i){
                companies[i-1].serie.add(time, Float.parseFloat(tokens[i]));
            }
        }
        in.close();
    }



    private void initUI() {
        mainFrame = new JFrame("Akcie");
        //mainFrame.setSize(768, (NUM*2+3)*35);
        mainFrame.setSize(WIDTH, HEIGHT);
        //mainFrame.setLayout(new GridLayout(NUM/2+3, 2));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                if(driver != null)
                    driver.quit();
                System.exit(0);
            }
        });

        /*mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                if(driver != null)
                    driver.quit();
                System.exit(0);
            }
        });*/

        wealthTF = new JTextField(wealth);
        wealthL = new JLabel("Wealth ");


        companies = new Company[num];
        companiesSettings = new CompanySetting[num];


        for(int i = 0; i < num; i++){
            companies[i] = new Company("Name " + i);
            companiesSettings[i] = new CompanySetting(checkBoxes[i], links[i], lowerBounds[i], upperBounds[i]);
        }

        control = new JPanel();
        control.setLayout(new FlowLayout());


        view = new JPanel();
        //view.setSize(1920,900);
        view.setPreferredSize(new Dimension(0,(int)Math.ceil(num/(double)COLUMNS)*ROW_HEIGHT));
        view.setLayout(new GridLayout((int)Math.ceil(num/(double)COLUMNS), COLUMNS));
        //System.out.println((int)Math.ceil(num/(double)COLUMNS));
        view.setVisible(true);



        scroll = new JScrollPane(view, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scroll.setSize(1920,900);
        //scroll.setPreferredSize(new Dimension(0,900));
        scroll.setLayout(new ScrollPaneLayout());
        scroll.setVisible(true);


        historyPane = new History(num);


        for(int i = 0; i < num; i++){
            view.add(companies[i]);
        }

        JTabbedPane tabbedPane = new JTabbedPane();

        JSplitPane splitPane = new JSplitPane();
        splitPane.setSize(WIDTH, HEIGHT);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(TOP_HEIGHT);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(scroll);
        splitPane.setBottomComponent(control);




        tabbedPane.addTab("Charts", splitPane);
        tabbedPane.addTab("History", historyPane);

        mainFrame.add(tabbedPane);
        mainFrame.setVisible(true);
    }

    private void displayFrame() {
        startButton = new JButton("Start");
        startButton.setEnabled(true);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                //changeGlobalVars();
                loop = true;
                thread = r.new Thr(); // should add in an executor
                thread.start();
            }
        });

        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
                loop = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                driver.quit();

            }
        });

        settingsButton = new JButton("Settings");
        settingsButton.setEnabled(true);
        settingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Settings.createFrame(companiesSettings, links, lowerBounds, upperBounds, checkBoxes);
            }
        });


        usa.setText("USA:");
        ger.setText("GER:");
        /*usaValue.setEnabled(false);
        gerValue.setEnabled(false);
        usaValue.setEditable(false);
        gerValue.setEditable(false);*/
        usaValue.setColumns(5);
        gerValue.setColumns(5);

        control.add(usa);
        control.add(usaValue);
        control.add(ger);
        control.add(gerValue);

        control.add(startButton);
        control.add(stopButton);
        control.add(settingsButton);



        mainFrame.setVisible(true);
        mainFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);


        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        //changeGlobalVars();
        loop = true;
        thread = r.new Thr(); // should add in an executor
        thread.start();

    }


    protected synchronized void play(Clip clip){

        try{
            if(clip.isOpen()){
                clip.start();
                clip.setMicrosecondPosition(0);     //like rewinding the clip
                try{
                    Thread.sleep(10);
                }
                catch (Exception e){}
            }
        }
        catch(Exception e){e.printStackTrace();}
    }

    public class Thr extends Thread {

        @Override
        public void run() {

            try {
                if(load){
                    loadDataFiles();
                    //loadAllFromOne();
                    load = false;
                }
            } catch (IOException e) {
                System.out.println("Error while loading data for charts.");
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }


            Capabilities caps = new DesiredCapabilities();
            ((DesiredCapabilities) caps).setJavascriptEnabled(true);
            ((DesiredCapabilities) caps).setCapability("takesScreenshot", true);
            ((DesiredCapabilities) caps).setCapability(
                    PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    "C:\\PhantomJS\\bin\\phantomjs.exe"
            );



           // File pathToBinary = new File("C:\\Program Files\\Mozilla Firefox\\firefox.exe");
           //FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
           // FirefoxProfile firefoxProfile = new FirefoxProfile();
            //driver = new FirefoxDriver(ffBinary,firefoxProfile);

           driver = new PhantomJSDriver(caps);

            driver.get(links[0]);
            stockNames[0] = driver.findElement(By.xpath("//span[starts-with(@class, 'stock-headline')]")).getText();
            companies[0].renameChart(stockNames[0]);
            historyPane.renameChart(0, stockNames[0]);
            List<java.lang.String> knownHandles = new ArrayList<java.lang.String>();
            knownHandles.add(driver.getWindowHandle());

            for(int i = 1; i < num; i++){
                ((JavascriptExecutor)driver).executeScript("window.open();");
                // find the new handle. we are getting a set
                for (java.lang.String handle : driver.getWindowHandles()) {
                    if (!knownHandles.contains(handle)) {
                        knownHandles.add(handle);
                        break;
                    }
                }
                java.lang.String newHandle = knownHandles.get(knownHandles.size() -1 );   //last added handle
                driver.switchTo().window(newHandle);
                driver.get(links[i]);
                stockNames[i] = driver.findElement(By.xpath("//span[starts-with(@class, 'stock-headline')]")).getText();
                companies[i].renameChart(stockNames[i]);
                historyPane.renameChart(i, stockNames[i]);

            }

            ((JavascriptExecutor)driver).executeScript("window.open();");
            // find the new handle. we are getting a set
            for (java.lang.String handle : driver.getWindowHandles()) {
                if (!knownHandles.contains(handle)) {
                    knownHandles.add(handle);
                    break;
                }
            }
            java.lang.String marketsHandle = knownHandles.get(knownHandles.size() -1 );   //last added handle
            driver.switchTo().window(marketsHandle);
            //driver.get("https://www.markets.com/cs");
            //driver.get("//https://widget.markets.com/platformQuotes?brand=markets&key=11&size=10&refresh_rate=3000&language=cs&iframe_height=430&iframe_width=317&brandless=true&skin_file=https://widget.markets.com/files/quotes/css/markets.css&symbol_search_url=https://www.markets.com/cs/instruments/&platform_link=https://www.markets.com/cs/real-registration2&uid=4fc6df60-9916-60f9-e254-e93321406fec\"");

      //      driver.get("https://www.markets.com/cs/instruments/germany30?from_search=2");
            /*try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            //////String gerStr = driver.findElement(By.xpath("//div[starts-with(@class, 'instrument-value')]/div[starts-with(@class, 'percentage')]")).getText();

    //        driver.get("https://www.markets.com/cs/instruments/usa30?from_search=2");
            /*try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            //////String usaStr = driver.findElement(By.xpath("//div[starts-with(@class, 'instrument-value')]/div[starts-with(@class, 'percentage')]")).getText();
            //usaValue.setText(usaStr);
            //gerValue.setText(gerStr);

            usaValue.setText("0");
            gerValue.setText("0");


            /*if(Float.parseFloat(usaStr.substring(0, usaStr.length() - 1)) >= 0){
                usaValue.setForeground(Color.GREEN);
            }
            else {
                usaValue.setForeground(Color.RED);
            }
            if(Float.parseFloat(gerStr.substring(0, gerStr.length() - 1)) >= 0){
                gerValue.setForeground(Color.GREEN);
            }
            else {
                gerValue.setForeground(Color.RED);
            }*/

            ///http://stackoverflow.com/questions/17631161/how-to-handle-same-multiple-windows-e-g-google-in-selenium-webdriver-with-java


            //open files to append
            for(int i = 0; i < num; i++){
                String fileName = dateToday + "_" + Integer.toString(i) + ".txt";

                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new FileOutputStream(
                            new File(".\\data\\" + fileName),
                            true /* append = true */));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                writers.add(i, pw);
            }






            delay = 666;
            try {
                while(loop){
                    Date date = new Date(System.currentTimeMillis());
                    Millisecond timeNow = new Millisecond(date);

                    java.lang.String down = "";
                    java.lang.String up = "";
                    peep = false;
                    if(delay < 30){
                        delay++;
                    }
                    else if (delay == 30){
                        delay++;
                        for(int i = 0; i < num; i++) {
                            companiesSettings[i].price.setBackground(Color.WHITE);
                        }
                    }
                    else{
                        delay++;
                    }
                    PrintWriter mW = null;
                    try {
                        mW = new PrintWriter(new FileOutputStream(
                                new File(".\\data\\" + dateToday + ".txt"),
                                true));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("y-M-d HH:mm:ss.SSS z");
                    mW.append(sdf.format(date));
                    for(int i = 0; i < num; i++){
                        java.lang.String newHandle = knownHandles.get(i);
                        driver.switchTo().window(newHandle);
                        java.lang.String priceStr = driver.findElement(By.xpath("//table[starts-with(@class, 'table no-header')]/tbody/tr[3]/td[1]/div[2]")).getAttribute("jsvalue");
                        /*try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        companiesSettings[i].setPrice(priceStr);
                        priceStr = priceStr.replace(",", ".");
                        float price = Float.parseFloat(priceStr);

                        companies[i].serie.add(timeNow, price);

                        String fileName = dateToday + "_" + Integer.toString(i) + ".txt";
                        PrintWriter pw = null;
                        try {
                            pw = new PrintWriter(new FileOutputStream(
                                    new File(".\\data\\" + fileName),
                                    true));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }


                        pw.append(sdf.format(date) + "," + priceStr + System.lineSeparator());
                        pw.close();
                        //udelat jen jeden soubor, kde v prvnim sloupci bude cas, v dalsich 16 pak hodnoty
                        mW.append("," + priceStr);




                        if(companiesSettings[i].getCheck() && price > companiesSettings[i].getUpperBound()){
                            //up += i + " ";
                            if(delay >= 60){
                                companiesSettings[i].price.setBackground(Color.GREEN);
                                peep = true;
                            }
                        }
                        if(companiesSettings[i].getCheck() && price < companiesSettings[i].getLowerBound()){
                            //down += i + " ";
                            if(delay >= 60){
                                companiesSettings[i].price.setBackground(Color.RED);
                                peep = true;
                            }
                        }
                    }

                    java.lang.String newHandle = knownHandles.get(num);
                    driver.switchTo().window(newHandle);
    //                driver.get("https://www.markets.com/cs/instruments/germany30?from_search=2");
                    /*try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    //////gerStr = driver.findElement(By.xpath("//div[starts-with(@class, 'instrument-value')]/div[starts-with(@class, 'percentage')]")).getText();

      //              driver.get("https://www.markets.com/cs/instruments/usa30?from_search=2");
                    /*try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    //////usaStr = driver.findElement(By.xpath("//div[starts-with(@class, 'instrument-value')]/div[starts-with(@class, 'percentage')]")).getText();
                    //usaValue.setText(usaStr);
                    //gerValue.setText(gerStr);

                    usaValue.setText("0");
                    gerValue.setText("0");

    /*                if(Float.parseFloat(usaStr.substring(0, usaStr.length() - 1)) >= 0){
                        usaValue.setForeground(Color.GREEN);
                    }
                    else {
                        usaValue.setForeground(Color.RED);
                    }
                    if(Float.parseFloat(gerStr.substring(0, gerStr.length() - 1)) >= 0){
                        gerValue.setForeground(Color.GREEN);
                    }
                    else {
                        gerValue.setForeground(Color.RED);
                    }*/


                    mW.append(System.lineSeparator());
                    mW.close();
                    if(peep){
                        ///play sound
                        AudioInputStream inputStream = null;
                        try {
                            inputStream = AudioSystem.getAudioInputStream(new File("C:\\Windows\\Media\\tada.wav"));
                        } catch (UnsupportedAudioFileException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Clip clip = null;
                        try {
                            clip = AudioSystem.getClip();
                        } catch (LineUnavailableException e) {
                            e.printStackTrace();
                        }
                        try {
                            clip.open(inputStream);
                        } catch (LineUnavailableException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        play(clip);

                        delay = 0;
                    }
                    if(down != ""){
                        JOptionPane.showMessageDialog(mainFrame,
                                down + "prices are under lower bounds.",
                                "WARNING",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    if(up != ""){
                        JOptionPane.showMessageDialog(mainFrame,
                                up + "prices are over upper bounds.",
                                "WARNING",
                                JOptionPane.WARNING_MESSAGE);
                    }

                    try {
                        Thread.sleep(1000); //wait 1 second before reading again
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                /*JOptionPane.showMessageDialog(mainFrame,
                        "Zase ti to spadlo.",
                        "WARNING",
                        JOptionPane.WARNING_MESSAGE);*/
                /*startButton.setEnabled(true);
                stopButton.setEnabled(false);

                driver.quit();


                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                //changeGlobalVars();
                loop = true;*/
                thread = r.new Thr(); // should add in an executor
                thread.start();
            }
            /*for(int i = 0; i < num; i++){
                writers.get(i).close();
            }*/
        }
    }
    static Thr thread = null;

    public static void main(java.lang.String[] args) {
        dateToday = getDateString();
        loadConfigFile();
        r = new Runner();
        r.displayFrame();
    }

}