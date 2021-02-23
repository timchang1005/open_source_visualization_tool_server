package ntut.csie.sslab.opensource.visualizer.adapter.controller.authenticate;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Splitter;
import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.adapter.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final WebClient webClient;
    private final GithubAPICaller githubAPICaller;

    @Autowired
    public GithubAuthenticateController(WebClient.Builder webClientBuilder, GithubAPICaller githubAPICaller) {
        this.webClient = webClientBuilder.baseUrl("https://github.com/login/oauth/access_token").build();
        this.githubAPICaller = githubAPICaller;
    }

    @PostMapping("/authenticate")
    public GithubUserInfo getGithubUserInfo(@RequestBody GithubAuthenticateInput githubAuthenticateInput) {
        String paramsString = webClient.post()
                .body(Mono.just(githubAuthenticateInput), GithubAuthenticateInput.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        Map<String, String> keyValueMap = Splitter.on("&").withKeyValueSeparator("=").split(paramsString);
        return githubAPICaller.getUserInfo(null, keyValueMap.get("access_token"));
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class GithubAuthenticateInput {
        String clientId;
        String clientSecret;
        String code;
        String redirectUri;
    }
}
