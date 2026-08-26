<template>
  <UModal fullscreen :dismissible="false" title="Embed Editor">
    <UButton icon="i-lucide-pencil" variant="subtle" color="neutral" size="xs" class="ml-2" />

    <template #body>
      <div v-if="useEditor().isConfigLoaded || useEditor().getEmbedEditor">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="ct-card p-4 mt-2 mb-2 rounded-lg relative">
            <div>
              <Collapse collapse-title="Author" class="ct-card p-4 mt-2 mb-2 rounded-lg relative">
                <div>
                  <div class="w-full mt-2">
                    <h2 class="text-sm">Author Name</h2>
                    <UInput placeholder="Author Name" v-model="authorName" variant="subtle" class="w-full" />
                  </div>

                  <div class="grid grid-cols-2 gap-2 mt-2">
                    <div class="w-full">
                      <h2 class="text-sm">Author Icon</h2>
                      <UInput placeholder="Author Icon URL" type="url" v-model="authorIcon" variant="subtle" class="w-full" />
                    </div>

                    <div>
                      <h2 class="text-sm">Author URL</h2>
                      <UInput placeholder="Author URL" type="url" v-model="authorUrl" variant="subtle" class="w-full" />
                    </div>
                  </div>

                </div>
              </Collapse>

              <Collapse collapse-title="Body" class="ct-card p-4 mt-2 mb-2 rounded-lg relative">
                <div class="w-full mt-2">
                  <h2 class="text-sm">Title</h2>
                  <UInput placeholder="Title" v-model="embedTitle" variant="subtle" class="w-full" />
                </div>

                <div class="w-full mt-2">
                  <h2 class="text-sm">Body</h2>
                  <UTextarea :rows="4" variant="subtle" placeholder="The body of the embed" class="w-full" v-model="embedBody" />
                </div>

                <div class="grid grid-cols-2 gap-2 mt-2">
                  <div class="w-full">
                    <h2 class="text-sm">Title URL</h2>
                    <UInput placeholder="Title URL" type="url" v-model="embedUrl" variant="subtle" class="w-full" />
                  </div>

                  <div>
                    <h2 class="text-sm">Color</h2>
                    <UPopover>
                      <UInput placeholder="Color" type="text" v-model="embedColor" variant="subtle" class="w-full">
                        <template #leading>
                          <span :style="chip" class="size-3 rounded-full" />
                        </template>
                      </UInput>

                      <template #content>
                        <UColorPicker v-model="embedColor" class="p-2" />
                      </template>
                    </UPopover>
                  </div>
                </div>

              </Collapse>

              <Collapse collapse-title="Images" class="ct-card p-4 mt-2 mb-2 rounded-lg relative">
                <div class="w-full mt-2">
                  <h2 class="text-sm">Thumbnail</h2>
                  <UInput placeholder="Thumbnail URL" type="url" v-model="embedThumbnail" variant="subtle" class="w-full" />
                </div>

                <div class="w-full mt-2">
                  <h2 class="text-sm">Image</h2>
                  <UInput placeholder="Image URL" type="url" v-model="embedImage" variant="subtle" class="w-full" />
                </div>

              </Collapse>

              <Collapse collapse-title="Footer" class="ct-card p-4 mt-2 mb-2 rounded-lg relative">
                <div>
                  <div class="w-full mt-2">
                    <h2 class="text-sm">Footer Text</h2>
                    <UInput placeholder="Footer Text" v-model="footerText" variant="subtle" class="w-full" />
                  </div>

                  <div class="grid grid-cols-2 gap-2 mt-2 items-center">
                    <div class="w-full">
                      <h2 class="text-sm">Footer Icon</h2>
                      <UInput placeholder="Footer Icon URL" type="url" v-model="footerIconUrl" variant="subtle" class="w-full" />
                    </div>

                    <div>
                      <h2 class="text-sm">Add Timestamp</h2>
                      <USwitch v-model="discordTimestamp" />
                    </div>
                  </div>

                </div>
              </Collapse>

              <Collapse collapse-title="Fields" class="ct-card p-4 mt-2 mb-2 rounded-lg relative">
                <template #button>
                  <UIcon name="i-lucide-plus" class="cursor-pointer" @click="addField()" />
                </template>

                <div v-for="(field, index) in embedFields" class="ct-card p-4 mt-2 mb-2 rounded-lg relative">
                  <div class="flex items-center justify-between border-b border-b-muted py-2">
                    <h1>Field {{ index + 1 }}</h1>
                    <UButton variant="ghost" color="error" icon="i-lucide-trash" size="sm" @click="deleteField(index)" />
                  </div>

                  <div class="flex items-end gap-2">
                    <div class="w-full mt-2">
                      <h2 class="text-sm">Title</h2>
                      <UInput placeholder="Title" v-model="field.name" variant="subtle" class="w-full" />
                    </div>

                    <USwitch v-model="field.inline" label="Inline" />
                  </div>

                  <div class="mt-2">
                    <h2 class="text-sm">Value</h2>
                    <UTextarea :rows="4" variant="subtle" placeholder="The value of the field" class="w-full" v-model="field.text" />
                  </div>
                </div>
              </Collapse>

              <div class="rounded-lg relative py-4">
                <div class="flex items-center justify-between gap-2">
                  <UButton label="Reset" color="error" variant="subtle" icon="i-lucide-undo-2" @click="reloadWindow()" />
                  <UButton label="Download" color="success" variant="subtle" icon="i-lucide-download" @click="generateEmbed()" />
                </div>
              </div>

            </div>
          </div>

          <div class="ct-card p-4 mt-2 mb-2 rounded-lg relative" :class="colorMode == 'light' ? 'discord-light-theme' : ''">
            <DiscordMessage author="Simple Discord Link" avatar="blue" :bot="true" compactMode="false">
              <DiscordEmbed
                slot="embeds"
                :borderColor="embedColor"
                :embedTitle="parsePlaceholders(embedTitle)"
                :url="parsePlaceholders(embedUrl)"
                :thumbnail="parsePlaceholders(embedThumbnail)"
                :image="parsePlaceholders(embedThumbnail)"
                :timestamp="getTimestampValue()"
                :footerIcon="parsePlaceholders(footerIconUrl)"
                :author-name="parsePlaceholders(authorName)"
                :author-icon="parsePlaceholders(authorIcon)"
                :author-url="parsePlaceholders(authorUrl)"
              >
                <div v-if="embedBody" v-html="md.render(parsePlaceholders(embedBody))" />

                <DiscordEmbedFields v-if="embedFields.length > 0">
                  <DiscordEmbedField v-for="field in embedFields" :fieldTitle="parsePlaceholders(field.name)" :inline="field.inline">{{ parsePlaceholders(field.text) }}</DiscordEmbedField>
                </DiscordEmbedFields>

                <template #footer v-if="footerText">{{ parsePlaceholders(footerText) }}</template>
              </DiscordEmbed>
            </DiscordMessage>

            <h2 class="font-bold mt-5">Embed Placeholders</h2>
            <div class="no-more-tailwind" v-html="md.render(placeholders)" />
          </div>
        </div>
      </div>
    </template>

  </UModal>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import { DiscordEmbed, DiscordEmbedField, DiscordEmbedFields, DiscordMessage } from '@discord-message-components/vue'
