package daineka.diplomastoragecloud.controller;

import daineka.diplomastoragecloud.dto.AuthentificationRequest;
import daineka.diplomastoragecloud.dto.AuthentificationResponse;
import daineka.diplomastoragecloud.exception.InputDataException;
import daineka.diplomastoragecloud.model.File;
import daineka.diplomastoragecloud.exception.SeanceException;
import daineka.diplomastoragecloud.model.FileData;
import daineka.diplomastoragecloud.service.AuthentificationService;
import daineka.diplomastoragecloud.service.FileService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j

public class CloudController {
    private final AuthentificationService authentificationService;
    private final FileService fileService;

    @PostMapping("/login")
    public ResponseEntity<AuthentificationResponse> login(@RequestBody AuthentificationRequest authentificationRequest) {
        AuthentificationResponse response = authentificationService.authentificationLogin(authentificationRequest);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader("auth-token") String authToken) {
        boolean flag = authentificationService.logout(authToken);
        if (!flag) {
            throw new SeanceException("Пользователь с таким логином не найден");
        }
        return ResponseEntity.ok().body(null);

    }


    @SneakyThrows
    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestBody @NotNull MultipartFile file) {
        String uploadedFileName = fileService.uploadFile(authToken, fileName, file.getBytes(), file.getContentType(), file.getSize());
        return ResponseEntity.ok().body("Файл " + uploadedFileName + " сохранен");
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestHeader("auth-token") String authToken,
                                             @RequestParam("filename") String fileName) {
        fileService.deleteFile(authToken, fileName);
        return ResponseEntity.ok().body("Файл " + fileName + " удален");
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> getFile(@RequestHeader("auth-token") @NotNull String authToken,
                                          @RequestParam("filename") @NotNull String fileName) {
        File uploadFile = fileService.getFile(authToken, fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + uploadFile.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(uploadFile.getFileContent());
    }

    @PutMapping("/file")
    public ResponseEntity<String> renameFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestParam("newFileName") @NotNull String newFileName) {
        String updatedFileName = fileService.renameFile(authToken, fileName, newFileName);
        return ResponseEntity.ok().body("Имя файла " + fileName + " изменено на " + updatedFileName);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileData>> getAllFiles(@RequestHeader("auth-token") @NotNull String authToken,
                                                      @RequestParam("limit") @NotNull int limit) {
        List<FileData> listFiles = fileService.getAllFiles(authToken, limit);
        return ResponseEntity.ok().body(listFiles);
    }

}
