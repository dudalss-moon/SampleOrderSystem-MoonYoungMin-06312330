package ssemi.order.repository;

import ssemi.order.domain.Sample;

import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    void save(Sample sample);
    Optional<Sample> findById(String id);
    List<Sample> findByName(String keyword);
    List<Sample> findAll();
    boolean existsById(String id);
    boolean existsByName(String name);
    int count();
    int totalStock();
}
