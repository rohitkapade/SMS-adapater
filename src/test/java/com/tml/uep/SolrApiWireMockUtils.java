package com.tml.uep;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.model.ContentLanguageCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class SolrApiWireMockUtils {

    @Value("${solr-api.auth-token}")
    private String solarApiAuthHeader;

    @Value("${solr-api.cv-opty-auth-token}")
    private String solarCvOptyApiAuthHeader;

    @Value("${solr-api.events-endpoint}")
    private String eventsEndpoint;

    @Value("${solr-api.customer-fetch-url}")
    private String customerFetchUrl;

    @Value("${solr-api.product-detail-endpoint}")
    private String productDetailEndpoint;

    @Value("${solr-api.opty-creation-endpoint}")
    private String optyCreationEndpoint;

    @Value("${solr-api.opty-creation-endpoint}")
    private String cvOptyCreationEndpoint;

    @Value("${solr-api.opty-details-endpoint}")
    private String cvOptyDetailsEndpoint;

    @Value("${transliterate-api.rev-app-name}")
    private String REV_APP_NAME;

    @Value("${transliterate-api.rev-api-key}")
    private String REV_API_KEY;

    @Value("${transliterate-api.rev-app-id}")
    private String REV_APP_ID;

    @Value("${transliterate-api.transliterate-url}")
    private String TRANSLITERATE_URL;

    @Value("${transliterate-api.transliterate-domain}")
    private String TRANSLITERATE_DOMAIN;

    @Value("${cbsl-conf-api.conf-call-endpoint}")
    private String CONF_CALL_ENDPOINT;

    @Value("${solr-api.division-list-endpoint}")
    private String DIVISION_LIST_END_POINT;

    @Value("${solr-api.city-list-endpoint}")
    private String CITY_LIST_ENDPOINT;

    @Value("${cbsl-conf-api.auth-key}")
    private String authKey;

    public void stubSolarApiResponse(String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(eventsEndpoint)
                                .withHeader(
                                        "Authorization", equalTo("Bearer " + solarApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubSolarProductDetailApiResponse(
            String requestBody, String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(productDetailEndpoint)
                                .withHeader(
                                        "Authorization", equalTo("Bearer " + solarApiAuthHeader))
                                .withRequestBody(equalTo(requestBody))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubCustomerSolarApiResponse(String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(customerFetchUrl)
                                .withHeader(
                                        "Authorization", equalTo("Bearer " + solarApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubCustomerSolarApiErrorResponse(WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(customerFetchUrl)
                                .withHeader(
                                        "Authorization", equalTo("Bearer " + solarApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(500)
                                                .withBody(""))));
    }

    public void stubSolarApiResponseForScenario(
            String response,
            String scenarioName,
            String scenarioState,
            String nextScenarioState,
            WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(eventsEndpoint)
                                .inScenario(scenarioName)
                                .whenScenarioStateIs(scenarioState)
                                .withHeader(
                                        "Authorization", equalTo("Bearer " + solarApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))
                                .willSetStateTo(nextScenarioState)));
    }

    public void stubOptyCreationSOLRApiSuccessResponse(String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(optyCreationEndpoint)
                                .withHeader(
                                        "Authorization", equalTo("Bearer " + solarApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubCvOptyCreationSOLRApiResponse(String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(cvOptyCreationEndpoint)
                                .withHeader(
                                        "Authorization",
                                        equalTo("Bearer " + solarCvOptyApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubCvOptyDetailsSOLRApiSuccessResponse(
            String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(cvOptyDetailsEndpoint)
                                .withHeader(
                                        "Authorization",
                                        equalTo("Bearer " + solarCvOptyApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubForTransliterationAPISuccessResponse(
            String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post("/")
                                .withHeader("Host", equalTo("localhost:8099"))
                                .withHeader("REV-API-KEY", equalTo(REV_API_KEY))
                                .withHeader("REV-APP-ID", equalTo(REV_APP_ID))
                                .withHeader("REV-APPNAME", equalTo(REV_APP_NAME))
                                .withHeader("cnt_lang", equalTo(ContentLanguageCode.en.toString()))
                                .withHeader("domain", equalTo(TRANSLITERATE_DOMAIN))
                                .withHeader("tgt_lang", equalTo(ContentLanguageCode.en.toString()))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubOptyCreationSOLRApi500ServerErrorResponse(WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(optyCreationEndpoint)
                                .withHeader(
                                        "Authorization", equalTo("Bearer " + solarApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(500)
                                                .withBody(
                                                        "Some Dummy Error Message from SOLR")))); // TODO: Update the body with actual content once SOLR API starts working and is tested.
    }

    public void stubInitiateCbslConfCallSuccessfully(
            WireMockRule cbslConfCallApiRule, String apiEndpoint, String response) {
        cbslConfCallApiRule.stubFor(
                WireMock.post(apiEndpoint)
                        .withHeader("Authkey", equalTo(authKey))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("Accept", "application/json")
                                        .withHeader(HttpHeaders.CONNECTION, "close")
                                        .withStatus(200)
                                        .withBody(response)));
    }

    public void stubDivisionListSOLRApiResponse(String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(DIVISION_LIST_END_POINT)
                                .withHeader(
                                        "Authorization",
                                        equalTo("Bearer " + solarCvOptyApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void stubCityListSOLRApiResponse(String response, WireMockRule solarApiRule) {
        solarApiRule.addStubMapping(
                solarApiRule.stubFor(
                        WireMock.post(CITY_LIST_ENDPOINT)
                                .withHeader(
                                        "Authorization",
                                        equalTo("Bearer " + solarCvOptyApiAuthHeader))
                                .willReturn(
                                        aResponse()
                                                .withHeader("Content-Type", "application/json")
                                                .withHeader("Accept", "application/json")
                                                .withHeader(HttpHeaders.CONNECTION, "close")
                                                .withStatus(200)
                                                .withBody(response))));
    }

    public void verifyTimesSolrApiWasCalled(int times) {
        verify(times, postRequestedFor(urlEqualTo(eventsEndpoint)));
    }

    public void verifyTimesCustomerSolrApiWasCalled(int times) {
        verify(times, postRequestedFor(urlEqualTo(customerFetchUrl)));
    }

    public void verifyTimesOpportunityCreationSolrApiWasCalled(int times) {
        verify(times, postRequestedFor(urlEqualTo(optyCreationEndpoint)));
    }

    public void verifyTimesCvOpportunityCreationSolrApiWasCalled(int times) {
        verify(times, postRequestedFor(urlEqualTo(cvOptyCreationEndpoint)));
    }

    public void verifyTimesCvOpportunityDetailsSolrApiWasCalled(int times) {
        verify(times, postRequestedFor(urlEqualTo(cvOptyDetailsEndpoint)));
    }

    public void verifyTimesTransliterationApiWasCalled(int times) {
        verify(
                times,
                postRequestedFor(urlEqualTo("/")).withHeader("Host", equalTo("localhost:8099")));
    }

    public void verifyTimesDivisionListSolrApiWasCalled(int times) {
        verify(times, postRequestedFor(urlEqualTo(DIVISION_LIST_END_POINT)));
    }

    public void verifyTimesCityListSolrApiWasCalled(int times) {
        verify(times, postRequestedFor(urlEqualTo(CITY_LIST_ENDPOINT)));
    }
}
