# randomspawn

<p align=center>
  <img src=img/RS.png alt=RS width=150/>
</p>

![Version](https://img.shields.io/badge/version-0.1-blue)
![Folia](https://img.shields.io/badge/Folia-supported-brightgreen)
![Paper](https://img.shields.io/badge/Paper-supported-brightgreen)
![Purpur](https://img.shields.io/badge/Purpur-supported-brightgreen)
![Bukkit](https://img.shields.io/badge/Bukkit-supported-brightgreen)

**Teleport players to a random location on first join and on death**

---

## Build

**Requirements:** Java 21, Gradle (wrapper included)

```bash
git clone https://github.com/mytai20100/randomspawn.git
cd randomspawn
./gradlew build
```

The compiled jar will be in `build/libs/`.

To run a local test server:

```bash
./gradlew runServer
```

---

## Permissions

| Permission | Description | Default |
|---|---|---|
| `randomspawn.admin` | Access to all `/rdsw` commands | OP |
## Commands

---


| Command | Description |
|---|---|
| `/rdsw around <radius>` | Teleport yourself to a random spot within the given radius |
| `/rdsw reload` | Reload the config |
| `/rdsw info` | Show plugin version, author, and AuthMe version (if installed) |

---

<details>
<summary>config.yml</summary>

```yaml
randomspawn:
  enabled: true
  around: 1000
  respawn_timeout: 30
  spawn_on_every_login: false
  main_world: world
  debug: false