package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import com.google.common.base.Splitter;
import ntut.csie.sslab.opensource.visualizer.adapter.controller.github.GithubAuthenticateController;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GithubAuthenticatorImpl implements GithubAuthenticator {
    private final WebClient webClient;

    @Autowired
    public GithubAuthenticatorImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://github.com/login/oauth/access_token").build();
    }

    @Override
    public String authenticate(GithubAuthenticateController.GithubAuthenticateRequest input) {
        String paramsString = webClient.post()
                .body(Mono.just(input), GithubAuthenticateController.GithubAuthenticateRequest.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        Map<String, String> keyValueMap = Splitter.on("&").withKeyValueSeparator("=").split(paramsString);
        return keyValueMap.get("access_token");
    }
}
