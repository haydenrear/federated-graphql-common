package com.hayden.graphql.federated.transport.health;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public interface HealthEvent {

    class FailedHealthEvent extends ApplicationEvent {
        private FederatedGraphQlServiceFetcherItemId serviceItemId;

        public FailedHealthEvent(FederatedGraphQlServiceFetcherItemId source) {
            super(source);
        }

        public FailedHealthEvent(FederatedGraphQlServiceFetcherItemId source, Clock clock) {
            super(source, clock);
        }

        @Override
        public FederatedGraphQlServiceFetcherItemId getSource() {
            return this.serviceItemId;
        }
    }

}
