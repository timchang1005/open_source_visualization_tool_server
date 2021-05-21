package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.pullrequest;

import com.google.common.collect.Lists;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class GithubPullRequestRepositoryDBImpl implements GithubPullRequestRepository {
    private final GithubPullRequestRepositoryPeer githubPullRequestRepositoryPeer;

    @Autowired
    public GithubPullRequestRepositoryDBImpl(GithubPullRequestRepositoryPeer githubPullRequestRepositoryPeer) {
        this.githubPullRequestRepositoryPeer = githubPullRequestRepositoryPeer;
        this.githubPullRequestRepositoryPeer.findAll(); // prevent from duplicate PRIMARY KEY
    }

    @Override
    public List<GithubPullRequestDTO> findAll() {
        return GithubPullRequestMapper.transformToDTO(Lists.newArrayList(githubPullRequestRepositoryPeer.findAll()));
    }

    @Override
    public Optional<GithubPullRequestDTO> findById(String id) {
        Optional<GithubPullRequestData> data = githubPullRequestRepositoryPeer.findById(id);
        return Optional.ofNullable(data.isPresent() ? GithubPullRequestMapper.transformToDTO(data.get()) : null);
    }

    @Override
    public void save(GithubPullRequestDTO dto) {
        githubPullRequestRepositoryPeer.save(GithubPullRequestMapper.transformToData(dto));
    }

    @Override
    public void save(List<GithubPullRequestDTO> dtos) {
        githubPullRequestRepositoryPeer.saveAll(GithubPullRequestMapper.transformToData(dtos));
    }

    @Override
    public void deleteById(String id) {
        githubPullRequestRepositoryPeer.deleteById(id);
    }

    @Override
    public List<GithubPullRequestDTO> findByRepoId(String repoId) {
        return GithubPullRequestMapper.transformToDTO(githubPullRequestRepositoryPeer.findByRepoId(repoId));
    }

    @Override
    public Optional<GithubPullRequestDTO> findLatest(String repoId) {
        Optional<GithubPullRequestData> pullRequest = githubPullRequestRepositoryPeer.findFirstByRepoIdOrderByUpdatedAtDesc(repoId);
        if (pullRequest.isPresent()) {
            return Optional.of(GithubPullRequestMapper.transformToDTO(pullRequest.get()));
        }
        return Optional.empty();
    }

    @Override
    public List<GithubPullRequestDTO> findSince(String repoId, Instant sinceTime) {
        List<GithubPullRequestData> pullRequests = githubPullRequestRepositoryPeer.findByRepoIdAndCreatedAtAfter(repoId, sinceTime);
        return GithubPullRequestMapper.transformToDTO(pullRequests);
    }
}
