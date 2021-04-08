package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubIssueInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load.LoadIssueInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load.LoadIssueUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load.LoadIssueUseCaseImpl;
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
public class GithubIssueController {
    private final GithubIssueRepository githubIssueRepository;
    private final GithubRepoRepository githubRepoRepository;
    private final LoadIssueUseCase loadIssueUseCase;

    @Autowired
    public GithubIssueController(GithubAPICaller githubAPICaller, GithubIssueRepository githubIssueRepository, GithubRepoRepository githubRepoRepository) {
        this.githubIssueRepository = githubIssueRepository;
        this.githubRepoRepository = githubRepoRepository;
        this.loadIssueUseCase = new LoadIssueUseCaseImpl(githubAPICaller, githubIssueRepository, githubRepoRepository);
    }

    @GetMapping("/issues")
    public List<GithubIssueInfo> getIssues(@RequestParam String repoOwner,
                                           @RequestParam String repoName,
                                           @RequestParam(defaultValue = "1970-01-01T00:00:00Z") Instant sinceTime) {
        Optional<GithubRepoDTO> repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName);
        if (repo.isPresent()) {
            List<GithubIssueDTO> issues = githubIssueRepository.findSince(repo.get().getId(), sinceTime);
            return GithubIssueInfo.fromDTO(issues, repoOwner, repoName);
        }
        return new ArrayList<>();
    }

    @PostMapping("/issues")
    public Output loadIssues(@RequestBody LoadIssueRequest request) {
        LoadIssueInput input = loadIssueUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(request.getRepoOwner());
        input.setRepoName(request.getRepoName());
        input.setAccessToken(request.getAccessToken());

        loadIssueUseCase.execute(input, output);

        return output;
    }

    @Data
    private static class LoadIssueRequest {
        private String repoOwner;
        private String repoName;
        private String accessToken;
    }
}
