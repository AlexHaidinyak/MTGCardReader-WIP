package api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APILookup {
    private static final String api = "https://api.scryfall.com/cards/";
    private static final String manualAPI = "https://api.scryfall.com/cards/named?fuzzy=";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static HttpResponse<String> lookupCard(String set, String id){
        String url = api + set + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "mtgCardStorageApp")
                .header("Accept", "application/json")
                .GET()
                .build();

        try{
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static HttpResponse<String> lookupCard(String[] cardInfo){
        String setId = cardInfo[0];
        String cardName = cardInfo[1].replace(' ', '+');

        String url = manualAPI + cardName + "&set=" + setId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "mtgCardStorageApp")
                .header("Accept", "application/json")
                .GET()
                .build();

        try{
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
