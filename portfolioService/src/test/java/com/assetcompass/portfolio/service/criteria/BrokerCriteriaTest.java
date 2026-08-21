package com.assetcompass.portfolio.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class BrokerCriteriaTest {

    @Test
    void newBrokerCriteriaHasAllFiltersNullTest() {
        var brokerCriteria = new BrokerCriteria();
        assertThat(brokerCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void brokerCriteriaFluentMethodsCreatesFiltersTest() {
        var brokerCriteria = new BrokerCriteria();

        setAllFilters(brokerCriteria);

        assertThat(brokerCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void brokerCriteriaCopyCreatesNullFilterTest() {
        var brokerCriteria = new BrokerCriteria();
        var copy = brokerCriteria.copy();

        assertThat(brokerCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(brokerCriteria)
        );
    }

    @Test
    void brokerCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var brokerCriteria = new BrokerCriteria();
        setAllFilters(brokerCriteria);

        var copy = brokerCriteria.copy();

        assertThat(brokerCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(brokerCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var brokerCriteria = new BrokerCriteria();

        assertThat(brokerCriteria).hasToString("BrokerCriteria{}");
    }

    private static void setAllFilters(BrokerCriteria brokerCriteria) {
        brokerCriteria.id();
        brokerCriteria.name();
        brokerCriteria.distinct();
    }

    private static Condition<BrokerCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria -> condition.apply(criteria.getId()) && condition.apply(criteria.getName()) && condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<BrokerCriteria> copyFiltersAre(BrokerCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
