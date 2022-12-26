package com.acmebank.account_manager.validators.validators;

import com.acmebank.account_manager.data.models.requests.TransferRequest;
import com.acmebank.account_manager.shared.Currency;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueOfEnumValidatorTests {
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    final String propertyPath = "currency";
    final long dummyAccountId = 1L;

    static Stream<Arguments> currencyTextProvider() {
        return Stream.of(
                Arguments.of("USD", true),
                Arguments.of(null, false),
                Arguments.of("HKD", false)
        );
    }

    @ParameterizedTest
    @MethodSource("currencyTextProvider")
    public void givenCurrencyText_whenValidate_thenProvidedShouldBeReported(final String currencyText, final boolean isViolated) {
        val message = "must be any of enum class " + Currency.class.getName();
        val request = new TransferRequest(dummyAccountId, dummyAccountId, BigDecimal.ONE, currencyText);
        Set<ConstraintViolation<TransferRequest>> violations = validator.validateProperty(request, propertyPath);
        if(isViolated){
            assertThat(violations.size()).isEqualTo(1);
            assertThat(violations).anyMatch(havingPropertyPath(propertyPath)
                    .and(havingMessage(message)));
        } else{
            assertThat(violations.isEmpty()).isTrue();
        }
    }

    public static Predicate<ConstraintViolation<TransferRequest>> havingMessage(final String message) {
        return l -> message.equals(l.getMessage());
    }

    public static Predicate<ConstraintViolation<TransferRequest>> havingPropertyPath(final String propertyPath) {
        return l -> propertyPath.equals(l.getPropertyPath()
                .toString());
    }
}
