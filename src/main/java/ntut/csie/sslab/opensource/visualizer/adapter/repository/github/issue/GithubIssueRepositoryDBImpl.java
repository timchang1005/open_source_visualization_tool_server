package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.issue;

import com.google.common.collect.Lists;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class GithubIssueRepositoryDBImpl implements GithubIssueRepository {

    private final GithubIssueRepositoryPeer githubIssueRepositoryPeer;

    @Autowired
    public GithubIssueRepositoryDBImpl(GithubIssueRepositoryPeer githubIssueRepositoryPeer) {
        this.githubIssueRepositoryPeer = githubIssueRepositoryPeer;
    }

    @Override
    public List<GithubIssueDTO> findAll() {
        return GithubIssueMapper.transformToDTO(Lists.newArrayList(githubIssueRepositoryPeer.findAll()));
    }

    @Override
    public Optional<GithubIssueDTO> findById(String id) {
        Optional<GithubIssueData> data = githubIssueRepositoryPeer.findById(id);
        return Optional.ofNullable(data.isPresent() ? GithubIssueMapper.transformToDTO(data.get()) : null);
    }

    @Override
    public void save(GithubIssueDTO issueDTO) {
        githubIssueRepositoryPeer.save(GithubIssueMapper.transformToData(issueDTO));
    }

    @Override
    public void save(List<GithubIssueDTO> issueDTOs) {
        githubIssueRepositoryPeer.saveAll(GithubIssueMapper.transformToData(issueDTOs));
    }

    @Override
    public void deleteById(String id) {
        githubIssueRepositoryPeer.deleteById(id);
    }

    @Override
    public List<GithubIssueDTO> findByRepoId(String repoId) {
        return GithubIssueMapper.transformToDTO(githubIssueRepositoryPeer.findByRepoId(repoId));
    }

    @Override
    public List<GithubIssueDTO> findSince(String repoId, Instant sinceTime) {
        List<GithubIssueData> issues = githubIssueRepositoryPeer.findByRepoIdAndCreatedAtAfter(repoId, sinceTime);
        return GithubIssueMapper.transformToDTO(issues);
    }

    @Override
    public Optional<GithubIssueDTO> findLatest(String repoId) {
        Optional<GithubIssueData> issue = githubIssueRepositoryPeer.findFirstByRepoIdOrderByUpdatedAtDesc(repoId);
        if(issue.isPresent()) {
            return Optional.of(GithubIssueMapper.transformToDTO(issue.get()));
        }
        return Optional.empty();
    }
}
