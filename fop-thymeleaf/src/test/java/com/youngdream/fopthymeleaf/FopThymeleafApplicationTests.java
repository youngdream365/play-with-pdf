package com.youngdream.fopthymeleaf;

import com.youngdream.fopthymeleaf.utils.Base64Util;
import com.youngdream.fopthymeleaf.utils.ChartUtil;
import com.youngdream.fopthymeleaf.utils.FOPUtil;
import com.youngdream.fopthymeleaf.utils.QRCodeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class FopThymeleafApplicationTests {

    @Test
    public void generatePDF() throws Exception {
        // 统计图
        String chartPngBase64Text = ChartUtil.getPngBase64Demo();
        String chartCleanSVGText = ChartUtil.getCleanSVGDemo();

        // 模拟表格数据
        List<User> users = getUsers();

        // 二维码，base64 和 svg 两种形式
        String imgBase64Text = Base64Util.DEFAULT_BASE64_PNG_PRIFIX + QRCodeUtil.encode2Base64("http://weixin.qq.com/r/mp/HkRrc7vE4_acrf8Q9xEM");
        String svgQRCodeText = QRCodeUtil.generate2SVG("http://weixin.qq.com/r/mp/HkRrc7vE4_acrf8Q9xEM", 200, true);

        // 数据源，传递到 Thymeleaf 的
        HashMap<String, Object> model = new HashMap<>();
        model.put("userList", users);
        model.put("userCount", users.size());
        model.put("nowDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        model.put("barcodeText", "coderush-123456");
        model.put("qrcodeImgBase64Text", imgBase64Text);
        model.put("chartPngBase64Text", chartPngBase64Text);
        model.put("chartCleanSVGText", chartCleanSVGText);
        model.put("svgQRCodeText", svgQRCodeText);

        // FOP 将 xsl-fo 转化为 PDF
        FOPUtil.generatePDF(model, "demo-fop", "demo-tpl", "demo.pdf");
    }

    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        int no = 0;
        for (int i = 0; i < 2; i++) {
            users.addAll(Arrays.asList(
                    new User(getUid(++no), "张三", 25, "The page is subdivided into regions: one or more region-body, and up to four other regions: region-before, region-after, region-start, and region-end."),
                    new User(getUid(++no), "张三", 25, "你站在桥上看风景，看风景人在楼上看你。明月装饰了你的窗子，你装饰了别人的梦。"),
                    new User(getUid(++no), "李四", 30, "3.141592653589&#x200B;79323846264338"),
                    // 超长连词折行：可手动插入零宽空格 &#x200B; 注意用：th:utext 非转义呈现。
                    new User(getUid(++no), "王五", 28, "wangwuwangwu&#x200B;wangwu@gmail.com"))
            );
        }
        return users;
    }

    public static String getUid(int i) {
        DecimalFormat format = new DecimalFormat("0000");
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy" + format.format(i)));
    }

}
