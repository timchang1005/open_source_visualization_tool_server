package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class GithubAuthenticateController {

    private final GithubAuthenticator githubAuthenticator;

    @Autowired
    public GithubAuthenticateController(GithubAuthenticator githubAuthenticator) {
        this.githubAuthenticator = githubAuthenticator;
    }

    @PostMapping("/authenticate")
    public String getAccessToken(@RequestBody GithubAuthenticateRequest request) {
        return githubAuthenticator.authenticate(request);
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class GithubAuthenticateRequest {
        String clientId;
        String clientSecret;
        String code;
        String redirectUri;
    }
}
