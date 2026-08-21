package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ValuationCriteriaTest {

    @Test
    void newValuationCriteriaHasAllFiltersNullTest() {
        var valuationCriteria = new ValuationCriteria();
        assertThat(valuationCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void valuationCriteriaFluentMethodsCreatesFiltersTest() {
        var valuationCriteria = new ValuationCriteria();

        setAllFilters(valuationCriteria);

        assertThat(valuationCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void valuationCriteriaCopyCreatesNullFilterTest() {
        var valuationCriteria = new ValuationCriteria();
        var copy = valuationCriteria.copy();

        assertThat(valuationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(valuationCriteria)
        );
    }

    @Test
    void valuationCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var valuationCriteria = new ValuationCriteria();
        setAllFilters(valuationCriteria);

        var copy = valuationCriteria.copy();

        assertThat(valuationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(valuationCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var valuationCriteria = new ValuationCriteria();

        assertThat(valuationCriteria).hasToString("ValuationCriteria{}");
    }

    private static void setAllFilters(ValuationCriteria valuationCriteria) {
        valuationCriteria.id();
        valuationCriteria.snapshotDate();
        valuationCriteria.totalValue();
        valuationCriteria.currency();
        valuationCriteria.accountId();
        valuationCriteria.distinct();
    }

    private static Condition<ValuationCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getSnapshotDate()) &&
                condition.apply(criteria.getTotalValue()) &&
                condition.apply(criteria.getCurrency()) &&
                condition.apply(criteria.getAccountId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ValuationCriteria> copyFiltersAre(ValuationCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getSnapshotDate(), copy.getSnapshotDate()) &&
                condition.apply(criteria.getTotalValue(), copy.getTotalValue()) &&
                condition.apply(criteria.getCurrency(), copy.getCurrency()) &&
                condition.apply(criteria.getAccountId(), copy.getAccountId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
