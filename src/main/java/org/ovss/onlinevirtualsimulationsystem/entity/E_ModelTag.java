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
@IdClass(E_ModelTag.ModelTagId.class)
public class E_ModelTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "ModelID")
    private E_Model model;

    @Id
    @ManyToOne
    @JoinColumn(name = "TagID")
    private E_Tag tag;

    public static class ModelTagId implements Serializable {
        private Long model;
        private Long tag;

        // equals and hashCode
    }
}
