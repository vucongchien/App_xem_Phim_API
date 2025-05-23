package com.appxemphim.firebaseBackend.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;

import org.checkerframework.checker.interning.qual.UsesObjectEquals;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.SetPassWordRequest;
import com.appxemphim.firebaseBackend.dto.response.PersonReviewDTO;
import com.appxemphim.firebaseBackend.service.AccountService;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login/{uid}")
    public ResponseEntity<Map<String, String>> login(@PathVariable String uid) {
        try {
            Map<String, String> jwtToken = accountService.createToken(uid);
            return ResponseEntity.ok(jwtToken);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/repass")
    public ResponseEntity<String> repass(@RequestBody SetPassWordRequest request) {
        try{
            String result = accountService.setPass(request);
            return ResponseEntity.ok(result);
        }catch(RuntimeException e){
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/information/{uid}")//uid ==1 thì lấy uid từ token 
    public ResponseEntity<?> getInformation(@PathVariable String uid) {
        try{
            PersonReviewDTO reviewDTO = accountService.getInformation(uid);
            return  ResponseEntity.ok().body(reviewDTO) ;
        }catch( Exception e){
            e.printStackTrace();
            return  ResponseEntity.status(405).body(e.getMessage());
        }
    }

   @GetMapping("/resettoken/{refreshToken}")
    public ResponseEntity<?> resetToken(@PathVariable String refreshToken) {
        try {
            String newAccessToken = accountService.resetToken(refreshToken);
            return ResponseEntity.ok( newAccessToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    

    @GetMapping("/video")
    public ResponseEntity<String> getGoogleDriveDownloadUrl(@RequestParam String url) {
        String finalDownloadUrl = url;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder htmlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) htmlBuilder.append(line);
            reader.close();

            String html = htmlBuilder.toString();
            Document doc = Jsoup.parse(html);

            Element form = doc.selectFirst("form#download-form");
            if (form != null) {
                String action = form.attr("action");
                Map<String, String> params = new HashMap<>();
                for (Element input : form.select("input")) {
                    String name = input.attr("name");
                    String value = input.attr("value");
                    if (!name.isEmpty()) {
                        params.put(name, value);
                    }
                }
                StringBuilder downloadUrl = new StringBuilder(action + "?");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    downloadUrl.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                            .append("&");
                }
                downloadUrl.setLength(downloadUrl.length() - 1);
                finalDownloadUrl = downloadUrl.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch Google Drive URL");
        }
        return ResponseEntity.ok(finalDownloadUrl);
    }




    //register được thực hiện ở app (client)
}
