/*
 * Copyright 2002-2026 the original author or authors.
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
package org.springframework.samples.petclinic.service;

import org.mockito.Mockito;

/**
 * Declares a {@link ClinicService} mock with a concrete return type so that the bean
 * factory can determine the bean type without instantiating it. Calling
 * {@code Mockito.mock} straight from XML no longer works: the generic return type
 * inference it relied on was removed in Spring Framework 6.
 */
public final class ClinicServiceMockFactory {

    private ClinicServiceMockFactory() {
    }

    public static ClinicService createMock() {
        return Mockito.mock(ClinicService.class);
    }

}
