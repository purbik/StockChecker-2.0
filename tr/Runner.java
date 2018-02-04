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

import main.Company;
import main.CompanySetting;

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

    static Runner r;
    public static final int MAX_NUM = 39;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int TOP_HEIGHT = 975;
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
    //private JLabel headL;
    private JButton startButton;
    private JButton stopButton;
    private JButton settingsButton;
    private JButton saveSettingsButton;



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
    private JPanel settings;
    private JPanel constants;
    private JPanel control;
    private JPanel view;
    private JScrollPane scroll;


    List<PrintWriter> writers = new ArrayList<PrintWriter>();



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
            checkBoxes[i] = companies[i].getCheck();
            links[i] = companies[i].getLink();
            lowerBounds[i] = companies[i].getLowerBound();
            upperBounds[i] = companies[i].getUpperBound();
        }
    }
    //DONE
    private static void saveCurrentConfig(int num) {
        try{
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("config.txt"), "utf-8"));
            System.out.println(num);
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
            String fileName = file.getName();
            String fileNumber = fileName.split("_")[1];
            fileNumber = fileNumber.substring(0, fileNumber.length() - 4);
            System.out.println(fileNumber);
            int i = Integer.parseInt(fileNumber);
            FileInputStream fstream = new FileInputStream(".\\data\\" + file.getName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((strLine = br.readLine()) != null)   {
                java.lang.String[] tokens = strLine.split(",");
                System.out.println(strLine);
                Date date = sdf.parse(tokens[0]);
                Millisecond time = new Millisecond(date);


                companies[i].serie.add(time, Float.parseFloat(tokens[1]));


            }
            in.close();
        }
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

        wealthTF = new JTextField(wealth);
        wealthL = new JLabel("Wealth ");
        //headL = new JLabel("                                      link to stock page                                              min               max            value");





        companies = new Company[num];
        companiesSettings = new CompanySetting[num];


        for(int i = 0; i < num; i++){
            companies[i] = new Company(checkBoxes[i], links[i], lowerBounds[i], upperBounds[i], "Name " + i);
            companiesSettings[i] = new CompanySetting(checkBoxes[i], links[i], lowerBounds[i], upperBounds[i], "Name " + i);
        }

        control = new JPanel();
        control.setLayout(new FlowLayout());
        constants = new JPanel();
        constants.setLayout(new FlowLayout());

        settings = new JPanel();
        settings.setSize(WIDTH,HEIGHT-TOP_HEIGHT);
        settings.setLayout(new GridLayout(2,1));
        //settings.add(constants);
        settings.add(control);
        settings.setVisible(true);

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


        JPanel settingsPane = new JPanel();

        for(int i = 0; i < num; i++){
            view.add(companies[i]);
            settingsPane.add(companiesSettings[i]);
        }

        JTabbedPane tabbedPane = new JTabbedPane();

        JSplitPane splitPane = new JSplitPane();
        splitPane.setSize(WIDTH, HEIGHT);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(TOP_HEIGHT);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(scroll);
        splitPane.setBottomComponent(settings);




        tabbedPane.addTab("Charts", splitPane);
        tabbedPane.addTab("Settings", settingsPane);
        mainFrame.add(tabbedPane);
        //mainFrame.add(splitPane);
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
                //otevrit dialog pro zmenu
            }
        });

        saveSettingsButton = new JButton("Save settings");
        saveSettingsButton.setEnabled(true);
        saveSettingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeGlobalVars(num);
                saveCurrentConfig(num);
            }
        });

        /*for(int i = 0; i < num; i++){
            settings.add(items[i]);
        }*/
        //head.add(headL);
        constants.add(wealthL);
        constants.add(wealthTF);
        control.add(startButton);
        control.add(stopButton);
        control.add(settingsButton);
        control.add(saveSettingsButton);
        mainFrame.setVisible(true);
        mainFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);


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
                loadDataFiles();
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



            //File pathToBinary = new File("C:\\Program Files\\Mozilla Firefox\\firefox.exe");
            //FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
            //FirefoxProfile firefoxProfile = new FirefoxProfile();
            //driver = new FirefoxDriver(ffBinary,firefoxProfile);

            driver = new PhantomJSDriver(caps);

            driver.get(links[0]);
            stockNames[0] = driver.findElement(By.xpath("//span[starts-with(@class, 'stock-headline')]")).getText();
            companies[0].renameChart(stockNames[0]);
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
            }

            ///http://stackoverflow.com/questions/17631161/how-to-handle-same-multiple-windows-e-g-google-in-selenium-webdriver-with-java


            //open files to append

            for(int i = 0; i < num; i++){
                String fileName = dateToday + "_" + Integer.toString(i) + ".txt";

                PrintWriter w = null;
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new FileOutputStream(
                            new File(".\\data\\" + fileName),
                            true /* append = true */));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                /*try {
                    w = new PrintWriter(".\\data\\" + fileName, "UTF-8");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/
                writers.add(i, pw);
            }






            delay = 666;
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
                        companies[i].price.setBackground(Color.WHITE);
                    }
                }
                else{
                    delay++;
                }
                for(int i = 0; i < num; i++){
                    java.lang.String newHandle = knownHandles.get(i);   //last added handle
                    driver.switchTo().window(newHandle);
                    java.lang.String priceStr = driver.findElement(By.xpath("//table[starts-with(@class, 'table no-header')]/tbody/tr[3]/td[1]/div[2]")).getAttribute("jsvalue");
                    /*try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    companies[i].setPrice(priceStr);
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

                    SimpleDateFormat sdf = new SimpleDateFormat("y-M-d HH:mm:ss.SSS z");

                    pw.append(sdf.format(date) + "," + priceStr + System.lineSeparator());
                    pw.close();




                    if(companies[i].getCheck() && price > companies[i].getUpperBound()){
                        //up += i + " ";
                        if(delay >= 60){
                            companies[i].price.setBackground(Color.GREEN);
                            peep = true;
                        }
                    }
                    if(companies[i].getCheck() && price < companies[i].getLowerBound()){
                        //down += i + " ";
                        if(delay >= 60){
                            companies[i].price.setBackground(Color.RED);
                            peep = true;
                        }
                    }
                }
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
                            down + "prices are under respective lower bounds.",
                            "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                }
                if(up != ""){
                    JOptionPane.showMessageDialog(mainFrame,
                            up + "prices are over respective upper bounds.",
                            "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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