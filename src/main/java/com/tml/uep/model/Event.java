package com.tml.uep.model;

public enum Event {
    NEXT_SERVICE_REMINDER("next_service_reminder"),
    SERVICING_BENEFITS("delivered_to_customer"),
    READY_FOR_DELIVERY("ready_for_delivery"),
    SERVICE_APPOINTMENT("service_appointment"),
    SERVICE_APPOINTMENT_PICKUP_DROP("service_appointment_pickupdrop"),
    WELCOME_LETTER("delivered_to_customer"),
    CUSTOMER_APP_LINKS("delivered_to_customer"),
    WORKSHOP_TOUR_VIDEO("delivered_to_customer"),
    SERVICE_QUALITY_FEEDBACK_LINK("service_request_closed"),
    SERVICE_INSTANT_FEEDBACK_LINK("service_request_closed"),
    SERVICE_INVOICE("service_invoice"),
    SERVICE_PROFORMA("service_proforma"),
    SERVICE_PAYMENT_RECEIPT("service_payment_receipt"),
    SERVICE_APPOINTMENT_BEFORE_DEALERSHIP_CONFIRMATION("service_booking"),
    SERVICE_JOB_CARD("service_job_card");

    private String eventName;

    Event(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String toString() {
        return this.eventName;
    }
}
