package ntut.csie.sslab.opensource.visualizer.usecase.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;
import org.json.JSONException;

import java.time.Instant;
import java.util.List;

public interface GithubAPICaller {
    GithubUserInfo getUserInfo(String userId, String accessToken);
    List<GithubCommitDTO> getCommits(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException;
    List<GithubIssueDTO> getIssues(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws InterruptedException;
    List<GithubTagDTO> getTags(String repoId, String repoOwner, String repoName, String accessToken);
}
