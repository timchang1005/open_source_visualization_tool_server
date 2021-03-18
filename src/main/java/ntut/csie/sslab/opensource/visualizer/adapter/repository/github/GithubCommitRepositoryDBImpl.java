package ntut.csie.sslab.opensource.visualizer.adapter.repository.github;

import com.google.common.collect.Lists;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class GithubCommitRepositoryDBImpl implements GithubCommitRepository {

    private final GithubCommitRepositoryPeer githubCommitRepositoryPeer;

    @Autowired
    public GithubCommitRepositoryDBImpl(GithubCommitRepositoryPeer githubCommitRepositoryPeer) {
        this.githubCommitRepositoryPeer = githubCommitRepositoryPeer;
    }

    @Override
    public List<GithubCommitDTO> findAll() {
        return GithubCommitMapper.transformToDTO(Lists.newArrayList(githubCommitRepositoryPeer.findAll()));
    }

    @Override
    public Optional<GithubCommitDTO> findById(String id) {
        Optional<GithubCommitData> data = githubCommitRepositoryPeer.findById(id);
        return Optional.ofNullable(data.isPresent() ? GithubCommitMapper.transformToDTO(data.get()) : null);
    }

    @Override
    public void save(GithubCommitDTO commitDTO) {
        githubCommitRepositoryPeer.save(GithubCommitMapper.transformToData(commitDTO));
    }

    @Override
    public void save(List<GithubCommitDTO> commitDTOs) {
        githubCommitRepositoryPeer.saveAll(GithubCommitMapper.transformToData(commitDTOs));
    }

    @Override
    public void deleteById(String id) {
        githubCommitRepositoryPeer.deleteById(id);
    }

    @Override
    public List<GithubCommitDTO> findBy(String repoOwner, String repoName) {
        return GithubCommitMapper.transformToDTO(githubCommitRepositoryPeer.findByRepoOwnerAndRepoName(repoOwner, repoName));
    }

    @Override
    public List<GithubCommitDTO> findSince(String repoOwner, String repoName, Instant sinceTime) {
        List<GithubCommitData> commits = githubCommitRepositoryPeer.findByRepoOwnerAndRepoNameAndCommittedDateAfter(repoOwner, repoName, sinceTime);
        return GithubCommitMapper.transformToDTO(commits);
    }

    @Override
    public Optional<GithubCommitDTO> findLastest(String repoOwner, String repoName) {
        Optional<GithubCommitData> commit = githubCommitRepositoryPeer.findFirstByRepoOwnerAndRepoNameOrderByCommittedDateDesc(repoOwner, repoName);
        if(commit.isPresent()) {
            return Optional.of(GithubCommitMapper.transformToDTO(commit.get()));
        }
        return Optional.empty();
    }
}
