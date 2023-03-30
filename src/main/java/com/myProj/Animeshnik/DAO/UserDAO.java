package com.myProj.Animeshnik.DAO;

import com.myProj.Animeshnik.model.User;
import com.myProj.Animeshnik.model.UserRepository;

import java.util.List;

public interface UserDAO {
    List<String> getUserListFromDB(User user, UserRepository userRepository, long chatId);
}
