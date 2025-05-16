package com.myfirstproject.example.service;

import com.myfirstproject.example.dao.UserDAO;
import com.myfirstproject.example.dto.UserDTO;
import com.myfirstproject.example.repository.MyRepo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Service
public class MyService {

    private MyRepo repo;

    @Autowired
    public MyService(MyRepo repo) {
        this.repo = repo;
    }

    public String setAllItems() {
        UserDTO dto = new UserDTO();
        List<UserDTO> list = new ArrayList<>();

        dto.setFirstName("Hello");
        dto.setLastName("World");
        dto.setRole("Engineer");

        list.add(dto);

        UserDAO dao = new UserDAO();

        dao.setFirstName(dto.getFirstName());
        dao.setLastName(dto.getLastName());
        dao.setRole(dto.getRole());

        this.repo.save(dao);

        return "Successfully Inserted";

    }

    public List<UserDTO> getAllItems() {

        List<UserDAO> dao = this.repo.findAll();

        List<UserDTO> list = new ArrayList<>();

        for(UserDAO user: dao) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setRole(user.getRole());
            list.add(dto);
        }

        return list;
    }

    public void nseBankNiftyFetcher() {
        String baseUrl = "https://www.nseindia.com";
        String apiUrl = "https://www.nseindia.com/api/quote-derivative?symbol=BANKNIFTY";

        try {
//            HttpClient client = HttpClient.newBuilder()
//                    .followRedirects(HttpClient.Redirect.ALWAYS)
//                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Step 1: Make initial request to homepage to get cookies
//            HttpRequest initialRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(baseUrl))
//                    .header("User-Agent", "Mozilla/5.0")
//                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .header("Referer", "https://www.nseindia.com/")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .GET()
                    .build();

            //client.send(initialRequest, HttpResponse.BodyHandlers.discarding());

            // Step 2: Call the actual API
//            HttpRequest apiRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(apiUrl))
//                    .header("User-Agent", "Mozilla/5.0")
//                    .header("Accept", "application/json")
//                    .header("Referer", "https://www.nseindia.com/")
//                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONArray stocks = json.getJSONArray("stocks");

                // Get the first future (nearest expiry)
                JSONObject firstFuture = stocks.getJSONObject(0);
                JSONObject tradeInfo = firstFuture
                        .getJSONObject("marketDeptOrderBook")
                        .getJSONObject("tradeInfo");

                double open = tradeInfo.getDouble("open");
                double high = tradeInfo.getDouble("dayHigh");
                double low = tradeInfo.getDouble("dayLow");
                double close = tradeInfo.getDouble("close");

                System.out.println("Bank Nifty Futures OHLC:");
                System.out.println("Open:  " + open);
                System.out.println("High:  " + high);
                System.out.println("Low:   " + low);
                System.out.println("Close: " + close);
            } else {
                System.out.println("Error: HTTP " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
