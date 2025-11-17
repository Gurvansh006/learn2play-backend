package com.learn2play.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@RestController
public class UploadController {

    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Check if file is empty
            if (file.isEmpty()) {
                UploadResponse res = new UploadResponse(null, null, "File is empty");
                return ResponseEntity.badRequest().body(res);
            }

            String originalName = file.getOriginalFilename();
            if (originalName == null || !originalName.contains(".")) {
                UploadResponse res = new UploadResponse(null, null, "File must have an extension");
                return ResponseEntity.badRequest().body(res);
            }

            // 2. Validate extension
            String ext = originalName.substring(originalName.lastIndexOf('.') + 1)
                    .toLowerCase(Locale.ROOT);

            if (!isAllowedExtension(ext)) {
                UploadResponse res = new UploadResponse(
                        null,
                        originalName,
                        "Only .pdf, .docx, .txt and .md files are allowed"
                );
                return ResponseEntity.badRequest().body(res);
            }

            // 3. Validate size
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                UploadResponse res = new UploadResponse(
                        null,
                        originalName,
                        "File is too large. Maximum size is 20 MB"
                );
                return ResponseEntity.badRequest().body(res);
            }

            // 4. Create uploads folder if it doesn't exist
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 5. Save the file
            Path filePath = uploadDir.resolve(originalName);
            Files.write(filePath, file.getBytes());

            // 6. Generate a mock fileId (later we can link this to AI)
            String fileId = "mock-" + System.currentTimeMillis();

            UploadResponse response = new UploadResponse(
                    fileId,
                    originalName,
                    "File uploaded successfully"
            );

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            UploadResponse res = new UploadResponse(null, null, "Error uploading file");
            return ResponseEntity.status(500).body(res);
        }
    }

    private boolean isAllowedExtension(String ext) {
        return ext.equals("pdf") ||
                ext.equals("docx") ||
                ext.equals("txt") ||
                ext.equals("md");
    }
}
