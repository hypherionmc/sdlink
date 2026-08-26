import {defineStore} from "pinia";

interface AppState {
  splashScreen: boolean
  identifier: string
}

export const useAppState = defineStore('editor-state', {
  state: (): AppState => ({
    splashScreen: true,
    identifier: '',
  }),

  actions: {
    setSplashScreen(payload: boolean) {
      this.splashScreen = payload;
    },
    setIdentifier(payload: string) {
      this.identifier = payload;
    },
  },

  getters: {
    getSplashScreen(state) {
      return state.splashScreen;
    },
    getIdentifier(state) {
      return state.identifier;
    }
  }
})
