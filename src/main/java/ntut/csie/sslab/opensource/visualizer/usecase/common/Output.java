package ntut.csie.sslab.opensource.visualizer.usecase.common;

public interface Output {
    ExitCode getExitCode();

    void setExitCode(ExitCode code);

    String getMessage();

    void setMessage(String message);
}
