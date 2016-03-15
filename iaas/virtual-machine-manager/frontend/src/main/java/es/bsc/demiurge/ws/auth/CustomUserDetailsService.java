package es.bsc.demiurge.ws.auth;

import es.bsc.demiurge.core.auth.User;
import es.bsc.demiurge.core.auth.UserDao;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.manager.VmManager;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public class CustomUserDetailsService implements UserDetailsService {
	@Override
	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
		UserDao dao = Config.INSTANCE.getVmManager().getDB().getUserDao();
		final User user = dao.loadUser(s);
		return new UserDetails() {
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return Collections.singleton(new SimpleGrantedAuthority(User.ROLE_USER));
			}

			@Override
			public String getPassword() {
				return user.getCipheredPassword();
			}

			@Override
			public String getUsername() {
				return user.getUserName();
			}

			@Override
			public boolean isAccountNonExpired() {
				return true;
			}

			@Override
			public boolean isAccountNonLocked() {
				return true;
			}

			@Override
			public boolean isCredentialsNonExpired() {
				return true;
			}

			@Override
			public boolean isEnabled() {
				return user.isEnabled();
			}
		};
	}

}
