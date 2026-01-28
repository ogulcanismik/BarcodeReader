package com.example.barcodereader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleScraper {

    private static final String API_KEY = BuildConfig.SerpAPI_API_KEY;

    public List<Seller> searchProduct(String query) {
        List<Seller> sellerList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();

        String searchUrl = "https://serpapi.com/search?api_key=" + API_KEY +
                "&engine=google_shopping&q=" + query +
                "&google_domain=google.com.tr&gl=tr&hl=tr";

        String startUrl = null;

        try {
            Request request1 = new Request.Builder().url(searchUrl).build();
            try (Response response1 = client.newCall(request1).execute()) {
                if (response1.body() != null) {
                    String body1 = response1.body().string();
                    JsonObject json1 = JsonParser.parseString(body1).getAsJsonObject();

                    if (json1.has("shopping_results")) {
                        JsonArray results = json1.getAsJsonArray("shopping_results");
                        if (results.size() > 0) {
                            JsonObject firstItem = results.get(0).getAsJsonObject();

                            if (firstItem.has("serpapi_immersive_product_api")) {
                                String rawUrl = firstItem.get("serpapi_immersive_product_api").getAsString();
                                startUrl = rawUrl + "&api_key=" + API_KEY + "&more_stores=1";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        }

        if (startUrl != null) {
            String nextUrl = startUrl;
            int pageCount = 0;
            int maxPages = 3;

            while (nextUrl != null && pageCount < maxPages) {
                String nextToken = null;
                try {
                    Request request2 = new Request.Builder().url(nextUrl).build();
                    try (Response response2 = client.newCall(request2).execute()) {
                        if (response2.body() != null && response2.isSuccessful()) {
                            String body2 = response2.body().string();
                            JsonObject json2 = JsonParser.parseString(body2).getAsJsonObject();

                            parseAndAddStores(json2, sellerList);

                            if (json2.has("product_results")) {
                                JsonObject pr = json2.getAsJsonObject("product_results");
                                if (pr.has("stores_next_page_token")) {
                                    nextToken = pr.get("stores_next_page_token").getAsString();
                                }
                            }
                            if (nextToken == null && json2.has("stores_next_page_token")) {
                                nextToken = json2.get("stores_next_page_token").getAsString();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (nextToken != null && !nextToken.isEmpty()) {
                    nextUrl = "https://serpapi.com/search?api_key=" + API_KEY +
                            "&engine=google_immersive_product&page_token=" + nextToken +
                            "&gl=tr&hl=tr&more_stores=1";
                } else {
                    nextUrl = null;
                }
                pageCount++;
            }
        }

        if (!sellerList.isEmpty()) {
            Collections.sort(sellerList, new Comparator<>() {
                @Override
                public int compare(Seller s1, Seller s2) {
                    return Double.compare(s1.getPrice(), s2.getPrice());
                }
            });
        }
        return sellerList;
    }

    private void parseAndAddStores(JsonObject rootJson, List<Seller> list) {
        JsonArray stores = new JsonArray();

        if (rootJson.has("product_results")) {
            JsonObject pr = rootJson.getAsJsonObject("product_results");
            if (pr.has("stores")) stores = pr.getAsJsonArray("stores");
            else if (pr.has("sellers")) stores = pr.getAsJsonArray("sellers");
        } else if (rootJson.has("stores")) {
            stores = rootJson.getAsJsonArray("stores");
        }

        for (int i = 0; i < stores.size(); i++) {
            JsonObject s = stores.get(i).getAsJsonObject();
            String name = s.get("name").getAsString();

            String link = "";
            link = s.get("link").getAsString();

            String price = "";
            if (s.has("total")) price = s.get("price").getAsString();

            if (!price.isEmpty()) {
                list.add(new Seller(name, price, link));
            }
        }
    }
}