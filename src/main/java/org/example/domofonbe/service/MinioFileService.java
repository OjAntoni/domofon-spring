package org.example.domofonbe.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MinioFileService implements FileService{
    private MinioClient minioClient;


    @PostConstruct
    protected void postConstruct(){
        minioClient = MinioClient.builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("user", "useruser")
                .build();
    }
    @Override
    @SneakyThrows
    public boolean folderExists(String name){
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
    }

    @Override
    @SneakyThrows
    public void createFolder(String name) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
    }

    @Override
    @SneakyThrows
    public String saveObject(InputStream inputStream, String fileName, String folder, String contentType) {
        if (folder!=null && !folder.isEmpty() && !folder.isBlank() && !folderExists(folder)){
            createFolder(folder);
        }
        return minioClient.putObject(
                PutObjectArgs.builder().bucket(folder).object(fileName).stream(
                                inputStream, -1, 10485760)
                        .contentType(contentType)
                        .build()).object();

    }

    @Override
    @SneakyThrows
    public String getObjectUrl(String fileName, String folder, String contentType) {
        if(fileName==null || folder==null || contentType==null)
            return null;

        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put("response-content-type", contentType);

        if(isObjectExist(folder, fileName)){
            return minioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(folder)
                                    .object(fileName)
                                    .expiry(10, TimeUnit.HOURS)
                                    .extraQueryParams(reqParams)
                                    .build());
        } else {
            return null;
        }
    }

    private boolean isObjectExist(String bucket, String name) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(name).build());
            return true;
        } catch (ErrorResponseException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
