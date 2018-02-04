package main;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


public class CompanySetting extends JPanel{
    public JCheckBox check;
    public JTextField link;
    public JTextField lowerBound;
    public JTextField upperBound;
    public JTextField price;


    public CompanySetting(Boolean tick, String url, float lB, float uB){
        this.setSize(WIDTH, HEIGHT);

        link = new JTextField(url);
        lowerBound = new JTextField(String.valueOf(lB));
        upperBound = new JTextField(String.valueOf(uB));
        price = new JTextField(String.valueOf(0));
        price.setEnabled(false);
        check = new JCheckBox();
        check.setEnabled(true);
        check.setSelected(tick);

        lowerBound.setColumns(4);
        upperBound.setColumns(4);
        price.setColumns(4);
        link.setColumns(20);

        this.setLayout(new GridBagLayout());
        this.add(check);
        this.add(link);
        this.add(lowerBound);
        this.add(upperBound);
        this.add(price);
        //DecimalFormat newFormat = new DecimalFormat("0.000");
    }



    public void setLink(String value){
        link.setText(value);
    }
    public String getLink(){
        return link.getText();
    }

    public void setLowerBound(float value){
        lowerBound.setText(String.valueOf(value));
    }
    public float getLowerBound(){
        return Float.parseFloat(lowerBound.getText());
    }

    public void setUpperBound(float value){
        upperBound.setText(String.valueOf(value));
    }
    public float getUpperBound(){
        return Float.parseFloat(upperBound.getText());
    }

    public void setPrice(String value){ price.setText(value); }
    public String getPrice(){ return price.getText(); }

    public void setCheck(Boolean tick){
        check.setSelected(tick);
    }
    public Boolean getCheck(){
        return check.isSelected();
    }
}
