package ntut.csie.sslab.opensource.visualizer.usecase.github.release.load;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Input;

public interface LoadReleaseInput extends Input {
    String getRepoOwner();

    void setRepoOwner(String repoOwner);

    String getRepoName();

    void setRepoName(String repoName);

    String getAccessToken();

    void setAccessToken(String accessToken);
}
