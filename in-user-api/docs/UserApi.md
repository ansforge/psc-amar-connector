# UserApi

All URIs are relative to *http://localhost:3000*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getUserById**](UserApi.md#getUserById) | **GET** /users | Get User Info by User ID

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

