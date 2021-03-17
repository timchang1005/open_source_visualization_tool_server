package ntut.csie.sslab.opensource.visualizer.usecase.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;

import java.time.Instant;
import java.util.List;

public interface GithubAPICaller {
    GithubUserInfo getUserInfo(String userId, String accessToken);
    List<GithubCommitDTO> getCommits(String repoOwner, String repoName, Instant sinceTime, String accessToken);
}
