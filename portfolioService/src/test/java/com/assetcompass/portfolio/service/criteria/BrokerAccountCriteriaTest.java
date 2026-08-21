package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class BrokerAccountCriteriaTest {

    @Test
    void newBrokerAccountCriteriaHasAllFiltersNullTest() {
        var brokerAccountCriteria = new BrokerAccountCriteria();
        assertThat(brokerAccountCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void brokerAccountCriteriaFluentMethodsCreatesFiltersTest() {
        var brokerAccountCriteria = new BrokerAccountCriteria();

        setAllFilters(brokerAccountCriteria);

        assertThat(brokerAccountCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void brokerAccountCriteriaCopyCreatesNullFilterTest() {
        var brokerAccountCriteria = new BrokerAccountCriteria();
        var copy = brokerAccountCriteria.copy();

        assertThat(brokerAccountCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(brokerAccountCriteria)
        );
    }

    @Test
    void brokerAccountCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var brokerAccountCriteria = new BrokerAccountCriteria();
        setAllFilters(brokerAccountCriteria);

        var copy = brokerAccountCriteria.copy();

        assertThat(brokerAccountCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(brokerAccountCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var brokerAccountCriteria = new BrokerAccountCriteria();

        assertThat(brokerAccountCriteria).hasToString("BrokerAccountCriteria{}");
    }

    private static void setAllFilters(BrokerAccountCriteria brokerAccountCriteria) {
        brokerAccountCriteria.id();
        brokerAccountCriteria.externalAccountId();
        brokerAccountCriteria.displayName();
        brokerAccountCriteria.brokerId();
        brokerAccountCriteria.distinct();
    }

    private static Condition<BrokerAccountCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getExternalAccountId()) &&
                condition.apply(criteria.getDisplayName()) &&
                condition.apply(criteria.getBrokerId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<BrokerAccountCriteria> copyFiltersAre(
        BrokerAccountCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getExternalAccountId(), copy.getExternalAccountId()) &&
                condition.apply(criteria.getDisplayName(), copy.getDisplayName()) &&
                condition.apply(criteria.getBrokerId(), copy.getBrokerId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
