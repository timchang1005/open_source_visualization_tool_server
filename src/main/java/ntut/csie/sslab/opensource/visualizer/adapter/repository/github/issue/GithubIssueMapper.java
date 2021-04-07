package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.issue;

import ntut.csie.sslab.opensource.visualizer.adapter.repository.github.commit.GithubCommitData;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;

import java.util.List;
import java.util.stream.Collectors;

public class GithubIssueMapper {

    public static GithubIssueDTO transformToDTO(GithubIssueData data) {
        return new GithubIssueDTO(
                data.getId(),
                data.getRepoId(),
                data.getNumber(),
                data.getState(),
                data.getCreatedAt(),
                data.getUpdatedAt(),
                data.getClosedAt()
        );
    }

    public static List<GithubIssueDTO> transformToDTO(List<GithubIssueData> datas) {
        return datas.stream()
                .map(GithubIssueMapper::transformToDTO)
                .collect(Collectors.toList());
    }

    public static GithubIssueData transformToData(GithubIssueDTO dto) {
        return new GithubIssueData(
                dto.getId(),
                dto.getRepoId(),
                dto.getNumber(),
                dto.getState(),
                dto.getCreateAt(),
                dto.getUpdateAt(),
                dto.getClosedAt()
        );
    }

    public static List<GithubIssueData> transformToData(List<GithubIssueDTO> dtos) {
        return dtos.stream().map(GithubIssueMapper::transformToData).collect(Collectors.toList());
    }
}
