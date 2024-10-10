package com.web.be.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;



@RestController
@CrossOrigin("*")
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/")
    public String getMethodName2() {
        return "loadimg.html";
    }
    

    // Hiển thị form upload
    @GetMapping("upload")
    public String uploadForm() {
        return "uploadForm.html";
    }

    @GetMapping("/tybao/{filename}")
    public ResponseEntity<Resource> serveFileByPath(@PathVariable(required = true,name = "filename") String filename) {
        try {
            // Tạo đường dẫn đầy đủ bằng cách ghép base path với tên file
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Xử lý upload file
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            
            return ResponseEntity.badRequest().body("Khong tim thay file");
        }

        String originalFileName = file.getOriginalFilename();
        Path path = Paths.get(uploadDir + originalFileName);
        String newFileName ="";
        // Kiểm tra nếu file đã tồn tại
        if (Files.exists(path)) {
            // Thêm hậu tố _1, _2, ... vào tên file cho đến khi tìm thấy tên duy nhất
            int count = 1;
            
            do {
                newFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) 
                    + "_" + count 
                    + originalFileName.substring(originalFileName.lastIndexOf('.'));
                path = Paths.get(uploadDir + newFileName);
                count++;
            } while (Files.exists(path));
        }

        try {
            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                Files.createDirectories(Paths.get(uploadDir)); // Tạo cả thư mục cha nếu cần
            }

            // Lưu file vào thư mục uploads
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            if(newFileName.isEmpty()){
                newFileName = originalFileName;
            }

            return ResponseEntity.ok("File uploaded successfully: " + uploadDir +newFileName);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Loi upload file");
        }
        
    }

    @GetMapping("/images")
    public ResponseEntity<List<String>> getAllUploadedImages() {
        try {
            File uploadDirectory = new File(uploadDir);
            File[] files = uploadDirectory.listFiles();
            List<String> imageNames = new ArrayList<>();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        imageNames.add(file.getName());
                    }
                }
            }

            return ResponseEntity.ok(imageNames);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }
}