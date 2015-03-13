package opentipbot.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import opentipbot.persistence.model.OpenTipBotCommand;
import opentipbot.persistence.model.OpenTipBotCommandEnum;
import opentipbot.persistence.model.OpenTipBotCommandStatus;

import java.util.List;

/**
 * @author Gilles Cadignan
 */
public interface OpenTipBotCommandRepository extends JpaRepository<OpenTipBotCommand, Long> {
    List<OpenTipBotCommand> findByTweetIdentifier(Long tweetIdentifier);

    List<OpenTipBotCommand> findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus status, OpenTipBotCommandEnum commandEnum);

    @Query("SELECT c from OpenTipBotCommand c WHERE c.fromUserName = :userName AND ( c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP" +
            " OR c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP_RAIN " +
            "OR c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP_RANDOM ) order by c.modificationTime desc")
    List<OpenTipBotCommand> getTipsSent(@Param("userName") String userName);


    @Query("SELECT c from OpenTipBotCommand c WHERE c.toUserName = :userName AND ( c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP" +
            " OR c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP_RAIN " +
            "OR c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP_RANDOM )  order by c.modificationTime desc")
    List<OpenTipBotCommand> getTipsReceived(@Param("userName") String userName);

    @Query("SELECT c from OpenTipBotCommand c WHERE c.fromUserName = :userName AND c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.WITHDRAW  order by c.modificationTime desc")
    List<OpenTipBotCommand> getWithDrawals(@Param("userName") String userName);

    @Query("SELECT c from OpenTipBotCommand c WHERE c.opentipbotCommandStatus = opentipbot.persistence.model.OpenTipBotCommandStatus.PROCESSED AND (c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP" +
            " OR c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP_RAIN " +
            "OR c.opentipbotCommandEnum = opentipbot.persistence.model.OpenTipBotCommandEnum.TIP_RANDOM) order by c.modificationTime desc")
    Page<OpenTipBotCommand> getLastTips(Pageable page);

}
