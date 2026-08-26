<template>
  <UPageCard :orientation="isEmptyOrNull(props.identifier) ? 'vertical' : 'horizontal'" variant="soft" :ui="{ wrapper: isEmptyOrNull(props.identifier) ? 'hidden' : 'block' }">
    <template #title>
      <h1 class="text-base text-pretty font-semibold text-highlighted">{{ headerToDisplay(props.identifier) }}</h1>
    </template>

    <template #description>
      <p class="text-xs text-muted">{{ useEditor().getConfig.comments[props.commentkey] }}</p>
    </template>

    <div class="w-full flex items-center" v-if="!(hasProp('commands') || hasProp('searchMode') || hasProp('minecraftCommand') || hasProp('botStatusType') || hasProp('rank'))">
      <!-- ============= Text Input ============ -->
      <UInput :disabled="(props.identifier === 'configVersion')"
        v-if="isStringField"
        v-model="field"
        variant="subtle"
        size="lg"
        class="w-full"
      />

<!--      <EmbedEditorModal v-if="props.identifier == 'embedLayout'" />-->

      <!-- ============ String Arrays ============= -->
      <UInputTags
        v-if="isStringArray(identifier)"
        v-model="field"
        variant="subtle"
        size="lg"
        class="w-full"
        placeholder="Press enter to save"
      />

      <!-- ============ Boolean ============= -->
      <USwitch
        v-if="typeof value == 'boolean'"
        size="lg"
        color="info"
        v-model="field"
      />

      <!-- ========== Number ============= -->
      <UInputNumber
        :disabled="props.identifier === 'configVersion' || props.identifier === 'version'"
        v-if="typeof value === 'number'"
        v-model="field"
        variant="subtle"
        class="w-full"
        size="lg"
      />

      <!-- =============== Select Field ================ -->
      <USelect
        v-if="selectField"
        variant="subtle"
        size="lg"
        v-model="field"
        class="w-full"
        :items="selectField"
      />
    </div>

    <!-- =============== Simple Discord Link Linked Commands ================ -->
    <SDLCommand
      v-if="hasProp('commands')"
      v-model="field"
      @delete="deleteArrayEntry(target, identifier)"
    />

    <!-- =============== Simple Discord Link Rank ================ -->
    <SDLRank
      v-if="hasProp('rank')"
      v-model="field"
      @delete="deleteArrayEntry(target, identifier)"
    />

    <!-- =============== Simple Discord Link Trigger Commands ================ -->
    <SDLLinkedCommand
      v-if="hasProp('minecraftCommand')"
      v-model="field"
      @delete="deleteArrayEntry(target, identifier)"
    />

    <!-- =============== Simple Discord Link Bot Status ================ -->
    <SDLBotStatus
      v-if="hasProp('botStatusType')"
      v-model="field"
      @delete="deleteArrayEntry(target, identifier)"
      :items="BOT_STATUS_TYPE"
    />

    <!-- ========== Simple Discord Link Message Filtering ========== -->
    <MessageFilterField
      v-if="hasProp('searchMode')"
      v-model="field"
      :applies-items="APPLIES_TO"
      :search-items="SEARCH_MODE_OPTIONS"
      :filter-items="FILTER_TARGET_MODE"
      :replace-items="REPLACE_MODE"
      @delete="deleteArrayEntry(target, identifier)"
    />

  </UPageCard>
</template>

<script setup lang="ts">
import {useEditor} from "../../stores/editor.ts";
import {headerToDisplay, isEmptyOrNull, isStringArray} from "../../composables/FieldUtils.ts";
import {computed} from "vue";
import SDLCommand from "./controls/SDLCommand.vue";
import SDLRank from "./controls/SDLRank.vue";
import SDLLinkedCommand from "./controls/SDLLinkedCommand.vue";
import SDLBotStatus from "./controls/SDLBotStatus.vue";
import MessageFilterField from "./controls/MessageFilterField.vue";
import EmbedEditorModal from "./EmbedEditorModal.vue";

const props = defineProps([
  'identifier',
  'value',
  'target',
  'commentkey'
]);

const TRI_BOOLEAN = [
  { value: 'ALWAYS', label: 'Always' },
  { value: 'NEVER', label: 'Never' },
  { value: 'GAMERULE', label: 'Game Rule' }
]

const BOT_ONLINE_STATUS = [
  { value: 'ONLINE', label: 'Online' },
  { value: 'IDLE', label: 'Idle' },
  { value: 'DO_NOT_DISTURB', label: 'Do Not Disturb' },
  { value: 'OFFLINE', label: 'Offline' }
]

const BOT_STATUS_TYPE = [
  { value: 'PLAYING', label: 'Playing' },
  { value: 'STREAMING', label: 'Streaming' },
  { value: 'WATCHING', label: 'Watching' },
  { value: 'LISTENING', label: 'Listening' },
  { value: 'CUSTOM_STATUS', label: 'Custom' }
]

const AVATAR_TYPE = [
  { value: 'AVATAR', label: 'Avatar' },
  { value: 'HEAD', label: 'Player Head' },
  { value: 'BODY', label: 'Body' },
  { value: 'COMBO', label: 'Combo' },
  { value: 'CUSTOM', label: 'Custom'}
]

const FILTER_TARGET_MODE = [
  { value: 'USERNAME', label: 'Username' },
  { value: 'CHAT', label: 'Chat' },
  { value: 'CONSOLE', label: 'Console' },
  { value: 'BOTH', label: 'Both' }
]

const APPLIES_TO = [
  { value: 'DISCORD', label: 'Discord' },
  { value: 'MINECRAFT', label: 'Minecraft' },
]

const REPLACE_MODE = [
  { value: 'REPLACE', label: 'Replace' },
  { value: 'IGNORE', label: 'Ignore Message' }
]

const SEARCH_MODE_OPTIONS = computed(() => {
  return [
    { value: 'CONTAINS', label: 'Contains' },
    { value: 'STARTS_WITH', label: 'Starts With' },
    { value: 'MATCHES', label: 'Exact Match' },
    { value: 'REGEX', label: 'Regex' }
  ]
})

const selectField = computed(() => {
  const joinedKey = `${useEditor().currentSection.join(".")}.${props.identifier}`

  if (joinedKey == 'channels.advancements.enabled' || joinedKey == 'channels.death.enabled') {
    return TRI_BOOLEAN
  }

  switch (props.identifier) {
    case 'botStatusType':
      return BOT_STATUS_TYPE

    case 'advancementMessages':
    case 'deathMessages':
      return useEditor().getConfig.config.general.configVersion > 26 ? TRI_BOOLEAN : null

    case 'playerAvatarType':
      return AVATAR_TYPE

    case 'maintenanceOnlineStatus':
      return BOT_ONLINE_STATUS

    default:
      return null
  }
})

const isStringField = computed(() => {
  return typeof props.value === 'string' && !selectField.value
})

const isObjectField = computed(() => {
  return typeof field.value === 'object' && field.value !== null
})

const hasProp = (prop: string) => {
  return isObjectField.value && Object.prototype.hasOwnProperty.call(field.value, prop)
}

const field = computed({
  get: () => props.target?.[props.identifier],
  set: (value) => {
    props.target[props.identifier] = value
  }
})

const deleteArrayEntry = (target: any, index: any) => {
    target.splice(index, 1);
}
</script>
