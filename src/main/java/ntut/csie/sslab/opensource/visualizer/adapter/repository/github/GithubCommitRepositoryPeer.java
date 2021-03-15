package ntut.csie.sslab.opensource.visualizer.adapter.repository.github;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubCommitRepositoryPeer extends CrudRepository<GithubCommitData, String> {
    List<GithubCommitData> findByRepoOwnerAndRepoName(String repoOwner, String repoName);
}
