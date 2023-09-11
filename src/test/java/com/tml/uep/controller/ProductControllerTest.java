package com.tml.uep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.ProductLine;
import com.tml.uep.model.entity.ProductLineVcMapping;
import com.tml.uep.repository.ProductLineVcMappingRepository;
import com.tml.uep.solr_api.dto.ProductDetailRequest;
import com.tml.uep.solr_api.dto.ProductDetailResponse;
import com.tml.uep.solr_api.dto.ProductLineResponse;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class ProductControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ProductLineVcMappingRepository repository;

    @Autowired private ObjectMapper mapper;

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    @Before
    public void setup() {
        this.repository.deleteAll();
        solarApiRule.resetRequests();
    }

    @Test
    public void shouldGetProductDetails() throws Exception {
        repository.save(new ProductLineVcMapping("LOB 1", "PPL 1", "PL 1", "VC 1"));
        repository.save(new ProductLineVcMapping("LOB 2", "PPL 2", "PL 2", "VC 2"));

        MvcResult mvcResult =
                mockMvc.perform(get("/product-lines")).andExpect(status().isOk()).andReturn();

        List<ProductLine> productLines =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertEquals(2, productLines.size());
        Assert.assertNotNull(productLines.get(0).getId());
        Assert.assertNotNull(productLines.get(1).getId());
        Assert.assertEquals("PL 1", productLines.get(0).getPl());
        Assert.assertEquals("PL 2", productLines.get(1).getPl());
    }

    @Test
    public void shouldSyncProductDetails() throws Exception {
        repository.save(new ProductLineVcMapping(1L, "UVs", "Nexon", "Nexon XT", "VC 1"));

        ProductDetailRequest lobRequest = new ProductDetailRequest("lob", null, null);
        ProductDetailResponse lobResponse = new ProductDetailResponse(List.of("UVs"));

        ProductDetailRequest pplRequest = new ProductDetailRequest("ppl", "UVs", null);
        ProductDetailResponse pplResponse = new ProductDetailResponse(List.of("Nexon"));

        ProductDetailRequest plRequest = new ProductDetailRequest("pl", "UVs", "Nexon");
        ProductLineResponse plResponse =
                new ProductLineResponse(
                        List.of(List.of("Nexon XE", "VC1234"), List.of("Nexon XM", "VC45666")));

        mockUtils.stubSolarProductDetailApiResponse(
                mapper.writeValueAsString(lobRequest),
                mapper.writeValueAsString(lobResponse),
                solarApiRule);

        mockUtils.stubSolarProductDetailApiResponse(
                mapper.writeValueAsString(pplRequest),
                mapper.writeValueAsString(pplResponse),
                solarApiRule);

        mockUtils.stubSolarProductDetailApiResponse(
                mapper.writeValueAsString(plRequest),
                mapper.writeValueAsString(plResponse),
                solarApiRule);

        mockMvc.perform(post("/product-lines/sync")).andExpect(status().isOk());

        List<ProductLineVcMapping> productLines = repository.findAll();

        Assert.assertEquals(2, productLines.size());
        Assert.assertEquals("UVs", productLines.get(0).getLob());
        Assert.assertEquals("UVs", productLines.get(1).getLob());
        Assert.assertEquals("Nexon", productLines.get(0).getPpl());
        Assert.assertEquals("Nexon", productLines.get(1).getPpl());
        Assert.assertEquals("Nexon XE", productLines.get(0).getPl());
        Assert.assertEquals("Nexon XM", productLines.get(1).getPl());
        Assert.assertEquals("VC1234", productLines.get(0).getVcNumber());
        Assert.assertEquals("VC45666", productLines.get(1).getVcNumber());
    }
}
