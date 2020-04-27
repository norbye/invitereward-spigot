# InviteReward
Spigot plugin to allow users to type in a reward-code and execute a command to the server matching that reward-code.

Created for being able to invite users to your server and enable them to cash in a reward if they decide to join.

## Configuration
```yaml
# InviteReward
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
`/redeem <code>` `invitereward.redeem | default:true`

Redeem a registered reward.

`/invitereward reload` `invitereward.reload | default:false`

Reload plugin config and reconnect to database.

`/invitereward list` `invitereward.list | default:false`

List active commands.

## Setup
1. Setup a mySQL database
2. Download the plugin jar or package it with maven yourself from this repo
3. Run your server
4. Configure the plugin config.yml with your db credentials
5. Reload the plugin
6. Add the reward-codes and commands you desire to use