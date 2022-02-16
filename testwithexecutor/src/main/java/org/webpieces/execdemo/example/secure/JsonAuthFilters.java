package org.webpieces.execdemo.example.secure;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class JsonAuthFilters implements Routes {
    @Override
    public void configure(DomainRouteBuilder domainRouteBldr) {
        RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();

        bldr.addPackageFilter("org.webpieces.execdemo.example.secure.*", JsonAuthFilter.class, null, FilterPortType.ALL_FILTER, Integer.MAX_VALUE);

    }
}
