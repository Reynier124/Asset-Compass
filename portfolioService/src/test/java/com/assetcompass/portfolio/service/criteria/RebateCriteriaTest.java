package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class RebateCriteriaTest {

    @Test
    void newRebateCriteriaHasAllFiltersNullTest() {
        var rebateCriteria = new RebateCriteria();
        assertThat(rebateCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void rebateCriteriaFluentMethodsCreatesFiltersTest() {
        var rebateCriteria = new RebateCriteria();

        setAllFilters(rebateCriteria);

        assertThat(rebateCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void rebateCriteriaCopyCreatesNullFilterTest() {
        var rebateCriteria = new RebateCriteria();
        var copy = rebateCriteria.copy();

        assertThat(rebateCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(rebateCriteria)
        );
    }

    @Test
    void rebateCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var rebateCriteria = new RebateCriteria();
        setAllFilters(rebateCriteria);

        var copy = rebateCriteria.copy();

        assertThat(rebateCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(rebateCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var rebateCriteria = new RebateCriteria();

        assertThat(rebateCriteria).hasToString("RebateCriteria{}");
    }

    private static void setAllFilters(RebateCriteria rebateCriteria) {
        rebateCriteria.id();
        rebateCriteria.rebateDate();
        rebateCriteria.amount();
        rebateCriteria.currency();
        rebateCriteria.accountId();
        rebateCriteria.operationId();
        rebateCriteria.distinct();
    }

    private static Condition<RebateCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getRebateDate()) &&
                condition.apply(criteria.getAmount()) &&
                condition.apply(criteria.getCurrency()) &&
                condition.apply(criteria.getAccountId()) &&
                condition.apply(criteria.getOperationId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<RebateCriteria> copyFiltersAre(RebateCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getRebateDate(), copy.getRebateDate()) &&
                condition.apply(criteria.getAmount(), copy.getAmount()) &&
                condition.apply(criteria.getCurrency(), copy.getCurrency()) &&
                condition.apply(criteria.getAccountId(), copy.getAccountId()) &&
                condition.apply(criteria.getOperationId(), copy.getOperationId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
