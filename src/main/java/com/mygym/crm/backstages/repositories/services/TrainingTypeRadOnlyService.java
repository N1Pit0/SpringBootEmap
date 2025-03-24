package com.mygym.crm.backstages.repositories.services;

import com.mygym.crm.backstages.domain.models.TrainingType;

import java.util.Optional;
import java.util.Set;

public interface TrainingTypeRadOnlyService {
    Optional<Set<TrainingType>> getTrainingType();
}
