package ntut.csie.sslab.opensource.visualizer.adapter.controller.authenticate;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Splitter;
import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping
public class GithubAuthenticateController {

    @PostMapping("/authenticate")
    public GithubUserInfo getGithubUserInfo(@RequestBody GithubAuthenticateInput githubAuthenticateInput) {
        WebClient webClient = WebClient.create("https://github.com/login/oauth/access_token");

        String paramsString = webClient.post()
                .body(Mono.just(githubAuthenticateInput), GithubAuthenticateInput.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        Map<String, String> map = Splitter.on("&").withKeyValueSeparator("=").split(paramsString);
        String accessToken = map.get("access_token");

//        WebClient webClient1 = WebClient.builder()
//                .baseUrl("https://api.github.com")
////                .defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
//                .build();
        WebClient webClient1 = WebClient.create("https://api.github.com");

        return webClient1.get()
                .uri("/user")
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(accessToken);
                })
                .retrieve()
                .bodyToMono(GithubUserInfo.class)
                .block();
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    static
    class GithubAuthenticateInput {
        String clientId;
        String clientSecret;
        String code;
        String redirectUri;
    }
}
