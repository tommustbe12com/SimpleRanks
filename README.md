# SimpleRanks

A lightweight and customizable rank management plugin for PaperMC (1.20+).  
Easily create, assign, and manage colored ranks that appear in chat and the tab list.

---

## Features

- Create unlimited custom ranks  
- Colored rank prefixes using `&` codes  
- Show ranks in `chat` and `tab list`  
- Per-player rank assignment  
- Default rank support  
- Optional `importanttext` toggle (white vs gray chat)  
- Tab completion for commands  
- Configurable through `config.yml`

---

## Configuration (`config.yml`)

default-rank: default  
ranks: {}  
player-ranks: {}

> Ranks and players will be added to this file automatically.  
> Customize rank prefix colors using `&` codes (e.g., `&cAdmin`).  
> Per-rank options (stored under `ranks.<rank>`): `priority` (lower = higher) and `brackets` (true/false).  
> Global option: `brackets.enabled` (true/false).

---

## Chat & Tab Display

Players will have their rank prefix automatically shown:

- Chat: `[&cAdmin&7] PlayerName: Hello world!`  
- Tab list: `[&cAdmin&7] PlayerName`  
- Message color: white (`importanttext: true`) or gray (`false`)

(these are just examples for a red colored Admin rank)

---

## Commands

All commands use `/rank`:

- `/rank create <rank>` - Create a new rank  
- `/rank delete <rank>` - Delete a rank  
- `/rank setdefault <rank>` - Set the default rank  
- `/rank set <player> <rank>` - Set a player's rank  
- `/rank give <player> <rank>` - Alias for `set`  
- `/rank get <player>` - View a player's current rank  
- `/rank list` - List all ranks  
- `/rank importanttext <rank> true|false` - Toggle white/gray message color
- `/rank priority <rank> <num>` - Set tab list ordering (0 is highest)
- `/rank brackets <on|off>` - Enable/disable brackets globally
- `/rank brackets <rank> <on|off>` - Enable/disable brackets for that rank

You can also use `/r`, as that is the alias for /rank, for quick and easy use.

---

## Notes

- Rank colors and brackets show up in chat and tab list  
- Color codes like `&c`, `&6`, `&l`, etc. are supported and encouraged
- Config saves everything automatically  
