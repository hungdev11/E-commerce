package com.pph.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    public String storeFile(MultipartFile file);
    public Stream<Path> loadAll(); // load all files in a folder
    public byte[] readFileContent(String fileName); // server response a byte array then client can see image in browser
    public void deleteAllFile();
}
