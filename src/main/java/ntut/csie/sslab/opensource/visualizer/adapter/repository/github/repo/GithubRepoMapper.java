package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.repo;

import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;

import java.util.List;
import java.util.stream.Collectors;

public class GithubRepoMapper {

    public static GithubRepoDTO transformToDTO(GithubRepoData data) {
        return new GithubRepoDTO(
                data.getId(),
                data.getOwner(),
                data.getName()
        );
    }

    public static List<GithubRepoDTO> transformToDTO(List<GithubRepoData> datas) {
        return datas.stream()
                .map(GithubRepoMapper::transformToDTO)
                .collect(Collectors.toList());
    }

    public static GithubRepoData transformToData(GithubRepoDTO dto) {
        return new GithubRepoData(
                dto.getId(),
                dto.getOwner(),
                dto.getName()
        );
    }

    public static List<GithubRepoData> transformToData(List<GithubRepoDTO> dtos) {
        return dtos.stream().map(GithubRepoMapper::transformToData).collect(Collectors.toList());
    }
}
