package com.nttdata.extensions.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.stream.Stream; 
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class EnterpriseService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${extension.githubcopilot.url}")
	private String API_URL;

    @Value("${extension.enterprises.group.enterprise}")
    private String ENTERPRISE;

    @Value("${extension.enterprises.group.business}")
    private String BUSINESS;

    @Value("${extension.enterprises.group.copilot}")
    private String COPILOT;

    @Value("${extension.enterprises.group.kirin}")
    private String KIRIN;

	private static final Logger logger = LoggerFactory.getLogger(EnterpriseService.class);

	/**
	 * ユーザーが登録済みのエンタープライズに所属しているかどうかを判定する。
	 * 
	 * @param principal        OAuth2User オブジェクト、認証されたユーザーの情報。
	 * @param authorizedClient OAuth2AuthorizedClient オブジェクト、認証されたクライアントの情報。
	 * @return ユーザーがエンタープライズに所属している場合は true、そうでない場合は false。
	 */
	public boolean isUserInRegisteredEnterprise(OAuth2User principal, OAuth2AuthorizedClient authorizedClient) throws Exception {
		List<String> slugList = getSlugList(principal, authorizedClient);
		logger.debug("slug list: {}", slugList);
		if (slugList == null || slugList.size() == 0) {
			return false;
		}
		boolean constantsEntLst = Stream.of(ENTERPRISE, BUSINESS, COPILOT, KIRIN).anyMatch(slugList::contains);
		return constantsEntLst;
	}

	/**
	 * githubのgraphqlAPIからユーザーに紐づく組織情報を取得する
	 * @param principal        OAuth2User オブジェクト、認証されたユーザーの情報。
	 * @param authorizedClient OAuth2AuthorizedClient オブジェクト、認証されたクライアントの情報。
	 * @return ユーザーのエンタープライズ情報
	 */
	private List<String> getSlugList(OAuth2User principal, OAuth2AuthorizedClient authorizedClient) throws Exception {
		String username = getUsername(principal);
		String ACCESS_TOKEN = authorizedClient.getAccessToken().getTokenValue();
		String GRAPHQL_URL = API_URL + "graphql";
		String requestBody = createHttpBody(username);
		HttpHeaders headers = createHttpHeader(ACCESS_TOKEN);
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		logger.debug("URL:{}", GRAPHQL_URL);
		ResponseEntity<JsonNode> response = restTemplate.exchange(GRAPHQL_URL, HttpMethod.POST, entity,
				new ParameterizedTypeReference<JsonNode>() {
				});
		JsonNode responseBody = response.getBody();
		if(responseBody == null || responseBody.get("errors") != null){
			return null;
		}
		return Arrays.asList(responseBody.get("data").get("user").get("enterprises").get("nodes")
				.findValuesAsText("slug").toArray(new String[0]));
	}

	/**
	 * 指定されたトークンを使用してHTTPヘッダーを作成する。
	 *
	 * @param token トークン
	 * @return 作成されたHTTPヘッダー
	 */
	private HttpHeaders createHttpHeader(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.valueOf("application/json")));
		headers.setBearerAuth(token);
		return headers;
	}

	/**
	 * ユーザー名を指定して、エンタープライズのリストを取得するGraphQLのHTTPリクエストボディを作成する。
	 *
	 * @param username ユーザー名
	 * @return HTTPリクエストのボディ(GraphQL)
	 */
	private String createHttpBody(String username) {
		StringBuilder requestBody = new StringBuilder();
		requestBody.append("{\"query\": \"query { user(login: ");
		requestBody.append("\\\"" + username + "\\\"");
		requestBody.append(") { enterprises(first: 100) { nodes { slug name } } } }\"}");
		logger.debug("GRAPHQL:{}", requestBody.toString());

		return requestBody.toString();
	}

	/**
	 * OAuth2ユーザーからユーザー名を取得する。
	 * 
	 * @param principal OAuth2ユーザー
	 * @return ユーザー名
	 */
	private String getUsername(OAuth2User principal) {
		return principal.getAttribute("login");
	}

}
