package daineka.diplomastoragecloud.service;

import daineka.diplomastoragecloud.dto.AuthentificationRequest;
import daineka.diplomastoragecloud.dto.AuthentificationResponse;
import daineka.diplomastoragecloud.model.User;
import daineka.diplomastoragecloud.model.Seance;
import daineka.diplomastoragecloud.repository.UserRepository;
import daineka.diplomastoragecloud.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service

@Slf4j
public class AuthentificationService {
    private final ConcurrentMap<String, Seance> sessions;
    private final UserRepository userRepository;

    public AuthentificationService(UserRepository userRepository) {
        this.sessions = new ConcurrentHashMap<>();
        this.userRepository = userRepository;
    }

    public AuthentificationResponse authentificationLogin(AuthentificationRequest authentificationRequest) {
        AuthentificationResponse response;

        Optional<User> userFromDataBase = userRepository.findUserByLoginAndPassword(authentificationRequest.getLogin(),
                authentificationRequest.getPassword());
        if (userFromDataBase.isPresent()) {
            Seance session = new Seance(CommonUtils.createID(), userFromDataBase.get().getId());
            sessions.put(session.getId(), session);
            response = new AuthentificationResponse(session.getId());
            log.info("Пользователь " + authentificationRequest.getLogin() + " авторизован");
        } else {
            log.error("Ошибка авторизации");
            response = null;
        }
        return response;
    }


    public boolean logout(String authToken) {
        Seance sessionResult = sessions.getOrDefault(authToken, null);
        boolean flag;
        if (sessionResult != null) {
            sessions.remove(sessionResult.getId(), sessionResult);
            flag = true;
            log.info("Пользователь " + authToken + " вышел из сессии");
        } else {
            log.warn("В сессии нет такого пользователя!");
            flag = false;
        }
        return flag;

    }

    public Seance getSession(String authToken) {
        return sessions.get(authToken);
    }
}
