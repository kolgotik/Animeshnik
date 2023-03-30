package com.myProj.Animeshnik.DAOImpl;

import com.myProj.Animeshnik.DAO.UserDAO;
import com.myProj.Animeshnik.model.User;
import com.myProj.Animeshnik.model.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAOImpl implements UserDAO {

    @Override
    public List<String> getUserListFromDB(User user, UserRepository userRepository, long chatId) {
        user = userRepository.findById(chatId).orElseThrow();
        List<String> retrievedList = user.getAnimeList();
        return retrievedList;
    }
}
