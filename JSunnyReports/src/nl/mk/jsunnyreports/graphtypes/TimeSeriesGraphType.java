package nl.mk.jsunnyreports.graphtypes;

import java.awt.BasicStroke;
import java.awt.Color;

import java.awt.geom.Rectangle2D;

import java.text.DecimalFormat;

import java.util.Calendar;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;

import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * TimeSeriesGraphType.java
 *
 * note: Still a lot of work to be done here ( This class is one huge mess )
 *
 *
 * @author  JFreechart 
 * @author  Martin Kleinman
 * @version 2.0.5
 * @since   0.0.0.0
 */
public class TimeSeriesGraphType {
    private static final Logger log = Logger.getLogger(TimeSeriesGraphType.class);

    public TimeSeriesGraphType(Settings settings) {
        ImageEncoderFactory.setImageEncoder("png", "org.jfree.chart.encoders.KeypointPNGEncoderAdapter");
        this.settings = settings;
        init();
    }

    private float installSize;
    private JFreeChart chart;
    private final Settings settings;

    private TimeSeriesCollection timeSeriesWattCollection;

    /** Constants **/
    private static final String STYLE_LINE = "line";
    private static final String STYLE_DASH = "dash";
    private static final String STYLE_DOT = "dot";

    public void init() {
        timeSeriesWattCollection = new TimeSeriesCollection();
    }

    /**
     *
     * @param style
     * @param lineWidth
     * @return
     */
    private BasicStroke toStroke(String style, float lineWidth) {
        BasicStroke result = null;
        if (style != null) {
            float dash[] = { 5.0f };
            float dot[] = { lineWidth };

            if (style.equalsIgnoreCase(STYLE_LINE)) {
                result = new BasicStroke(lineWidth);
            } else if (style.equalsIgnoreCase(STYLE_DASH)) {
                result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
            } else if (style.equalsIgnoreCase(STYLE_DOT)) {
                result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
            }
        } //else: input unavailable
        return result;
    }

    /**
     *
     * 11-05-2010 MK : Added xyir.setSeriesShape(seriesIndex, new Rectangle2D.Double(-1, -1, 1, 1)); to force a shape for the line, improves graph
     *                 when spline is enabled.
     *
     * @param seriesIndex
     * @param color
     */
    public void setLineColor(int seriesIndex, Color color) {
        Plot plot = chart.getPlot();

        if (plot instanceof CategoryPlot) {
            CategoryPlot categoryPlot = chart.getCategoryPlot();
            CategoryItemRenderer cir = categoryPlot.getRenderer();
            try {
                cir.setSeriesPaint(seriesIndex, color); //series line style
            } catch (Exception e) {
                log.error("Error setting color for series '" + seriesIndex + "' of chart '" + chart + "': " + e);
            }
        } else if (plot instanceof XYPlot) {
            XYPlot xyPlot = chart.getXYPlot();
            XYItemRenderer xyir = xyPlot.getRenderer();
            try {
                xyir.setSeriesPaint(seriesIndex, color); //series line style
                xyir.setSeriesShape(seriesIndex, new Rectangle2D.Double(-1, -1, 1, 1));
            } catch (Exception e) {
                log.error("Error setting color for series '" + seriesIndex + "' of chart '" + chart + "': " + e);
            }
        } else {
            log.error("setLineColor() unsupported plot: " + plot);
        }
    }

    /**
     *
     * @param seriesIndex
     * @param red
     * @param green
     * @param blue
     */
    public void setLineColor(int seriesIndex, int red, int green, int blue) {
        Color newColor = new Color(red, green, blue);
        setLineColor(seriesIndex, newColor);
    }

    /**
     * set a STYLE_TYPE for a certain dataset (seriesIndex ) with a certain line width.
     *
     * @param seriesIndex
     * @param style
     * @param lineWidth
     */
    public void setLineStyle(int seriesIndex, String style, float lineWidth) {
        if (chart != null && style != null) {
            BasicStroke stroke = toStroke(style, lineWidth);

            Plot plot = chart.getPlot();

            if (plot instanceof CategoryPlot) {
                CategoryPlot categoryPlot = chart.getCategoryPlot();
                CategoryItemRenderer cir = categoryPlot.getRenderer();
                try {
                    cir.setSeriesStroke(seriesIndex, stroke); //series line style
                } catch (Exception e) {
                    log.error("Error setting style '" + style + "' for series '" + seriesIndex + "' of chart '" + chart + "': " + e);
                }
            } else if (plot instanceof XYPlot) {
                XYPlot xyPlot = chart.getXYPlot();
                XYItemRenderer xyir = xyPlot.getRenderer();

                try {
                    xyir.setSeriesStroke(seriesIndex, stroke); //series line style
                } catch (Exception e) {
                    log.error("Error setting style '" + style + "' for series '" + seriesIndex + "' of chart '" + chart + "': " + e);
                }
            } else {
                log.error("setSeriesColor() unsupported plot: " + plot);
            }
        }
    }


