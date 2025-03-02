package com.mygym.crm.backstages.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_trainer", referencedColumnName = "user_id")
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_trainee",  referencedColumnName = "user_id")
    private Trainee trainee;

    @Column(nullable = false)
    private String trainingName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;

    @Column(nullable = false)

    private LocalDate trainingDate;

    @Column(nullable = false)
    private Integer trainingDuration;

}
