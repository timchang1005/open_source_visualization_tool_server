package ntut.csie.sslab.opensource.visualizer.usecase.common;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    List<T> findAll();

    Optional<T> findById(ID id);

    void save(T t);

    void save(List<T> t);

    void deleteById(ID id);
}
