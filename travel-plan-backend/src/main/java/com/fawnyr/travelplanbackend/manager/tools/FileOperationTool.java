package com.fawnyr.travelplanbackend.manager.tools;

import cn.hutool.core.io.FileUtil;
import com.fawnyr.travelplanbackend.config.CosClientConfig;
import com.fawnyr.travelplanbackend.constant.FileConstant;
import com.fawnyr.travelplanbackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class FileOperationTool {


    private final CosManager cosManager;
    private final CosClientConfig cosClientConfig;

    public FileOperationTool(CosManager cosManager, CosClientConfig cosClientConfig) {
        this.cosManager = cosManager;
        this.cosClientConfig = cosClientConfig;
    }

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        String uploadPathPrefix = "file";
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, fileName);
        COSObject object = cosManager.getObject(uploadPath);
        COSObjectInputStream objectContent = object.getObjectContent();
        try {
            byte[] bytes = IOUtils.toByteArray(objectContent);
            return new String(bytes);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(
            @ToolParam(description = "Name of the file to write") String fileName,
            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            // 文件上传地址
            String uploadPathPrefix = "file";
            String uploadPath = String.format("/%s/%s", uploadPathPrefix, fileName);
            File file = new File(filePath);
            // 上传图片到对象存储
            cosManager.putObject(uploadPath,file);
            file.delete();
            return "File written successfully to: " + cosClientConfig.getHost()+ "/" + uploadPath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }
}