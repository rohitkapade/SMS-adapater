package com.tml.uep.model.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "uep_product_line_vc_mapping")
@NoArgsConstructor
@AllArgsConstructor
public class ProductLineVcMapping {

    @Id
    @SequenceGenerator(
            name = "ppl_vc_id_sequence",
            sequenceName = "ppl_vc_id_sequence",
            allocationSize = 1)
    @GeneratedValue(generator = "ppl_vc_id_sequence")
    private Long id;

    private String lob;

    private String ppl;

    private String pl;

    private String vcNumber;

    public ProductLineVcMapping(String lob, String ppl, String pl, String vcNumber) {
        this.lob = lob;
        this.ppl = ppl;
        this.pl = pl;
        this.vcNumber = vcNumber;
    }
}
