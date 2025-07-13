package com.fawnyr.travelplanbackend.manager.tools;

import cn.hutool.core.io.FileUtil;
import com.fawnyr.travelplanbackend.config.CosClientConfig;
import com.fawnyr.travelplanbackend.constant.FileConstant;
import com.fawnyr.travelplanbackend.manager.CosManager;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
@Slf4j

public class PDFGenerationTool {
    private final CosManager cosManager;
    private final  CosClientConfig cosClientConfig;

    public PDFGenerationTool(CosManager cosManager, CosClientConfig cosClientConfig) {
        this.cosManager = cosManager;
        this.cosClientConfig = cosClientConfig;
    }

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {

        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
                        .toAbsolutePath().toString();
                PdfFont font = PdfFontFactory.createFont(fontPath,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
//                // 使用内置中文字体
//                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落并关闭文档
                document.add(paragraph);
            }
            // 上传到对象存储
            // 文件上传地址
            String uploadPathPrefix = "file";
            String uploadPath = String.format("/%s/%s", uploadPathPrefix, fileName);
            File file = new File(filePath);
            // 上传图片到对象存储
            cosManager.putObject(uploadPath,file);
            file.delete();
            return "PDF generated successfully to: " + cosClientConfig.getHost()+ "/" + uploadPath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
