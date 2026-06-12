package ssemi.order.service;

import ssemi.order.domain.Sample;
import ssemi.order.repository.SampleRepository;

import java.util.List;

public final class SampleService {

    private final SampleRepository repository;

    public SampleService(SampleRepository repository) {
        this.repository = repository;
    }

    public Sample register(String id, String name, int avgProductionTime, double yield) {
        if (repository.existsById(id))
            throw new IllegalArgumentException("이미 존재하는 시료 ID입니다: " + id);
        if (repository.existsByName(name))
            throw new IllegalArgumentException("이미 존재하는 시료 이름입니다: " + name);
        Sample sample = new Sample(id, name, avgProductionTime, yield, 0);
        repository.save(sample);
        return sample;
    }

    public List<Sample> findAll() {
        return repository.findAll();
    }

    public Sample findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시료 ID입니다: " + id));
    }

    public List<Sample> search(String keyword) {
        return repository.findByName(keyword);
    }
}
