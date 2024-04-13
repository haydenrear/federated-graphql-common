package com.hayden.graphql.federated.transport.health;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.util.Objects;

public interface HealthEvent {

    class FailedHealthEvent extends ApplicationEvent {
        private FederatedGraphQlServiceItemId serviceItemId;

        public FailedHealthEvent(FederatedGraphQlServiceItemId source) {
            super(source);
        }

        public FailedHealthEvent(FederatedGraphQlServiceItemId source, Clock clock) {
            super(source, clock);
        }

        @Override
        public FederatedGraphQlServiceItemId getSource() {
            return this.serviceItemId;
        }
    }

}
