package com.singularity.rentit.service;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    @Value("${storage.mode:local}")
    private String mode;

    @Value("${storage.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${storage.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${storage.bucket:rentit-files}")
    private String bucket;

    @Value("${storage.endpoint:http://localhost:9000}")
    private String endpoint;

    @Autowired(required = false)
    private MinioClient minioClient;

    public String uploadFile(MultipartFile file, String folder) {
        if (isLocal()) {
            return uploadFileLocal(file.getOriginalFilename(), folder, readBytes(file), file.getContentType());
        }
        return uploadFileToMinio(file, folder);
    }

    public String uploadBytes(byte[] data, String folder, String fileName, String contentType) {
        if (isLocal()) {
            return uploadFileLocal(fileName, folder, data, contentType);
        }
        return uploadBytesToMinio(data, folder, fileName, contentType);
    }

    public void deleteFile(String url) {
        if (isLocal()) {
            deleteFileLocal(url);
        } else {
            deleteFileFromMinio(url);
        }
    }

    private boolean isLocal() {
        return !"minio".equalsIgnoreCase(mode);
    }

    private String uploadFileLocal(String originalName, String folder, byte[] data, String contentType) {
        try {
            String ext = getExtension(originalName);
            String fileName = UUID.randomUUID() + ext;
            Path dir = Paths.get(uploadDir, folder);
            Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), data);
            String url = baseUrl + "/uploads/" + folder + "/" + fileName;
            log.info("Saved file locally: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Failed to save file locally", e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    private void deleteFileLocal(String url) {
        try {
            String prefix = baseUrl + "/uploads/";
            if (url.startsWith(prefix)) {
                Path path = Paths.get(uploadDir, url.substring(prefix.length()));
                Files.deleteIfExists(path);
                log.info("Deleted local file: {}", path);
            }
        } catch (Exception e) {
            log.warn("Failed to delete local file: {}", url, e);
        }
    }

    private String uploadFileToMinio(MultipartFile file, String folder) {
        try {
            ensureBucketExists();
            String objectName = folder + "/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("Uploaded to MinIO: {}/{}", bucket, objectName);
            return endpoint + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    private String uploadBytesToMinio(byte[] data, String folder, String fileName, String contentType) {
        try {
            ensureBucketExists();
            String objectName = folder + "/" + UUID.randomUUID() + "_" + fileName;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType(contentType)
                    .build());
            log.info("Uploaded bytes to MinIO: {}/{}", bucket, objectName);
            return endpoint + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("Failed to upload bytes to MinIO", e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    private void deleteFileFromMinio(String url) {
        try {
            String prefix = endpoint + "/" + bucket + "/";
            String objectName = url.startsWith(prefix) ? url.substring(prefix.length()) : url;
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
            log.info("Deleted from MinIO: {}/{}", bucket, objectName);
        } catch (Exception e) {
            log.warn("Failed to delete file from MinIO: {}", url, e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("Created MinIO bucket: {}", bucket);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file bytes", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
