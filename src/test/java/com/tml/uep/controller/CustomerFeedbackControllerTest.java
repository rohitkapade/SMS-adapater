package com.tml.uep.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.ErrorResponse;
import com.tml.uep.model.Group;
import com.tml.uep.model.dto.CustomerFeedback.CustomerFeedbackRequest;
import com.tml.uep.model.dto.CustomerFeedback.FeedbackSentiment;
import com.tml.uep.model.dto.CustomerFeedback.FeedbackContext;
import com.tml.uep.model.dto.CustomerFeedback.Category;
import com.tml.uep.model.entity.CustomerFeedback;
import com.tml.uep.repository.CustomerFeedbackRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class CustomerFeedbackControllerTest {

    @Autowired private CustomerFeedbackRepository customerFeedbackRepository;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper mapper;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Before
    public void setUp() {
        customerFeedbackRepository.deleteAll();
    }

    @Test
    public void shouldSaveCustomerFeedback() throws Exception {

        CustomerFeedbackRequest request =
                new CustomerFeedbackRequest(
                        "8310603980",
                        Category.PRODUCT,
                        FeedbackSentiment.HAPPY,
                        Group.SALES,
                        false,
                        new FeedbackContext("It is very good", "1-6IYQ8K9", "Nano CX_2012"));

        mockMvc.perform(
                        post("/customer-feedback")
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<CustomerFeedback> all = customerFeedbackRepository.findAll();
        CustomerFeedback customerFeedback = all.get(0);

        Assert.assertNotNull(customerFeedback);
        Assert.assertNotNull(customerFeedback.getFeedbackId());
        Assert.assertNotNull(customerFeedback.getCreatedAt());
        Assert.assertEquals(request.getMobileNumber(), customerFeedback.getMobileNumber());
        Assert.assertEquals(request.getCategory(), customerFeedback.getCategory());
        Assert.assertEquals(request.getFeedbackSentiment(), customerFeedback.getFeedbackSentiment());
        Assert.assertEquals(request.getGroupName(), customerFeedback.getGroupName());
        Assert.assertEquals(
                request.getFeedbackContext().getFeedbackTargetId(),
                customerFeedback.getFeedbackContext().getFeedbackTargetId());
        Assert.assertEquals(
                request.getFeedbackContext().getFeedbackText(),
                customerFeedback.getFeedbackContext().getFeedbackText());
    }

    @Test
    public void shouldReturnBadRequestWithProperErrorMessageWhenRequestHasInvalidEnums()
            throws Exception {

        URL fileUrlForInvalidFeedbackRequest =
                classLoader.getResource(
                        "mockResponses/CustomerFeedback/InvalidCustomerFeedbackRequest.json");
        String invalidCustomerFeedbackRequest =
                Files.readString(Path.of(fileUrlForInvalidFeedbackRequest.toURI()));

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/customer-feedback")
                                        .content(invalidCustomerFeedbackRequest)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals(List.of("Invalid value for category"), errorResponse.getErrors());
        Assert.assertEquals("400", errorResponse.getStatus());
    }

    @Test
    public void shouldReturnBadRequestWithProperErrorMessageWhenRequestIsMissingFields()
            throws Exception {

        URL fileUrlForInvalidFeedbackRequest =
                classLoader.getResource(
                        "mockResponses/CustomerFeedback/MissingFieldsInCustomerFeedbackRequest.json");
        String invalidCustomerFeedbackRequest =
                Files.readString(Path.of(fileUrlForInvalidFeedbackRequest.toURI()));

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/customer-feedback")
                                        .content(invalidCustomerFeedbackRequest)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals(List.of("category must not be null"), errorResponse.getErrors());
        Assert.assertEquals("400", errorResponse.getStatus());
    }

    @Test
    public void shouldFetchCustomerFeedbackBasedOnStartAndEndDateTimeAndGroup() throws Exception {

        OffsetDateTime startDateTime = OffsetDateTime.now();

        CustomerFeedbackRequest feedbackRequest1 =
                new CustomerFeedbackRequest(
                        "8310603980",
                        Category.PRODUCT,
                        FeedbackSentiment.HAPPY,
                        Group.SALES,
                        false,
                        new FeedbackContext("It is very good", "1-6IYQ8K9", "Nano CX_2012"));

        CustomerFeedback customerFeedback1 = new CustomerFeedback(feedbackRequest1);

        CustomerFeedbackRequest feedbackRequest2 =
                new CustomerFeedbackRequest(
                        "8310603980",
                        Category.DEALERSHIP,
                        FeedbackSentiment.NEUTRAL,
                        Group.SALES,
                        true,
                        new FeedbackContext("It is good", "1-6YM79VS", "M/S. DADA MOTORS LTD."));
        CustomerFeedback customerFeedback2 = new CustomerFeedback(feedbackRequest2);

        OffsetDateTime endDateTime = OffsetDateTime.now();

        CustomerFeedbackRequest feedbackRequest3 =
                new CustomerFeedbackRequest(
                        "8310603980",
                        Category.SERVICE,
                        FeedbackSentiment.SAD,
                        Group.SALES,
                        true,
                        new FeedbackContext("It is bad", null,"Annual Maintenance"));
        CustomerFeedback customerFeedback3 = new CustomerFeedback(feedbackRequest3);

        List<CustomerFeedback> customerFeedbacks =
                List.of(customerFeedback1, customerFeedback2, customerFeedback3);
        customerFeedbackRepository.saveAll(customerFeedbacks);

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/customer-feedback")
                                        .param("startDateTime", startDateTime.toString())
                                        .param("endDateTime", endDateTime.toString())
                                        .param("groupName", "SALES"))
                        .andExpect(status().isOk())
                        .andReturn();

        List<CustomerFeedback> customerFeedbackList =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsByteArray(), new TypeReference<>() {});

        Assert.assertNotNull(customerFeedbackList);
        Assert.assertEquals(2, customerFeedbackList.size());
        Assert.assertNotNull(customerFeedbackList.get(0).getFeedbackId());
        Assert.assertEquals(
                feedbackRequest1.getMobileNumber(), customerFeedbackList.get(0).getMobileNumber());
        Assert.assertEquals(
                feedbackRequest1.getCategory(), customerFeedbackList.get(0).getCategory());
        Assert.assertEquals(
                feedbackRequest1.getFeedbackSentiment(),
                customerFeedbackList.get(0).getFeedbackSentiment());
        Assert.assertEquals(
                feedbackRequest1.getGroupName(), customerFeedbackList.get(0).getGroupName());
        Assert.assertEquals(
                feedbackRequest1.getFeedbackContext().getFeedbackTargetId(),
                customerFeedbackList.get(0).getFeedbackContext().getFeedbackTargetId());
        Assert.assertEquals(
                feedbackRequest1.getFeedbackContext().getFeedbackText(),
                customerFeedbackList.get(0).getFeedbackContext().getFeedbackText());

        Assert.assertNotNull(customerFeedbackList.get(1).getFeedbackId());
        Assert.assertEquals(
                feedbackRequest2.getMobileNumber(), customerFeedbackList.get(1).getMobileNumber());
        Assert.assertEquals(
                feedbackRequest2.getCategory(), customerFeedbackList.get(1).getCategory());
        Assert.assertEquals(
                feedbackRequest2.getFeedbackSentiment(),
                customerFeedbackList.get(1).getFeedbackSentiment());
        Assert.assertEquals(
                feedbackRequest2.getGroupName(), customerFeedbackList.get(1).getGroupName());
        Assert.assertEquals(
                feedbackRequest2.getFeedbackContext().getFeedbackTargetId(),
                customerFeedbackList.get(1).getFeedbackContext().getFeedbackTargetId());
        Assert.assertEquals(
                feedbackRequest2.getFeedbackContext().getFeedbackText(),
                customerFeedbackList.get(1).getFeedbackContext().getFeedbackText());
    }

    @Test
    public void shouldReturnBadRequestWithProperErrorMessageWhenGroupQueryParamIsInvalid()
            throws Exception {

        OffsetDateTime startDateTime = OffsetDateTime.now();

        CustomerFeedbackRequest feedbackRequest =
                new CustomerFeedbackRequest(
                        "8310603980",
                        Category.PRODUCT,
                        FeedbackSentiment.HAPPY,
                        Group.SALES,
                        false,
                        new FeedbackContext("It is very good", "1-6IYQ8K9", "Nano CX_2012"));
        CustomerFeedback customerFeedback = new CustomerFeedback(feedbackRequest);
        OffsetDateTime endDateTime = OffsetDateTime.now();
        customerFeedbackRepository.save(customerFeedback);

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/customer-feedback")
                                        .param("startDateTime", startDateTime.toString())
                                        .param("endDateTime", endDateTime.toString())
                                        .param("groupName", "SALE"))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals(
                List.of("Invalid value for parameter groupName"), errorResponse.getErrors());
        Assert.assertEquals("400", errorResponse.getStatus());
    }

    @Test
    public void shouldReturnBadRequestWithProperErrorMessageWhenDatesQueryParamsAreInvalid1()
            throws Exception {

        CustomerFeedbackRequest feedbackRequest =
                new CustomerFeedbackRequest(
                        "8310603980",
                        Category.PRODUCT,
                        FeedbackSentiment.HAPPY,
                        Group.SALES,
                        false,
                        new FeedbackContext("It is very good", "1-6IYQ8K9", "Nano CX_2012"));
        CustomerFeedback customerFeedback = new CustomerFeedback(feedbackRequest);
        customerFeedbackRepository.save(customerFeedback);

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/customer-feedback")
                                        .param("startDateTime", "2000-10-31")
                                        .param("endDateTime", "2000-11-30")
                                        .param("groupName", "SALES"))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals(
                List.of("Invalid value for parameter startDateTime"), errorResponse.getErrors());
        Assert.assertEquals("400", errorResponse.getStatus());
    }
}
