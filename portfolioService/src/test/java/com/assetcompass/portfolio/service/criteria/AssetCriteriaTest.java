package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AssetCriteriaTest {

    @Test
    void newAssetCriteriaHasAllFiltersNullTest() {
        var assetCriteria = new AssetCriteria();
        assertThat(assetCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void assetCriteriaFluentMethodsCreatesFiltersTest() {
        var assetCriteria = new AssetCriteria();

        setAllFilters(assetCriteria);

        assertThat(assetCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void assetCriteriaCopyCreatesNullFilterTest() {
        var assetCriteria = new AssetCriteria();
        var copy = assetCriteria.copy();

        assertThat(assetCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(assetCriteria)
        );
    }

    @Test
    void assetCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var assetCriteria = new AssetCriteria();
        setAllFilters(assetCriteria);

        var copy = assetCriteria.copy();

        assertThat(assetCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(assetCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var assetCriteria = new AssetCriteria();

        assertThat(assetCriteria).hasToString("AssetCriteria{}");
    }

    private static void setAllFilters(AssetCriteria assetCriteria) {
        assetCriteria.id();
        assetCriteria.ticket();
        assetCriteria.category();
        assetCriteria.country();
        assetCriteria.description();
        assetCriteria.distinct();
    }

    private static Condition<AssetCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTicket()) &&
                condition.apply(criteria.getCategory()) &&
                condition.apply(criteria.getCountry()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AssetCriteria> copyFiltersAre(AssetCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTicket(), copy.getTicket()) &&
                condition.apply(criteria.getCategory(), copy.getCategory()) &&
                condition.apply(criteria.getCountry(), copy.getCountry()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
