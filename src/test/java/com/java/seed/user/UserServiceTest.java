package com.java.seed.user;

import com.java.seed.api.model.UserRequest;
import com.java.seed.shared.KafkaProperties;
import com.java.seed.user.events.UserEvent;
import com.java.seed.user.events.UserEventKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String USER_TOPIC = "user_topic";

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private KafkaTemplate<UserEventKey, UserEvent> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<UserEventKey, UserEvent>> future;

    @Captor
    private ArgumentCaptor<UserEventKey> keyCaptor;

    @Captor
    private ArgumentCaptor<UserEvent> eventCaptor;

    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(kafkaProperties, kafkaTemplate);
        given(kafkaProperties.getTopics()).willReturn(Map.of("user", USER_TOPIC));
    }

    @Test
    void shouldPublishUserEvent() throws Exception {
        // given
        UserRequest request = new UserRequest().name("John Doe");

        given(kafkaTemplate.send(anyString(), any(), any())).willReturn(future);

        // when
        UUID userId = userService.publish(request);

        // then
        assertThat(userId).isNotNull();

        then(kafkaTemplate).should().send(eq(USER_TOPIC), keyCaptor.capture(), eventCaptor.capture());
        
        assertThat(keyCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(eventCaptor.getValue().getName()).isEqualTo(request.getName());
        assertThat(eventCaptor.getValue().getCreatedAt()).isNotNull();
    }
}
