package ntut.csie.sslab.opensource.visualizer.adapter.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class GithubIssueInfo {
    private final String repoOwner;
    private final String repoName;
    private final int number;
    private final String state;
    private final String createdAt;
    private final String updatedAt;
    private final String closedAt;

    public static GithubIssueInfo fromDTO(GithubIssueDTO issue, String repoOwner, String repoName) {
        return new GithubIssueInfo(
                repoOwner,
                repoName,
                issue.getNumber(),
                issue.getState(),
                issue.getCreatedAt().toString(),
                issue.getUpdatedAt().toString(),
                issue.getClosedAt() == null ? null : issue.getClosedAt().toString()
        );
    }

    public static List<GithubIssueInfo> fromDTO(List<GithubIssueDTO> dtos, String repoOwner, String repoName) {
        return dtos.stream()
                .map(x -> fromDTO(x, repoOwner, repoName))
                .collect(Collectors.toList());
    }
}
