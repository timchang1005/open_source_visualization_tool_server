package ntut.csie.sslab.opensource.visualizer.adapter.presenter;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GithubUserInfo {
    String login;
    String avatarUrl;
}
