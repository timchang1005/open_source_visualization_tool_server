package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.pullrequest;

import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GithubPullRequestMapper {

    public static GithubPullRequestDTO transformToDTO(GithubPullRequestData data) {
        return new GithubPullRequestDTO(
                data.getId(),
                data.getRepoId(),
                data.getNumber(),
                data.getState(),
                data.getCreatedAt(),
                data.getUpdatedAt(),
                data.getClosedAt()
        );
    }

    public static List<GithubPullRequestDTO> transformToDTO(List<GithubPullRequestData> datas) {
        return datas.stream().map(GithubPullRequestMapper::transformToDTO).collect(Collectors.toList());
    }

    public static GithubPullRequestData transformToData(GithubPullRequestDTO dto) {
        return new GithubPullRequestData(
                dto.getId(),
                dto.getRepoId(),
                dto.getNumber(),
                dto.getState(),
                dto.getCreatedAt(),
                dto.getUpdatedAt(),
                dto.getClosedAt()
        );
    }

    public static List<GithubPullRequestData> transformToData(List<GithubPullRequestDTO> dtos) {
        return dtos.stream().map(GithubPullRequestMapper::transformToData).collect(Collectors.toList());
    }
}
