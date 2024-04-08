package org.p2p.solanaj.rpc;


import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class RpcClientProxy extends RpcClient {

    private OkHttpClient httpClient;

    private RpcApi rpcApi;

    public RpcClientProxy(boolean httpProxyEnable, String httpProxyHost, int httpProxyPost, Cluster endpoint) {
        super(endpoint.getEndpoint());
        initializeHttpClient(httpProxyEnable, httpProxyHost, httpProxyPost);
    }

    public RpcClientProxy(boolean httpProxyEnable, String httpProxyHost, int httpProxyPost, String endpoint) {
        super(endpoint);
        initializeHttpClient(httpProxyEnable, httpProxyHost, httpProxyPost);
    }

    private void initializeHttpClient(boolean httpProxyEnable, String httpProxyHost, int httpProxyPost) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (httpProxyEnable) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPost));
            clientBuilder.proxy(proxy);
        }
        httpClient = clientBuilder.build();
        rpcApi = getApi();
    }
}
