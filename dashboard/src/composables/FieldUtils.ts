export const addToArray = (target: any, key: any) => {
    if (key === 'permissions') {
        const mcCmd = {
            role: "0",
            commands: [],
            permissionLevel: 1
        }

        target.push(mcCmd);
    }

    if (key === 'requiredRoles' || key === 'deniedRoles' || key === 'ignoredCommands' || key === 'ignoredThreads'
      || key === 'allowedChannels' || key === 'verifiedRole') {
        target.push('')
    }

    if (key === 'entries') {
        const filterEntry = {
            search: "",
            target: "CHAT",
            replace: "",
            searchMode: "CONTAINS",
            action: "IGNORE",
            appliesTo: "DISCORD",
            ignoreConsole: false
        }

        target.push(filterEntry);
    }

    if (key === 'roleAdded' || key === 'roleRemoved') {
        const roleAdded = {
            discordRole: '',
            minecraftCommand: []
        }

        target.push(roleAdded);
    }

    if (key === 'botStatus') {
        const botStatus = {
            status: '',
            botStatusType: 'CUSTOM_STATUS',
            botStatusStreamingURL: 'https://twitch.tv/twitch'
        }

        target.push(botStatus);
    }

    if (key === 'syncs') {
        const sync = {
            rank: '',
            role: ''
        }

        target.push(sync);
    }
}

export const headerToDisplay = (identifier: string) => {
    if (!identifier)
        return '';

    if (typeof identifier !== 'string')
      return '';

    const tempIdent = headerManual(identifier);

    if (tempIdent != identifier)
      return tempIdent;

    const words = identifier.match(/([A-Z]+(?=[A-Z][a-z])|[A-Z]?[a-z]+)/g) || [];
    // @ts-ignore
    const capitalizedWords = words.map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase());
    return capitalizedWords.join(' ');
}

const headerManual = (identifier: string) => {
  switch (identifier) {
    case 'ftbranks':
      return 'FTB Ranks';

    case 'ftbessentials':
      return 'FTB Essentials';

    case 'cobblemonguilds':
      return 'Cobblemon Guilds';

    case 'ftbteams_chat':
      return 'FTB Teams Chat';

    case 'mcPrefix':
      return 'Minecraft Prefix';

    case 'mcReplyFormatting':
      return "Minecraft Reply Formatting"

    case 'playerroles':
      return 'Player Roles';

    case 'luckperms':
      return 'LuckPerms';

    default:
      return identifier;
  }
}

export const isStringArray = (value: unknown) => {
  const stringArrays = [
    'ignoredThreads',
    'ignoredCommands',
    'requiredRoles',
    'deniedRoles',
    'verifiedRole',
    'allowedChannels',
    "channels",
    "default_chat",
    "default_event",
    "default_console"
  ]

  return value != null && stringArrays.includes(value as string);
}

export const isEmptyOrNull = (value: unknown) => {
  return value == null || value === '' || typeof value === 'number';
}
