package com.tml.uep.model;

import java.util.List;
import javax.activation.DataSource;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailMessage {
    private String subject;
    private String body;
    private List<DataSource> attachments;
}
