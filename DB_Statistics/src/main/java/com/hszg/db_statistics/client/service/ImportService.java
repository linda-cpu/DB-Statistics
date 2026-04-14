package com.hszg.db_statistics.client.service;

import com.hszg.db_statistics.client.dto.DelayReasonDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {
    ;
    private final WebClient dbManagementWebClient;

    public void importData() {
        dbManagementWebClient
                .post()
                .uri("/import/xmls")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DelayReasonDto.class)
                .block();
    }

    public List<String> uploadXMLFiles(MultipartFile[] files) throws IOException {
        List<String> uploadedFiles = new ArrayList<>();
        String UPLOAD_DIR = "/app/data/xml";
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Verzeichnis erstellt: {}", UPLOAD_DIR);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String fileName = file.getOriginalFilename();
            if (fileName == null) continue;

            Path filePath = uploadPath.resolve(fileName);

            // Datei schreiben
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            uploadedFiles.add(fileName);
            log.info("Datei erfolgreich gespeichert: {}", filePath);
        }

        // Nachdem die Dateien im Ordner liegen, könntest du hier 
        // theoretisch direkt importData() aufrufen, um den Prozess zu starten.
        importData();

        return uploadedFiles;
    }

}