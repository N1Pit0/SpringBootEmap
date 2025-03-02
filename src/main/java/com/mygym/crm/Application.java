package com.mygym.crm;

import com.mygym.crm.backstages.ApplicationFacade;
import com.mygym.crm.backstages.config.Configs;
import com.mygym.crm.backstages.core.dtos.TraineeDto;
import com.mygym.crm.backstages.core.dtos.TrainerDto;
import com.mygym.crm.backstages.core.dtos.TrainingDto;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Configs.class);
        ApplicationFacade facade = context.getBean(ApplicationFacade.class);

        TraineeDto traineeDto = new TraineeDto();

        traineeDto.setFirstName("John");
        traineeDto.setLastName("Doe");
        traineeDto.setActive(true);

        TraineeDto traineeDto1 = new TraineeDto();

        traineeDto1.setFirstName("John");
        traineeDto1.setLastName("Doe");
        traineeDto1.setActive(true);

        TrainerDto trainerDto = new TrainerDto();

        trainerDto.setFirstName("John");
        trainerDto.setLastName("Doe");
        trainerDto.setActive(true);

        TrainerDto trainerDto1 = new TrainerDto();

        trainerDto1.setFirstName("sad");
        trainerDto1.setLastName("sadasd");
        trainerDto1.setActive(true);

        TrainingDto trainingDto = new TrainingDto();
//        trainingDto.setTrainingKey(trainingKey);
        trainingDto.setTrainingDuration(2);
        trainingDto.setTrainingName("adasdsad");

        System.out.println(facade.selectTrainee(6L));
        facade.createTrainee(traineeDto);
        facade.updateTrainee(6L, traineeDto1);
        facade.deleteTrainee(6L);

        System.out.println(facade.selectTrainer(18L));
        facade.createTrainer(trainerDto);
        facade.updateTrainer(18L, trainerDto1);

    }
}
