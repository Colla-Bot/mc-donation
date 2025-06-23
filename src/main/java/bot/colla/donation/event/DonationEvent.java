/*
 * Copyright 2025 Argo
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package bot.colla.donation.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * 플레이어가 후원을 받았을 때 발생됩니다. 취소되지 않으면 {@code /api} 명령이 호출됩니다.
 */
public class DonationEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Type type;
    private final String nickname;
    private final int amount;
    private boolean cancelled;

    public DonationEvent(Player player, Type type, String nickname, int amount) {
        super(player);
        this.type = type;
        this.nickname = nickname;
        this.amount = amount;
    }

    /**
     * 후원 유형을 가져옵니다.
     *
     * @return 후원 유형
     */
    public Type getType() {
        return type;
    }

    /**
     * 후원자 닉네임을 가져옵니다. 미션의 경우, 미션 제목을 가져옵니다.
     * 공백 및 특수문자가 포함될 수 있습니다.
     *
     * @return 후원자 닉네임 또는 미션 제목
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 후원 금액을 100원(별풍선 개수) 단위로 가져옵니다.
     *
     * @return 후원 금액
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 후원한 후원 화폐(별풍선, 치즈) 개수를 가져옵니다.
     *
     * @return 후원 개수
     */
    public int getCount() {
        return amount * 100 / type.getPlatform().getPrice();
    }

    /**
     * 알림 테스트롤 통해 발생한 후원인지 반환합니다.
     *
     * @return 테스트 후원 여부
     */
    public boolean isTestDonation() {
        return type.getPlatform() == Platform.CHZZK && "TEST".equals(nickname);
    }

    /**
     * 문장 형태의 사용자 친화적인 문자열 표현을 반환합니다.
     * <ul>
     *     <li>홍길동님으로부터 별풍선 33개를 후원</li>
     *     <li>도전미션 "탈출 성공"을/를 성공하여 별풍선 5개를 후원</li>
     * </ul>
     *
     * @return 사용자 친화적인 문자열 표현
     */
    public String getDisplayString() {
        if (type == Type.CHALLENGE_MISSION) {
            return "%s \"%s\"을/를 성공하여 %s %d개를 후원".formatted(
                    type.getName(), nickname, type.getPlatform().getCurrency(), getCount());
        }
        return "%s님으로부터 %s %d개를 후원".formatted(nickname, type.getName(), getCount());
    }

    @Override
    public String toString() {
        return "DonationEvent{" +
                "player=" + player +
                ", type=" + type +
                ", nickname='" + nickname + '\'' +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * 방송 플랫폼
     */
    public enum Platform {
        /**
         * <a href="https://www.sooplive.co.kr/">SOOP</a>
         */
        SOOP("숲", "별풍선", 100),
        /**
         * <a href="https://chzzk.naver.com/">치지직</a>
         */
        CHZZK("치지직", "치즈", 1);

        private final String name;
        private final String currency;
        private final int price;

        Platform(String name, String currency, int price) {
            this.name = name;
            this.currency = currency;
            this.price = price;
        }

        /**
         * 플랫폼 이름을 가져옵니다.
         *
         * @return 이름
         */
        public String getName() {
            return name;
        }

        /**
         * 후원 화폐(별풍선, 치즈) 이름을 가져옵니다.
         *
         * @return 후원 화폐
         */
        public String getCurrency() {
            return currency;
        }

        /**
         * 후원 화폐 가격을 가져옵니다.
         *
         * @return 후원 화폐 가격
         */
        public int getPrice() {
            return price;
        }
    }

    /**
     * 후원 유형
     */
    public enum Type {
        /**
         * 별풍선 (SOOP)
         */
        BALLOON("별풍선", Platform.SOOP),
        /**
         * 애드벌룬 (SOOP)
         */
        ADBALLOON("애드벌룬", Platform.SOOP),
        /**
         * 대결미션 (SOOP, 승리 여부에 관계 없이 후원한 방송에 후원 시 전달)
         */
        BATTLE_MISSION("대결미션", Platform.SOOP),
        /**
         * 도전미션 (SOOP, 미션 성공 시 전달)
         */
        CHALLENGE_MISSION("도전미션", Platform.SOOP),
        /**
         * 영상풍선 (SOOP)
         */
        VIDEO_BALLOON("영상풍선", Platform.SOOP),
        /**
         * 치즈 채팅 후원 (치지직)
         */
        CHEESE("치즈", Platform.CHZZK),
        /**
         * 치즈 영상 후원 (치지직)
         */
        VIDEO_DONATION("영상 후원", Platform.CHZZK);

        private final String name;
        private final Platform platform;

        Type(String name, Platform platform) {
            this.name = name;
            this.platform = platform;
        }

        /**
         * 후원 유형 이름을 가져옵니다.
         *
         * @return 이름
         */
        public String getName() {
            return name;
        }

        /**
         * 방송 플랫폼을 가져옵니다.
         *
         * @return 플랫폼
         */
        public Platform getPlatform() {
            return platform;
        }
    }
}
