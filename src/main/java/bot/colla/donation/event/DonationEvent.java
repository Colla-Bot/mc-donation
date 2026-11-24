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
     * 후원한 후원 화폐(별풍선, 치즈, 캐시) 개수를 가져옵니다.
     * 유튜브의 경우, 원화로 환산한 금액을 가져옵니다.
     *
     * @return 후원 개수
     */
    public int getCount() {
        return amount * 100 / type.getPlatform().getPrice();
    }

    /**
     * 알림 테스트를 통해 발생한 후원인지 반환합니다.
     * <p>
     * 치지직과 투네이션(투네이션을 통해 받은 Super Chat 포함)의 알림 테스트만 구분할 수 있으며,
     * 닉네임이 {@code TEST}인 후원을 테스트로 판단합니다. 투네이션의 알림 테스트는 닉네임이 {@code TEST}로 전달됩니다.
     *
     * @return 테스트 후원 여부
     */
    public boolean isTestDonation() {
        return "TEST".equals(nickname) && (type.getPlatform() == Platform.CHZZK
                || type.getPlatform() == Platform.TOONATION
                || type == Type.SUPER_CHAT);
    }

    /**
     * 문장 형태의 사용자 친화적인 문자열 표현을 반환합니다.
     * SOOP 이외의 플랫폼은 후원 화폐를 단위로 사용하며, 천 단위 구분 기호는 서버의 로캘을 따릅니다.
     * <ul>
     *     <li>홍길동님으로부터 별풍선 33개를 후원</li>
     *     <li>도전미션 "탈출 성공"을/를 성공하여 별풍선 5개를 후원</li>
     *     <li>홍길동님으로부터 10,000 치즈를 후원</li>
     *     <li>홍길동님으로부터 영상 후원 10,000 치즈를 후원</li>
     *     <li>홍길동님으로부터 슈퍼챗 5,000 원을 후원</li>
     * </ul>
     *
     * @return 사용자 친화적인 문자열 표현
     */
    public String getDisplayString() {
        Platform platform = type.getPlatform();
        if (type == Type.CHALLENGE_MISSION) {
            return "%s \"%s\"을/를 성공하여 %s %,d개를 후원".formatted(
                    type.getName(), nickname, platform.getCurrency(), getCount());
        }
        if (platform == Platform.SOOP) {
            return "%s님으로부터 %s %,d개를 후원".formatted(nickname, type.getName(), getCount());
        }
        String currency = platform.getCurrency();
        // 후원 유형의 이름이 후원 화폐와 같으면(치즈, 빔, 캐시) 반복하지 않습니다.
        String kind = type.getName().equals(currency) ? "" : type.getName() + " ";
        return "%s님으로부터 %s%,d %s%s 후원".formatted(
                nickname, kind, getCount(), currency, hasFinalConsonant(currency) ? "을" : "를");
    }

    // 목적격 조사(을/를)를 고르기 위해 마지막 글자의 받침 유무를 확인합니다.
    private static boolean hasFinalConsonant(String word) {
        char last = word.charAt(word.length() - 1);
        return last < 0xAC00 || last > 0xD7A3 || (last - 0xAC00) % 28 != 0;
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
         * <a href="https://www.sooplive.com/">SOOP</a>
         */
        SOOP("숲", "별풍선", 100),
        /**
         * <a href="https://chzzk.naver.com/">치지직</a>
         */
        CHZZK("치지직", "치즈", 1),
        /**
         * <a href="https://ci.me/">씨미</a>
         */
        CIME("씨미", "빔", 1),
        /**
         * <a href="https://www.youtube.com/">유튜브</a>
         */
        YOUTUBE("유튜브", "원", 1),
        /**
         * <a href="https://toon.at/">투네이션</a>
         */
        TOONATION("투네이션", "캐시", 1);

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
        VIDEO_DONATION("영상 후원", Platform.CHZZK),
        /**
         * 빔 채팅 후원 (씨미)
         */
        BEAM("빔", Platform.CIME),
        /**
         * 빔 영상 후원 (씨미)
         */
        VIDEO_BEAM("영상 후원", Platform.CIME),
        /**
         * Super Chat 및 Super Sticker (유튜브, KRW/USD/JPY 결제만 받으며 1 USD = 100 JPY = 1,000원으로 환산)
         * <p>
         * 유튜브와 투네이션 중 어느 쪽에 로그인해도 받을 수 있으며,
         * 둘 다 로그인한 경우 같은 Super Chat이 두 번 전달됩니다.
         */
        SUPER_CHAT("슈퍼챗", Platform.YOUTUBE),
        /**
         * 캐시 후원 (투네이션, 텍스트·음성·영상·룰렛 등 후원 유형에 관계없이 전달)
         * <p>
         * 럭키박스처럼 결과에 따라 환불될 수 있는 후원은 크리에이터가 받는 금액이 확정되었을 때 그 금액으로 전달되며,
         * 퀘스트·플레이 후원은 전달되지 않습니다.
         */
        TOONATION_CASH("캐시", Platform.TOONATION);

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
