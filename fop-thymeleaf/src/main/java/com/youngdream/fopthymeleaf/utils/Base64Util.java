package com.youngdream.fopthymeleaf.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 实现字符串与 Base64 之间编码和解码
 *
 * @author coderush
 */
public class Base64Util {
    private Base64Util() {
    }

    // 默认图片格式
    public static final String DEFAULT_BASE64_PNG_PRIFIX = "data:image/png;base64,";
    public static final String DEFAULT_FORMAT_NAME= "png";

    /**
     * 编码：原文转 Base64
     */
    public String encode(String originalText) {
        if (originalText == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(originalText.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码：Base64 转原文
     */
    public String decode(String base64Data) {
        if (base64Data == null) {
            return null;
        }
        // 处理 Data URI 格式
        String pureBase64 = base64Data.split(",").length > 1
                ? base64Data.split(",")[1]
                : base64Data;
        byte[] decodedBytes = Base64.getDecoder().decode(pureBase64);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 编码：通过图片文件路径转换为 Base64
     */
    public String encodeImage(String imgFilePath) {
        try {
            byte[] imageData = Files.readAllBytes(Paths.get(imgFilePath));
            return Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            System.err.println("图片转 Base64 失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 编码：通过 File 对象转换为 Base64
     */
    public String encodeImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            return null;
        }
        try {
            byte[] imageData = Files.readAllBytes(imageFile.toPath());
            return Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            System.err.println("图片转 Base64 失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 将 BufferedImage 转换为 PNG 格式的 Base64 字符串
     * @param image 图片对象
     * @return Base64 字符串（不带 data: 前缀），失败返回 null
     */
    public static String encodeImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 尝试使用 PNG 格式编码
            if (!ImageIO.write(image, "PNG", baos)) {
                throw new IOException("未找到支持 PNG 格式的 ImageWriter");
            }
            // 转换为 Base64 字符串
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            System.err.println("图片转Base64失败: " + e.getMessage());
            return null;
        }
    }

    // 带格式参数的扩展方法
    public static String encodeImage(BufferedImage image, String formatName) {
        if (image == null || formatName == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, formatName, baos)) {
                throw new IOException("未找到支持 " + formatName + " 格式的 ImageWriter");
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            System.err.println(formatName + "转Base64失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将 Base64 字符串转换为 BufferedImage 对象
     *
     * @param base64Data 支持两种格式：
     *                   1. 纯Base64数据（如：iVBORw0KGgo...）
     *                   2. Data URI格式（如：data:image/png;base64,iVBORw0...）
     * @return 转换后的图片对象，失败返回 null
     */
    public static BufferedImage convertToImage(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }
        try {
            // 处理 Data URI 格式
            String pureBase64 = base64Data.split(",").length > 1
                    ? base64Data.split(",")[1]
                    : base64Data;

            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Base64转图片失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将 Base64 字符串保存为图片文件
     *
     * @param base64Data Base64数据
     * @param outputPath 输出路径（如：/path/to/image.png）
     * @param formatName 图片格式（png/jpg等）
     * @return 是否保存成功
     */
    public static boolean saveToFile(String base64Data, String outputPath, String formatName) {
        BufferedImage image = convertToImage(base64Data);
        if (image == null) {
            return false;
        }
        try {
            File outputFile = new File(outputPath);
            // 自动创建父目录
            if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                System.err.println("无法创建目录: " + outputFile.getParent());
                return false;
            }
            return ImageIO.write(image, formatName, outputFile);
        } catch (IOException e) {
            System.err.println("图片保存失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 自动识别格式的保存方法（从Data URI或文件扩展名获取格式）
     *
     * @param base64Data Base64数据
     * @param outputPath 带扩展名的输出路径（如：/path/to/image.jpg）
     * @return 是否保存成功
     */
    public static boolean saveToFileAutoFormat(String base64Data, String outputPath) {
        // 从输出路径获取格式
        String format = outputPath.substring(outputPath.lastIndexOf(".") + 1);
        // 尝试从Data URI获取格式
        if (base64Data.startsWith("data:image/")) {
            String mimeType = base64Data.split(";")[0].split("/")[1];
            format = mimeType.equalsIgnoreCase("jpeg") ? "jpg" : mimeType;
        }
        return saveToFile(base64Data, outputPath, format);
    }

}
