import {defineStore} from "pinia";

interface EditorState {
  hasConfigLoaded: boolean
  currentSection: string[]
  configData: any
  savedConfig: boolean
  tomlConfig: string
  embedJson: string
  isSocketConfig: boolean
  isEmbedEditor: boolean
  downloadConfig: {
    original: string
    modified: string
    isSaved: boolean
  }
}

export const useEditor = defineStore('editor-editor', {
  state: (): EditorState => ({
    hasConfigLoaded: false,
    currentSection: [],
    configData: undefined,
    savedConfig: false,
    tomlConfig: "",
    embedJson: "",
    isSocketConfig: false,
    isEmbedEditor: false,
    downloadConfig: {
      original: "",
      modified: "",
      isSaved: false,
    }
  }),

  actions: {
    setConfigLoaded(payload: boolean) {
      this.hasConfigLoaded = payload;
    },
    setCurrentSection(payload: string[]) {
      this.currentSection = payload;
    },
    setConfig(payload: any) {
      this.configData = payload;
    },
    setSocketConfig(payload: boolean) {
      this.isSocketConfig = payload;
    },
    setTomlConfig(payload: any) {
      if (payload == undefined) {
        this.savedConfig = false;
        this.tomlConfig = "";
      } else {
        this.savedConfig = true;
        this.tomlConfig = payload;
      }
    },
    setOriginalConfig(payload: string) {
      this.downloadConfig.original = payload;
    },
    setDownloadConfig(payload: boolean, modified: string) {
      this.downloadConfig.isSaved = payload;
      this.downloadConfig.modified = modified;
    },
    setEmbedEditor(payload: boolean) {
      this.isEmbedEditor = payload;
    }
  },

  getters: {
    isConfigLoaded(state) {
      return state.hasConfigLoaded;
    },
    getCurrentSection(state) {
      return state.currentSection;
    },
    getConfig(state) {
      return state.configData;
    },
    getTomlConfig(state) {
      return state.tomlConfig;
    },
    hasSavedConfig(state) {
      return state.savedConfig;
    },
    checkIsSocketConfig(state) {
      return state.isSocketConfig;
    },
    hasDownloadConfig(state) {
      return state.downloadConfig;
    },
    getEmbedEditor(state) {
      return state.isEmbedEditor
    }
  }
})
