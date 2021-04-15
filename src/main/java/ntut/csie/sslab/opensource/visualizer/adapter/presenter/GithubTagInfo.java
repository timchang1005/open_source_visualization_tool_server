package ntut.csie.sslab.opensource.visualizer.adapter.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class GithubTagInfo {
    private String repoOwner;
    private String repoName;
    private String publisher;
    private String tagName;
    private String createdAt;

    public static GithubTagInfo fromDTO(GithubTagDTO tag, String repoOwner, String repoName) {
        return new GithubTagInfo(repoOwner, repoName, tag.getTagger(), tag.getName(), tag.getCreatedAt().toString());
    }

    public static List<GithubTagInfo> fromDTO(List<GithubTagDTO> tags, String repoOwner, String repoName) {
        return tags.stream().map(x -> fromDTO(x, repoOwner, repoName)).collect(Collectors.toList());
    }
}
