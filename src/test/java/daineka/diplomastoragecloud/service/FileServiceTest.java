package daineka.diplomastoragecloud.service;

import daineka.diplomastoragecloud.DiplomaStorageCloudApplication;
import daineka.diplomastoragecloud.dto.AuthentificationRequest;
import daineka.diplomastoragecloud.dto.AuthentificationResponse;
import daineka.diplomastoragecloud.exception.FileNotFoundException;
import daineka.diplomastoragecloud.model.File;
import daineka.diplomastoragecloud.model.FileData;
import daineka.diplomastoragecloud.repository.FileRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
//устал
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = DiplomaStorageCloudApplication.class)
@AutoConfigureMockMvc
public class FileServiceTest {

    @Autowired
    private AuthentificationService authentificationService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private daineka.diplomastoragecloud.service.FileService fileService;

    @SneakyThrows
    public MultipartFile multipartFileGet(String fileNameTest) {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        URL resource = getClass().getClassLoader().getResource(fileNameTest);

        URLConnection urlConnection = Objects.requireNonNull(resource).openConnection();
        byte[] content = ((InputStream) urlConnection.getContent()).readAllBytes();
        String contentMimeType = urlConnection.getContentType();

        Mockito.when(multipartFile.getContentType()).thenReturn(contentMimeType);
        Mockito.when(multipartFile.getBytes()).thenReturn(content);
        Mockito.when(multipartFile.getSize()).thenReturn((long) content.length);

        return multipartFile;
    }
    @SneakyThrows
    @Test
    public void deleteFileTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("Виталий", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        String fileNameTest2 = "testDeleteFile.txt";
        MultipartFile multipartFile = multipartFileGet(fileNameTest2);
        String contentType = multipartFile.getContentType();
        byte[] bytes = multipartFile.getBytes();
        long sizeFile = multipartFile.getSize();
        fileService.uploadFile(authToken, fileNameTest2, bytes, contentType, sizeFile);
        String actual = fileService.deleteFile(authToken, fileNameTest2);
        String expected = "Файл " + fileNameTest2 + " удален";
        assertEquals(expected, actual);
    }

    @Test
    public void deleteFileTestException() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("Виталий", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        assertThrows(FileNotFoundException.class, () -> {
            fileService.deleteFile(authToken, "testDeleteFileException.txt");
        });
    }
    @Test
    public void getFileTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("Виталий", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        String fileName = "testGetFile.txt";
        File file = fileService.getFile(authToken, fileName);
        assertNotNull(file);
        assertEquals(fileName, file.getFileName());
    }

    @Test
    public void renameFileTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("Виталий", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        String fileName = "testRenameFile.txt";
        String newFileName = "newTestRenameFile.txt";
        String renamedFileName = fileService.renameFile(authToken, fileName, newFileName);
        assertEquals(newFileName, renamedFileName);
    }

    @Test
    public void getAllFilesTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("Виталий", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        int limit = 10;
        List<FileData> files = fileService.getAllFiles(authToken, limit);
        assertNotNull(files);
        assertTrue(files.size() <= limit);
    }

    @Test
    public void checkUserTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("Виталий", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        Long expected = 1L;
        Long actual = fileService.checkUser(authToken);
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    public void checkFileTest() {
        AuthentificationResponse response = authentificationService.authentificationLogin(
                new AuthentificationRequest("Виталий", "qwerty"));
        String authToken = Objects.requireNonNull(response).getAuthToken();
        String fileName = "File1.txt";
        MultipartFile multipartFile = multipartFileGet(fileName);
        Long userId = authentificationService.getSession(authToken).getUserID();
        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isEmpty()) {
            fileService.uploadFile(authToken, fileName, multipartFile.getBytes(), multipartFile.getContentType(), multipartFile.getSize());
        }
        var actual = fileService.checkFile(userId, fileName).getFileName();
        assertEquals(fileName, actual);
    }
}