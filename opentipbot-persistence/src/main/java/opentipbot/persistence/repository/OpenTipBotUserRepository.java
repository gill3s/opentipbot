package opentipbot.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import opentipbot.persistence.model.OpenTipBotUser;

/**
 * @author Gilles Cadignan
 */
public interface OpenTipBotUserRepository extends JpaRepository<OpenTipBotUser, Long> {
    OpenTipBotUser findByTwitterIdentifier(String twitterIdentifier);
    OpenTipBotUser findByUserName(String UserName);
}
