/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.util;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

/**
 * Declares a Mockito mock as a bean of the mocked type.
 * <p/>
 * A plain {@code <bean class="org.mockito.Mockito" factory-method="mock"/>} definition cannot be
 * used for this: Mockito declares several overloads of {@code mock}, one of which infers the mocked
 * type from a reified vararg, so the bean type is not determinable from the bean definition alone
 * and the mock is not found by type when another bean is autowired by type before it.
 *
 * @param <T> the type to mock
 */
public class MockitoFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> mockedType;

    public MockitoFactoryBean(Class<T> mockedType) {
        this.mockedType = mockedType;
    }

    @Override
    public T getObject() {
        return Mockito.mock(this.mockedType);
    }

    @Override
    public Class<T> getObjectType() {
        return this.mockedType;
    }

}
