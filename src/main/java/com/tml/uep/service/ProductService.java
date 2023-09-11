package com.tml.uep.service;

import com.tml.uep.model.ProductLine;
import com.tml.uep.model.entity.ProductLineVcMapping;
import com.tml.uep.repository.ProductLineVcMappingRepository;
import com.tml.uep.solr_api.ProductSolrService;
import com.tml.uep.solr_api.dto.ProductDetailResponse;
import com.tml.uep.solr_api.dto.ProductLineResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    @Autowired private ProductSolrService productSolrService;

    @Autowired private ProductLineVcMappingRepository repository;

    public List<ProductLine> getAllProductLines() {
        List<ProductLineVcMapping> all = this.repository.findAll();
        return all.stream()
                .map(value -> new ProductLine(value.getId(), value.getPl()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void syncProductLines() {
        ProductDetailResponse lobs = productSolrService.getLobs();
        if (lobs.getData() != null && !lobs.getData().isEmpty()) {
            lobs.getData()
                    .forEach(
                            lob -> {
                                ProductDetailResponse ppls = productSolrService.getPplForLob(lob);
                                if (ppls.getData() != null && !ppls.getData().isEmpty()) {
                                    ppls.getData()
                                            .forEach(
                                                    ppl -> {
                                                        ProductLineResponse pls =
                                                                productSolrService
                                                                        .getPlForPplAndLob(
                                                                                lob, ppl);
                                                        if (pls.getData() != null
                                                                && !pls.getData().isEmpty()) {
                                                            List<ProductLineVcMapping>
                                                                    plVcMappings =
                                                                            getProductVcMapping(
                                                                                    lob, ppl, pls);
                                                            repository.deleteByPpl(ppl);
                                                            repository.saveAll(plVcMappings);
                                                        }
                                                    });
                                }
                            });
        }
    }

    private List<ProductLineVcMapping> getProductVcMapping(
            String lob, String ppl, ProductLineResponse pls) {
        return pls.getData().stream()
                .map(pl -> new ProductLineVcMapping(lob, ppl, pl.get(0), pl.get(1)))
                .collect(Collectors.toList());
    }
}
