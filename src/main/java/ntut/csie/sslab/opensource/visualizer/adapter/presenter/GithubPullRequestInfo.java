package ntut.csie.sslab.opensource.visualizer.adapter.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestDTO;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class GithubPullRequestInfo {
    private final String id;
    private final String repoOwner;
    private final String repoName;
    private final int number;
    private final String state;
    private final String createdAt;
    private final String updatedAt;
    private final String mergedAt;
    private final String closedAt;

    public static GithubPullRequestInfo fromDTO(GithubPullRequestDTO pullRequest, String repoOwner, String repoName) {
        return new GithubPullRequestInfo(
                pullRequest.getId(),
                repoOwner,
                repoName,
                pullRequest.getNumber(),
                pullRequest.getState(),
                pullRequest.getCreatedAt().toString(),
                pullRequest.getUpdatedAt().toString(),
                pullRequest.getMergedAt() == null ? null : pullRequest.getMergedAt().toString(),
                pullRequest.getClosedAt() == null ? null : pullRequest.getClosedAt().toString()
        );
    }

    public static List<GithubPullRequestInfo> fromDTO(List<GithubPullRequestDTO> pullRequests, String repoOwner, String repoName) {
        return pullRequests.stream()
                .map(x -> fromDTO(x, repoOwner, repoName))
                .collect(Collectors.toList());
    }
}
