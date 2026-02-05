package com.keeplynk.ai.memory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class MemoryService {

    private final AgentMemoryRepository repo;

    public MemoryService(AgentMemoryRepository repo) {
        this.repo = repo;
    }

    public String reuseOrCreate(String rawTag, String userId) {

        String normalized = TagNormalizer.normalize(rawTag);

        // 1️⃣ Exact match for this user
        Optional<AgentMemory> exact =
            repo.findByUserIdAndTypeAndValue(userId, "TAG", normalized);

        if (exact.isPresent()) {
            increment(exact.get());
            return exact.get().getValue();
        }

        // 2️⃣ Alias match for this user
        Optional<AgentMemory> alias =
            repo.findByUserIdAndTypeAndAliasesContaining(userId, "TAG", normalized);

        if (alias.isPresent()) {
            increment(alias.get());
            return alias.get().getValue();
        }

        // 3️⃣ Create new for this user
        AgentMemory mem = new AgentMemory();
        mem.setUserId(userId);
        mem.setType("TAG");
        mem.setValue(normalized);
        mem.setAliases(List.of(rawTag.toLowerCase()));
        mem.setUsageCount(1);
        mem.setCreatedAt(Instant.now());
        mem.setLastUsedAt(Instant.now());

        repo.save(mem);
        return normalized;
    }
    
    public String reuseOrCreateCategory(String rawCategory, String userId) {

        if (rawCategory == null || rawCategory.isBlank()) {
            return "General";
        }

        String normalized = rawCategory.trim();

        // 1️⃣ Exact match for this user
        Optional<AgentMemory> exact =
                repo.findByUserIdAndTypeAndValue(userId, "CATEGORY", normalized);

        if (exact.isPresent()) {
            increment(exact.get());
            return exact.get().getValue();
        }

        // 2️⃣ Alias match for this user
        Optional<AgentMemory> alias =
                repo.findByUserIdAndTypeAndAliasesContaining(userId, "CATEGORY", normalized);

        if (alias.isPresent()) {
            increment(alias.get());
            return alias.get().getValue();
        }

        // ⚠️ Guardrail: limit category creation per user
        long categoryCount = repo.countByUserIdAndType(userId, "CATEGORY");
        if (categoryCount >= 12) {
            return "General";
        }

        // 3️⃣ Create new for this user
        AgentMemory mem = new AgentMemory();
        mem.setUserId(userId);
        mem.setType("CATEGORY");
        mem.setValue(normalized);
        mem.setAliases(List.of(rawCategory));
        mem.setUsageCount(1);
        mem.setCreatedAt(Instant.now());
        mem.setLastUsedAt(Instant.now());

        repo.save(mem);
        return normalized;
    }


    private void increment(AgentMemory mem) {
        mem.setUsageCount(mem.getUsageCount() + 1);
        mem.setLastUsedAt(Instant.now());
        repo.save(mem);
    }
}
