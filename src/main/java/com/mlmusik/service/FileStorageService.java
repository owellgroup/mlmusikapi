package com.mlmusik.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.cover-art-dir}")
    private String coverArtDir;

    @Value("${file.upload.songs-dir}")
    private String songsDir;

    public void initDirectories() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(coverArtDir));
            Files.createDirectories(Paths.get(songsDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    public String storeCoverArt(MultipartFile file) throws IOException {
        initDirectories();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        Path targetLocation = Paths.get(coverArtDir).resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation.toString();
    }

    public String storeSong(MultipartFile file) throws IOException {
        initDirectories();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".mp3";
        String filename = UUID.randomUUID().toString() + extension;
        Path targetLocation = Paths.get(songsDir).resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation.toString();
    }

    public File getFile(String filePath) {
        return new File(filePath);
    }
}


