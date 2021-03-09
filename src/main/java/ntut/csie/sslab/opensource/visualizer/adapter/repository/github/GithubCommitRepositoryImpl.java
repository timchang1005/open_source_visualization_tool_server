package ntut.csie.sslab.opensource.visualizer.adapter.repository.github;

import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GithubCommitRepositoryImpl implements GithubCommitRepository {

    private List<GithubCommitDTO> commitDTOs;

    public GithubCommitRepositoryImpl() {
        this.commitDTOs = new ArrayList<>();
    }

    @Override
    public List<GithubCommitDTO> findAll() {
        return commitDTOs;
    }

    @Override
    public Optional<GithubCommitDTO> findById(String id) {
        return commitDTOs.stream().filter(commit -> commit.getId().equals(id)).findAny();
    }

    @Override
    public void save(GithubCommitDTO commitDTO) {
        commitDTOs.add(commitDTO);
    }

    @Override
    public void save(List<GithubCommitDTO> commitDTOs) {
        this.commitDTOs.addAll(commitDTOs);
    }

    @Override
    public void deleteById(String id) {
        commitDTOs.removeIf(commit -> commit.getId().equals(id));
    }

    @Override
    public List<GithubCommitDTO> findCommitByRepoOwnerAndRepoName(String repoOwner, String repoName) {
        return commitDTOs.stream()
                .filter(commit -> commit.getRepoOwner().equals(repoOwner) && commit.getRepoName().equals(repoName))
                .collect(Collectors.toList());
    }
}
