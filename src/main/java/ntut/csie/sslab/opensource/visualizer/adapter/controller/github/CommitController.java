package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubCommitInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path="/api/v1")
public class CommitController {
    private final GithubCommitRepository githubCommitRepository;

    @Autowired
    public CommitController(GithubCommitRepository githubCommitRepository) {
        this.githubCommitRepository = githubCommitRepository;
    }

    @GetMapping("/commits")
    public List<GithubCommitInfo> getCommits(@RequestParam String repoOwner, @RequestParam String repoName) {
        return GithubCommitInfo.fromDTO(githubCommitRepository.findCommitByRepoOwnerAndRepoName(repoOwner, repoName));
    }
}
