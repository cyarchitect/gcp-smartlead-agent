package com.example.smartlead.infrastructure.config;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class EventPublisher {

    // Multicast sink allows multiple clients to subscribe to the same event stream
    private final Sinks.Many<Object> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Publishes an event (Lead or GoldenLead) to the stream.
     */
    public void publish(Object event) {
        sink.tryEmitNext(event);
    }

    /**
     * Returns the stream of events as a Flux.
     */
    public Flux<Object> getEventStream() {
        return sink.asFlux();
    }
}
