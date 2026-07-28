package org.springframework.samples.petclinic.service;

import org.mockito.Mockito;

/**
 * Creates the {@link ClinicService} mock used by the web-tier test contexts.
 * <p>
 * Mockito 5 added the reified {@code mock(T... reified)} overloads, so {@code Mockito.mock}
 * now has more than one single-argument candidate and the container can no longer infer the
 * return type of {@code <bean class="org.mockito.Mockito" factory-method="mock">} before the
 * bean is instantiated. Beans that need a {@code ClinicService} injected by type -- such as
 * the {@code PetTypeFormatter} inner bean of {@code conversionService} -- would then find no
 * candidate. This factory exposes a single, unambiguously typed method instead.
 */
public final class ClinicServiceMockFactory {

    private ClinicServiceMockFactory() {
    }

    public static ClinicService createMock() {
        return Mockito.mock(ClinicService.class);
    }

}
