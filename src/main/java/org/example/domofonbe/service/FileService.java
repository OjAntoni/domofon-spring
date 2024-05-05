package org.example.domofonbe.service;

import java.io.File;
import java.io.InputStream;

public interface FileService {
    boolean folderExists(String name);
    void createFolder(String name);
    String saveObject(InputStream inputStream, String filename, String folder, String contentType);
    String getObjectUrl(String fileName, String folder, String contentType);
}
