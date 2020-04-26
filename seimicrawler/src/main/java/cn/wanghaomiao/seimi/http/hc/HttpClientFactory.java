/*
   Copyright 2015 Wang Haomiao<seimimaster@gmail.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package cn.wanghaomiao.seimi.http.hc;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import cn.wanghaomiao.seimi.struct.CrawlerModel;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 *         Date: 2014/11/13.
 */
public class HttpClientFactory {
    public static HttpClient getHttpClient() {
        return cliBuilder(null, 10000, null, null).build();
    }

    public static HttpClient getHttpClient(CrawlerModel crawlerModel, int timeout, String proxyUserName, String proxyPassword) {
        return cliBuilder(crawlerModel, timeout, proxyUserName, proxyPassword).build();
    }

    public static HttpClient getHttpClient(CrawlerModel crawlerModel, int timeout, CookieStore cookieStore, String proxyUserName, String proxyPassword) {
        return cliBuilder(crawlerModel, timeout, proxyUserName, proxyPassword).setDefaultCookieStore(cookieStore).build();
    }

    public static HttpClientBuilder cliBuilder(CrawlerModel crawlerModel, int timeout, String proxyUserName, String proxyPassword) {
        HttpRequestRetryHandler retryHander = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount > 3) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof java.net.SocketTimeoutException) {
                    //特殊处理
                    return true;
                }
                if (exception instanceof InterruptedIOException) {
                    // Timeout
                    return true;
                }
                if (exception instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }

                if (exception instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };
        
        RedirectStrategy redirectStrategy = new SeimiRedirectStrategy();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = HttpClientConnectionManagerProvider.getHcPoolInstance();
        HttpClientBuilder builder =  HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(poolingHttpClientConnectionManager)
                .setRedirectStrategy(redirectStrategy).setRetryHandler(retryHander);
        
        HttpHost h = crawlerModel.getProxy();
        if (h != null) {
        	if (proxyPassword != null && !proxyPassword.isEmpty()) {
            	CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(h.getHostName(), h.getPort()),
                        new UsernamePasswordCredentials(proxyUserName, proxyPassword));
                builder.setDefaultCredentialsProvider(credsProvider);
            }
        }
        
        return builder;
    }
}
