package com.myProj.Animeshnik.serviceImpl;

import com.myProj.Animeshnik.DAO.UserDAO;
import com.myProj.Animeshnik.service.AnimeDBService;
import com.myProj.Animeshnik.model.User;
import com.myProj.Animeshnik.model.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AnimeDBServiceImpl implements AnimeDBService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDAO userDAO;

    @Override
    @Transactional
    public void addAnimeToWatchlist(Long chatId, String anime) {

        User user;
        try {
            user = userRepository.findById(chatId)
                    .orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
            List<String> animeList;
            if (user.getAnimeList() != null) {
                animeList = user.getAnimeList();
                if(user.getAnimeList().contains(anime)){
                    return;
                }
                animeList.add(anime);
            } else {
                animeList = new ArrayList<>();
                animeList.add(anime);
            }

            user.setAnimeList(animeList);

            userRepository.save(user);

        } catch (UserPrincipalNotFoundException e) {
            log.error("Entity/User not found: " + e.getMessage());
            throw new EntityNotFoundException(e);
        }
    }

    @Override
    @Transactional
    public void removeAnimeFromWatchlist(User user, String animeTitle, long chatId) {
        try {
            user = userRepository.findById(chatId)
                    .orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
            List<String> animeList = null;
            if (user.getAnimeList() != null) {
                animeList = user.getAnimeList();
                if(user.getAnimeList().contains(animeTitle)){
                    animeList.remove(animeTitle);
                    log.info("Removed " + animeTitle);
                }

            }

            user.setAnimeList(animeList);

            userRepository.save(user);

        } catch (UserPrincipalNotFoundException e) {
            log.error("Entity/User not found: " + e.getMessage());
            throw new EntityNotFoundException(e);
        }
    }

}
