package ntut.csie.sslab.opensource.visualizer.adapter.common;

import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;

public class UseCaseOutput implements Output {
    private ExitCode exitCode;

    @Override
    public ExitCode getExitCode() {
        return exitCode;
    }

    @Override
    public void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }
}
