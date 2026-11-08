package com.lib.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApiResultTest {

    private final Gson gson = new Gson();

    @After
    public void resetSuccessCodes() {
        ApiResult.setSuccessCodes(0, 200);
    }

    @Test
    public void parseBean() {
        String json = "{\"code\":0,\"msg\":\"ok\",\"data\":{\"id\":7,\"name\":\"ann\"}}";
        ApiResult<User> result = ApiResults.parse(gson, json, beanType());
        assertTrue(result.isSuccess());
        assertEquals(0, result.getCode());
        assertEquals("ok", result.getMsg());
        assertNotNull(result.getData());
        assertEquals(7, result.getData().id);
        assertEquals("ann", result.getData().name);
        assertTrue(result.isCodeSuccess());
        assertTrue(result.hasData());
        assertFalse(result.isDataEmpty());
        assertTrue(result.isListDataEmpty());
        assertFalse(result.hasListData());
        assertEquals(result.getData(), Single.just(result).compose(ApiResults.unwrap()).blockingGet());
    }

    @Test
    public void parseListAndStringCode() {
        String json = "{\"code\":\"200\",\"message\":\"done\",\"data\":[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"}]}";
        ApiResult<List<User>> result = ApiResults.parse(gson, json, listType());
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertEquals("done", result.getMsg());
        assertEquals(2, result.getData().size());
        assertEquals("b", result.getData().get(1).name);
        assertTrue(result.hasData());
        assertTrue(result.hasListData());
        assertFalse(result.isListDataEmpty());
    }

    @Test
    public void emptyStringDataIsNullForBean() {
        String json = "{\"code\":0,\"msg\":\"ok\",\"data\":\"\"}";
        ApiResult<User> result = ApiResults.parse(gson, json, beanType());
        assertTrue(result.isCodeSuccess());
        assertNull(result.getData());
        assertTrue(result.isDataEmpty());
        assertFalse(result.hasData());
        try {
            ApiResults.requireData(result);
            fail();
        } catch (ApiException e) {
            assertEquals(0, e.getCode());
            assertEquals("data 为空", e.getMessage());
        }
    }

    @Test
    public void emptyStringDataKeptForString() {
        String json = "{\"code\":0,\"msg\":\"ok\",\"data\":\"\"}";
        Type type = new TypeToken<ApiResult<String>>() {
        }.getType();
        ApiResult<String> result = ApiResults.parse(gson, json, type);
        assertEquals("", result.getData());
        assertTrue(result.isDataEmpty());
        assertFalse(result.hasData());
    }

    @Test
    public void businessFailure() {
        String json = "{\"code\":-1,\"errorMsg\":\"登录失效\"}";
        ApiResult<User> result = ApiResults.parse(gson, json, beanType());
        assertFalse(result.isCodeSuccess());
        assertFalse(result.hasData());
        assertEquals(-1, result.getCode());
        assertEquals("登录失效", result.getMsg());
        try {
            Single.just(result).compose(ApiResults.<User>unwrap()).blockingGet();
            fail();
        } catch (ApiException e) {
            assertEquals(-1, e.getCode());
            assertEquals("登录失效", e.getMessage());
        }
    }

    @Test
    public void customSuccessCode() {
        ApiResult.setSuccessCodes(1000);
        String json = "{\"code\":1000,\"msg\":\"ok\",\"data\":{\"id\":1,\"name\":\"a\"}}";
        ApiResult<User> result = ApiResults.parse(gson, json, beanType());
        assertTrue(result.isSuccess());
        assertFalse(ApiResults.parse(gson, "{\"code\":0,\"msg\":\"no\",\"data\":{}}", beanType()).isSuccess());
    }

    @Test
    public void emptyListIsData() {
        String json = "{\"code\":0,\"msg\":\"ok\",\"data\":[]}";
        ApiResult<List<User>> result = ApiResults.parse(gson, json, listType());
        assertTrue(result.isCodeSuccess());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().size());
        assertTrue(result.isDataEmpty());
        assertTrue(result.isListDataEmpty());
        assertFalse(result.hasData());
        assertFalse(result.hasListData());
        assertEquals(0, Single.just(result).compose(ApiResults.<List<User>>unwrap()).blockingGet().size());
    }

    @Test
    public void notJson() {
        ApiResult<User> result = ApiResults.parse(gson, "<html>", beanType());
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().startsWith("响应不是 JSON"));
    }

    private static Type beanType() {
        return new TypeToken<ApiResult<User>>() {
        }.getType();
    }

    private static Type listType() {
        return new TypeToken<ApiResult<List<User>>>() {
        }.getType();
    }

    private static final class User {
        int id;
        String name;
    }
}
