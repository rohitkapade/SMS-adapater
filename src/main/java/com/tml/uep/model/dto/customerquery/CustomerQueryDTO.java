package com.tml.uep.model.dto.customerquery;
import com.tml.uep.model.entity.CustomerQuery;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
@Data
public class CustomerQueryDTO {

    private Long id;

    private String customerId;

    private String mobileNumber;

    private String query;

    private Long imageId;

    private CustomerQueryStatus status;

    private String assignedTo;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private String updatedBy;

    public static CustomerQueryDTO fromCustomerQueryEntity(CustomerQuery customerQuery) {
        OffsetDateTime createdAt = customerQuery.getCreatedAt() != null ? OffsetDateTime.ofInstant(customerQuery.getCreatedAt().toInstant(), ZoneOffset.UTC) : null;
        OffsetDateTime updatedAt = customerQuery.getUpdatedAt() != null ? OffsetDateTime.ofInstant(customerQuery.getUpdatedAt().toInstant(), ZoneOffset.UTC) : null;
        return new CustomerQueryDTO(customerQuery.getId(), customerQuery.getCustomerId(), customerQuery.getMobileNumber(),
                customerQuery.getQuery(), customerQuery.getImageId(), customerQuery.getStatus(), customerQuery.getAssignedTo(),
                createdAt,
                updatedAt,
                customerQuery.getUpdatedBy()
        );
    }
}
