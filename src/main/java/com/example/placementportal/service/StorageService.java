package com.example.placementportal.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    
    /**
     * Uploads a file (e.g., resume PDF) and returns the public URL or relative path.
     * 
     * @param file The file to upload
     * @return The URL or path to access the uploaded file
     * @throws IOException If the upload fails
     */
    String uploadFile(MultipartFile file) throws IOException;
}
