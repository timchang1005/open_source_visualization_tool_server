package ntut.csie.sslab.opensource.visualizer.usecase.github.release;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.util.List;
import java.util.Optional;

public interface GithubReleaseRepository extends Repository<GithubReleaseDTO, String> {
    List<GithubReleaseDTO> findByRepoId(String repoId);
    Optional<GithubReleaseDTO> findLatest(String repoId);
}
