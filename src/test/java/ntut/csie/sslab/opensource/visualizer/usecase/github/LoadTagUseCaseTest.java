package ntut.csie.sslab.opensource.visualizer.usecase.github;

import ntut.csie.sslab.opensource.visualizer.AbstractUseCaseTest;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.load.LoadTagInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.load.LoadTagUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadTagUseCaseTest extends AbstractUseCaseTest {

    @Test
    public void load_all_tags_from_github() {
        String repoOwner = "timchang1005";
        String repoName = "repo_for_testing";

        LoadTagUseCase loadTagUseCase = newLoadTagUseCase();
        LoadTagInput input = loadTagUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(repoOwner);
        input.setRepoName(repoName);
        input.setAccessToken(getGithubApiToken());

        loadTagUseCase.execute(input, output);

        assertEquals(ExitCode.SUCCESS, output.getExitCode());

        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName).get();
        List<GithubTagDTO> tagDTOs = githubTagRepository.findByRepoId(repo.getId());
        assertEquals(3, tagDTOs.size());
    }
}
