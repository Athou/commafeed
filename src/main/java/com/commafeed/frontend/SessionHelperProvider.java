package com.commafeed.frontend;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class SessionHelperProvider implements InjectableProvider<Context, Type> {
	
    private final ThreadLocal<HttpServletRequest> request;

    public SessionHelperProvider(@Context ThreadLocal<HttpServletRequest> request) {
        this.request = request;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, final Context session, Type type) {
        if (type.equals(SessionHelper.class)) {
            return new Injectable<SessionHelper>() {
                @Override
                public SessionHelper getValue() {
                    final HttpServletRequest req = request.get();
                    if (req != null) {
                        return new SessionHelper(req);
                    }
                    return null;
                }
            };
        }
        return null;
    }
}