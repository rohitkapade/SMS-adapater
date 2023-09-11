package com.tml.uep.model.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.tml.uep.model.dto.customerquery.CustomerQueryRequest;
import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "customer_query")
public class CustomerQuery extends Auditable<String> {

    @Id
    @SequenceGenerator(
            name = "customer_query_id_seq",
            sequenceName = "customer_query_id_seq",
            allocationSize = 1)
    @GeneratedValue(generator = "customer_query_id_seq")
    private Long id;

    @NotBlank
    private String customerId;

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String query;

    private Long imageId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CustomerQueryStatus status;

    private String assignedTo;


    public CustomerQuery(CustomerQueryRequest customerQueryRequest, String customerId) {
        this.customerId = customerId;
        this.mobileNumber = customerQueryRequest.getMobileNumber();
        this.query = customerQueryRequest.getQuery();
        this.imageId = customerQueryRequest.getImageId();
        this.status = CustomerQueryStatus.NOT_STARTED;
    }

    public CustomerQuery(String customerId, String mobileNumber, String query, Long imageId, CustomerQueryStatus status, String assignedTo) {
        this.customerId = customerId;
        this.mobileNumber = mobileNumber;
        this.query = query;
        this.imageId = imageId;
        this.status = status;
        this.assignedTo = assignedTo;
    }
}
