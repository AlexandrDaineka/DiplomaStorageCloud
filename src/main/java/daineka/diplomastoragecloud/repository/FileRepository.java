package daineka.diplomastoragecloud.repository;

import daineka.diplomastoragecloud.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findFileByUserIdAndFileName(Long userId, String fileName);

    Optional<File> findFileByFileName(String fileName);

    List<File> findFilesByUserId(Long userId);

}
