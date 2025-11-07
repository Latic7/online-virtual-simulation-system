package org.ovss.onlinevirtualsimulationsystem.repository;

import org.ovss.onlinevirtualsimulationsystem.entity.ModelEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<ModelEntity, Long> {
    @Query("SELECT DISTINCT m FROM ModelEntity m " +
            "LEFT JOIN m.uploader u " +
            "LEFT JOIN m.tags mt " +
            "LEFT JOIN mt.tag t " +
            "WHERE LOWER(m.modelName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<ModelEntity> searchModels(@Param("search") String search, Sort sort);
}
