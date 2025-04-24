package com.appxemphim.firebaseBackend.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeiliConfig {

    @Value("${meili.host}")
    private String meiliHost;

    @Value("${meili.api-key}")
    private String meiliApiKey;

    @Bean
    public Client meiliClient() {
        // Khởi tạo Config với host và apiKey
        Config config = new Config(meiliHost, meiliApiKey);
        // Nếu muốn dùng Jackson thay vì Gson mặc định, có thể:
        // Config config = new Config(meiliHost, meiliApiKey, new JacksonJsonHandler());
        return new Client(config);
    }
}
