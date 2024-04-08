package org.p2p.solanaj.rpc;


import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import okhttp3.*;
import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.RpcResponse;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

public class RpcClientProxy extends RpcClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


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

    public <T> T call(String method, List<Object> params, Class<T> clazz) throws RpcException {
        RpcRequest rpcRequest = new RpcRequest(method, params);

        JsonAdapter<RpcRequest> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest.class);
        JsonAdapter<RpcResponse<T>> resultAdapter = new Moshi.Builder().build()
                .adapter(Types.newParameterizedType(RpcResponse.class, Type.class.cast(clazz)));

        Request request = new Request.Builder().url(getEndpoint())
                .post(RequestBody.create(rpcRequestJsonAdapter.toJson(rpcRequest), JSON)).build();

        try {
            Response response = httpClient.newCall(request).execute();
            final String result = response.body().string();
            // System.out.println("Response = " + result);
            RpcResponse<T> rpcResult = resultAdapter.fromJson(result);

            if (rpcResult.getError() != null) {
                throw new RpcException(rpcResult.getError().getMessage());
            }

            return (T) rpcResult.getResult();
        } catch (SSLHandshakeException e) {
            this.httpClient = new OkHttpClient.Builder().build();
            throw new RpcException(e.getMessage());
        } catch (IOException e) {
            throw new RpcException(e.getMessage());
        }
    }
}
