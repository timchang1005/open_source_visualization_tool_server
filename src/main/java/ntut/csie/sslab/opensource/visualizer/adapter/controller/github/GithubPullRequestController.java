package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubPullRequestInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.load.LoadPullRequestInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.load.LoadPullRequestUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.load.LoadPullRequestUseCaseImpl;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/api/v1")
public class GithubPullRequestController {
    private final GithubPullRequestRepository githubPullRequestRepository;
    private final GithubRepoRepository githubRepoRepository;
    private final LoadPullRequestUseCase loadPullRequestUseCase;

    @Autowired
    public GithubPullRequestController(GithubAPICaller githubAPICaller, GithubPullRequestRepository githubPullRequestRepository, GithubRepoRepository githubRepoRepository) {
        this.githubPullRequestRepository = githubPullRequestRepository;
        this.githubRepoRepository = githubRepoRepository;
        this.loadPullRequestUseCase = new LoadPullRequestUseCaseImpl(githubAPICaller, githubPullRequestRepository, githubRepoRepository);
    }


    @GetMapping("/pulls")
    public List<GithubPullRequestInfo> getPullRequests(@RequestParam String repoOwner,
                                                       @RequestParam String repoName,
                                                       @RequestParam(defaultValue = "1970-01-01T00:00:00Z") Instant sinceTime) {
        Optional<GithubRepoDTO> repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName);
        if (repo.isPresent()) {
            List<GithubPullRequestDTO> pullRequests = githubPullRequestRepository.findSince(repo.get().getId(), sinceTime);
            return GithubPullRequestInfo.fromDTO(pullRequests, repoOwner, repoName);
        }
        return new ArrayList<>();
    }

    @PostMapping("/pulls")
    public Output loadPullRequests(@RequestBody LoadPullRequestRequest request) {
        LoadPullRequestInput input = loadPullRequestUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(request.getRepoOwner());
        input.setRepoName(request.getRepoName());
        input.setAccessToken(request.getAccessToken());

        loadPullRequestUseCase.execute(input, output);

        if (output.getMessage() != null) {
            System.out.println("something wrong during loading pull requests");
            throw new RuntimeException(output.getMessage());
        }
        return output;
    }

    @Data
    private static class LoadPullRequestRequest {
        private String repoOwner;
        private String repoName;
        private String accessToken;
    }
}
