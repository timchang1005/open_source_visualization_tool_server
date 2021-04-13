package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.release;

import ntut.csie.sslab.opensource.visualizer.usecase.github.release.GithubReleaseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GithubReleaseMapper {
    public static GithubReleaseDTO transformToDTO(GithubReleaseData data) {
        return new GithubReleaseDTO(
                data.getId(),
                data.getRepoId(),
                data.getPublisher(),
                data.getTagName(),
                data.getPublishedAt()
        );
    }

    public static List<GithubReleaseDTO> transformToDTO(List<GithubReleaseData> datas) {
        return datas.stream()
                .map(GithubReleaseMapper::transformToDTO)
                .collect(Collectors.toList());
    }

    public static GithubReleaseData transformToData(GithubReleaseDTO dto) {
        return new GithubReleaseData(
                dto.getId(),
                dto.getRepoId(),
                dto.getPublisher(),
                dto.getTagName(),
                dto.getPublishedAt()
        );
    }

    public static List<GithubReleaseData> transformToData(List<GithubReleaseDTO> dtos) {
        return dtos.stream()
                .map(GithubReleaseMapper::transformToData)
                .collect(Collectors.toList());
    }
}
