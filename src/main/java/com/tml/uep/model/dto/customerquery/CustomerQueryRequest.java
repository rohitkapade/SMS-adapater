package com.tml.uep.model.dto.customerquery;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class CustomerQueryRequest {

    @NotNull private String mobileNumber;

    @NotNull private String query;

    private Long imageId;

}
