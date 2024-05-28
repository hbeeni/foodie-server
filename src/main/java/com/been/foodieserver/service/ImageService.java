package com.been.foodieserver.service;

import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {

    @Value("${images.dir.user-profile}")
    private String userProfileImageDir;

    private static final String PNG_TYPE = "image/png";
    private static final String JPEG_TYPE = "image/jpeg";

    public String save(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.ATTACH_IMAGE);
        }

        //지원하는 이미지 형식인지 검증
        String mimeType = Files.probeContentType(new File(file.getOriginalFilename()).toPath());
        if (isNotSupportedImageType(mimeType)) {
            throw new CustomException(ErrorCode.NOT_SUPPORTED_IMAGE_TYPE);
        }

        String imageName = UUID.randomUUID() + ".jpg";
        BufferedImage original = ImageIO.read(file.getInputStream());
        File imageFile = new File(getFullPath(imageName));

        Thumbnails.of(original)
                .crop(Positions.CENTER).size(400, 400).outputFormat("jpg")
                .toFile(imageFile);

        return imageName;
    }

    public void delete(String imageName) {
        String fullPath = getFullPath(imageName);

        try {
            Path path = Path.of(fullPath);
            Files.delete(path);
            log.info("[이미지 삭제 성공] path = {}", fullPath);
        } catch (IOException e) {
            log.error("[이미지 삭제 실패: {}] path = {}", e.getMessage(), fullPath);
        }
    }

    private boolean isNotSupportedImageType(String type) {
        return !type.equals(PNG_TYPE) && !type.equals(JPEG_TYPE);
    }

    private String getFullPath(String imageName) {
        return userProfileImageDir + imageName;
    }
}
