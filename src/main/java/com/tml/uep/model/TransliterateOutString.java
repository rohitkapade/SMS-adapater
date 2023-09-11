package com.tml.uep.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransliterateOutString {
    private String apiStatus;
    private String inString;
    private List<String> outString;
}
