package com.mygym.crm.backstages.core.services;

import com.mygym.crm.backstages.annotations.security.SecureMethod;
import com.mygym.crm.backstages.core.dtos.request.traineedto.TraineeDto;
import com.mygym.crm.backstages.core.dtos.security.SecurityDto;
import com.mygym.crm.backstages.domain.models.Trainee;
import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.domain.models.Training;
import com.mygym.crm.backstages.exceptions.custom.NoTraineeException;
import com.mygym.crm.backstages.interfaces.daorepositories.TraineeDao;
import com.mygym.crm.backstages.interfaces.services.TraineeService;
import com.mygym.crm.backstages.interfaces.services.TrainerService;
import com.mygym.crm.backstages.interfaces.services.TrainingService;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private final TraineeDao traineeDao;
    private final UserService userService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public TraineeServiceImpl(@Qualifier("traineeDaoImpl") TraineeDao traineeDao, UserService userService,
                              TrainerService trainerService, TrainingService trainingService) {
        this.traineeDao = traineeDao;
        this.userService = userService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @Transactional
    @Override
    public Optional<Trainee> create(TraineeDto traineeDto) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            Trainee newTrainee = map(traineeDto);
            newTrainee.setIsActive(true);

            logger.info("Trying to generate new password while attempting to create a new trainee");
            newTrainee.setPassword(userService.generatePassword());

            logger.info("Trying to generate new username while attempting to create a new trainee");
            newTrainee.setUserName(userService.generateUserName(traineeDto));

            logger.info("Trying to create new trainee with UserName: {}", newTrainee.getUserName());
            Optional<Trainee> optionalTrainee = traineeDao.create(newTrainee);

            optionalTrainee.ifPresentOrElse(
                    (trainee) -> logger.info("trainee with userName: {} has been created", trainee.getUserName()),
                    () -> logger.warn("trainee with userName: {} was not created", newTrainee.getUserName())
            );

            return optionalTrainee;
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional
    @SecureMethod
    @Override
    public Optional<Trainee> update(SecurityDto securityDto, Long id, TraineeDto traineeDto) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            Trainee oldTrainee = getById(securityDto, id).orElseThrow(() -> {
                logger.error("Trainee with ID: {} not found", id);
                return new NoTraineeException("could not find trainee with id " + id);
            });
            Trainee newTrainee = map(traineeDto);

            logger.info("Setting with old UserId Password and UserName");
            newTrainee.setUserId(oldTrainee.getUserId());
            newTrainee.setPassword(oldTrainee.getPassword());
            newTrainee.setUserName(oldTrainee.getUserName());
            newTrainee.setTrainings(oldTrainee.getTrainings());
            newTrainee.setIsActive(oldTrainee.getIsActive());

            logger.info("Trying to update Trainee with ID: {}", id);
            Optional<Trainee> optionalTrainee = traineeDao.update(newTrainee);

            optionalTrainee.ifPresentOrElse(
                    (trainee) -> {
                        logger.info("trainee with ID: {} has been updated", trainee.getUserId());
                        trainee.getTrainings().size();
                    },
                    () -> logger.warn("trainee with ID: {} was not updated", newTrainee.getUserId())
            );

            return optionalTrainee;
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional
    @SecureMethod
    @Override
    public Optional<Trainee> updateByUserName(SecurityDto securityDto, String userName, TraineeDto traineeDto) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            Trainee oldTrainee = getByUserName(securityDto, userName).orElseThrow(() -> {
                logger.error("Trainee with UserName {} not found", userName);
                return new NoTraineeException("could not find trainee with UserName " + userName);
            });
            Trainee newTrainee = map(traineeDto);

            logger.info("Setting with old UserId Password and UserName inside updateByUserName");
            newTrainee.setUserId(oldTrainee.getUserId());
            newTrainee.setPassword(oldTrainee.getPassword());
            newTrainee.setUserName(oldTrainee.getUserName());
            newTrainee.setTrainings(oldTrainee.getTrainings());
            newTrainee.setIsActive(oldTrainee.getIsActive());

            logger.info("Trying to update Trainee with userName: {}", userName);
            Optional<Trainee> optionalTrainee = traineeDao.update(newTrainee);

            optionalTrainee.ifPresentOrElse(
                    (trainee) -> {
                        logger.info("trainee with userName: {} has been updated", trainee.getUserName());
                        trainee.getTrainings().size();
                    },
                    () -> logger.warn("trainee with userName: {} was not updated", newTrainee.getUserName())
            );

            return optionalTrainee;
        } finally {
            MDC.remove("transactionId");
        }
    }

