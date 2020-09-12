package com.tuneit.itc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;

import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.ComparisonProduct;
import com.tuneit.itc.commons.model.rest.NameCodePair;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.ComparisonProductService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;

@Data
@ManagedBean
@ViewScoped
public class ComparisonList implements Serializable {

    @ManagedProperty("#{comparisonProductService}")
    private ComparisonProductService comparisonProductService;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;

    private boolean signedIn = false;
    private long userId;
    private List<ProductResponse.ProductHitSource> foundResults;
    private List<ProductResponse.ProductHitSource> selectedCategoryProducts;
    private Set<String> selectedProductFeatureNames;
    private List<ComparisonCategory> comparisonCategories;
    private int activeTabIndex = 0;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Data
    @AllArgsConstructor
    public static class ComparisonCategory {
        private NameCodePair category;
        private Long count;
    }

    @PostConstruct
    public void init() {
        this.signedIn = LiferayPortletHelperUtil.getThemeDisplay().isSignedIn();
        this.userId = LiferayPortletHelperUtil.getUserId();
        if (isSignedIn()) {
            loadComparisons();
        }
    }

    private void loadComparisons() {
        List<ComparisonProduct> comparisonProducts = comparisonProductService.findAllForUser(userId);
        List<String> productIds = comparisonProducts.stream()
            .map(ComparisonProduct::getProductCode)
            .collect(Collectors.toList());
        if (comparisonProducts.isEmpty()) {
            foundResults = new ArrayList<>();
        } else {
            try {
                List<ProductResponse> body = productsService.getByIdsWithFeatures(productIds)
                    .execute()
                    .body();
                foundResults = body.stream()
                    .map(ProductResponse::getHits)
                    .map(ResponseBase.Hits::getHits)
                    .flatMap(List::stream)
                    .map(ResponseBase.Hit::getSource)
                    .sorted(Comparator.comparing(ProductResponse.ProductHitSource::getCode))
                    .collect(Collectors.toList());
                Map<NameCodePair, Long> productTypesMap = new HashMap<>();
                for (ProductResponse.ProductHitSource product : foundResults) {
                    NameCodePair nomenclatureType = product.getNomenclatureType();
                    if (!productTypesMap.containsKey(nomenclatureType)) {
                        productTypesMap.put(nomenclatureType, 1L);
                    } else {
                        productTypesMap.replace(nomenclatureType, productTypesMap.get(nomenclatureType) + 1);
                    }
                }
                this.comparisonCategories = new ArrayList<>();
                for (Map.Entry<NameCodePair, Long> entry : productTypesMap.entrySet()) {
                    this.comparisonCategories.add(new ComparisonCategory(entry.getKey(), entry.getValue()));
                }
                this.comparisonCategories.sort(Comparator.comparing(comparisonCategory ->
                    comparisonCategory.getCategory().getName())
                );
                selectCategory();
            } catch (IOException exc) {
                log.warn("Error while load comparisons", exc);
                LocalizedFacesMessage.error("ict.comparisons.loading-error");
            }
        }

    }

    public List<ProductResponse.ProductHitSource.ProductFeature> getFeaturesForSelectedProductsByFeatureName(
        String featureName) {
        List<ProductResponse.ProductHitSource.ProductFeature> result = new ArrayList<>();
        if (selectedCategoryProducts == null) {
            return result;
        }
        outerLoop:
        for (ProductResponse.ProductHitSource p : selectedCategoryProducts) {
            if (p.getFeatures() == null) {
                result.add(null);
                continue;
            }
            for (ProductResponse.ProductHitSource.ProductFeature feature : p.getFeatures()) {
                if (feature.getName().equals(featureName)) {
                    result.add(feature);
                    continue outerLoop;
                }
            }
            result.add(null);

        }
        return result;
    }

    public void selectCategory() {
        log.debug("Selected tab id is: " + activeTabIndex);
        if (activeTabIndex == comparisonCategories.size()) {
            activeTabIndex = comparisonCategories.size() - 1;
            if (activeTabIndex < 0) {
                return;
            }
        }
        NameCodePair category = comparisonCategories.get(activeTabIndex).getCategory();
        if (category == null) {
            log.warn("Cann't find category");
            return;
        }
        final NameCodePair findCategoryCopy = category;
        selectedCategoryProducts = foundResults.stream()
            .filter(productHitSource -> productHitSource.getNomenclatureType().equals(findCategoryCopy))
            .collect(Collectors.toList());
        selectedProductFeatureNames = new HashSet<>();
        for (ProductResponse.ProductHitSource selectedProduct : selectedCategoryProducts) {
            if (selectedProduct.getFeatures() == null) {
                continue;
            }
            selectedProductFeatureNames.addAll(selectedProduct.getFeatures().stream()
                .map(ProductResponse.ProductHitSource.ProductFeature::getName)
                .collect(Collectors.toSet()));
        }
    }

    public String getProductImageUrl(ProductResponse.ProductHitSource product) {
        return WindowsPathNormalizer.getProductImageUrl(product);
    }

    public String openProduct(ProductResponse.ProductHitSource productHitSource) {
        return String.format("/catalogue?faces-redirect=true&%s=%s",
            ParamNameConstants.PRODUCT, productHitSource.getCode()
        );
    }

    public void removeProductFromComparison(ProductResponse.ProductHitSource product) {
        ComparisonProduct comparisonProduct = comparisonProductService.findComparison(userId, product.getCode());
        comparisonProduct = comparisonProductService.attachToManager(comparisonProduct);
        comparisonProductService.delete(comparisonProduct);
        if (selectedCategoryProducts.size() == 1) {
            loadComparisons();
        } else {
            loadComparisons();
            //selectCategory();
        }
    }

    public List<String> getSelectedProductFeatureNames() {
        if (this.selectedProductFeatureNames == null) {
            return new ArrayList<>();
        }
        ArrayList<String> features = new ArrayList<>(this.selectedProductFeatureNames);
        features.sort(String::compareTo);
        return features;
    }

    @SneakyThrows
    public String productViewUrl(String productCode) {
        LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
        catalogueUrl.setParameter(ParamNameConstants.PRODUCT, productCode);
        return catalogueUrl.toString();
    }

}
