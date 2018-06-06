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
import java.util.Collections;
import java.util.List;

public abstract class RestTemplateClient {
    protected static final ParameterizedTypeReference<Void> VOID = new ParameterizedTypeReference<Void>(){};
    protected static final MultiValueMap<String, String> EMPTY_MAP = new LinkedMultiValueMap<>();
    protected final RestTemplate restTemplate;
    protected String basePath = "";

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public RestTemplateClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected MultiValueMap<String, String> createQueryParameters(Object... keyValues) {
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

    protected <T> ResponseEntity<T> invokeAPI(String path, HttpMethod method, MultiValueMap<String, String> queryParams, Object body, ParameterizedTypeReference<T> returnType) {
        URI baseUrl = restTemplate.getUriTemplateHandler().expand(basePath);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(baseUrl).path(path).queryParams(queryParams);
        RequestEntity.BodyBuilder requestBuilder = RequestEntity.method(method, uriBuilder.build().toUri());
        customizeRequest(requestBuilder);
        RequestEntity<Object> requestEntity = requestBuilder.body(body);
        return restTemplate.exchange(requestEntity, returnType);
    }

    protected void customizeRequest(RequestEntity.BodyBuilder requestBuilder) {
    }
}