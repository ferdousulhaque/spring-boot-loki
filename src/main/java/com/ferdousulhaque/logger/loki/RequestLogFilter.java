package com.ferdousulhaque.logger.loki;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestLogFilter extends GenericFilterBean {

    @Value("${loki.truncateCount}")
    private int truncateAfterWordCount;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
        chain.doFilter(requestWrapper, responseWrapper);
        logSetter(requestWrapper, responseWrapper);
    }

    @SneakyThrows
    private void logSetter(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response){
        InetAddress ipaddr = getIp();
        String hostname = getHostname();
//        String requestHeaders = getRequestHeaders(request);
        String requestBody = getRequestBody(request);
        String requestParams = getRequestParams(request);
        String responseHeaders = getResponseHeaders(response);
        String responseBody = getResponseBody(response);
        String responseCode = getResponseCode(response);
        response.copyBodyToResponse();

        // Set
        MDC.put("ipaddr", ipaddr.toString());
        MDC.put("hostname", hostname);
//        MDC.put("requestHeaders", requestHeaders);
        MDC.put("requestBody", requestBody);
        MDC.put("requestParams", requestParams);
        MDC.put("responseHeaders", responseHeaders);
        MDC.put("responseBody", responseBody);
        MDC.put("responseCode", responseCode);

        // Push
        log.info("{}", jsonEncodeAndTruncate(request.getMethod() + " " + request.getRequestURI()));
    }

    private InetAddress getIp() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    private String getHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    private String getRequestHeaders(ContentCachingRequestWrapper request){
        return multiLineToSingleLine(
                    headersToString(
                            Collections.list(request.getHeaderNames()), request::getHeader));
    }
    @SneakyThrows
    private String getRequestBody(ContentCachingRequestWrapper request){
        StringBuilder body = new StringBuilder();
        String line;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        if(body.isEmpty()){
            body.append(new String(request.getContentAsByteArray()));
        }
        return jsonEncodeAndTruncate(body.toString());
    }
    @SneakyThrows
    private String getRequestParams(ContentCachingRequestWrapper request) {
        return parametersToString(request.getParameterMap());
    }

    private String getResponseHeaders(ContentCachingResponseWrapper response){
        return headersToString(response.getHeaderNames(), response::getHeader);
    }
    @SneakyThrows
    private String getResponseBody(ContentCachingResponseWrapper response){
        return jsonEncodeAndTruncate(new String(response.getContentAsByteArray()));
    }
    @SneakyThrows
    private String getResponseCode(ContentCachingResponseWrapper response) {
        return String.valueOf(response.getStatus());
    }

    // Helper Functions
    private static String joinMapIntoString(Map<String, String> logMap) {
        return logMap.entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(e -> String.join(" ", e.getKey(), e.getValue()))
                .collect(Collectors.joining(""));
    }

    @SneakyThrows
    private String headersToString(Collection<String> headerNames, Function<String, String> headerValueResolver) {
        return headerNames.stream()
                .map(header -> String.join("=", header, headerValueResolver.apply(header)))
                .collect(Collectors.joining("\n"));
    }

    private String parametersToString(Map<String, String[]> parameterMap) {
        return parameterMap.entrySet().stream()
                .map(param -> String.join("=", param.getKey(), Arrays.toString(param.getValue())))
                .collect(Collectors.joining("\n"));
    }

    private String multiLineToSingleLine(String multiLine){
        return multiLine.replaceAll("\r\n", " ");
    }

    private String jsonEncodeAndTruncate(String payload){
        payload = payload.replace("\"", "\\\"")
                        .replace("\n", " ")
                        .replace("\r", " ");
        if(payload.length() > truncateAfterWordCount) {
            payload = payload.substring(0, truncateAfterWordCount);
        }
        return payload;
    }
}