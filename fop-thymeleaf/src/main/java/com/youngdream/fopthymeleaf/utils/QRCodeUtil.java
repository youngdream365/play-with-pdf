package com.youngdream.fopthymeleaf.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码编码和解码工具类（依赖 ZXing）
 *
 * @author coderush
 */
public class QRCodeUtil {
    // 默认二维码尺寸（像素）
    private static final int DEFAULT_SIZE = 300;
    // 默认生成图片格式
    private static final String DEFAULT_FORMAT = "PNG";
    // 默认纠错级别（默认M）：可恢复15%的数据
    private static final ErrorCorrectionLevel DEFAULT_ERROR_CORRECTION = ErrorCorrectionLevel.M;
    // 默认边界像素大小（建议值1-4）
    private static final int DEFAULT_BORDER_MARGIN = 0;

    private QRCodeUtil() {
    }

    // 输入参数验证
    private static void validateInput(String originalText, String path) {
        if (originalText == null || originalText.isEmpty()) {
            throw new IllegalArgumentException("二维码内容不能为空");
        }
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
    }

    // 创建二维码配置参数
    private static Map<EncodeHintType, Object> createHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, DEFAULT_ERROR_CORRECTION);
        hints.put(EncodeHintType.MARGIN, DEFAULT_BORDER_MARGIN);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
        return hints;
    }

    /**
     * 生成二维码并保存为文件
     *
     * @param originalText 二维码内容
     * @param filePath     保存路径（如：/path/qrcode.png）
     * @param size         图片尺寸（像素）
     */
    public static void generate2Png(String originalText, String filePath, int size) throws WriterException, IOException {
        validateInput(originalText, filePath);
        Map<EncodeHintType, Object> hints = createHints();
        BitMatrix matrix = new QRCodeWriter().encode(
                originalText,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
        );
        MatrixToImageWriter.writeToPath(matrix, DEFAULT_FORMAT, new File(filePath).toPath());
    }

    public static void encode2File(String originalText, String filePath) throws WriterException, IOException {
        generate2Png(originalText, filePath, DEFAULT_SIZE);
    }

    /**
     * 生成二维码 SVG 内容
     *
     * @param originalText 二维码内容
     * @param size         图片尺寸（像素）
     * @return
     * @throws Exception
     */
    public static String generate2SVG(String originalText, int size, boolean onlyPath) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(originalText, BarcodeFormat.QR_CODE, size, size);
        StringBuilder svgPath = new StringBuilder();
        String svgTagPreffix = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 " + matrix.getWidth() + " " + matrix.getHeight() + "'>";
        svgPath.append("<path d='");
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    svgPath.append("M" + x + "," + y + "h1v1h-1z"); // 绘制方块
                }
            }
        }
        svgPath.append("' fill='black'/>");
        String svgTagSuffix = "</svg>";
        return onlyPath ? svgPath.toString() : (svgTagPreffix + svgPath + svgTagSuffix);
    }

    /**
     * 生成二维码的Base64字符串（不带data:前缀）
     *
     * @param width        图片尺寸：宽（像素）
     * @param height       图片尺寸：高（像素）
     * @param originalText 二维码内容
     * @return Base64编码的PNG图片数据
     */
    public static String encode2Base64(int width, int height, String originalText) throws WriterException, IOException {
        validateInput(originalText, "base64");
        Map<EncodeHintType, Object> hints = createHints();
        String base64Text = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BitMatrix matrix = new MultiFormatWriter().encode(originalText, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }
            // 将 BufferedImage 转换为 Base64
            ImageIO.write(image, DEFAULT_FORMAT, baos);
            base64Text = Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64Text;
    }

    // 快捷方法（默认配置）
    public static String encode2Base64(int size, String originalText) throws WriterException, IOException {
        return encode2Base64(size, size, originalText);
    }

    public static String encode2Base64(String originalText) throws WriterException, IOException {
        return encode2Base64(DEFAULT_SIZE, originalText);
    }

    /**
     * 从图片文件解码二维码
     */
    public static String decodeFromFile(String filePath) {
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            return decodeQRCode(image);
        } catch (IOException | NotFoundException e) {
            System.err.println("解码失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从二维码 Base64 解码到原文
     */
    public static String decodeFromBase64(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }
        try {
            // 处理 Data URI 格式
            String pureBase64 = base64Data.split(",").length > 1
                    ? base64Data.split(",")[1]
                    : base64Data;
            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            return decodeQRCode(image);
        } catch (IOException | NotFoundException e) {
            System.err.println("解码失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从二维码 BufferedImage 解码到原文
     */
    private static String decodeQRCode(BufferedImage image) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, Object> hints = new HashMap<>();
        // 增强识别模式
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

}
