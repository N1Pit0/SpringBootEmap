package com.mygym.crm.backstages.controllers;

import com.mygym.crm.backstages.core.dtos.request.ChangePasswordDto;
import com.mygym.crm.backstages.core.dtos.request.common.CombineUpdateUserDtoWithSecurityDto;
import com.mygym.crm.backstages.core.dtos.request.trainerdto.TrainerDto;
import com.mygym.crm.backstages.core.dtos.response.trainerdto.select.SelectTrainerDto;
import com.mygym.crm.backstages.core.dtos.response.trainerdto.update.UpdateTrainerDto;
import com.mygym.crm.backstages.core.dtos.security.SecurityDto;
import com.mygym.crm.backstages.domain.models.Trainer;
import com.mygym.crm.backstages.exceptions.NoTrainerException;
import com.mygym.crm.backstages.mapper.TrainerMapper;
import com.mygym.crm.backstages.repositories.services.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "users/trainers")
public class TrainerController {
    private TrainerService trainerService;
    private TrainerMapper mapper;

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping(value = "/{userName:.+}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SelectTrainerDto> getTrainerProfile(@PathVariable("userName") String userName,
                                                                @RequestBody SecurityDto securityDto) {

        Optional<Trainer> optionalTrainer = trainerService.getByUserName(securityDto, userName);

        return optionalTrainer.map(mapper::trainerToSelectTrainerDto)
                .map((trainer) -> new ResponseEntity<>(trainer, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<SecurityDto> registerTrainer(@RequestBody TrainerDto trainerDto) {

        Optional<Trainer> optionalTrainer = trainerService.create(trainerDto);

        Trainer trainer1 = optionalTrainer.orElseThrow(() -> new NoTrainerException("no trainer"));

        return new ResponseEntity<>(new SecurityDto(trainer1.getUserName(), trainer1.getPassword()),
                HttpStatus.CREATED);
    }

    @PutMapping(value = {"/{userName:.+}"}, consumes = "application/json", produces = "application/json")
    public ResponseEntity<UpdateTrainerDto> updateTrainerProfile(@PathVariable("userName") String userName,
                                                                 @RequestBody CombineUpdateUserDtoWithSecurityDto<TrainerDto> updateTrainerDtoWithSecurityDto){

        Optional<Trainer> optionalTrainer = trainerService.updateByUserName(updateTrainerDtoWithSecurityDto.getSecurityDto(),
                userName,
                updateTrainerDtoWithSecurityDto.getUserDto());

        return optionalTrainer
                .map(mapper::trainerToUpdateTrainerDto)
                .map((trainee) -> new ResponseEntity<>(trainee, HttpStatus.OK))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.valueOf(405)).build());
    }

    @PutMapping(value = "/{userName:.+}/change-login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> changeLogin(@PathVariable("userName")String userName,
                                            @RequestBody ChangePasswordDto changePasswordDto){

        boolean isPassed = trainerService.changePassword(
                new SecurityDto(userName, changePasswordDto.getOldPassword()),
                userName,
                changePasswordDto.getNewPassword()
        );

        if(isPassed) return ResponseEntity.ok().build();

        return ResponseEntity.notFound().build();
    }
}
