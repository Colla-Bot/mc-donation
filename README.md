# mc-donation

[![Discord](https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/B2PmZHUrUn)

## 주요 기능

* SOOP 및 치지직 공식 OpenAPI 연동
* 도전미션, 대결미션, 영상 후원 지원
* 클라이언트 사이드 처리로 서버 리소스 부담 및 개인정보 처리 최소화
* Skript 및 명령어 호출 지원

## 지원 버전

- Java 17+
- **서버 플러그인:** Bukkit/Spigot API 1.13.1+, Folia 지원
- **클라이언트 모드:** Fabric, Forge, NeoForge

## 플러그인 설정

`plugins/donation/config.yml`

```yaml
# 받을 후원 개수 (별풍선 100원 단위)
acceptedDonations:
  - 100
  - 200
# 틱당 받을 이벤트 수
limitPerTick: 5
```

> [!IMPORTANT]  
> 지정되지 않은 개수의 후원은 서버로 전송되지 않습니다.

## 연동

### 1. API

[![Maven Central Version](https://img.shields.io/maven-central/v/bot.colla/donation-api)](https://central.sonatype.com/artifact/bot.colla/donation-api) [![GitHub Release](https://img.shields.io/github/v/release/Colla-Bot/mc-donation)](https://github.com/Colla-Bot/mc-donation/releases/latest) [![Javadoc Status](https://img.shields.io/github/actions/workflow/status/Colla-Bot/mc-donation/pages/pages-build-deployment?label=Javadoc)](https://colla-bot.github.io/mc-donation/)

#### 의존성

```xml
<dependency>
    <groupId>bot.colla</groupId>
    <artifactId>donation-api</artifactId>
    <version>2.0.1</version>
    <scope>provided</scope>
</dependency>
```

```groovy
compileOnly 'bot.colla:donation-api:2.0.1'
```

> [!TIP]  
> Capability 충돌이 발생할 경우, 다음과 같이 해결 전략을 지정하여야 할 수도 있습니다.
> ```groovy
> configurations.configureEach {
>     resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
>         selectHighestVersion()
>     }
> }
> ```

#### 플러그인 의존성

```yaml
# plugin.yml
depend: [donation]

# paper-plugin.yml
dependencies:
  server:
    donation:
      load: BEFORE
      required: true
      join-classpath: true
```

#### 사용법

```java
import bot.colla.donation.event.DonationEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DonationListener implements Listener {
    @EventHandler
    public void onDonation(DonationEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("%s님이 %s %d개를 선물하였습니다!".formatted(
                event.getNickname(), event.getType().getName(), event.getCount()));
        event.setCancelled(true);
    }
}
```

> [!NOTE]  
> 이벤트를 처리한 경우 취소하여야 아래 명령어가 호출되지 않습니다.

### 2. Skript

```sk
[on] (api|donation|balloon|cheese) [received]:
  send "%event-text%님이 %event-donation type% %event-integer%개를 선물하였습니다!" to event-player
  cancel event
```

> [!TIP]  
> `event-integer`는 100원(별풍선 개수) 단위입니다.

### 3. 명령어 호출

위 이벤트가 취소되지 않았으면 `/api [플레이어] [유형] [액수] [닉네임]` 명령이 호출됩니다. 닉네임은 공백 또는 특수문자를 포함할 수 있습니다.

## 사용법

- `/[숲/치지직] [로그인/로그아웃/시작/중지]`: 후원 연동 로그인, 로그아웃, 시작, 중지
- `/[숲/치지직] 테스트 [유형] [닉네임] [액수]`: 테스트 이벤트 전송 (OP 권한 필요)
- `/donation reload`: 플러그인 설정 리로드 (OP 또는 `donation.donation` 권한 필요)

> [!TIP]  
> - SOOP 방송 시작 또는 재시작 시 연결에 최대 1분 가량 소요될 수 있습니다.
> - F3 디버그 화면에서 연결 상태를 확인할 수 있습니다. 1.21.9 이상에서는 F3+F6에서 `api_status` 또는 `donation:api_status` 항목을 활성화하여야 합니다.

## 후원 유형

| 유형 | SOOP | 치지직 |
| -- | ---- | --- |
| 채팅 후원 | ✅ `BALLOON`, `ADBALLOON`[^1] | ✅ `CHEESE` |
| 영상 후원 | ✅ `VIDEO_BALLOON` | ✅ `VIDEO_DONATION` |
| 미션 후원 | ✅ `CHALLENGE_MISSION`[^2] | ❌ |
| 대결미션 | ✅ `BATTLE_MISSION`[^3] | - |

[^1]: VOD 또는 방송국에 후원한 경우, 방송 입장 시 알림이 표시될 때 이벤트가 발생됩니다.
[^2]: 미션 성공 시 이벤트가 발생되며, 개수가 5% 내 초과되어도 인정됩니다.
[^3]: 승리 여부에 관계 없이 후원 시 후원한 방송에 이벤트가 발생됩니다.

## 제한 및 금지 행위

- 환금성이 있거나 사행성·도박성 콘텐츠에서 사용할 수 있는 재화를 지급해서는 안 됩니다.
- 포인트 등 후원 금액에 비례하여 재화를 지급하거나, 후원 순위를 산정·공개하는 등 직·간접적으로 후원 금액을 파악하거나 비교할 수 있는 행위를 해서는 안 됩니다.
- 서버 운영자에 대한 후원을 통해 특정 플레이어에게 경쟁상 우위를 제공해서는 안 되며, 확률형 아이템을 지급하는 경우 해당 확률을 명확히 공개하여야 합니다.
- 후원 로그를 기록하는 경우, 밸런스 조정, 기능 개선, 총 후원 금액 집계 등의 목적에 한하여 활용하여야 합니다.

## 라이선스

서버 플러그인과 클라이언트 모드는 모든 권리를 보유합니다.

API, 문서 및 견본 코드는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE-API](LICENSE-API) 파일을 참조하세요.

Javadoc 프로그램은 별도의 라이선스를 적용받습니다. 자세한 내용은 [docs/legal](docs/legal)을 참조하세요.

> All rights reserved for server plugins and client mods.
>
> The API, documentation, and sample code are distributed under the MIT license. Refer to the [LICENSE-API](LICENSE-API) file for details.
>
> The Javadoc program is subject to a separate license. Refer to [docs/legal](docs/legal) for details.

## 법적 고지

SOOP Open API는 [서비스 이용 정책](https://developers.sooplive.co.kr/?szWork=support&sub=terms), 치지직 API는 [치지직 개발자 센터 이용약관](https://developers.chzzk.naver.com/termsDetail)의 적용을 받습니다.

> This project contains references to the Bukkit API, which is licensed under the GNU General Public License (GPL). These references are included solely for compatibility purposes. It is our good-faith belief that such usage qualifies as fair use, as defined under applicable copyright law.
>
> This project does not include or distribute the source code of the Bukkit API, nor does it incorporate any part of the original implementation. All references to the Bukkit API are strictly limited to publicly available interfaces, as necessary for interoperability and plugin development.
>
> This project is not affiliated with, endorsed by, or sponsored by Bukkit, Mojang, Microsoft, SOOP, or CHZZK. All trademarks and copyrights are the property of their respective owners.

이 프로젝트는 Bukkit, Mojang, Microsoft, SOOP 또는 CHZZK과 관련이 없으며, 이들로부터 승인받거나 후원받지
않았습니다. 모든 상표권 및 저작권은 각 소유자의 재산입니다.
