package com.mygym.crm.backstages.controllers;

import com.mygym.crm.backstages.core.dtos.request.ChangePasswordDto;
import com.mygym.crm.backstages.core.dtos.request.common.CombineUserDtoWithSecurityDto;
import com.mygym.crm.backstages.core.dtos.request.traineedto.TraineeDto;
import com.mygym.crm.backstages.core.dtos.response.traineedto.select.SelectTraineeDto;
import com.mygym.crm.backstages.core.dtos.response.traineedto.select.SelectTraineeTrainingsDtoSet;
import com.mygym.crm.backstages.core.dtos.response.traineedto.update.UpdateTraineeDto;
import com.mygym.crm.backstages.core.dtos.response.traineedto.select.SelectTrainerNotAssignedDtoSet;
import com.mygym.crm.backstages.core.dtos.security.SecurityDto;
import com.mygym.crm.backstages.core.services.UserService;
import com.mygym.crm.backstages.domain.models.Trainee;
import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.domain.models.Training;
import com.mygym.crm.backstages.exceptions.NoTraineeException;
import com.mygym.crm.backstages.mapper.TraineeMapper;
import com.mygym.crm.backstages.repositories.services.TraineeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(value = "/users/trainees")
public class TraineeController {
    private TraineeService traineeService;
    private UserService userService;
    private TraineeMapper mapper;

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setMapper(TraineeMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/{userName:.+}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SelectTraineeDto> getTraineeProfile(@PathVariable("userName") String userName,
                                                              @RequestBody SecurityDto securityDto) throws NoTraineeException {

        Optional<Trainee> optionalTrainee = traineeService.getByUserName(securityDto, userName);

        return optionalTrainee.map(mapper::traineeToSelectTraineeDto)
                .map((trainee) -> new ResponseEntity<>(trainee, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/{userName:.+}/list-trainee-trainings", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SelectTraineeTrainingsDtoSet> getTraineeTrainings(@PathVariable("userName") String userName,
                        @RequestBody SecurityDto securityDto,
                        @RequestParam(name = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
                        @RequestParam(name = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
                        @RequestParam(name = "trainerName", required = false) String trainerName,
                        @RequestParam(name = "trainingTypename", required = false) String trainingTypename) {

        Optional<Set<Training>> optionalTrainings = traineeService.getTraineeTrainings(
                securityDto,
                userName,
                periodFrom,
                periodTo,
                trainerName,
                trainingTypename
        );

        return optionalTrainings
                .map(mapper::trainingToSelectTraineeTrainingDtoSet)
                .map(trainings -> new ResponseEntity<>(trainings, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/{userName:.+}/not-assigned-trainers")
    public ResponseEntity<SelectTrainerNotAssignedDtoSet> getTrainersNotTrainingTraineesWithUserName(
            @PathVariable("userName") String UserName,
            @RequestBody SecurityDto securityDto){

        Optional<Set<Trainer>> optionalTrainings = traineeService.getTrainersNotTrainingTraineesWithUserName(
                securityDto,
                UserName
        );

        return optionalTrainings
                .map(mapper::trainerNotAssignedToSelectTrainerDtoSet)
                .map(trainers -> new ResponseEntity<>(trainers, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<SecurityDto> registerTrainee(@RequestBody TraineeDto traineeDto) {
        userService.validateDto(traineeDto);

        Optional<Trainee> optionalTrainee = traineeService.create(traineeDto);

        Trainee trainee = optionalTrainee.orElseThrow(() -> new NoTraineeException("no trainee"));

        return new ResponseEntity<>(new SecurityDto(trainee.getUserName(), trainee.getPassword()),
                HttpStatus.CREATED);
    }

    @PutMapping(value = {"/{userName:.+}"}, consumes = "application/json", produces = "application/json")
    public ResponseEntity<UpdateTraineeDto> updateTraineeProfile(@PathVariable("userName") String userName,
                         @RequestBody CombineUserDtoWithSecurityDto<TraineeDto> updateTraineeDtoWithSecurityDto) {

        userService.validateDto(updateTraineeDtoWithSecurityDto);
        userService.validateDto(updateTraineeDtoWithSecurityDto.getUserDto());

        Optional<Trainee> optionalTrainee = traineeService.updateByUserName(updateTraineeDtoWithSecurityDto.getSecurityDto(),
                userName,
                updateTraineeDtoWithSecurityDto.getUserDto());

        return optionalTrainee
                .map(mapper::traineeToUpdateTraineeDto)
                .map((trainee) -> new ResponseEntity<>(trainee, HttpStatus.OK))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.valueOf(405)).build());
    }

    @PutMapping(value = "/{userName:.+}/change-login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> changeLogin(@PathVariable("userName") String userName,
                                            @RequestBody ChangePasswordDto changePasswordDto) {

        userService.validateDto(changePasswordDto);

        boolean isPassed = traineeService.changePassword(
                new SecurityDto(userName, changePasswordDto.getOldPassword()),
                userName,
                changePasswordDto.getNewPassword()
        );

        if (isPassed) return ResponseEntity.ok().build();

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping(value = "/{userName:.+}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable("userName") String userName,
                                                     @RequestBody SecurityDto securityDto) {
        Optional<Trainee> optionalTrainee = traineeService.deleteWithUserName(securityDto, userName);

        if (optionalTrainee.isPresent()) {

            return new ResponseEntity<>(HttpStatus.valueOf(204));
        }

        return ResponseEntity.status(HttpStatusCode.valueOf(405)).build();
    }

    @PatchMapping(value = "/{userName:.+}/toggleActive", consumes = "application/json")
    public ResponseEntity<Void> toggleIsActive(@PathVariable("userName") String userName,
                                               @RequestBody SecurityDto securityDto) {

        boolean isPerformed = traineeService.toggleIsActive(securityDto, userName);

        if (isPerformed) {
            return new ResponseEntity<>(HttpStatus.valueOf(200));
        }

        return ResponseEntity.status(HttpStatusCode.valueOf(405)).build();
    }

}