    /**
     *
     * @param color
     */
    public void setBackgroundColor(Color color) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(color);
    }

    /**
     *
     * @param format
     */
    public void setYaxisFormat(String format) {
        XYPlot plot = chart.getXYPlot();
        NumberAxis rangeAxis1 = (NumberAxis)plot.getRangeAxis();
        DecimalFormat decimalFormat = new DecimalFormat(format);
        rangeAxis1.setNumberFormatOverride(decimalFormat);
    }

    /**
     *
     *
     * @param splineEnabled
     */
    public void setSpline(boolean splineEnabled) {
        XYPlot plot = chart.getXYPlot();

        if (splineEnabled) {
            XYSplineRenderer xys = new XYSplineRenderer();
            plot.setRenderer(xys);

        } else {
            plot.setRenderer(new XYLineAndShapeRenderer());

        }
    }

    /**
     *
     *
     */
    public void setSpline() {
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new XYSplineRenderer());
    }

    public void addSerie(String seriesName) {
        TimeSeries newSerie = new TimeSeries(seriesName);
        timeSeriesWattCollection.addSeries(newSerie);
    }

    /**
     * Gets the right TimeSeriesCollection, if it does not exist it will add a new one.
     *
     * @param seriesName Seriesname to get and return
     * @return TimeSeries
     */
    public TimeSeries getTimeSerieByName(String seriesName) {
        if (timeSeriesWattCollection.getSeries(seriesName) == null) {
            timeSeriesWattCollection.addSeries(new TimeSeries(seriesName));
        }
        return timeSeriesWattCollection.getSeries(seriesName);
    }

    /**
     *
     * 11-04-2010 MK : Updated add() method to addOrUpdate(). Hopefully partially fixed timezone issues and failed graphs.
     *
     *
     * @param seriesName Name of the graphSerie
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @param seriesValue the value to add for this seriesName
     */
    public void addTimeEntry(String seriesName, int year, int month, int day, int hour, int minute, int second, float seriesValue) {

        int yearNum = year;
        int monthNum = month;
        int dayNum = day;

        Second s = null;
        s = new Second(second, minute, hour, dayNum, monthNum, yearNum);

        // hopefully with this construction we avoid the addorupdate exception.
        try {
            // and add the value to the timeSerie found by the name.
            this.getTimeSerieByName(seriesName).addOrUpdate(s, seriesValue);

        } catch (Exception e) {
            log.error(e.getMessage());

        }
    }

    /**
     *
     * 11-04-2010 MK : Updated add() method to addOrUpdate(). Hopefully partially fixed timezone issues and failed graphs.
     *
     *
     * @param seriesName Name of the graphSerie
     * @param hour
     * @param minute
     * @param second
     * @param seriesValue The cssValueAttribute for this entry
     */
    public void addTimeEntry(String seriesName, int hour, int minute, int second, float seriesValue) {

        Calendar cal = Calendar.getInstance();
        int dayNum = cal.get(Calendar.DAY_OF_MONTH);
        int monthNum = cal.get(Calendar.MONTH) + 1;
        int yearNum = cal.get(Calendar.YEAR);
        
        Second s = null;
        
        s = new Second(second, minute, hour, dayNum, monthNum, yearNum);

        // hopefully with this construction we avoid the addorupdate exception.
        try {
            // and add the value to the timeSerie found by the name.
            this.getTimeSerieByName(seriesName).addOrUpdate(s, seriesValue);

        } catch (Exception e) {
            log.error(e.getMessage());

        }
    }


    /**
     *
     *
     * @param installSize
     */
    public void setInstallSize(float installSize) {
        this.installSize = installSize;
    }


    /**
     * Sets the upper bound for the left y-Axis column ( this usually is the power column in W ).
     *
     *
     * @param maxRange MaxRange value for the Y-Asis on the left ( Power column usually )
     */
    public void setMaxLeftAxis(int maxRange) {
        XYPlot plot = chart.getXYPlot();
        ValueAxis axis1 = plot.getRangeAxis(0);
        Double d = new Double(maxRange);
        axis1.setUpperBound(d);

    }

    /**
     * Sets the upper bound for the left y-Axis column ( this usually is the power column in W ).
     *
     *
     */
    public void setMinLeftAxis(int minRange) {
        XYPlot plot = chart.getXYPlot();
        ValueAxis axis1 = plot.getRangeAxis(0);
        Double d = new Double(minRange);
        axis1.setLowerBound(d);
    }

    public void enablePercentageAxis() {
        if (installSize > 0f) {
            double maxPercentage;

            XYPlot plot = chart.getXYPlot();
            ValueAxis axis1 = plot.getRangeAxis(0);
            Range axis1Range = axis1.getRange();

            maxPercentage = (axis1Range.getUpperBound() / (installSize)) * 100;
            ValueAxis axis2 = new NumberAxis("%");
            plot.setRangeAxis(1, axis2);
            axis2.setRange(0, maxPercentage);
        }
    }

    public void showXAxisTickLabels(boolean set) {
        XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setTickLabelsVisible(set);
    }

    public void setTitle(String title) {
        TextTitle subtitle1 = new TextTitle(title);
        chart.addSubtitle(subtitle1);
    }

    /**
     *
     * @param title
     * @param xAxisCaption
     * @param yAxisCaption
     */
    @SuppressWarnings("oracle.jdeveloper.java.semantic-warning")
    public void createChart(String title, String xAxisCaption, String yAxisCaption) {
        chart = ChartFactory.createTimeSeriesChart(title, xAxisCaption, yAxisCaption, timeSeriesWattCollection, true, true, false);
        this.setBackgroundColor( new Color( 240,240,240 ));

        this.setSpline();

        chart.setAntiAlias(true);

        for (int i = 0; i <= (timeSeriesWattCollection.getSeries().size() - 1); i++) {
            this.setLineStyle(i, this.STYLE_LINE, 2f);
        }
    }

    public JFreeChart getChart() {
        return chart;
    }
}

