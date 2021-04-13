package ntut.csie.sslab.opensource.visualizer.usecase.github.release.load;

import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.release.GithubReleaseDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.release.GithubReleaseRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

public class LoadReleaseUseCaseImpl implements LoadReleaseUseCase {
    private final GithubAPICaller githubAPICaller;
    private final GithubReleaseRepository githubReleaseRepository;
    private final GithubRepoRepository githubRepoRepository;

    @Autowired
    public LoadReleaseUseCaseImpl(GithubAPICaller githubAPICaller, GithubReleaseRepository githubReleaseRepository, GithubRepoRepository githubRepoRepository) {
        this.githubAPICaller= githubAPICaller;
        this.githubReleaseRepository = githubReleaseRepository;
        this.githubRepoRepository = githubRepoRepository;
    }

    @Override
    public void execute(LoadReleaseInput input, Output output) {
        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(input.getRepoOwner(), input.getRepoName()).orElse(null);
        if (repo == null) {
            repo = new GithubRepoDTO(UUID.randomUUID().toString(), input.getRepoOwner(), input.getRepoName());
            githubRepoRepository.save(repo);
        }

        Optional<GithubReleaseDTO> latestRelease = githubReleaseRepository.findLatest(repo.getId());


    }

    @Override
    public LoadReleaseInput newInput() {
        return new LoadReleaseInputImpl();
    }

    private class LoadReleaseInputImpl implements LoadReleaseInput {
        private String repoOwner;
        private String repoName;
        private String accessToken;

        @Override
        public String getRepoOwner() {
            return repoOwner;
        }

        @Override
        public void setRepoOwner(String repoOwner) {
            this.repoOwner = repoOwner;
        }

        @Override
        public String getRepoName() {
            return repoName;
        }

        @Override
        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        @Override
        public String getAccessToken() {
            return accessToken;
        }

        @Override
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
