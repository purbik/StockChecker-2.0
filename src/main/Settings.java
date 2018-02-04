package main;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Alena on 26. 5. 2017.
 */





public class Settings {
    private static void changeGlobalVars(CompanySetting[] companiesSettings, java.lang.String[] links, float[] lowerBounds, float[] upperBounds, Boolean[] checkBoxes) {
        int num = companiesSettings.length;
        for(int i = 0; i < num; i++){
                checkBoxes[i] = companiesSettings[i].getCheck();
                links[i] = companiesSettings[i].getLink();
                lowerBounds[i] = companiesSettings[i].getLowerBound();
                upperBounds[i] = companiesSettings[i].getUpperBound();
        }
    }
    //tady asi staci ukladat hodnoty primo z objektu takze misto checkBoxes[i] pouzit companiesSetting[i].check
    private static void saveCurrentConfig(CompanySetting[] companiesSettings, java.lang.String[] links, float[] lowerBounds, float[] upperBounds, Boolean[] checkBoxes) {
        int num = companiesSettings.length;
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

    public static void createFrame(CompanySetting[] companiesSettings, java.lang.String[] links, float[] lowerBounds, float[] upperBounds, Boolean[] checkBoxes)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JFrame frame = new JFrame("Settings");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setOpaque(true);

                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new FlowLayout());

                JPanel settingsPanel = new JPanel();
                settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
                settingsPanel.setOpaque(true);

                for(int i = 0; i < companiesSettings.length; ++i){
                    settingsPanel.add(companiesSettings[i]);
                }


                JButton button = new JButton("Save settings");
                button.setEnabled(true);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        changeGlobalVars(companiesSettings, links, lowerBounds, upperBounds, checkBoxes);
                        saveCurrentConfig(companiesSettings, links, lowerBounds, upperBounds, checkBoxes);
                    }
                });
                buttonPanel.add(button);
                panel.add(settingsPanel);
                panel.add(buttonPanel);
                frame.getContentPane().add(BorderLayout.CENTER, panel);
                frame.pack();
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
                frame.setResizable(false);
            }
        });
    }
}
