package com.keeplynk.ai.memory;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgentMemoryRepository
extends MongoRepository<AgentMemory, String> {

Optional<AgentMemory> findByTypeAndValue(
String type,
String value
);

Optional<AgentMemory> findByTypeAndAliasesContaining(
String type,
String alias
);

long countByType(String type);

// User-specific queries
Optional<AgentMemory> findByUserIdAndTypeAndValue(
String userId,
String type,
String value
);

Optional<AgentMemory> findByUserIdAndTypeAndAliasesContaining(
String userId,
String type,
String alias
);

long countByUserIdAndType(String userId, String type);
}