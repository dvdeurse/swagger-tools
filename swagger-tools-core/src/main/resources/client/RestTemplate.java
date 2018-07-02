package {{package}};

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

public abstract class BaseClient {
    protected static final ParameterizedTypeReference<Void> VOID = new ParameterizedTypeReference<Void>() {};
    private static final Map<String, String> EMPTY_MAP = new HashMap<>();
    private static final MultiValueMap<String, String> EMPTY_MULTI_MAP = new LinkedMultiValueMap<>();

    protected final RestTemplate restTemplate;
    protected String basePath = "";
    protected Map<String, List<String>> headers;
    protected Consumer<RequestEntity.BodyBuilder> requestCustomizer;

    public BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BaseClient(RestTemplate restTemplate, String basePath) {
        this.restTemplate = restTemplate;
        this.basePath = basePath;
    }

    public BaseClient(RestTemplate restTemplate, String basePath, Map<String, List<String>> headers) {
        this.restTemplate = restTemplate;
        this.basePath = basePath;
        this.headers = headers;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Consumer<RequestEntity.BodyBuilder> getRequestCustomizer() {
        return requestCustomizer;
    }

    public void setRequestCustomizer(Consumer<RequestEntity.BodyBuilder> requestCustomizer) {
        this.requestCustomizer = requestCustomizer;
    }

    protected MultiValueMap<String, String> createQueryParameters(Object... keyValues) {
        if (keyValues.length == 0) {
            return EMPTY_MULTI_MAP;
        }
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (value != null) {
                if (value instanceof List) {
                    parameters.put(key.toString(), (List) value);
                } else {
                    parameters.put(key.toString(), Collections.singletonList(value.toString()));
                }
            }
        }
        return parameters;
    }

    protected Map<String, String> createUrlVariables(Object... keyValues) {
        if (keyValues.length == 0) {
            return EMPTY_MAP;
        }
        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (value != null) {
                parameters.put(key.toString(), value.toString());
            }
        }
        return parameters;
    }

    protected <T> ResponseEntity<T> invokeAPI(String path, String method, Map<String, String> urlVariables, MultiValueMap<String, String> queryParams, Object body, ParameterizedTypeReference<T> returnType) {
        URI baseUrl = restTemplate.getUriTemplateHandler().expand(basePath);
        URI uri = UriComponentsBuilder
                .fromUri(baseUrl)
                .path(path)
                .queryParams(queryParams)
                .buildAndExpand(urlVariables)
                .toUri();
        RequestEntity.BodyBuilder requestBuilder = RequestEntity.method(HttpMethod.resolve(method), uri);
        customizeRequest(requestBuilder);
        RequestEntity<Object> requestEntity = requestBuilder.body(body);
        return restTemplate.exchange(requestEntity, returnType);
    }

    protected void customizeRequest(RequestEntity.BodyBuilder requestBuilder) {
        if (headers != null) {
            for(Map.Entry<String, List<String>> e : headers.entrySet()) {
                requestBuilder.header(e.getKey(), e.getValue().toArray(new String[0]));
            }
        }
        if (requestCustomizer != null) {
            requestCustomizer.accept(requestBuilder);
        }
    }

}