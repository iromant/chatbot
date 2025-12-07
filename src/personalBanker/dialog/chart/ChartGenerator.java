package personalBanker.dialog.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

public class ChartGenerator {

    public static InputStream generatePieChart(Map<String, Double> data, String title) {
        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                if (entry.getValue() > 0) {
                    dataset.setValue(entry.getKey(), entry.getValue());
                }
            }

            if (dataset.getItemCount() == 0) return null;

            JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            plot.setNoDataMessage("Нет данных");
            plot.setCircular(true);
            plot.setLabelGap(0.02);

            Color[] colors = {
                    new Color(255, 99, 132), new Color(54, 162, 235),
                    new Color(255, 206, 86), new Color(75, 192, 192),
                    new Color(153, 102, 255)
            };

            int i = 0;
            for (Object key : dataset.getKeys()) {
                plot.setSectionPaint(key.toString(), colors[i % colors.length]);
                i++;
            }

            BufferedImage image = chart.createBufferedImage(800, 600);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}