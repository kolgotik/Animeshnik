package com.myProj.Animeshnik.serviceImpl;

import com.google.gson.Gson;
import com.myProj.Animeshnik.service.GetAnimeByRatingService;
import jakarta.ws.rs.client.ResponseProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class GetAnimeByRatingServiceImpl implements GetAnimeByRatingService {

    private final OkHttpClient client = new OkHttpClient();
    @Value("${api.max-pages}") //~16 000
    int maxPage;

    @Override
    public String getAnimeByRating50() {

        Random random = new Random();
        String url = "https://graphql.anilist.co";
        int randomPage = random.nextInt(maxPage) + 1;

        Map<String, Object> variables = new HashMap<>();
        variables.put("page", randomPage);

        String below50Query = """
                query ($page: Int) {
                    Page(page: $page, perPage: 1) {
                      media(type: ANIME, sort: SCORE_DESC, averageScore_lesser: 50, averageScore_greater: 0, averageScore_not: null) {
                        id
                        startDate {
                          year
                          month
                        }
                        endDate {
                          year
                          month
                        }
                        title {
                          english
                          romaji
                        }
                        episodes
                        description
                        averageScore
                        genres
                      }
                    }
                  }
                """;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", below50Query);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {

            responseBody = response.body().string();

            log.info("API response for below 50 rating: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }
        return responseBody;
    }

    @Override
    public String getAnimeByRating50to60() {
        Random random = new Random();
        String url = "https://graphql.anilist.co";
        int randomPage = random.nextInt(maxPage) + 1;

        Map<String, Object> variables = new HashMap<>();
        variables.put("page", randomPage);

        String fiftyToSixtyQuery = """
                query ($page: Int) {
                    Page(page: $page, perPage: 1) {
                      media(type: ANIME, sort: SCORE_DESC, averageScore_lesser: 61, averageScore_greater: 50, averageScore_not: null) {
                        id
                        startDate {
                          year
                          month
                        }
                        endDate {
                          year
                          month
                        }
                        title {
                          english
                          romaji
                        }
                        episodes
                        description
                        averageScore
                        genres
                      }
                    }
                  }
                """;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", fiftyToSixtyQuery);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {

            responseBody = response.body().string();

            log.info("API response for 50 - 60 rating: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }
        return responseBody;
    }

    @Override
    public String getAnimeByRating60to80() {
        Random random = new Random();
        String url = "https://graphql.anilist.co";
        int randomPage = random.nextInt(maxPage) + 1;

        Map<String, Object> variables = new HashMap<>();
        variables.put("page", randomPage);

        String sixtyToEightyQuery = """
                query ($page: Int) {
                    Page(page: $page, perPage: 1) {
                      media(type: ANIME, sort: SCORE_DESC, averageScore_lesser: 81, averageScore_greater: 59, averageScore_not: null) {
                        id
                        startDate {
                          year
                          month
                        }
                        endDate {
                          year
                          month
                        }
                        title {
                          english
                          romaji
                        }
                        episodes
                        description
                        averageScore
                        genres
                      }
                    }
                  }
                """;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", sixtyToEightyQuery);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {

            responseBody = response.body().string();

            log.info("API response for 60 - 80  rating: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }
        return responseBody;
    }

    @Override
    public String getAnimeByRating80to100() {
        Random random = new Random();
        String url = "https://graphql.anilist.co";
        int randomPage = random.nextInt(maxPage) + 1;

        Map<String, Object> variables = new HashMap<>();
        variables.put("page", randomPage);

        String eightyToHundredQuery = """
                query ($page: Int) {
                    Page(page: $page, perPage: 1) {
                      media(type: ANIME, sort: SCORE_DESC, averageScore_lesser: 100, averageScore_greater: 80, averageScore_not: null) {
                        id
                        startDate {
                          year
                          month
                        }
                        endDate {
                          year
                          month
                        }
                        title {
                          english
                          romaji
                        }
                        episodes
                        description
                        averageScore
                        genres
                      }
                    }
                  }
                """;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", eightyToHundredQuery);
        requestBody.put("variables", variables);

        Gson gson = new Gson();
        String jsonRequestBody = gson.toJson(requestBody);

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        String responseBody = null;
        try (Response response = client.newCall(request).execute()) {

            responseBody = response.body().string();

            log.info("API response for 80 - 100 rating: " + responseBody);
        } catch (ResponseProcessingException | IOException e) {
            log.error("Error occurred during response processing: " + e.getMessage());
        }
        return responseBody;
    }


    @Override
    public SendMessage getAnimeByRatingOptions(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("""
                Know that the higher the score, the slower the search. If nothing seems to be happening, wait, the answer will come. 
                
                Now...
                
                Choose an option:
                """);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        var belowFiftyButton = new InlineKeyboardButton();
        belowFiftyButton.setText("Average score from 0 - 49");
        belowFiftyButton.setCallbackData("BELOW-FIFTY");
        keyboard.add(List.of(belowFiftyButton));

        var fromFiftyToSixty = new InlineKeyboardButton();
        fromFiftyToSixty.setText("Average score from 50 - 60");
        fromFiftyToSixty.setCallbackData("FIFTY-SIXTY");
        keyboard.add(List.of(fromFiftyToSixty));

        var fromSixtyToEighty = new InlineKeyboardButton();
        fromSixtyToEighty.setText("Average score from 60 - 80");
        fromSixtyToEighty.setCallbackData("SIXTY-EIGHTY");
        keyboard.add(List.of(fromSixtyToEighty));

        var fromEightyToHundred = new InlineKeyboardButton();
        fromEightyToHundred.setText("Average score from 80 - 100");
        fromEightyToHundred.setCallbackData("EIGHTY-HUNDRED");
        keyboard.add(List.of(fromEightyToHundred));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }
}
