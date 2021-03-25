package ntut.csie.sslab.opensource.visualizer.usecase.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import org.json.JSONException;

import java.time.Instant;
import java.util.List;

public interface GithubAPICaller {
    GithubUserInfo getUserInfo(String userId, String accessToken);
    List<GithubCommitDTO> getCommits(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException;
}
