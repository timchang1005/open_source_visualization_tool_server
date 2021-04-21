package ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.load;

import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoadPullRequestUseCaseImpl implements LoadPullRequestUseCase {
    private final GithubAPICaller githubAPICaller;
    private final GithubPullRequestRepository githubPullRequestRepository;
    private final GithubRepoRepository githubRepoRepository;

    @Autowired
    public LoadPullRequestUseCaseImpl(GithubAPICaller githubAPICaller, GithubPullRequestRepository githubPullRequestRepository, GithubRepoRepository githubRepoRepository) {
        this.githubAPICaller = githubAPICaller;
        this.githubPullRequestRepository = githubPullRequestRepository;
        this.githubRepoRepository = githubRepoRepository;
    }

    @Override
    public void execute(LoadPullRequestInput input, Output output) {
        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(input.getRepoOwner(), input.getRepoName()).orElse(null);
        if (repo == null) {
            repo = new GithubRepoDTO(UUID.randomUUID().toString(), input.getRepoOwner(), input.getRepoName());
            githubRepoRepository.save(repo);
        }

        Optional<GithubPullRequestDTO> latestPullRequest = githubPullRequestRepository.findLatest(repo.getId());
        Instant sinceTime = latestPullRequest.isPresent() ? latestPullRequest.get().getUpdatedAt() : Instant.EPOCH;

        try {
            List<GithubPullRequestDTO> pullRequestDTOs = githubAPICaller.getPullRequests(repo.getId(), repo.getOwner(), repo.getName(), sinceTime, input.getAccessToken());
            githubPullRequestRepository.save(pullRequestDTOs);
            output.setExitCode(ExitCode.SUCCESS);
        } catch (Exception e) {
            output.setExitCode(ExitCode.FAILURE);
            output.setMessage(e.getMessage());
        }

    }

    @Override
    public LoadPullRequestInput newInput() {
        return new LoadPullRequestInputImpl();
    }

    private class LoadPullRequestInputImpl implements LoadPullRequestInput {
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
