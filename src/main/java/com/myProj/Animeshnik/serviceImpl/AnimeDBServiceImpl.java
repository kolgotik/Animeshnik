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
    public void addAnimeToWatchlist(Long chatId, String anime, Integer animeId) {

        User user;
        try {
            user = userRepository.findById(chatId)
                    .orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
            List<String> animeList;
            List<Integer> animeIdList;
            if (user.getAnimeList() != null && user.getAnimeIdList() != null) {
                animeList = user.getAnimeList();
                animeIdList = user.getAnimeIdList();
                if(user.getAnimeList().contains(anime)){
                    return;
                }
                animeList.add(anime);
                animeIdList.add(animeId);
            } else {
                animeList = new ArrayList<>();
                animeIdList = new ArrayList<>();
                animeList.add(anime);
                animeIdList.add(animeId);
            }

            user.setAnimeList(animeList);
            user.setAnimeIdList(animeIdList);

            userRepository.save(user);

        } catch (UserPrincipalNotFoundException e) {
            log.error("Entity/User not found: " + e.getMessage());
            throw new EntityNotFoundException(e);
        }
    }

    @Override
    @Transactional
    public void removeAnimeFromWatchlist(User user, Integer animeTitleId, long chatId) {
        try {
            user = userRepository.findById(chatId)
                    .orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
            List<String> animeList = null;
            List<Integer> animeIdList = null;
            if (user.getAnimeList() != null) {
                animeList = user.getAnimeList();
                animeIdList = user.getAnimeIdList();
                if(user.getAnimeIdList().contains(animeTitleId)){
                    int indexOfAnime = animeIdList.indexOf(animeTitleId);
                    animeIdList.remove(animeTitleId);
                    String animeToRemove = animeList.get(indexOfAnime);
                    animeList.remove(animeToRemove);
                    log.info("Removed " + animeTitleId + " " + animeToRemove);
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
