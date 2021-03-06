package fi.livi.like.client.android.backgroundservice.http;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.livi.like.client.android.backgroundservice.LikeService;
import fi.livi.like.client.android.backgroundservice.http.ssl.SslUtil;

public class RestTemplateProvider {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(RestTemplateProvider.class);

    private final LikeService likeService;
    private final Context context;
    private List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
    private HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory;
    private HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if(exception instanceof NoHttpResponseException && executionCount < 3){
                // reason for this is mainly the same 5 sec time on tracking
                // interval and connection keep-alive
                return true;
            }
            return false;
        }
    };

    public RestTemplateProvider(LikeService likeService) {
        this.likeService = likeService;
        this.context = likeService.getBackgroundService();
        prepareMessageConverters();
        prepareHttpClientFactory();
    }

    private CloseableHttpClient getNewHttpClient() {

        PoolingHttpClientConnectionManager ccm = null;
        try {
            ccm = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", new PlainConnectionSocketFactory())
                        .register("https", new SSLConnectionSocketFactory(
                                SslUtil.getSslContextForBothTrustedCasAndSelfSigned(
                                        context, likeService.getDataStorage().getConfiguration().getHttpsCertFile()),
                                SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER))
                            .build());
            ccm.setMaxTotal(10);
            ccm.setDefaultMaxPerRoute(4);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        return HttpClientBuilder.create()
            .setRetryHandler(retryHandler)
            .setConnectionManager(ccm)
            .build();
    }

    private void prepareHttpClientFactory() {

        log.info("preparing ssl for https");
        httpComponentsClientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory(getNewHttpClient());
        final int timeout = likeService.getDataStorage().getConfiguration().getNetworkTimeout();
        httpComponentsClientHttpRequestFactory.setReadTimeout(timeout);
        httpComponentsClientHttpRequestFactory.setConnectTimeout(timeout);
    }

    private void prepareMessageConverters() {

        ObjectMapper objectMapper = new ObjectMapper();
        MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        jsonMessageConverter.setObjectMapper(objectMapper);
        messageConverters.add(jsonMessageConverter);
    }

    public RestTemplate getRestTemplate() {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(messageConverters);
        restTemplate.setRequestFactory(httpComponentsClientHttpRequestFactory);

        return restTemplate;
    }
}
