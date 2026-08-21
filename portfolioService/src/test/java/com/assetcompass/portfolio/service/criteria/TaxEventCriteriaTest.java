package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class TaxEventCriteriaTest {

    @Test
    void newTaxEventCriteriaHasAllFiltersNullTest() {
        var taxEventCriteria = new TaxEventCriteria();
        assertThat(taxEventCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void taxEventCriteriaFluentMethodsCreatesFiltersTest() {
        var taxEventCriteria = new TaxEventCriteria();

        setAllFilters(taxEventCriteria);

        assertThat(taxEventCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void taxEventCriteriaCopyCreatesNullFilterTest() {
        var taxEventCriteria = new TaxEventCriteria();
        var copy = taxEventCriteria.copy();

        assertThat(taxEventCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(taxEventCriteria)
        );
    }

    @Test
    void taxEventCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var taxEventCriteria = new TaxEventCriteria();
        setAllFilters(taxEventCriteria);

        var copy = taxEventCriteria.copy();

        assertThat(taxEventCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(taxEventCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var taxEventCriteria = new TaxEventCriteria();

        assertThat(taxEventCriteria).hasToString("TaxEventCriteria{}");
    }

    private static void setAllFilters(TaxEventCriteria taxEventCriteria) {
        taxEventCriteria.id();
        taxEventCriteria.type();
        taxEventCriteria.taxDate();
        taxEventCriteria.amount();
        taxEventCriteria.currency();
        taxEventCriteria.accountId();
        taxEventCriteria.operationId();
        taxEventCriteria.incomeEventId();
        taxEventCriteria.distinct();
    }

    private static Condition<TaxEventCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getTaxDate()) &&
                condition.apply(criteria.getAmount()) &&
                condition.apply(criteria.getCurrency()) &&
                condition.apply(criteria.getAccountId()) &&
                condition.apply(criteria.getOperationId()) &&
                condition.apply(criteria.getIncomeEventId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<TaxEventCriteria> copyFiltersAre(TaxEventCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getTaxDate(), copy.getTaxDate()) &&
                condition.apply(criteria.getAmount(), copy.getAmount()) &&
                condition.apply(criteria.getCurrency(), copy.getCurrency()) &&
                condition.apply(criteria.getAccountId(), copy.getAccountId()) &&
                condition.apply(criteria.getOperationId(), copy.getOperationId()) &&
                condition.apply(criteria.getIncomeEventId(), copy.getIncomeEventId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
