package ssemi.order.repository;

import ssemi.order.domain.Sample;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SampleRepository {

    private final Map<String, Sample> store = new LinkedHashMap<>();

    public void save(Sample sample) {
        store.put(sample.getId(), sample);
    }

    public Optional<Sample> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Sample> findByName(String keyword) {
        return store.values().stream()
            .filter(s -> s.getName().contains(keyword))
            .toList();
    }

    public List<Sample> findAll() {
        return List.copyOf(store.values());
    }

    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    public boolean existsByName(String name) {
        return store.values().stream().anyMatch(s -> s.getName().equals(name));
    }

    public int count() {
        return store.size();
    }

    public int totalStock() {
        return store.values().stream().mapToInt(Sample::getStock).sum();
    }
}
