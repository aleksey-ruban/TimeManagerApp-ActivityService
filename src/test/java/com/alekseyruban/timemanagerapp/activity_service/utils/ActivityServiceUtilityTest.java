package com.alekseyruban.timemanagerapp.activity_service.utils;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.VersionedSyncObject;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncCursor;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncStatus;
import com.alekseyruban.timemanagerapp.activity_service.exception.ErrorCode;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncHandler;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncHandlerRegistry;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncResult;
import com.alekseyruban.timemanagerapp.activity_service.validators.TimeZoneValidator;
import com.alekseyruban.timemanagerapp.activity_service.validators.ValidTimeZone;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.lang.annotation.Annotation;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityServiceUtilityTest {

    private final TextValidator textValidator = new TextValidator();
    private final SyncCursorCodec syncCursorCodec = new SyncCursorCodec();
    private final ExceptionFactory exceptionFactory = new ExceptionFactory();

    @ParameterizedTest
    @ValueSource(strings = {"RED", "red", "Light_Blue", "purple"})
    void activityColorAcceptsKnownColorsIgnoringCase(String value) {
        assertThat(ActivityColor.isValidColor(value)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-color", " blue ", "1"})
    void activityColorRejectsUnknownValues(String value) {
        assertThat(ActivityColor.isValidColor(value)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"John", "Mary Jane", "O'Connor", "Jean-Luc"})
    void textValidatorAcceptsValidNames(String value) {
        assertThat(textValidator.isValidName(value)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "A", "Name!", "A@", "?"})
    void textValidatorRejectsInvalidNames(String value) {
        assertThat(textValidator.isValidName(value)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Work", "Deep Work 2", "A"})
    void textValidatorAcceptsValidCategories(String value) {
        assertThat(textValidator.isValidCategory(value)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "Category!", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
    void textValidatorRejectsInvalidCategories(String value) {
        assertThat(textValidator.isValidCategory(value)).isFalse();
    }

    @Test
    void syncCursorCodecEncodesAndDecodesRoundTrip() {
        SyncCursor cursor = new SyncCursor(42L, 123);

        String encoded = syncCursorCodec.encode(cursor);
        SyncCursor decoded = syncCursorCodec.decode(encoded);

        assertThat(encoded).isNotBlank();
        assertThat(decoded).usingRecursiveComparison().isEqualTo(cursor);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad-value", "%%%%", "eyJub3QiOiAiY3Vyc29yIn0="})
    void syncCursorCodecRejectsInvalidValues(String value) {
        assertThatThrownBy(() -> syncCursorCodec.decode(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid cursor");
    }

    @Test
    void syncHandlerRegistryReturnsHandlerBySupportedType() {
        SyncHandler categoryHandler = new TestSyncHandler(SyncObjectType.CATEGORY);
        SyncHandler activityHandler = new TestSyncHandler(SyncObjectType.ACTIVITY);
        SyncHandlerRegistry registry = new SyncHandlerRegistry(List.of(categoryHandler, activityHandler));

        assertThat(registry.get(SyncObjectType.ACTIVITY)).isSameAs(activityHandler);
    }

    @Test
    void syncHandlerRegistryReturnsNullForUnknownType() {
        SyncHandlerRegistry registry = new SyncHandlerRegistry(List.of(new TestSyncHandler(SyncObjectType.CATEGORY)));

        assertThat(registry.get(SyncObjectType.ACTIVITY_RECORD)).isNull();
    }

    @Test
    void syncHandlerRegistryRejectsDuplicateHandlers() {
        assertThatThrownBy(() -> new SyncHandlerRegistry(List.of(
                new TestSyncHandler(SyncObjectType.CATEGORY),
                new TestSyncHandler(SyncObjectType.CATEGORY)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void syncResultOkFactoryBuildsSuccessResult() {
        SyncResult result = SyncResult.ok(10L, 99L);

        assertThat(result.getServerId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(SyncStatus.OK);
        assertThat(result.getLastModifiedVersion()).isEqualTo(99L);
        assertThat(result.getErrorCode()).isNull();
    }

    @Test
    void syncResultErrorFactoryWithServerIdBuildsErrorResult() {
        SyncResult result = SyncResult.error(10L, "CODE", "Message");

        assertThat(result.getServerId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(SyncStatus.ERROR);
        assertThat(result.getErrorCode()).isEqualTo("CODE");
        assertThat(result.getErrorMessage()).isEqualTo("Message");
    }

    @Test
    void syncResultErrorFactoryWithoutServerIdBuildsErrorResult() {
        SyncResult result = SyncResult.error("CODE", "Message");

        assertThat(result.getServerId()).isNull();
        assertThat(result.getStatus()).isEqualTo(SyncStatus.ERROR);
        assertThat(result.getErrorCode()).isEqualTo("CODE");
    }

    @ParameterizedTest
    @CsvSource({
            "Europe/Moscow,false,true",
            "UTC,false,true",
            "'',false,false",
            "'',true,true",
            "'   ',true,true",
            "Bad/Zone,false,false"
    })
    void timeZoneValidatorValidatesExpectedCases(String value, boolean allowEmpty, boolean expected) {
        TimeZoneValidator validator = new TimeZoneValidator();
        validator.initialize(annotation(allowEmpty));

        assertThat(validator.isValid(value, null)).isEqualTo(expected);
    }

    @Test
    void userNotFoundExceptionHasExpectedPayload() {
        assertThat(exceptionFactory.userNotFountException().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exceptionFactory.userNotFountException().getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void badNameExceptionHasExpectedPayload() {
        assertThat(exceptionFactory.badNameException().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exceptionFactory.badNameException().getCode()).isEqualTo(ErrorCode.BAD_NAME);
    }

    @Test
    void oldVersionExceptionHasExpectedPayload() {
        assertThat(exceptionFactory.oldVersion().getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exceptionFactory.oldVersion().getCode()).isEqualTo(ErrorCode.OLD_OBJECT_VERSION);
    }

    @Test
    void badSyncPullParamsExceptionHasExpectedPayload() {
        assertThat(exceptionFactory.badSyncPullParams().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exceptionFactory.badSyncPullParams().getCode()).isEqualTo(ErrorCode.BAD_PARAMS);
    }

    private ValidTimeZone annotation(boolean allowEmpty) {
        return new ValidTimeZone() {
            @Override
            public boolean allowEmpty() {
                return allowEmpty;
            }

            @Override
            public String message() {
                return "Invalid IANA time zone";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ValidTimeZone.class;
            }
        };
    }

    private static final class DummyVersionedSyncObject implements VersionedSyncObject {
        private final Long id;
        private final Long lastModifiedVersion;

        private DummyVersionedSyncObject(Long id, Long lastModifiedVersion) {
            this.id = id;
            this.lastModifiedVersion = lastModifiedVersion;
        }

        @Override
        public Long getLastModifiedVersion() {
            return lastModifiedVersion;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

    private static class TestSyncHandler implements SyncHandler {
        private final SyncObjectType type;

        private TestSyncHandler(SyncObjectType type) {
            this.type = type;
        }

        @Override
        public SyncObjectType supports() {
            return type;
        }

        @Override
        public SyncResult handle(Long userDomainId, com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncOperation operation, Object payload) {
            return SyncResult.ok(userDomainId, ZoneOffset.UTC.getTotalSeconds() * 1L);
        }
    }
}
