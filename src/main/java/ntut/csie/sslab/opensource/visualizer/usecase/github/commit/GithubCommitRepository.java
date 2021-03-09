package ntut.csie.sslab.opensource.visualizer.usecase.github.commit;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.util.List;

public interface GithubCommitRepository extends Repository<GithubCommitDTO, String> {
    List<GithubCommitDTO> findCommitByRepoOwnerAndRepoName(String repoOwner, String repoName);
}
