import {useEditor} from "../stores/editor.ts";
import {useWebsocketClient} from "./websocketClient.ts";

export const BACKEND_URL = import.meta.env.MODE === 'production' ? '' : "http://localhost:3000";

export const saveConfigFile = (download: boolean = false, isEmbed = false) => {
 if (isEmbed) {
   useEditor().setTomlConfig("");
 } else {
   const data = useEditor().getConfig
   const websocket = useWebsocketClient()
   websocket.sendMessage({
     socketCode: 'WS_SAVE_CONFIG',
     message: JSON.stringify(data),
   })
 }
}

export const downloadFile = (fileContent: any, isEmbed: boolean = false) => {
  const blob = new Blob([fileContent], { type: 'application/toml' });
  const link = document.createElement('a');

  link.href = URL.createObjectURL(blob);
  // @ts-ignore
  link.download = isEmbed ? "embed.json" : useEditor().getConfig.filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
