import { ref, readonly } from 'vue';
import {BACKEND_URL} from "./EditorFunctions.ts";
import {useAppState} from "../stores/appstate.ts";
import { useEditor } from "../stores/editor.ts";

export function useWebSocket() {
  const ws = ref<WebSocket | null>(null);
  const isConnected = ref(false);
  const identifier = ref<string | null>(null);
  let pingInterval: number | null = null;
  const isWaiting = ref(false);
  const toast = useToast();

  const connect = (id: string) => {
    identifier.value = id;
    ws.value = new WebSocket(`${BACKEND_URL}/ws/config?identifier=${id}`);

    ws.value.onopen = () => {
      isConnected.value = true;
      isWaiting.value = true;

      ws.value?.send(JSON.stringify({
        socketCode: "WS_CHECK_SESSION",
        identifier: id
      }));

      pingInterval = window.setInterval(() => {
        if (ws.value?.readyState === WebSocket.OPEN) {
          ws.value.send(JSON.stringify({ socketCode: "PING" }));
        }
      }, 20000);
    };

    ws.value.onmessage = (event) => {
      const data = JSON.parse(event.data);
      handleMessage(data);
    };

    ws.value.onclose = (ev: CloseEvent) => {
      isConnected.value = false;
      if (pingInterval) {
        clearInterval(pingInterval);
        pingInterval = null;
      }
      handleClose(ev);
    };
  };

  const disconnect = () => {
    if (pingInterval) {
      clearInterval(pingInterval);
      pingInterval = null;
    }
    if (ws.value) {
      ws.value.close();
      ws.value = null;
    }
    isConnected.value = false;
    identifier.value = null;
  };

  const sendMessage = (data: any) => {
    if (ws.value && isConnected.value && ws.value.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify(data));
    }
  };

  const handleMessage = (data: any) => {
    if (data.socketCode === 'WS_VALID_SESSION') {
      toast.add({
        title: "Success",
        description: "Session opened. Waiting for data",
        color: 'success',
        duration: 2000
      })

      ws.value?.send(JSON.stringify({
        socketCode: "WS_GET_CONFIG",
        identifier: identifier.value,
      }));
    }

    if (data.socketCode === 'WS_INVALID_SESSION') {
      toast.add({
        title: 'Error',
        description: "Invalid Session. Please try again",
        duration: 2000,
        color: "error"
      })
    }

    if (data.socketCode === 'WS_NOTIFY') {
      toast.add({
        title: 'Information',
        description: data.message,
        duration: 2000,
        color: "info"
      })
    }

    if (data.socketCode === 'WS_ERROR') {
      toast.add({
        title: 'Error',
        description: data.message,
        duration: 2000,
        color: "error"
      })
    }

    if (data.socketCode === 'WS_CONFIG_ERROR') {
      toast.add({
        title: "Websocket Error",
        description: data.message,
        color: "error",
        duration: 2000
      })
    }

    if (data.socketCode === 'WS_SEND_CONFIG') {
      isWaiting.value = false;
      const dt = JSON.parse(data.message);
      useAppState().setSplashScreen(false);
      useEditor().setConfig(dt);
      useEditor().setSocketConfig(true);
      useEditor().setConfigLoaded(true);
      const firstKey = Object.keys(dt.config)[0]
      useEditor().setCurrentSection([firstKey])

      toast.add({
        title: 'Success',
        color: 'success',
        duration: 2000
      })
    }
  };

  const handleClose = (ev: CloseEvent) => {
    useEditor().setConfigLoaded(false);
    toast.add({
      title: "Session Closed",
      description: ev.reason || "Session Terminated",
      color: "error",
      duration: 2000
    })

    console.log(ev)

    setTimeout(() => {
      //window.location.href = "/";
    }, 2000);
  };

  return {
    ws: readonly(ws),
    isConnected: readonly(isConnected),
    identifier: readonly(identifier),
    connect,
    disconnect,
    sendMessage,
    isWaiting
  };
}

let wsInstance: ReturnType<typeof useWebSocket> | null = null;

export const useWebsocketClient = () => {
  if (!wsInstance) {
    wsInstance = useWebSocket();
  }
  return wsInstance;
};
