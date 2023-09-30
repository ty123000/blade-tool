/**
 * Copyright (c) 2018-2028, DreamLu 卢春梦 (qq596392912@gmail.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springblade.core.cloud.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import org.springblade.core.cloud.header.BladeFeignAccountGetter;
import org.springblade.core.cloud.props.BladeFeignHeadersProperties;
import org.springblade.core.tool.utils.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Http RestTemplateHeaderInterceptor 配置
 *
 * @author L.cm
 */
@AutoConfiguration
@AllArgsConstructor
@ConditionalOnClass(okhttp3.OkHttpClient.class)
@EnableConfigurationProperties(BladeFeignHeadersProperties.class)
public class RestTemplateConfiguration {
	private final ObjectMapper objectMapper;

	/**
	 * dev, test 环境打印出BODY
	 *
	 * @return HttpLoggingInterceptor
	 */
	@Bean("httpLoggingInterceptor")
	@Profile({"dev", "test"})
	public HttpLoggingInterceptor testLoggingInterceptor() {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new OkHttpSlf4jLogger());
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		return interceptor;
	}

	/**
	 * ontest 环境 打印 请求头
	 *
	 * @return HttpLoggingInterceptor
	 */
	@Bean("httpLoggingInterceptor")
	@Profile("ontest")
	public HttpLoggingInterceptor onTestLoggingInterceptor() {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new OkHttpSlf4jLogger());
		interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
		return interceptor;
	}

	/**
	 * prod 环境只打印请求url
	 *
	 * @return HttpLoggingInterceptor
	 */
	@Bean("httpLoggingInterceptor")
	@Profile("prod")
	public HttpLoggingInterceptor prodLoggingInterceptor() {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new OkHttpSlf4jLogger());
		interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
		return interceptor;
	}

	/**
	 * okhttp3 链接池配置
	 *
	 * @return okhttp3.ConnectionPool
	 */
	@Bean
	public okhttp3.ConnectionPool connectionPool() {
		int maxIdleConnections = 5; // 最大空闲连接数
		long keepAliveDuration = 5 * 60 * 1000; // 连接保持活跃时间（毫秒）
		return new okhttp3.ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MILLISECONDS);
	}

	/**
	 * 配置OkHttpClient
	 *
	 * @param connectionPool 链接池配置
	 * @param interceptor    拦截器
	 * @return OkHttpClient
	 */
	@Bean
	@ConditionalOnMissingBean(okhttp3.OkHttpClient.class)
	public okhttp3.OkHttpClient httpClient(
		okhttp3.ConnectionPool connectionPool,
		HttpLoggingInterceptor interceptor) {
		OkHttpClient okHttpClient = new OkHttpClient();
		OkHttpClient.Builder builder = okHttpClient.newBuilder();
		builder.setConnectionPool$okhttp(connectionPool);
		builder.addInterceptor(interceptor);
		return builder.build();
	}

	@Bean
	public RestTemplateHeaderInterceptor requestHeaderInterceptor(
		@Autowired(required = false) @Nullable BladeFeignAccountGetter accountGetter,
		BladeFeignHeadersProperties properties) {
		return new RestTemplateHeaderInterceptor(accountGetter, properties);
	}

	/**
	 * 普通的 RestTemplate，不透传请求头，一般只做外部 http 调用
	 *
	 * @param httpClient OkHttpClient
	 * @return RestTemplate
	 */
	@Bean
	@ConditionalOnMissingBean(RestTemplate.class)
	public RestTemplate restTemplate(okhttp3.OkHttpClient httpClient) {
		RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(httpClient));
		configMessageConverters(restTemplate.getMessageConverters());
		return restTemplate;
	}

	/**
	 * 支持负载均衡的 LbRestTemplate
	 *
	 * @param httpClient  OkHttpClient
	 * @param interceptor RestTemplateHeaderInterceptor
	 * @return LbRestTemplate
	 */
	@Bean
	@LoadBalanced
	@ConditionalOnMissingBean(LbRestTemplate.class)
	public LbRestTemplate lbRestTemplate(okhttp3.OkHttpClient httpClient, RestTemplateHeaderInterceptor interceptor) {
		LbRestTemplate lbRestTemplate = new LbRestTemplate(new OkHttp3ClientHttpRequestFactory(httpClient));
		lbRestTemplate.setInterceptors(Collections.singletonList(interceptor));
		configMessageConverters(lbRestTemplate.getMessageConverters());
		return lbRestTemplate;
	}

	private void configMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.removeIf(x -> x instanceof StringHttpMessageConverter || x instanceof MappingJackson2HttpMessageConverter);
		converters.add(new StringHttpMessageConverter(Charsets.UTF_8));
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
	}
}
