package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/api/v1")
public class GetUserInfoController {

    private final GithubAPICaller githubAPICaller;

    @Autowired
    public GetUserInfoController(GithubAPICaller githubAPICaller) {
        this.githubAPICaller = githubAPICaller;
    }

    @PostMapping("/user")
    public GithubUserInfo getUserInfo(@RequestBody GetUserInfoRequest request) {
        return githubAPICaller.getUserInfo(request.getUserId(), request.getAccessToken());
    }

    @Data
    public static class GetUserInfoRequest {
        String userId;
        String accessToken;
    }
}
