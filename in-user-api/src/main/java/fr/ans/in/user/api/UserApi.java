package fr.ans.in.user.api;

import fr.ans.in.user.ApiClient;

import fr.ans.in.user.model.ContactInfos;
import fr.ans.in.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-12-25T17:18:27.062Z[GMT]")@Component("fr.ans.in.user.api.UserApi")
public class UserApi {
    private ApiClient apiClient;

    public UserApi() {
        this(new ApiClient());
    }

    @Autowired
    public UserApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get User Info by User ID
     * Retrieve the information of the user with the matching user ID.
     * <p><b>200</b> - User Found
     * <p><b>404</b> - User Not Found
     * @param nationalId  (required)
     * @return User
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public User getUserById(String nationalId) throws RestClientException {
        return getUserByIdWithHttpInfo(nationalId).getBody();
    }

    /**
     * Get User Info by User ID
     * Retrieve the information of the user with the matching user ID.
     * <p><b>200</b> - User Found
     * <p><b>404</b> - User Not Found
     * @param nationalId  (required)
     * @return ResponseEntity&lt;User&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<User> getUserByIdWithHttpInfo(String nationalId) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'nationalId' is set
        if (nationalId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'nationalId' when calling getUserById");
        }
        String path = UriComponentsBuilder.fromPath("/users").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nationalId", nationalId));

        final String[] accepts = { 
            "application/json"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<User> returnType = new ParameterizedTypeReference<User>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Update User Contact Information
     * Update the information of an existing user.
     * <p><b>200</b> - OK
     * <p><b>404</b> - User Not Found
     * <p><b>409</b> - Email Already Taken
     * @param nationalId  (required)
     * @param body  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void putUsersContactInfos(String nationalId, ContactInfos body) throws RestClientException {
        putUsersContactInfosWithHttpInfo(nationalId, body);
    }

    /**
     * Update User Contact Information
     * Update the information of an existing user.
     * <p><b>200</b> - OK
     * <p><b>404</b> - User Not Found
     * <p><b>409</b> - Email Already Taken
     * @param nationalId  (required)
     * @param body  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> putUsersContactInfosWithHttpInfo(String nationalId, ContactInfos body) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'nationalId' is set
        if (nationalId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'nationalId' when calling putUsersContactInfos");
        }
        String path = UriComponentsBuilder.fromPath("/users").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nationalId", nationalId));

        final String[] accepts = {  };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { 
            "application/json"
         };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<Void> returnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
}
