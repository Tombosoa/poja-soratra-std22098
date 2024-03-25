package hei.school.soratra.endpoint.rest.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hei.school.soratra.PojaGenerated;
import hei.school.soratra.file.BucketComponent;
import hei.school.soratra.repository.model.ListURL;
import lombok.AllArgsConstructor;

@PojaGenerated
@RestController
@AllArgsConstructor
public class SoratraController {

    BucketComponent bucketComponent;

    private static final String SORATRA_KEY = "soratra/";

    @PutMapping(value = "/soratra/{id}")
    public ResponseEntity<?> convertSoratra(
            @PathVariable String id,
            @RequestBody byte[] file
    ) {
        try {
            File fileToUpload = convertToTempFile(file, id);
            bucketComponent.upload(fileToUpload, SORATRA_KEY + id + ".txt");

            File fileTransformed = convertToTempFileTransformed(fileToUpload);
            bucketComponent.upload(fileTransformed, SORATRA_KEY + "UpperCase/" + id + ".txt");

            return ResponseEntity.ok().body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du traitement du fichier text: " + e.getMessage());
        }
    }

    @GetMapping(value = "/soratra/{id}")
    public ResponseEntity<?> convertSoratra(
            @PathVariable String id
    ) {
        try {
            ListURL listUrl = new ListURL(
                    bucketComponent.presign(SORATRA_KEY + id + ".txt", Duration.ofMinutes(4)).toString(),
                    bucketComponent.presign(SORATRA_KEY + "UpperCase/" + id + ".txt", Duration.ofMinutes(4)).toString());
            return ResponseEntity.status(HttpStatus.OK).body(listUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ID not found : " + e.getMessage());
        }
    }

    private File convertToTempFile(byte[] file, String id) throws IOException {
        File tempFile = File.createTempFile(id, ".txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file);
        }
        return tempFile;
    }

    private static File convertToTempFileTransformed(File file) throws IOException {
        String upperCaseString = convertToUpperCase(file.getName());

        File fileToUpperCase = new File("file-to-upper-case.txt");
        try (FileOutputStream fos = new FileOutputStream(upperCaseString)) {
            fos.write(upperCaseString.getBytes());
        }

        return fileToUpperCase;
    }

    public static String convertToUpperCase(String myFile) throws IOException {
        File file = new File(myFile);

        StringBuilder content = new StringBuilder();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();
                content.append(ligne.toUpperCase()).append("\n");
            }
        }

        return content.toString();
    }
}
