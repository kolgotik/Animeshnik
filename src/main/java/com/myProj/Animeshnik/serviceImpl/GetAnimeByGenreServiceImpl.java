package com.myProj.Animeshnik.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.myProj.Animeshnik.service.GetAnimeByGenreService;
import jakarta.ws.rs.client.ResponseProcessingException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Setter
@Slf4j
public class GetAnimeByGenreServiceImpl implements GetAnimeByGenreService {

    ConcurrentHashMap<Long, ConcurrentHashMap<String, Boolean>> userGenreOptions = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Boolean> getGenreOption(long chatId) {
        return userGenreOptions.computeIfAbsent(chatId, k -> {
            ConcurrentHashMap<String, Boolean> genreOptions = new ConcurrentHashMap<>();
            genreOptions.put("Action", false);
            genreOptions.put("Adventure", false);
            genreOptions.put("Comedy", false);
            genreOptions.put("Drama", false);
            genreOptions.put("Ecchi", false);
            genreOptions.put("Fantasy", false);
            genreOptions.put("Mahou Shoujo", false);
            genreOptions.put("Horror", false);
            genreOptions.put("Mecha", false);
            genreOptions.put("Music", false);
            genreOptions.put("Mystery", false);
            genreOptions.put("Psychological", false);
            genreOptions.put("Romance", false);
            //genreOptions.put("School", false);
            genreOptions.put("Sci-Fi", false);
            genreOptions.put("Slice of Life", false);
            genreOptions.put("Sports", false);
            genreOptions.put("Supernatural", false);
            genreOptions.put("Thriller", false);
            return genreOptions;
        });
    }


    @Override
    public String getAnimeIdForGenreSelection(List<String> selectedGenres, int page, String sort) {

        Random random = new Random();
        String url = "https://graphql.anilist.co";

        Map<String, Object> variables = new HashMap<>();
        variables.put("page", page);
        variables.put("genre", selectedGenres.toArray());
        variables.put("sort", sort);

        String query = """
                query ($genre: [String], $page: Int, $tag: [String], $sort: [MediaSort]) {
                   Page(page: $page, perPage: 50) {
                   pageInfo{
                         hasNextPage
                       }
                     media(type: ANIME, genre_in: $genre, tag_in: $tag, sort: $sort) {
                       id
                     }
                   }
                 }""";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        String responseBody = null;
        final OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(request).execute()) {

            responseBody = response.body().string();

            log.info("API response: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }
        return responseBody;
    }

    @Override
    public List<Integer> getListOfAnimeID(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Integer> idList = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode mediaNode = rootNode.path("data").path("Page").path("media");

            for (int i = 0; i < mediaNode.size(); i++) {
                int id = mediaNode.get(i).path("id").asInt();
                idList.add(id);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return idList;
    }

    @Override
    public EditMessageText updateGenreListOnSelect(long chatId, int messageId, Map<String, Boolean> updatedGenreList) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId(messageId);
        message.setText("Select genres:");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String genre : updatedGenreList.keySet()) {
            if (updatedGenreList.get(genre)) {
                var genreButton = new InlineKeyboardButton();
                genreButton.setText("✅ " + genre);
                genreButton.setCallbackData(genre);
                keyboard.add(List.of(genreButton));
            } else {
                var genreButton = new InlineKeyboardButton();
                genreButton.setText(genre);
                genreButton.setCallbackData(genre);
                keyboard.add(List.of(genreButton));
            }
        }
        if (updatedGenreList.containsValue(true)) {
            var confirmButton = new InlineKeyboardButton();
            confirmButton.setText("\uD83D\uDC49  Confirm  \uD83D\uDC48");
            confirmButton.setCallbackData("CONFIRM_GENRES");
            keyboard.add(List.of(confirmButton));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        userGenreOptions.put(chatId, new ConcurrentHashMap<>(updatedGenreList));

        return message;
    }

    @Override
    public SendMessage sendGenreSelection(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select genres:");

        Map<String, Boolean> genresList = getGenreOption(chatId);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String genre : genresList.keySet()) {
            var genreButton = new InlineKeyboardButton();
            genreButton.setText(genre);
            genreButton.setCallbackData(genre);
            keyboard.add(List.of(genreButton));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;
    }
}
