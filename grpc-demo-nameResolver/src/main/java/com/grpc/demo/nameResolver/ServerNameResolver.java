package com.grpc.demo.nameResolver;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by yeyc on 2016/9/17.
 */
public class ServerNameResolver extends NameResolver {

    private final String authority;
    private final String host;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;
    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;
    @GuardedBy("this")
    private ExecutorService executor;

    ServerNameResolver(String target) {
        URI nameUri = URI.create("//" + target);
        this.authority = nameUri.getAuthority();
        this.host = target;
        this.executorResource = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
        this.executor = SharedResourceHolder.get(this.executorResource);
    }

    @Override
    public String getServiceAuthority() {
        return this.authority;
    }

    @Override
    public void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "started");
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }

    @Override
    public void shutdown() {
        if (!this.shutdown) {
            this.shutdown = true;
            if (this.executor != null) {
                this.executor = SharedResourceHolder.release(this.executorResource, this.executor);
            }
        }
    }

    @GuardedBy("this")
    private void resolve() {
        if (!this.resolving && !this.shutdown) {
            this.executor.execute(this.resolutionRunnable);
        }
    }

    public final synchronized void refresh() {
        Preconditions.checkState(this.listener != null, "not started");
        resolve();
    }


    private final Runnable resolutionRunnable = new Runnable() {
        public void run() {
            Listener savedListener;
            synchronized (ServerNameResolver.this) {
                if (ServerNameResolver.this.shutdown) {
                    return;
                }
                savedListener = ServerNameResolver.this.listener;
                ServerNameResolver.this.resolving = true;
            }

            try {
                label142:
                {
                    InetSocketAddress[] inetSocketAddresses;
                    try {
                        inetSocketAddresses = ServerNameResolver.this.getAllByName(ServerNameResolver.this.host);
                    } catch (Exception var22) {
                        savedListener.onError(Status.UNAVAILABLE.withCause(var22));
                        break label142;
                    }

                    List<List<ResolvedServerInfo>> serversList = new ArrayList<>();

                    List<ResolvedServerInfo> var25 = new ArrayList(inetSocketAddresses.length);
                    for (int var6 = 0; var6 < inetSocketAddresses.length; ++var6) {
                        InetSocketAddress inetSocketAddress = inetSocketAddresses[var6];
                        var25.add(new ResolvedServerInfo(new InetSocketAddress(inetSocketAddress.getHostName(), inetSocketAddress.getPort()), Attributes.EMPTY));

                    }
                    serversList.add(var25);
                    List<ResolvedServerInfo> var26 =var25;
                    serversList.add(var26);
                    savedListener.onUpdate(serversList, Attributes.EMPTY);
                }
            } finally {
                synchronized (ServerNameResolver.this) {
                    ServerNameResolver.this.resolving = false;
                }
            }
        }
    };

    @VisibleForTesting
    InetSocketAddress[] getAllByName(String host) {
        String[] hostArray = host.split(",");
        InetSocketAddress[] inetAddresses = new InetSocketAddress[hostArray.length];

        for (int i = 0; i < inetAddresses.length; ++i) {
            String[] temp = hostArray[i].split(":");
            if (temp.length < 2) {
                throw new RuntimeException("fail to format host for curr host " + hostArray[i]);
            }

            try {
                int e = Integer.parseInt(temp[1].trim());
                inetAddresses[i] = InetSocketAddress.createUnresolved(temp[0].trim(), e);
            } catch (NumberFormatException var7) {
                throw new RuntimeException("fail to format port for curr host " + hostArray[i]);
            }
        }

        return inetAddresses;
    }


}
