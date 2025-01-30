package com.example.swapit.config.oauth;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.Users;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

/**
 * OAuth 로그인 성공 후 사용자 정보 처리
 * 구글 로그인 후 필요한 정보들을 가져와 DB를 체크하여 DB에 소셜로그인을 한 기록이 없다면 DB에 저장하고 그렇지 않다면 저장하지 않는 로직
 */
@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

	private final UsersRepository usersRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2User oauth2User = super.loadUser(userRequest);

		String provider = userRequest.getClientRegistration()
			.getRegistrationId(); //google
		String provideId = oauth2User.getAttribute("sub");
		String email = oauth2User.getAttribute("email");
		String nickname = oauth2User.getAttribute("name");
		String profile = oauth2User.getAttribute("picture");

		Optional<Users> users = usersRepository.findByEmail(email);

		//이미 소셜로그인을 한적이 있는지 없는지
		if (users.isEmpty()) {
			Users newUser = Users.builder()
				.email(email)
				.nickname(nickname)
				.profileImageUrl(profile)
				.provider(provider)
				.providerId(provideId)
				.build();

			usersRepository.save(newUser);
			return new PrincipalDetails(newUser, oauth2User.getAttributes()); // 인증 정보 반환
		} else {
			return new PrincipalDetails(users.get(), oauth2User.getAttributes());
		}
	}
}
