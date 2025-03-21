package com.mygym.crm.backstages.repositories.services;


import com.mygym.crm.backstages.core.dtos.request.trainerdto.TrainerDto;
import com.mygym.crm.backstages.core.dtos.security.SecurityDto;
import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.domain.models.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerService extends UserService<TrainerDto, Trainer> {

    List<Training> getTrainerTrainings(SecurityDto securityDTO, String username, LocalDate fromDate, LocalDate toDate,
                                       String traineeName);

    List<Trainer> getTrainersNotTrainingTraineesWithUserName(SecurityDto securityDto,
                                                             String TrainerUserName,String TraineeUserName);
}
