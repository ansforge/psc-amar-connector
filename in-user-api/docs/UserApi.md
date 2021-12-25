# UserApi

All URIs are relative to *http://localhost:3000*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getUserById**](UserApi.md#getUserById) | **GET** /users | Get User Info by User ID
[**putUsersContactInfos**](UserApi.md#putUsersContactInfos) | **PUT** /users | Update User Contact Information

<a name="getUserById"></a>
# **getUserById**
> User getUserById(nationalId)

Get User Info by User ID

Retrieve the information of the user with the matching user ID.

### Example
```java
// Import classes:
//import fr.ans.in.user.ApiException;
//import fr.ans.in.user.api.UserApi;


UserApi apiInstance = new UserApi();
String nationalId = "nationalId_example"; // String | 
try {
    User result = apiInstance.getUserById(nationalId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UserApi#getUserById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **nationalId** | **String**|  |

### Return type

[**User**](User.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="putUsersContactInfos"></a>
# **putUsersContactInfos**
> putUsersContactInfos(nationalId, body)

Update User Contact Information

Update the information of an existing user.

### Example
```java
// Import classes:
//import fr.ans.in.user.ApiException;
//import fr.ans.in.user.api.UserApi;


UserApi apiInstance = new UserApi();
String nationalId = "nationalId_example"; // String | 
ContactInfos body = new ContactInfos(); // ContactInfos | 
try {
    apiInstance.putUsersContactInfos(nationalId, body);
} catch (ApiException e) {
    System.err.println("Exception when calling UserApi#putUsersContactInfos");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **nationalId** | **String**|  |
 **body** | [**ContactInfos**](ContactInfos.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