import markdownit from 'markdown-it'
import {useColorMode, useFileDialog} from '@vueuse/core'
import {BACKEND_URL, saveConfigFile} from "../../composables/EditorFunctions.ts";
import {useEditor} from "../../stores/editor.ts";
import {useToast} from "@nuxt/ui/composables";

const md = markdownit({
  html: true,
  linkify: true,
  typographer: true
})

const toast = useToast()

const placeholders = "1) `%author%` -> The name of the Player/Server. For example: `HypherionSA`\n" +
  "2) `%avatar%` -> The player/server avatar link\n" +
  "3) `%message_contents%` -> The actual contents of the message\n" +
  "4) `%username%` -> The raw username of the player/server. For example, `hypherionsa` or `server`\n" +
  "5) `%player_avatar%` -> Get the Player avatar in server messages (Join/Leave, Death etc)\n" +
  "6) `%player_name%` -> Get the name of the player in server messages (Join/Leave, Death etc)";

// Embed Data
const authorName = ref<string | undefined>("%author%")
const authorIcon = ref<string | undefined>("%avatar%")
const authorUrl = ref<string | undefined>(undefined)

const embedTitle = ref<string | undefined>(undefined)
const embedUrl = ref<string | undefined>(undefined)
const embedColor = ref<string>("#000000")

const embedBody = ref<string | undefined>("%message_contents%")

const embedThumbnail = ref<string | undefined>(undefined)
const embedImage = ref<string | undefined>(undefined)

const footerText = ref<string | undefined>(undefined)
const footerIconUrl = ref<string | undefined>(undefined)
const discordTimestamp = ref(false)

interface embedField {
  name: string,
  text: string,
  inline: boolean
}

const getTimestampValue = () => {
  if (discordTimestamp.value == false) return undefined;

  return new Date().toDateString()
}

const embedFields = reactive<embedField[]>([])

