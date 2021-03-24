package ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load;

import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class LoadCommitUseCaseImpl implements LoadCommitUseCase {

    private final GithubAPICaller githubAPICaller;
    private final GithubCommitRepository githubCommitRepository;
    private final GithubRepoRepository githubRepoRepository;

    @Autowired
    public LoadCommitUseCaseImpl(GithubAPICaller githubAPICaller, GithubCommitRepository githubCommitRepository, GithubRepoRepository githubRepoRepository) {
        this.githubAPICaller = githubAPICaller;
        this.githubCommitRepository = githubCommitRepository;
        this.githubRepoRepository = githubRepoRepository;
    }

    @Override
    public void execute(LoadCommitInput input, Output output) {
        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(input.getRepoOwner(), input.getRepoName()).orElse(null);
        if (repo == null) {
            repo = new GithubRepoDTO(UUID.randomUUID().toString(), input.getRepoOwner(), input.getRepoName());
            githubRepoRepository.save(repo);
        }

        List<GithubCommitDTO> commitDTOs = githubAPICaller.getCommits(repo.getId(), repo.getOwner(), repo.getName(), input.getSinceTime(), input.getAccessToken());
        githubCommitRepository.save(commitDTOs);

        output.setExitCode(ExitCode.SUCCESS);
    }

    @Override
    public LoadCommitInput newInput() {
        return new LoadCommitInputImpl();
    }

    private class LoadCommitInputImpl implements LoadCommitInput {
        private String repoOwner;
        private String repoName;
        private String accessToken;
        private Instant sinceTime;

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
        public Instant getSinceTime() {
            return sinceTime;
        }

        @Override
        public void setSinceTime(Instant sinceTime) {
            this.sinceTime = sinceTime;
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
