package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.model.Anime;
import com.myProj.Animeshnik.model.AnimeRelatedActions;
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
public class AnimeRelatedActionsHandler implements AnimeRelatedActions {
    @Autowired
    private UserRepository userRepository;

    @Override
    public void addAnimeToWatchlist(Long chatId, Anime animeTitle) {

    }

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
                animeList.add(anime);
            } else {
                animeList = new ArrayList<>();
                animeList.add(anime);
            }

            user.setAnimeList(animeList);

            userRepository.save(user);

            log.info("Animes: " + user.getAnimeList().toString());
        } catch (UserPrincipalNotFoundException e) {
            log.error("Entity/User not found: " + e.getMessage());
            throw new EntityNotFoundException(e);
        }
    }

}
