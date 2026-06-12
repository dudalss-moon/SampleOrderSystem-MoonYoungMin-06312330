package ssemi.order.repository;

import ssemi.order.domain.Sample;

import java.util.ArrayList;
import java.util.List;

public final class SampleRepository {

    private final List<Sample> samples = new ArrayList<>();

    public void save(Sample sample) {
        samples.add(sample);
    }

    public List<Sample> findAll() {
        return List.copyOf(samples);
    }

    public int count() {
        return samples.size();
    }

    public int totalStock() {
        return samples.stream().mapToInt(Sample::getStock).sum();
    }
}
