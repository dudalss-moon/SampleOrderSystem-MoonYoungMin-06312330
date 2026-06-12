package ssemi.order.repository.inmemory;

import ssemi.order.domain.Sample;
import ssemi.order.repository.SampleRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemorySampleRepository implements SampleRepository {

    private final Map<String, Sample> store = new LinkedHashMap<>();

    @Override
    public void save(Sample sample) {
        store.put(sample.getId(), sample);
    }

    @Override
    public Optional<Sample> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Sample> findByName(String keyword) {
        return store.values().stream()
            .filter(s -> s.getName().contains(keyword))
            .toList();
    }

    @Override
    public List<Sample> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    @Override
    public boolean existsByName(String name) {
        return store.values().stream().anyMatch(s -> s.getName().equals(name));
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public int totalStock() {
        return store.values().stream().mapToInt(Sample::getStock).sum();
    }
}
