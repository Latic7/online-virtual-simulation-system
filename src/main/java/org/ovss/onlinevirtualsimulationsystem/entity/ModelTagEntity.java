package org.ovss.onlinevirtualsimulationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "modeltag")
@IdClass(ModelTagEntity.ModelTagId.class)
public class ModelTagEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "ModelID")
    private ModelEntity model;

    @Id
    @ManyToOne
    @JoinColumn(name = "TagID")
    private TagEntity tag;

    public static class ModelTagId implements Serializable {
        private Long model;
        private Long tag;

        // equals and hashCode
    }
}
