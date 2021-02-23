package ntut.csie.sslab.opensource.visualizer.framework.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GithubAPICallerImpl implements GithubAPICaller {

    private final WebClient webClient;

    @Autowired
    public GithubAPICallerImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    @Override
    public GithubUserInfo getUserInfo(String userId, String accessToken) {
        return webClient.get()
                .uri(userId==null ? "/user" : String.format("/users/%s", userId))
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(accessToken);
                })
                .retrieve()
                .bodyToMono(GithubUserInfo.class)
                .block();
    }
}
