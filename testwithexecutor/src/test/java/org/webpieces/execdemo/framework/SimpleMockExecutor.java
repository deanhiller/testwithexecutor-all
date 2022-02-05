package org.webpieces.execdemo.framework;

import java.util.ArrayList;
import java.util.List;

public class SimpleMockExecutor implements java.util.concurrent.Executor {
    private List<Runnable> cache = new ArrayList<>();
    @Override
    public void execute(Runnable runnable) {
        cache.add(runnable);
    }

    public List<Runnable> getCache() {
        return cache;
    }
}
