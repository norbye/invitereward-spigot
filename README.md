# InviteReward
Spigot plugin to allow users to type in a reward-code and execute a command to the server matching that reward-code.

Created for being able to invite users to your server and enable them to cash in a reward if they decide to join.

## Configuration
```yaml
# InviteReward v1.0.0
config-version: 1
debug: false
db:
  host: localhost
  port: 3306
  name:
  user:
  pass:
```

## Commands
`/invitereward reload` - Reload the config and restart the mysql connection

`/invitereward <reward-code>` - Receice your reward-code

## Setup
1. Setup a mySQL database
2. Download the plugin jar or package with maven them yourself from this code
3. Run your server
4. Configure the plugin config.yml with your db credentials
5. Reload the plugin
6. Add the reward-codes and commands you desire to use