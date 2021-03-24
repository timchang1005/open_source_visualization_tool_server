package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.commit;

import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;

import java.util.List;
import java.util.stream.Collectors;

public class GithubCommitMapper {

    public static GithubCommitDTO transformToDTO(GithubCommitData data) {
        return new GithubCommitDTO(
                data.getId(),
                data.getRepoId(),
                data.getAuthor(),
                data.getCommittedDate(),
                data.getAdditions(),
                data.getDeletions()
        );
    }

    public static List<GithubCommitDTO> transformToDTO(List<GithubCommitData> datas) {
        return datas.stream()
                .map(GithubCommitMapper::transformToDTO)
                .collect(Collectors.toList());
    }

    public static GithubCommitData transformToData(GithubCommitDTO dto) {
        return new GithubCommitData(
                dto.getId(),
                dto.getRepoId(),
                dto.getAuthor(),
                dto.getCommittedDate(),
                dto.getAdditions(),
                dto.getDeletions()
        );
    }

    public static List<GithubCommitData> transformToData(List<GithubCommitDTO> dtos) {
        return dtos.stream().map(GithubCommitMapper::transformToData).collect(Collectors.toList());
    }
}
