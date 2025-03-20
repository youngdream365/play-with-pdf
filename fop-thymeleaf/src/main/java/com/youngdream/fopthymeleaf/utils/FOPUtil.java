package com.youngdream.fopthymeleaf.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.FopFactory;
import org.apache.xmlgraphics.util.MimeConstants;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Apache FOP 配置和导出
 *
 * @author coderush
 */
public class FOPUtil {
    private FOPUtil() {
    }

    // 默认 PDF 保存目录
    private static final String DEFAULT_RES_FOP_ABSOLUTE_DIR = "fop/";
    private static final String DEFAULT_XCONF_SUFFIX = ".xconf";
    private static final String DEFAULT_TEMPLATE_FO_SUFFIX = ".fo";
    private static final String DEFAULT_TEMPLATE_DEFAULT_PREFIX = "/fop/";
    private static final String DEFAULT_PDF_SUFFIX = ".pdf";

    private static final TemplateEngine TEMPLATE_ENGINE_INSTANCE = initThymeleaf();

    /**
     * 初始化模板引擎
     *
     * @return
     */
    private static TemplateEngine initThymeleaf() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setTemplateMode(TemplateMode.XML);
        templateResolver.setPrefix(DEFAULT_TEMPLATE_DEFAULT_PREFIX);
        templateResolver.setSuffix(DEFAULT_TEMPLATE_FO_SUFFIX);
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        return engine;
    }

    /**
     * thymeleaf 处理 XSL-FO 模板内容
     *
     * @param tplName XSL-FO 模板名称
     * @param model   填充对象模型
     * @return
     */
    private static String processXslFo(String tplName, Map<String, Object> model) {
        return TEMPLATE_ENGINE_INSTANCE.process(tplName, new Context(Locale.getDefault(), model));
    }

    /**
     * 生成 PDF
     *
     * @param model     模型数据
     * @param tplName   tpl模板文件名称（含后缀.fo）
     * @param xconfName xsl-fo配置文件名称（含后缀.xconf）
     * @param pdfPath   pdf保存路径名称（含后缀.pdf）
     */
    public static void generatePDF(HashMap<String, Object> model, String xconfName, String tplName, String pdfPath) {
        if (model == null || StringUtils.isAnyBlank(xconfName, tplName, pdfPath)) {
            System.err.println("传参数不能为空");
            return;
        }
        try {
            String xslFo = processXslFo(tplName, model);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 加载 xconf 配置文件
            File configFile = new File(FOPUtil.class.getClassLoader()
                    .getResource(DEFAULT_RES_FOP_ABSOLUTE_DIR + xconfName + DEFAULT_XCONF_SUFFIX).getFile()
            );

            DefaultHandler defaultHandler = FopFactory.newInstance(configFile)
                    .newFop(MimeConstants.MIME_PDF, out)
                    .getDefaultHandler();

            Source src = new StreamSource(new StringReader(xslFo));
            Result res = new SAXResult(defaultHandler);

            TransformerFactory.newInstance().newTransformer().transform(src, res);
            if (!pdfPath.endsWith(DEFAULT_PDF_SUFFIX)) {
                pdfPath += DEFAULT_PDF_SUFFIX;
            }
            BufferedOutputStream pdfOut = new BufferedOutputStream(new FileOutputStream(pdfPath));
            pdfOut.write(out.toByteArray());
        } catch (IOException | SAXException | TransformerException e) {
            throw new RuntimeException();
        }
    }

}
