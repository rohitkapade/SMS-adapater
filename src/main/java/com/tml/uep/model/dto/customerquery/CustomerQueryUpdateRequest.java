package com.tml.uep.model.dto.customerquery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@ToString
public class CustomerQueryUpdateRequest {
    @NotNull CustomerQueryStatus status;
    @NotBlank String assignedTo;
    @NotBlank String updatedBy;
}