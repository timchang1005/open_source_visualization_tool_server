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
    private final String author;
    private final String committedDate;
    private final int additions;
    private final int deletions;

    public static GithubCommitInfo fromDTO(GithubCommitDTO dto) {
        return new GithubCommitInfo(
                dto.getRepoOwner(),
                dto.getRepoName(),
                dto.getAuthor(),
                dto.getCommittedDate().toString(),
                dto.getAdditions(),
                dto.getDeletions());
    }

    public static List<GithubCommitInfo> fromDTO(List<GithubCommitDTO> dtos) {
        return dtos.stream().map(GithubCommitInfo::fromDTO).collect(Collectors.toList());
    }
}
