package org.ovss.onlinevirtualsimulationsystem.repository;

import org.ovss.onlinevirtualsimulationsystem.entity.ModelTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelTagRepository extends JpaRepository<ModelTagEntity, ModelTagEntity.ModelTagId> {
    List<ModelTagEntity> findByModel_ModelId(Long modelId);
}

