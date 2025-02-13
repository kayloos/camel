/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.dynamicrouter.support;

import java.util.function.Supplier;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.component.dynamicrouter.DynamicRouterComponent;
import org.apache.camel.component.dynamicrouter.DynamicRouterConsumer;
import org.apache.camel.component.dynamicrouter.DynamicRouterControlChannelProcessor;
import org.apache.camel.component.dynamicrouter.DynamicRouterEndpoint;
import org.apache.camel.component.dynamicrouter.DynamicRouterProducer;
import org.apache.camel.component.dynamicrouter.message.DynamicRouterControlMessage;
import org.apache.camel.component.dynamicrouter.processor.DynamicRouterProcessor;
import org.apache.camel.component.dynamicrouter.processor.PrioritizedFilterProcessor;
import org.apache.camel.spi.ExchangeFactory;
import org.apache.camel.support.builder.PredicateBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.camel.component.dynamicrouter.DynamicRouterConstants.COMPONENT_SCHEME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * This support class facilitates testing the Dynamic Router component code. It provides convenient mocking to help to
 * make the tests cleaner and easier to follow.
 */
@ExtendWith(MockitoExtension.class)
public class CamelDynamicRouterTestSupport extends CamelTestSupport {

    public static final String DYNAMIC_ROUTER_CHANNEL = "test";
    public static final String BASE_URI = String.format("%s:%s", COMPONENT_SCHEME, DYNAMIC_ROUTER_CHANNEL);
    public static final String PROCESSOR_ID = "testProcessorId";
    public static final String MESSAGE_ID = "testId";
    public static final long TIMEOUT = 30000L;

    @Mock
    protected ExtendedCamelContext context;

    @Mock
    protected ExchangeFactory exchangeFactory;

    @Mock
    protected DynamicRouterComponent component;

    @Mock
    protected DynamicRouterEndpoint endpoint;

    @Mock
    protected DynamicRouterProcessor processor;

    @Mock
    protected DynamicRouterControlChannelProcessor controlChannelProcessor;

    @Mock
    protected DynamicRouterProducer producer;

    @Mock
    protected DynamicRouterConsumer consumer;

    @Mock
    protected PrioritizedFilterProcessor filterProcessor;

    @Mock
    protected DynamicRouterControlMessage controlMessage;

    @Mock
    protected AsyncCallback asyncCallback;

    @Mock
    protected Exchange exchange;

    // Since most pieces of the Dynamic Router are instantiated by calling factories,
    // this provides greatly simplified testing of all components without extensive
    // mocking or entangling of external units
    protected DynamicRouterEndpoint.DynamicRouterEndpointFactory endpointFactory;
    protected DynamicRouterProcessor.DynamicRouterProcessorFactory processorFactory;
    protected DynamicRouterControlChannelProcessor.DynamicRouterControlChannelProcessorFactory controlChannelProcessorFactory;
    protected DynamicRouterProducer.DynamicRouterProducerFactory producerFactory;
    protected DynamicRouterConsumer.DynamicRouterConsumerFactory consumerFactory;
    protected PrioritizedFilterProcessor.PrioritizedFilterProcessorFactory filterProcessorFactory;

    /**
     * Sets up lenient mocking so that regular behavior "just happens" in tests, but each test can customize behavior
     * for any of these mocks by resetting them, or by redefining the instance, and then creating the desired
     * interactions.
     *
     * @throws Exception if there is a problem with setting up via the superclass
     */
    @BeforeEach
    protected void setup() throws Exception {
        super.setUp();

        lenient().when(exchangeFactory.newExchangeFactory(any(Consumer.class))).thenReturn(exchangeFactory);

        lenient().when(consumer.getProcessor()).thenReturn(processor);

        lenient().doNothing().when(processor).process(any(Exchange.class));

        lenient().when(component.getCamelContext()).thenReturn(context);
        lenient().when(component.getConsumer(anyString())).thenReturn(consumer);

        lenient().when(endpoint.getCamelContext()).thenReturn(context);
        lenient().when(endpoint.getComponent()).thenReturn(component);
        lenient().when(endpoint.getDynamicRouterComponent()).thenReturn(component);
        lenient().when(endpoint.getChannel()).thenReturn(DYNAMIC_ROUTER_CHANNEL);
        lenient().when(endpoint.getTimeout()).thenReturn(TIMEOUT);
        lenient().when(endpoint.isFailIfNoConsumers()).thenReturn(false);
        lenient().when(endpoint.isBlock()).thenReturn(true);

        lenient().when(context.adapt(ExtendedCamelContext.class)).thenReturn(context);
        lenient().when(context.getExchangeFactory()).thenReturn(exchangeFactory);

        lenient().when(filterProcessor.getId()).thenReturn(MESSAGE_ID);
        lenient().when(filterProcessor.getPriority()).thenReturn(Integer.MAX_VALUE);

        lenient().doNothing().when(asyncCallback).done(anyBoolean());

        lenient().when(controlMessage.getId()).thenReturn(MESSAGE_ID);
        lenient().when(controlMessage.getChannel()).thenReturn(DYNAMIC_ROUTER_CHANNEL);
        lenient().when(controlMessage.getPriority()).thenReturn(1);
        lenient().when(controlMessage.getPredicate()).thenReturn(PredicateBuilder.constant(true));
        lenient().when(controlMessage.getEndpoint()).thenReturn("test");

        endpointFactory = new DynamicRouterEndpoint.DynamicRouterEndpointFactory() {
            @Override
            public DynamicRouterEndpoint getInstance(
                    String uri, String channel, DynamicRouterComponent component,
                    Supplier<DynamicRouterProcessor.DynamicRouterProcessorFactory> processorFactorySupplier,
                    Supplier<DynamicRouterProducer.DynamicRouterProducerFactory> producerFactorySupplier,
                    Supplier<DynamicRouterConsumer.DynamicRouterConsumerFactory> consumerFactorySupplier,
                    Supplier<PrioritizedFilterProcessor.PrioritizedFilterProcessorFactory> filterProcessorFactorySupplier) {
                return endpoint;
            }
        };

        processorFactory = new DynamicRouterProcessor.DynamicRouterProcessorFactory() {
            @Override
            public DynamicRouterProcessor getInstance(
                    String id, CamelContext camelContext, boolean warnDroppedMessage,
                    Supplier<PrioritizedFilterProcessor.PrioritizedFilterProcessorFactory> filterProcessorFactorySupplier) {
                return processor;
            }
        };

        controlChannelProcessorFactory
                = new DynamicRouterControlChannelProcessor.DynamicRouterControlChannelProcessorFactory() {
                    @Override
                    public DynamicRouterControlChannelProcessor getInstance(DynamicRouterComponent component) {
                        return controlChannelProcessor;
                    }
                };

        producerFactory = new DynamicRouterProducer.DynamicRouterProducerFactory() {
            @Override
            public DynamicRouterProducer getInstance(DynamicRouterEndpoint endpoint) {
                return producer;
            }
        };

        consumerFactory = new DynamicRouterConsumer.DynamicRouterConsumerFactory() {
            @Override
            public DynamicRouterConsumer getInstance(
                    DynamicRouterEndpoint endpoint, Processor processor, String channel) {
                return consumer;
            }
        };

        filterProcessorFactory = new PrioritizedFilterProcessor.PrioritizedFilterProcessorFactory() {
            @Override
            public PrioritizedFilterProcessor getInstance(
                    String id, int priority, CamelContext context, Predicate predicate, Processor processor) {
                return filterProcessor;
            }
        };
    }
}
