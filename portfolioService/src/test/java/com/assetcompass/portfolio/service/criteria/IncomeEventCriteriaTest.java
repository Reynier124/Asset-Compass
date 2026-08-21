package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class IncomeEventCriteriaTest {

    @Test
    void newIncomeEventCriteriaHasAllFiltersNullTest() {
        var incomeEventCriteria = new IncomeEventCriteria();
        assertThat(incomeEventCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void incomeEventCriteriaFluentMethodsCreatesFiltersTest() {
        var incomeEventCriteria = new IncomeEventCriteria();

        setAllFilters(incomeEventCriteria);

        assertThat(incomeEventCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void incomeEventCriteriaCopyCreatesNullFilterTest() {
        var incomeEventCriteria = new IncomeEventCriteria();
        var copy = incomeEventCriteria.copy();

        assertThat(incomeEventCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(incomeEventCriteria)
        );
    }

    @Test
    void incomeEventCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var incomeEventCriteria = new IncomeEventCriteria();
        setAllFilters(incomeEventCriteria);

        var copy = incomeEventCriteria.copy();

        assertThat(incomeEventCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(incomeEventCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var incomeEventCriteria = new IncomeEventCriteria();

        assertThat(incomeEventCriteria).hasToString("IncomeEventCriteria{}");
    }

    private static void setAllFilters(IncomeEventCriteria incomeEventCriteria) {
        incomeEventCriteria.id();
        incomeEventCriteria.type();
        incomeEventCriteria.eventDate();
        incomeEventCriteria.amount();
        incomeEventCriteria.currency();
        incomeEventCriteria.accountId();
        incomeEventCriteria.assetId();
        incomeEventCriteria.distinct();
    }

    private static Condition<IncomeEventCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getEventDate()) &&
                condition.apply(criteria.getAmount()) &&
                condition.apply(criteria.getCurrency()) &&
                condition.apply(criteria.getAccountId()) &&
                condition.apply(criteria.getAssetId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<IncomeEventCriteria> copyFiltersAre(IncomeEventCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getEventDate(), copy.getEventDate()) &&
                condition.apply(criteria.getAmount(), copy.getAmount()) &&
                condition.apply(criteria.getCurrency(), copy.getCurrency()) &&
                condition.apply(criteria.getAccountId(), copy.getAccountId()) &&
                condition.apply(criteria.getAssetId(), copy.getAssetId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
