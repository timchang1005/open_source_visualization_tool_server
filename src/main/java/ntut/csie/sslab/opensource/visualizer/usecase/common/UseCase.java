package ntut.csie.sslab.opensource.visualizer.usecase.common;

public interface UseCase<I extends Input, O extends Output> {
    void execute(I input, O output);
    I newInput();
}
