package ntut.csie.sslab.opensource.visualizer.usecase.github;

import ntut.csie.sslab.opensource.visualizer.AbstractUseCaseTest;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.release.GithubReleaseDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.release.load.LoadReleaseInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.release.load.LoadReleaseUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadReleaseUseCaseTest extends AbstractUseCaseTest {

    // since that some repo use release and some repo use tag only, and
    // the repo is based on tag, it's not necessary to load release first.
    // To implement only tag is simpler, so I won't implement the function to
    // load release. However, due to the restriction of tag API, I must load
    // commits before loading tags. And for the same reason, storing tags
    // into DB cannot speed up the loading process. Thus, no LoadUseCase
    // for release or tag would be implemented. The client just call the
    // controller, then controller would first load commit and then load tag.

    @Test
    public void load_all_releases_from_github() {
        String repoOwner = "timchang1005";
        String repoName = "repo_for_testing";

        LoadReleaseUseCase loadReleaseUseCase = newLoadReleaseUseCase();
        LoadReleaseInput input = loadReleaseUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(repoOwner);
        input.setRepoName(repoName);
        input.setAccessToken(getGithubApiToken());

        loadReleaseUseCase.execute(input, output);

        assertEquals(ExitCode.SUCCESS, output.getExitCode());

        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName).get();
        List<GithubReleaseDTO> releaseDTOs = githubReleaseRepository.findByRepoId(repo.getId());
        assertEquals(3, releaseDTOs.size());
    }
}
