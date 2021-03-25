package ntut.csie.sslab.opensource.visualizer.adapter.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class GithubCommitInfo {
    private final String repoOwner;
    private final String repoName;
    private final String committer;
    private final String committedDate;
    private final int additions;
    private final int deletions;

    public static GithubCommitInfo fromDTO(GithubCommitDTO commit, String repoOwner, String repoName) {
        return new GithubCommitInfo(
                repoOwner,
                repoName,
                commit.getCommitter(),
                commit.getCommittedDate().toString(),
                commit.getAdditions(),
                commit.getDeletions());
    }

    public static List<GithubCommitInfo> fromDTO(List<GithubCommitDTO> dtos, String repoOwner, String repoName) {
        return dtos.stream()
                .map(x -> fromDTO(x, repoOwner, repoName))
                .collect(Collectors.toList());
    }
}