//    public Optional<Set<Trainer>> updateTraineeTrainers(SecurityDto securityDto, String userName,
//                                                        Set<String> trainerUserNames) {
//        logger.info("Trying to delete Trainings with Trainee userName: {}", userName);
//
//        int deletedRows = trainingService.deleteWithTraineeUsername(userName);
//
//        if (deletedRows > 0) logger.info("Successfully deleted Trainings with Trainee userName: {}", userName);
//        else logger.warn("Failed to delete Trainings with Trainee userName: {}", userName);
//
//        for (String trainerUserName : trainerUserNames) {
//            Optional<Trainer> optionalTrainer = trainerService.getByUserName(trainerUserName);
//            trainingService.add()
//        }
//
//    }

    @Transactional
    @SecureMethod
    @Override
    public Optional<Trainee> delete(SecurityDto securityDto, Long id) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Trying to delete Trainee with ID: {}", id);
            Optional<Trainee> optionalTrainee = traineeDao.delete(id);

            optionalTrainee.ifPresentOrElse(
                    (trainee) -> logger.info("trainee with userId: {} has been deleted", trainee.getUserId()),
                    () -> logger.warn("trainee with userId: {} was not deleted", id)
            );

            return optionalTrainee;
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional
    @SecureMethod
    @Override
    public Optional<Trainee> deleteWithUserName(SecurityDto securityDto, String userName) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Trying to delete Trainee with userName: {}", userName);
            Optional<Trainee> optionalTrainee = traineeDao.deleteWithUserName(userName);

            optionalTrainee.ifPresentOrElse(
                    (trainee) -> logger.info("trainee with userName: {} has been deleted", trainee.getUserName()),
                    () -> logger.warn("trainee with userName: {} can't be found", userName)
            );

            return optionalTrainee;
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional(noRollbackFor = HibernateException.class, readOnly = true)
    @SecureMethod
    @Override
    public Optional<Trainee> getById(SecurityDto securityDto, Long id) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Trying to find Trainee with ID: {}", id);

            Optional<Trainee> traineeOptional = traineeDao.select(id);

            traineeOptional.ifPresentOrElse(
                    trainee -> {
                        trainee.getTrainings().size();
                        logger.info("Found Trainee with ID: {}", id);
                    },
                    () -> logger.warn("No Trainee found with ID: {}", id)
            );

            return traineeOptional;
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional(noRollbackFor = HibernateException.class, readOnly = true)
    @SecureMethod
    @Override
    public Optional<Trainee> getByUserName(SecurityDto securityDto, String userName) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Trying to find Trainee with UserName: {}", userName);

            Optional<Trainee> traineeOptional = traineeDao.selectWithUserName(userName);

            traineeOptional.ifPresentOrElse(
                    trainee -> {
                        logger.info("Found Trainee with UserName: {}", userName);
                    },
                    () -> logger.warn("No Trainee found with UserName: {}", userName)
            );

            return traineeOptional;
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional
    @SecureMethod
    @Override
    public boolean changePassword(SecurityDto securityDto, String username, String newPassword) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Trying to change password for Trainee with UserName: {}", username);

            boolean success = traineeDao.changePassword(username, newPassword);

            if (success) {
                logger.info("Successfully changed password for Trainee with UserName: {}", username);
                return true;
            } else {
                logger.warn("Failed to change password for Trainee with UserName: {}", username);
                return false;
            }
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional
    @SecureMethod
    @Override
    public boolean toggleIsActive(SecurityDto securityDto, String username) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Trying to toggle isActive for Trainee with UserName: {}", username);

            boolean success = traineeDao.toggleIsActive(username);

            if (success) {
                logger.info("Successfully toggled isActive for Trainee with UserName: {}", username);
                return true;
            } else {
                logger.warn("Failed to toggled isActive for Trainee with UserName: {}", username);
                return false;
            }
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional(noRollbackFor = HibernateException.class, readOnly = true)
    @SecureMethod
    @Override
    public Optional<Set<Training>> getTraineeTrainings(SecurityDto securityDto, String username, LocalDate fromDate,
                                                       LocalDate toDate, String trainerName, String trainingTypeName) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            Set<Training> trainings = traineeDao.getTraineeTrainings(username, fromDate, toDate, trainerName, trainingTypeName);
            if (trainings.isEmpty()) {
                logger.warn("No training found for Trainee with UserName: {}", username);
            } else
                logger.info("training record of size: {} was found for Trainee with UserName: {}", trainings.size(), username);
            return Optional.of(trainings);
        } finally {
            MDC.remove("transactionId");
        }
    }

    @Transactional(noRollbackFor = HibernateException.class, readOnly = true)
    @SecureMethod
    @Override
    public Optional<Set<Trainer>> getTrainersNotTrainingTraineesWithUserName(SecurityDto securityDto,
                                                                             String traineeUserName) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            Set<Trainer> trainers = traineeDao.getTrainersNotTrainingTraineesWithUserName(traineeUserName);
            if (trainers.isEmpty()) {
                logger.warn("No unassigned trainers found");
            } else
                logger.info("Trainer record of size: {} was found for Trainers not matched with Trainee with username: {}",
                        trainers.size(), traineeUserName);
            return Optional.of(trainers);
        } finally {
            MDC.remove("transactionId");
        }
    }

    private Trainee map(TraineeDto traineeDto) {
        Trainee trainee = new Trainee();
        logger.info("New Trainee, populating it with given traineeDto");

        trainee.setFirstName(traineeDto.getFirstName());
        trainee.setLastName(traineeDto.getLastName());
        trainee.setDateOfBirth(traineeDto.getDateOfBirth());
        trainee.setAddress(traineeDto.getAddress());

        logger.info("New Trainee has been successfully populated with given traineeDto");
        return trainee;
    }
}
