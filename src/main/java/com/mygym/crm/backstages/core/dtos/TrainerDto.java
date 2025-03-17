package com.mygym.crm.backstages.core.dtos;

import com.mygym.crm.backstages.core.dtos.common.UserDto;
import com.mygym.crm.backstages.domain.models.TrainingType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrainerDto extends UserDto {
    @NotNull
    private String trainingTypeName;

}
