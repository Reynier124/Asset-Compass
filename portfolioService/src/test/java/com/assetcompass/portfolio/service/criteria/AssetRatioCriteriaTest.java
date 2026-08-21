package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AssetRatioCriteriaTest {

    @Test
    void newAssetRatioCriteriaHasAllFiltersNullTest() {
        var assetRatioCriteria = new AssetRatioCriteria();
        assertThat(assetRatioCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void assetRatioCriteriaFluentMethodsCreatesFiltersTest() {
        var assetRatioCriteria = new AssetRatioCriteria();

        setAllFilters(assetRatioCriteria);

        assertThat(assetRatioCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void assetRatioCriteriaCopyCreatesNullFilterTest() {
        var assetRatioCriteria = new AssetRatioCriteria();
        var copy = assetRatioCriteria.copy();

        assertThat(assetRatioCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(assetRatioCriteria)
        );
    }

    @Test
    void assetRatioCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var assetRatioCriteria = new AssetRatioCriteria();
        setAllFilters(assetRatioCriteria);

        var copy = assetRatioCriteria.copy();

        assertThat(assetRatioCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(assetRatioCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var assetRatioCriteria = new AssetRatioCriteria();

        assertThat(assetRatioCriteria).hasToString("AssetRatioCriteria{}");
    }

    private static void setAllFilters(AssetRatioCriteria assetRatioCriteria) {
        assetRatioCriteria.id();
        assetRatioCriteria.ratio();
        assetRatioCriteria.effectiveFrom();
        assetRatioCriteria.assetId();
        assetRatioCriteria.distinct();
    }

    private static Condition<AssetRatioCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getRatio()) &&
                condition.apply(criteria.getEffectiveFrom()) &&
                condition.apply(criteria.getAssetId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AssetRatioCriteria> copyFiltersAre(AssetRatioCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getRatio(), copy.getRatio()) &&
                condition.apply(criteria.getEffectiveFrom(), copy.getEffectiveFrom()) &&
                condition.apply(criteria.getAssetId(), copy.getAssetId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
