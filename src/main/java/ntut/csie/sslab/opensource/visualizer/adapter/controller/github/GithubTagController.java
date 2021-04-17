package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubTagInfo;
import ntut.csie.sslab.opensource.visualizer.adapter.repository.github.tag.GithubTagMapper;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCaseImpl;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.load.LoadTagInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.load.LoadTagUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.load.LoadTagUseCaseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/api/v1")
public class GithubTagController {
    private final GithubTagRepository githubTagRepository;
    private final GithubRepoRepository githubRepoRepository;
    private final LoadTagUseCase loadTagUseCase;

    @Autowired
    public GithubTagController(GithubAPICaller githubAPICaller, GithubTagRepository githubTagRepository, GithubRepoRepository githubRepoRepository) {
        this.githubTagRepository = githubTagRepository;
        this.githubRepoRepository = githubRepoRepository;
        this.loadTagUseCase = new LoadTagUseCaseImpl(githubAPICaller, githubTagRepository, githubRepoRepository);
    }

    @GetMapping("/tags")
    public List<GithubTagInfo> getTags(@RequestParam String repoOwner,
                                       @RequestParam String repoName,
                                       @RequestParam(defaultValue = "1970-01-01T00:00:00Z") Instant sinceTime) {
        Optional<GithubRepoDTO> repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName);
        if(repo.isPresent()) {
            List<GithubTagDTO> tags = githubTagRepository.findSince(repo.get().getId(), sinceTime);
            return GithubTagInfo.fromDTO(tags, repoOwner, repoName);
        }
        return new ArrayList<>();
    }

    @PostMapping("/tags")
    public Output getTags(@RequestBody LoadTagsRequest request) {
        LoadTagInput input = loadTagUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(request.getRepoOwner());
        input.setRepoName(request.getRepoName());
        input.setAccessToken(request.getAccessToken());

        loadTagUseCase.execute(input, output);

        if(output.getMessage() != null) {
            System.out.println("something wrong");
            throw new RuntimeException(output.getMessage());
        }

        return output;
    }

    @Data
    private static class LoadTagsRequest {
        private String repoOwner;
        private String repoName;
        private String accessToken;
    }
}
