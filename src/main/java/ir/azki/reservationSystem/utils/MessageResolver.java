package ir.azki.reservationSystem.utils;

import ir.azki.reservationSystem.enums.ServiceResultStatus;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageResolver {
    @Getter
    private static MessageResolver instance;


    private final MessageSource messageSource;

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
        instance = this;
    }

    public String getMessage(ServiceResultStatus status) {
        return messageSource.getMessage(
                status.name(),
                null,
                new Locale("fa")
        );
    }
}

