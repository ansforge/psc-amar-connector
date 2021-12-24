# DefaultApi

All URIs are relative to *http://localhost:3000*

Method | HTTP request | Description
------------- | ------------- | -------------
[**putUsersContactInfos**](DefaultApi.md#putUsersContactInfos) | **PUT** /users | Update User Contact Information

<a name="putUsersContactInfos"></a>
# **putUsersContactInfos**
> putUsersContactInfos(body)

Update User Contact Information

Update the information of an existing user.

### Example
```java
// Import classes:
//import fr.ans.in.user.ApiException;
//import fr.ans.in.user.api.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
ContactInfos body = new ContactInfos(); // ContactInfos | 
try {
    apiInstance.putUsersContactInfos(body);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#putUsersContactInfos");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ContactInfos**](ContactInfos.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

