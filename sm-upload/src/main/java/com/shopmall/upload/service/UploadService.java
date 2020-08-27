package com.shopmall.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties prop;

    public String uploadImage(MultipartFile file) {
        try {
            // 校验文件类型
            String contentType = file.getContentType();
            if (!prop.getAllowTypes().contains(contentType)) {
                throw new SmException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new SmException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 上传到FastDFS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            // 返回路径
            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            // 上传失败
            log.error("[文件上传] 上传文件失败", e);
            throw new SmException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
