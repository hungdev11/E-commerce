package com.pph.ecommerce.service.Imp;

import com.pph.ecommerce.service.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ImageStorageService implements StorageService {
    private final Path storageFolder = Paths.get("uploads");
    public ImageStorageService () {
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize storage", e);
        }
    }
    private boolean isImageFile(MultipartFile file) {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        return Arrays.asList(new String[] {"png", "jpg", "jpeg", "bmp"})
                .contains(fileExtension.trim().toLowerCase());
    }
    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }
            if (!isImageFile(file)) {
                throw new RuntimeException("You can only upload image file");
            }
            float fileSizeInMb = file.getSize() / 1_000_000.0f;
            if (fileSizeInMb > 5.0f) {
                throw new RuntimeException("File must be less than or equal 5 Mb");
            }
            // Rename cuz if 2 file with duplicate name can be overrided
            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
            String generateFileName = UUID.randomUUID().toString().replace("-", "");
            generateFileName = generateFileName + "." + fileExtension;
            Path destinationFilePath = this.storageFolder.resolve(
                    Paths.get(generateFileName)).normalize().toAbsolutePath();
            if (!destinationFilePath.getParent().equals(this.storageFolder.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory");
            }
            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return generateFileName;
        } catch(IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.storageFolder, 1)
                    .filter(path -> !path.equals(this.storageFolder))
                    .map(this.storageFolder::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load stored files", e);
        }
    }

    @Override
    public byte[] readFileContent(String fileName) {
        try {
            Path file = storageFolder.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
                return bytes;
            } else {
                throw  new RuntimeException("Could not read file " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public void deleteAllFile() {

    }
}
