package com.youngdream.fopthymeleaf.utils;

import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * 图表工具类
 */
public class ChartUtil {
    private ChartUtil() {
    }

    // 1. 加载项目 static 目录下的字体文件
    private static final String ALIBABA_PUHUITI_REGULAR_FONT_PATH = "/fonts/Alibaba-PuHuiTi-Regular.ttf";

    private static JFreeChart CHART_INSTANCE;

    private static Font ALIBABA_PUHUITI;

    // 类加载时初始化
    static {
        initChartDemo();
    }

    private static void initChartDemo() {
        InputStream fontStream = ChartUtil.class.getResourceAsStream(ALIBABA_PUHUITI_REGULAR_FONT_PATH);
        if (fontStream == null) {
            throw new RuntimeException("字体文件未找到: " + ALIBABA_PUHUITI_REGULAR_FONT_PATH);
        }
        // 默认使用宋体
        ALIBABA_PUHUITI = new Font("宋体", Font.PLAIN, 12);
        // 读取字体文件并注册字体到系统
        try {
            ALIBABA_PUHUITI = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, 12);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(ALIBABA_PUHUITI);
        } catch (Exception e) {
            System.err.println("加载自定义字体异常: " + e.getMessage());
        }
        // 创建数据集
        XYSeriesCollection dataset = new XYSeriesCollection();
        // 第一条折线（CPU使用率）
        XYSeries cpuSeries = new XYSeries("CPU使用率 (%)");
        cpuSeries.add(1, 20);
        cpuSeries.add(2, 45);
        cpuSeries.add(3, 32);
        cpuSeries.add(4, 52);
        cpuSeries.add(5, 28);
        dataset.addSeries(cpuSeries);
        // 第二条折线（内存使用率）
        XYSeries memorySeries = new XYSeries("内存使用率 (%)");
        memorySeries.add(1, 35);
        memorySeries.add(2, 50);
        memorySeries.add(3, 48);
        memorySeries.add(4, 65);
        memorySeries.add(5, 45);
        dataset.addSeries(memorySeries);
        // 生成折线图
        JFreeChart chart = ChartFactory.createXYLineChart(
                "系统监控-2025", // 标题
                "活动时间",           // X轴标签
                "使用率",      // Y轴标签
                dataset,                 // 数据集
                PlotOrientation.VERTICAL,
                true,   // 显示图例
                true,   // 显示工具提示
                false   // 不生成URL链接
        );
        // 1.全局抗锯齿设置（改善渲染效果）
        chart.setTextAntiAlias(true);
        chart.setAntiAlias(true);
        // 2. 设置图表字体
        chart.getTitle().setFont(new Font(ALIBABA_PUHUITI.getName(), Font.BOLD, 18)); // 设置标题字体
        chart.getLegend().setItemFont(new Font(ALIBABA_PUHUITI.getName(), Font.BOLD, 15)); // 设置图例字体
        chart.getXYPlot().getDomainAxis().setTickLabelFont(new Font(ALIBABA_PUHUITI.getName(), Font.BOLD, 15)); // 设置X轴刻度字体
        chart.getXYPlot().getDomainAxis().setLabelFont(new Font(ALIBABA_PUHUITI.getName(), Font.BOLD, 15));
        chart.getXYPlot().getRangeAxis().setTickLabelFont(new Font(ALIBABA_PUHUITI.getName(), Font.BOLD, 15)); // 设置Y轴刻度字体
        chart.getXYPlot().getRangeAxis().setLabelFont(new Font(ALIBABA_PUHUITI.getName(), Font.BOLD, 15));
        // 3. 设置图表背景半透明
        chart.setBackgroundPaint(null);
        // 4. 获取绘图区对象
        XYPlot plot = chart.getXYPlot();
        // 5. 设置绘图区背景透明
        plot.setBackgroundPaint(null);
        // 6. 设置绘图区前景色透明度
        plot.setForegroundAlpha(1f);
        // 7. 设置网格线颜色为透明
        plot.setDomainGridlinePaint(new Color(219, 253, 1));
        plot.setRangeGridlinePaint(new Color(219, 253, 1));
        // 8. 设置渲染器，调整线条和数据点样式

        // 获取渲染器并设置（自定义标签背景（防止文字重叠））
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
            @Override
            public void drawItem(Graphics2D g2, XYItemRendererState state,
                                 Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
                                 ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
                                 int series, int item, CrosshairState crosshairState, int pass) {
                super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
                        dataset, series, item, crosshairState, pass);
            }
        };
        // 设置标签生成器
        renderer.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator(
                StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT,
                new DecimalFormat("0"),  // X值格式
                new DecimalFormat("0'%'") // Y值格式（添加单位）
        ));
        // 启用标签显示
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(Color.BLACK);   // 标签颜色
        renderer.setDefaultItemLabelFont(new Font(ALIBABA_PUHUITI.getName(), Font.BOLD, 18)); // 字体
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER  // 标签位置（数据点上方）
        ));

        // 第一条线样式（CPU）
        renderer.setSeriesPaint(0, new Color(255, 0, 0));   // 红色
        renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // 线宽
        renderer.setSeriesShapesVisible(0, true);           // 显示数据点
        // 第二条线样式（内存）
        renderer.setSeriesPaint(1, new Color(0, 0, 255));   // 蓝色
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(1, true);

        renderer.setDefaultShapesVisible(true); // 显示数据点
        renderer.setDefaultShapesFilled(true);
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(true);

        plot.setRenderer(renderer);
        CHART_INSTANCE = chart;
    }

    public static String getPngBase64Demo() {
        // 输出 png 格式 base64
        String dataUri = null;
        try {
            int dpi = 200; // 尺寸为4英寸 x 3英寸
            int width = 4 * dpi;
            int height = 3 * dpi;
            // ChartUtils.saveChartAsPNG(new File("xxx.png"), chart, width, height);
            BufferedImage chartImage = CHART_INSTANCE.createBufferedImage(width, height);
            String base64String = Base64Util.encodeImage(chartImage);
            dataUri = Base64Util.DEFAULT_BASE64_PNG_PRIFIX + base64String;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataUri;
    }

    public static String getCleanSVGDemo() {
        // 创建 SVG 图形
        DOMImplementation domImpl = null;
        try {
            domImpl = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.setFont(ALIBABA_PUHUITI);

        // SVG 画布尺寸
        int width = 800;
        int height = 600;
        svgGenerator.setSVGCanvasSize(new Dimension(width, height));
        // 将图表绘制到 SVG 画布
        CHART_INSTANCE.draw(svgGenerator, new Rectangle(width, height));
        // 获取 <svg> 根节点
        Element root = svgGenerator.getRoot();
        // 提取 <svg> 内部的所有子节点
        String innerSVG = extractInnerXML(root);
        svgGenerator.dispose();
        // 去掉重复的<xml>标签
        return innerSVG.replaceAll("<\\?xml.*?>", "");
    }

    // 解析 <svg> 标签的子节点内容
    private static String extractInnerXML(Element svgRoot) {
        // 使用 LSSerializer 将子节点序列化为字符串
        DOMImplementationLS domImplementationLS = (DOMImplementationLS) svgRoot.getOwnerDocument().getImplementation();
        LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("format-pretty-print", true); // 格式化输出

        StringBuilder innerContent = new StringBuilder();
        NodeList children = svgRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            innerContent.append(lsSerializer.writeToString(child)); // 将子节点转换为字符串
        }
        return innerContent.toString();
    }

}
