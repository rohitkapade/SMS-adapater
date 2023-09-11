package com.tml.uep.model.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Attachment {
    String name;
    byte[] data;
}
