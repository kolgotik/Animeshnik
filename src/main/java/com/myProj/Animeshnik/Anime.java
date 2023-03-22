package com.myProj.Animeshnik;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myProj.Animeshnik.model.MediaTitle;
import com.myProj.Animeshnik.service.AniListAPIv2TotalAvailablePages;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Getter
@Setter
@Slf4j
@Component
public class Anime implements AniListAPIv2TotalAvailablePages {

    @Override
    public int getMaxPages() {
        OkHttpClient client = new OkHttpClient();
        Random random = new Random();
        String url = "https://graphql.anilist.co";

        int pageSize = 10; // number of results to retrieve in each query
        int totalPages = 0; // estimated total number of pages
        int maxQueries = 100; // maximum number of queries to make

        Map<String, Object> variables = new HashMap<>();
        variables.put("perPage", pageSize);
        variables.put("page", 1);

        String query = """
            query ($page: Int, $perPage: Int) {
              Page(page: $page, perPage: $perPage) {
                pageInfo {
                  total
                  perPage
                }
              }
            }""";

        int numQueries = 0; // number of queries made so far
        boolean reachedEnd = false; // whether we have reached the end of the list

        while (!reachedEnd && numQueries < maxQueries) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("variables", variables);

            Gson gson = new Gson();
            String jsonRequestBody = gson.toJson(requestBody);

            RequestBody body = RequestBody.create(jsonRequestBody, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonObject pageInfo = root.getAsJsonObject("data").getAsJsonObject("Page").getAsJsonObject("pageInfo");
                int total = pageInfo.get("total").getAsInt();
                int perPage = pageInfo.get("perPage").getAsInt();
                int numPages = (int) Math.ceil((double) total / perPage);
                totalPages += numPages;
                reachedEnd = (numPages == 0 || numPages * perPage < total);
                numQueries++;
                variables.put("page", numPages + 1); // update the query to retrieve the next page
            } catch (IOException e) {
                log.error("Error occurred when trying to get max pages: " + e.getMessage());
            }
        }

        return totalPages;
    }


}
