/**
 * Created by Alena on 3. 3. 2017.
 */

package main;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;


public class Company extends JPanel{
    private static final int MAX_ITEM_COUNT = 1000000;
    public TimeSeries serie;
    public XYDataset dataset;
    public JFreeChart chart;
    public ChartPanel chartPanel;

    private JPanel setting;



    public Company(String name){
        this.setSize(WIDTH, HEIGHT);
        serie = new TimeSeries( "Serie " + name);
        serie.setMaximumItemCount(MAX_ITEM_COUNT);
        dataset = createEmptyDataset(serie);
        chart = createChart( "Chart " + name, dataset );

        //DecimalFormat newFormat = new DecimalFormat("0.000");

        chartPanel = new ChartPanel( chart );
        chartPanel.setMouseZoomable( true , false );
        this.setLayout(new GridLayout(1,1));
        this.add(chartPanel);
    }

    private XYDataset createEmptyDataset(TimeSeries s)
    {
        //s.add(current, 0 );
        return new TimeSeriesCollection(s);
    }

    private JFreeChart createChart(final String name, final XYDataset dataset )
    {
        return ChartFactory.createTimeSeriesChart(
                name,
                "time",
                "price",
                dataset,
                false,
                false,
                false);
    }

    public void renameChart(String name){
        chart.setTitle(name);
    }

}