const parsePlaceholders = (data: string | undefined) => {
  if (!data)
    return "";

  data = data.replace("%author%", "HypherionSA");
  data = data.replace("%avatar%", "https://skinatar.firstdark.dev/head/c973a5eb-2012-46ff-9eed-1767b5ccba71")
  data = data.replace("%message_contents%", "Hello Minecraft people! How are you today?")
  data = data.replace("%player_avatar%", "https://skinatar.firstdark.dev/head/c973a5eb-2012-46ff-9eed-1767b5ccba71")
  data = data.replace("%player_name%", "TheRealHypherionSA")
  data = data.replace("%current_time%", "TBD")
  data = data.replace("%username%", "server")

  return data;
}

const addField = async () => {
  embedFields.push({
    name: "Field",
    text: "Body",
    inline: true
  })
}

const deleteField = (index: number) => {
  embedFields.splice(index, 1);
}

const cleanEmbedValues = (value: string | undefined | null) => {
  if (value == null)
    return undefined;

  if (!value)
    return null;

  return value;
}

const generateEmbedFields = () => {
  const fields: { name: string; value: string; inline: boolean }[] = [];

  for (const field of embedFields) {
    if (field.name.trim() && field.text.trim()) {
      fields.push({
        name: field.name,
        value: field.text,
        inline: field.inline
      });
    }
  }

  return fields.length ? fields : null;
};

const generateEmbed = () => {
  const embed = {
    title: cleanEmbedValues(embedTitle.value),
    description: cleanEmbedValues(embedBody.value),
    color: embedColor.value,
    timestamp: 0,
    url: cleanEmbedValues(embedUrl.value),
    author: authorName.value == undefined ? null : {
      name: authorName.value,
      url: cleanEmbedValues(authorUrl.value),
      icon_url: cleanEmbedValues(authorIcon.value)
    },
    thumbnail: embedThumbnail.value == undefined ? null : {
      url: embedThumbnail.value
    },
    image: embedImage.value == undefined ? null : {
      url: embedImage.value
    },
    footer: (footerText.value == undefined || footerIconUrl.value == undefined) ? null : {
      text: cleanEmbedValues(footerText.value),
      icon_url: cleanEmbedValues(footerIconUrl.value)
    },
    fields: embedFields.length == 0 ? null : generateEmbedFields()
  };

  useEditor().embedJson = JSON.stringify(embed, null, 2);
  saveConfigFile(false, true)
}

onMounted(() => {
  useEditor().setEmbedEditor(true)
})

const { open, onChange } = useFileDialog({
  accept: '.json'
});

onChange((files) => {
  if (files?.item(0)) {
    const formData = new FormData();
    formData.append('file', files[0]);

    fetch(`${BACKEND_URL}/v1/parseembed`, {
      method: 'POST',
      body: formData
    }).then(async res => {
      const dt = await res.json();

      if (!dt.error) {
        const data = JSON.parse(dt.data)

        authorName.value = data.author == null ? undefined : (data.author.name || undefined)
        authorUrl.value = data.author == null ? undefined : (data.author.url || undefined)
        authorIcon.value = data.author == null ? undefined : (data.author.icon_url || undefined)

        embedTitle.value = data.title == null ? undefined : data.title
        embedUrl.value = data.url == null ? undefined : data.url
        embedBody.value = data.description == null ? undefined : data.description
        embedColor.value = data.color || "#000000"

        embedThumbnail.value = data.thumbnail == null ? undefined : (data.thumbnail.url || undefined)
        embedImage.value = data.image == null ? undefined : (data.image.url || undefined)

        footerText.value = data.footer == null ? undefined : (data.footer.text || undefined)
        footerIconUrl.value = data.footer == null ? undefined : (data.footer.icon_url || undefined)

        if (data.fields) {
          for (const ff of data.fields) {
            const field = ff as any;

            embedFields.push({
              name: field.name,
              text: field.value,
              inline: field.inline || false
            })
          }
        }

        // TODO Timestamp

        toast.add({
          title: "Success",
          description: "Embed has been loaded",
          color: "success",
          duration: 2000,
        })
      } else {
        toast.add({
          title: "Error",
          description: dt.message,
          color: "error",
          duration: 2000,
        })
      }
    }).catch(err => {
      toast.add({
        title: "Error",
        description: err,
        color: "error",
        duration: 2000,
      })
    })
  }
})

const reloadWindow = () => {
  window.location.reload()
}

const chip = computed(() => ({ backgroundColor: embedColor.value }))
const colorMode = useColorMode()
</script>

<style lang="css">
code {
  background: rgb(53 53 53) !important;
  padding: 0.1rem 0.5rem;
  color: white !important;
  border-radius: 5px;
}
</style>
