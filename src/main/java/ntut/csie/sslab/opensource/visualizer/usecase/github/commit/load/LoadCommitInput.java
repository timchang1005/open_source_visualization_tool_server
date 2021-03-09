package ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Input;

import java.time.Instant;

public interface LoadCommitInput extends Input {
    String getRepoOwner();

    void setRepoOwner(String repoOwner);

    String getRepoName();

    void setRepoName(String repoName);

    Instant getSinceTime();

    void setSinceTime(Instant instant);

    String getAccessToken();

    void setAccessToken(String token);
}
