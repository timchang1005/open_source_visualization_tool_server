package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.controller.github.GithubAuthenticateController;

public interface GithubAuthenticator {
    String authenticate(GithubAuthenticateController.GithubAuthenticateInput input);
}
