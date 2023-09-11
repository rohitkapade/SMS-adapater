package com.tml.uep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tml.uep.model.OpportunityCreationRequest;
import com.tml.uep.model.entity.Opportunity;
import com.tml.uep.model.entity.ProductLineVcMapping;
import com.tml.uep.repository.OpportunityAuditRepository;
import com.tml.uep.repository.ProductLineVcMappingRepository;
import com.tml.uep.solr_api.CustomerSolrService;
import com.tml.uep.solr_api.OpportunitySolrService;
import com.tml.uep.solr_api.dto.CustomerSolrResponse;
import com.tml.uep.solr_api.dto.opportunity.OpportunitySolrRequest;
import com.tml.uep.solr_api.dto.opportunity.OpportunitySolrResponse;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class OpportunityService {

    @Autowired private ProductLineVcMappingRepository productLineVcMappingRepository;

    @Autowired private OpportunityAuditRepository opportunityAuditRepository;

    @Autowired private CustomerSolrService customerSolrService;

    @Autowired private OpportunitySolrService opportunitySolrService;

    public Optional<OpportunitySolrResponse> createOpportunity(
            OpportunityCreationRequest opportunityCreationRequest) throws JsonProcessingException {

        Optional<ProductLineVcMapping> productLineVcMappingOptional =
                productLineVcMappingRepository.findById(opportunityCreationRequest.getPLId());

        if (productLineVcMappingOptional.isPresent()) {
            ProductLineVcMapping productLineVcMapping = productLineVcMappingOptional.get();
            List<CustomerSolrResponse> customerSolrResponseList =
                    customerSolrService.getCustomerDetails(
                            opportunityCreationRequest.getMobileNumber());
            if (!CollectionUtils.isEmpty(customerSolrResponseList)
                    && customerSolrResponseList.get(0) != null) {
                return callOpportunitySolrService(
                        opportunityCreationRequest, productLineVcMapping, customerSolrResponseList);
            } else {
                log.info(
                        "Customer Info was Not Obtained from Customer SOLR API. Hence, using disposition data to build request for Opty Creation.");
                return callOpportunitySolrService(opportunityCreationRequest, productLineVcMapping);
            }

        } else {
            log.error(
                    "Mandatory Product Line & VC info was not found in DB. Hence, not calling the SOLR API for Opty Creation");
            return Optional.empty();
        }
    }

    private Optional<OpportunitySolrResponse> callOpportunitySolrService(
            OpportunityCreationRequest opportunityCreationRequest,
            ProductLineVcMapping productLineVcMapping,
            List<CustomerSolrResponse> customerSolrResponseList)
            throws JsonProcessingException {
        OpportunitySolrRequest opportunitySolrRequest;
        if (!StringUtils.isEmpty(customerSolrResponseList.get(0).getContactId())) {
            // contact details other than contact id & address details are NOT required.
            opportunitySolrRequest =
                    new OpportunitySolrRequest(
                            customerSolrResponseList.get(0).getContactId(),
                            productLineVcMapping,
                            opportunityCreationRequest);
        } else {
            // All contact & address details are required.
            opportunitySolrRequest =
                    new OpportunitySolrRequest(
                            customerSolrResponseList.get(0),
                            productLineVcMapping,
                            opportunityCreationRequest);
        }

        OpportunitySolrResponse opportunitySolrResponse =
                opportunitySolrService.createOpportunityViaSOLRApi(opportunitySolrRequest);
        saveCreatedOpportunityInDB(opportunityCreationRequest, opportunitySolrResponse);

        return Optional.of(opportunitySolrResponse);
    }

    private Optional<OpportunitySolrResponse> callOpportunitySolrService(
            OpportunityCreationRequest opportunityCreationRequest,
            ProductLineVcMapping productLineVcMapping)
            throws JsonProcessingException {
        // All contact & address details are required.
        OpportunitySolrRequest opportunitySolrRequest =
                new OpportunitySolrRequest(productLineVcMapping, opportunityCreationRequest);

        log.info("OpportunitySolrRequest: {}", opportunitySolrRequest);
        OpportunitySolrResponse opportunitySolrResponse =
                opportunitySolrService.createOpportunityViaSOLRApi(opportunitySolrRequest);
        saveCreatedOpportunityInDB(opportunityCreationRequest, opportunitySolrResponse);

        return Optional.of(opportunitySolrResponse);
    }

    private void saveCreatedOpportunityInDB(
            OpportunityCreationRequest opportunityCreationRequest,
            OpportunitySolrResponse opportunitySolrResponse) {
        Opportunity opportunity =
                new Opportunity(
                        opportunitySolrResponse.getId(),
                        opportunityCreationRequest.getMobileNumber(),
                        opportunityCreationRequest.getConversationId(),
                        OffsetDateTime.now(ZoneId.of("UTC")));

        opportunityAuditRepository.save(opportunity);
    }
}
