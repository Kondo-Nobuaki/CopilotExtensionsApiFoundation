package com.nttdata.extensions.contoroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.nttdata.extensions.service.EnterpriseService;

@Controller
public class IndexContoroller {

    @Autowired
    private EnterpriseService enterpriseService;

    /**
     * 初期表示
     * 
     * @return メイン画面
     */
    @GetMapping("index")
	public String forward(@AuthenticationPrincipal OAuth2User principal,
			@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) throws Exception {
        if(enterpriseService.isUserInRegisteredEnterprise(principal, authorizedClient)){
            return "error";
        }
        return "loginPage";
    }
}
