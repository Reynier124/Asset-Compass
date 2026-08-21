package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class OperationCriteriaTest {

    @Test
    void newOperationCriteriaHasAllFiltersNullTest() {
        var operationCriteria = new OperationCriteria();
        assertThat(operationCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void operationCriteriaFluentMethodsCreatesFiltersTest() {
        var operationCriteria = new OperationCriteria();

        setAllFilters(operationCriteria);

        assertThat(operationCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void operationCriteriaCopyCreatesNullFilterTest() {
        var operationCriteria = new OperationCriteria();
        var copy = operationCriteria.copy();

        assertThat(operationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(operationCriteria)
        );
    }

    @Test
    void operationCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var operationCriteria = new OperationCriteria();
        setAllFilters(operationCriteria);

        var copy = operationCriteria.copy();

        assertThat(operationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(operationCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var operationCriteria = new OperationCriteria();

        assertThat(operationCriteria).hasToString("OperationCriteria{}");
    }

    private static void setAllFilters(OperationCriteria operationCriteria) {
        operationCriteria.id();
        operationCriteria.type();
        operationCriteria.operationDate();
        operationCriteria.quantity();
        operationCriteria.price();
        operationCriteria.amount();
        operationCriteria.currency();
        operationCriteria.underlyingPrice();
        operationCriteria.commission();
        operationCriteria.accountId();
        operationCriteria.assetId();
        operationCriteria.closesOperationId();
        operationCriteria.distinct();
    }

    private static Condition<OperationCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getOperationDate()) &&
                condition.apply(criteria.getQuantity()) &&
                condition.apply(criteria.getPrice()) &&
                condition.apply(criteria.getAmount()) &&
                condition.apply(criteria.getCurrency()) &&
                condition.apply(criteria.getUnderlyingPrice()) &&
                condition.apply(criteria.getCommission()) &&
                condition.apply(criteria.getAccountId()) &&
                condition.apply(criteria.getAssetId()) &&
                condition.apply(criteria.getClosesOperationId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<OperationCriteria> copyFiltersAre(OperationCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getOperationDate(), copy.getOperationDate()) &&
                condition.apply(criteria.getQuantity(), copy.getQuantity()) &&
                condition.apply(criteria.getPrice(), copy.getPrice()) &&
                condition.apply(criteria.getAmount(), copy.getAmount()) &&
                condition.apply(criteria.getCurrency(), copy.getCurrency()) &&
                condition.apply(criteria.getUnderlyingPrice(), copy.getUnderlyingPrice()) &&
                condition.apply(criteria.getCommission(), copy.getCommission()) &&
                condition.apply(criteria.getAccountId(), copy.getAccountId()) &&
                condition.apply(criteria.getAssetId(), copy.getAssetId()) &&
                condition.apply(criteria.getClosesOperationId(), copy.getClosesOperationId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
