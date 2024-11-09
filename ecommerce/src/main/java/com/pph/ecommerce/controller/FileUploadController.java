package com.pph.ecommerce.controller;

import com.pph.ecommerce.dto.response.ApiResponse;
import com.pph.ecommerce.service.StorageService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/upload-file")
@RequiredArgsConstructor
public class FileUploadController {
    private final StorageService storageService;
    @PostMapping("")
    public ApiResponse<?> uploadFile(@RequestParam("file")MultipartFile file) {
        try {
            // save files to a folder -> use a service
            String generatedFileName = storageService.storeFile(file);
            return ApiResponse.builder()
                    .code(HttpStatus.OK.value())
                    .message("upload file successfully")
                    .data(generatedFileName)
                    .build();
        }catch (Exception e) {
            return ApiResponse.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("upload file unsuccessfully")
                    .build();
        }
    }
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<byte[]> readDetailFile(@PathVariable String fileName) {
        try {
            byte[] bytes = storageService.readFileContent(fileName);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
                    .body(bytes);
        }catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }
    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getUploadFiles() {
        try {
            List<String> urls = storageService.loadAll().map(
                    path -> {
                        String urlPath = MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                                "readDetailFile", path.getFileName().toString()).build().toUri().toString();
                        return urlPath;
                    }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(HttpStatus.OK.value())
                    .message("List files successfully")
                    .data(urls)
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(HttpStatus.OK.value())
                    .message("List files unsuccessfully")
                    .build()
            );
        }
    }

}
