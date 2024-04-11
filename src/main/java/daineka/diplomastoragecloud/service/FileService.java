package daineka.diplomastoragecloud.service;

import daineka.diplomastoragecloud.model.File;
import daineka.diplomastoragecloud.model.User;
import daineka.diplomastoragecloud.exception.FileNotFoundException;
import daineka.diplomastoragecloud.exception.InputDataException;
import daineka.diplomastoragecloud.exception.SeanceException;
import daineka.diplomastoragecloud.model.FileData;
import daineka.diplomastoragecloud.model.Seance;
import daineka.diplomastoragecloud.repository.FileRepository;
import daineka.diplomastoragecloud.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service

@AllArgsConstructor
@Slf4j
public class FileService {

    private FileRepository fileRepository;
    private UserRepository userRepository;
    private AuthentificationService authentificationService;


    public String uploadFile(String authToken, String fileName, byte[] bytes, String contentType, long sizeFile) {
        Long userId = checkUser(authToken);
        File uploadFile;
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isPresent()) {
            log.error("Файл с именем {} уже существует", fileName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл с таким именем уже существует");
        }
        User user = userRepository.getReferenceById(userId);
        uploadFile = File.builder()
                .fileName(fileName)
                .type(contentType)
                .fileContent(bytes)
                .size(sizeFile)
                .user(user)
                .build();
        fileRepository.save(uploadFile);
        log.info("Пользователь с id {} успешно загрузил файл {}", userId, fileName);
        return fileName;
    }

    public String deleteFile(String authToken, String fileName) {
        Long userId = checkUser(authToken);
        File deleteFile = checkFile(userId, fileName);
        fileRepository.deleteById(deleteFile.getId());
        log.info("Пользователь с id {} успешно удалил файл {}", userId, fileName);
        return "Файл " + fileName + " удален";
    }

    public File getFile(String authToken, String fileName) {
        Long userId = checkUser(authToken);
        File uploadFile = checkFile(userId, fileName);
        log.info("Пользователь с id {} успешно скачал файл {}", userId, fileName);
        return uploadFile;
    }

    public String renameFile(String authToken, String fileName, String newFileName) {
        Long userId = checkUser(authToken);
        File fileToRename = checkFile(userId, fileName);
        if (fileRepository.findFileByUserIdAndFileName(userId, newFileName).isPresent()) {
            log.warn("Файл с таким именем уже существует в базе данных");
            throw new InputDataException("Файл с таким именем уже существует");
        }
        fileToRename.setFileName(newFileName);
        fileRepository.save(fileToRename);
        log.info("Пользователь с id {} успешно изменил имя файла {} на {}", userId, fileName, newFileName);
        return newFileName;
    }

    public List<FileData> getAllFiles(String authToken, int limit) {
        Long userId = checkUser(authToken);
        if (limit < 0) {
            log.warn("Значение лимита ошибочно");
            throw new InputDataException("Значение лимита ошибочно");
        }
        List<File> allFiles = fileRepository.findFilesByUserId(userId);
        List<FileData> listFiles = allFiles.stream()
                .map(file -> FileData.builder()
                        .fileName(file.getFileName())
                        .size(file.getSize())
                        .build()).collect(Collectors.toList());
        log.info("Был получен список файлов пользователя с id {}", userId);
        return listFiles;
    }

    public Long checkUser(String authToken) {
        Seance sessionResult = authentificationService.getSession(authToken);
        if (sessionResult == null) {
            log.error("Пользователь не найден");
            throw new SeanceException("Пользователь с таким логином не найден");
        }
        return Objects.requireNonNull(sessionResult).getUserID();
    }

    public File checkFile(Long userId, String fileName) {
        var checkFile = fileRepository.findFileByUserIdAndFileName(userId, fileName);
        if (checkFile.isEmpty()) {
            log.error("Файл с именем " + fileName + " не найден!");
            throw new FileNotFoundException("Файл с именем " + fileName + " не найден!");
        }
        return checkFile.get();
    }
}