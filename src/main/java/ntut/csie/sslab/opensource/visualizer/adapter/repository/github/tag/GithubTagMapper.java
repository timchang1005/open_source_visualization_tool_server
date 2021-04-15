package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.tag;

import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;

import java.util.List;
import java.util.stream.Collectors;

public class GithubTagMapper {

    public static GithubTagDTO transformToDTO(GithubTagData data) {
        return new GithubTagDTO(
                data.getId(),
                data.getRepoId(),
                data.getName(),
                data.getTagger(),
                data.getCreatedAt()
        );
    }

    public static List<GithubTagDTO> transformToDTO(List<GithubTagData> datas) {
        return datas.stream()
                .map(GithubTagMapper::transformToDTO)
                .collect(Collectors.toList());
    }

    public static GithubTagData transformToData(GithubTagDTO dto) {
        return new GithubTagData(
                dto.getId(),
                dto.getRepoId(),
                dto.getName(),
                dto.getTagger(),
                dto.getCreatedAt()
        );
    }

    public static List<GithubTagData> transformToData(List<GithubTagDTO> dtos) {
        return dtos.stream().map(GithubTagMapper::transformToData).collect(Collectors.toList());
    }
}
