package org.webpieces.execdemo.framework;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.mock.lib.MockExecutor;
import org.webpieces.util.context.Context;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.execdemo.Server;
import org.webpieces.execdemo.json.ExampleRestAPI;
import org.webpieces.execdemo.json.SaveApi;
import org.webpieces.execdemo.mock.JavaCache;
import org.webpieces.execdemo.mock.MockRemoteService;
import org.webpieces.execdemo.service.RemoteService;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

/**
 * These are working examples of tests that sometimes are better done with the BasicSeleniumTest example but are here for completeness
 * so you can test the way you would like to test
 *
 * @author dhiller
 *
 */
public class FeatureTest extends CompanyTest {

    private final static Logger log = LoggerFactory.getLogger(FeatureTest.class);
    private String[] args = { "-http.port=:0", "-https.port=:0", "-hibernate.persistenceunit=org.webpieces.execdemo.db.DbSettingsInMemory", "-hibernate.loadclassmeta=true" };

    protected SaveApi saveApi;
    protected ExampleRestAPI exampleRestAPI;
    protected MockRemoteService mockRemoteService = new MockRemoteService();
    protected SimpleMockExecutor mockExecutor = new SimpleMockExecutor();
    protected SimpleMeterRegistry metrics;

    @Before
    public void setUp() throws InterruptedException, ClassNotFoundException, ExecutionException, TimeoutException {
        log.info("Setting up test");
        super.initialize();
        saveApi = super.createRestClient(SaveApi.class);
        exampleRestAPI = super.createRestClient(ExampleRestAPI.class);
    }

    @After
    public void tearDown() {
        //do not leak context between tests
        Context.clear();
    }

    @Override
    protected void startServer() {
        metrics = new SimpleMeterRegistry();
        Server webserver = new Server(getOverrides(metrics),new AppOverridesModule(),
                new ServerConfig(JavaCache.getCacheLocation()), args
        );
        webserver.start();

        serverHttpsAddr = new InetSocketAddress("localhost", webserver.getUnderlyingHttpsChannel().getLocalAddress().getPort());
        serverHttpAddr = new InetSocketAddress("localhost", webserver.getUnderlyingHttpChannel().getLocalAddress().getPort());
    }

    private class AppOverridesModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(RemoteService.class).toInstance(mockRemoteService);

            binder.bind(Executor.class).toInstance(mockExecutor);
        }
    }

}
