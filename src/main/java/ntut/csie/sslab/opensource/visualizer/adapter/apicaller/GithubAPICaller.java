package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.controller.github.GetUserInfoController;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;

public interface GithubAPICaller {
    GithubUserInfo getUserInfo(GetUserInfoController.GetUserInfoInput input);
}
