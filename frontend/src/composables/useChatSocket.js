import {computed, onBeforeUnmount, ref} from "vue";
import {Client} from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";

export const useChatSocket = () => {
    const clientRef = ref(null);
    const connected = ref(false);
    const connecting = ref(false);
    const lastError = ref("");
    const subscriptionSpecs = [];
    const activeSubscriptions = new Map();
    const handlerIds = new WeakMap();
    let handlerSeq = 0;

    const status = computed(() => {
        if (connected.value) return "CONNECTED";
        if (connecting.value) return "CONNECTING";
        if (lastError.value) return "ERROR";
        return "DISCONNECTED";
    });

    const keyOf = (destination, handler) => {
        if (!handlerIds.has(handler)) {
            handlerSeq += 1;
            handlerIds.set(handler, handlerSeq);
        }
        return `${destination}::${handlerIds.get(handler)}`;
    };

    const bindSubscription = (client, destination, handler) => {
        const key = keyOf(destination, handler);
        if (activeSubscriptions.has(key)) {
            return activeSubscriptions.get(key);
        }
        const sub = client.subscribe(destination, (frame) => {
            try {
                const payload = JSON.parse(frame.body || "{}");
                handler?.(payload);
            } catch (e) {
            }
        });
        activeSubscriptions.set(key, sub);
        return sub;
    };

    const connect = () => {
        if (clientRef.value || connecting.value || connected.value) {
            return;
        }
        connecting.value = true;
        lastError.value = "";
        const client = new Client({
            webSocketFactory: () => new SockJS("/ws"),
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000
        });
        client.onConnect = () => {
            connected.value = true;
            connecting.value = false;
            subscriptionSpecs.forEach((spec) => bindSubscription(client, spec.destination, spec.handler));
        };
        client.onStompError = (frame) => {
            lastError.value = frame?.headers?.message || "STOMP error";
        };
        client.onWebSocketClose = () => {
            connected.value = false;
            connecting.value = false;
            activeSubscriptions.clear();
        };
        client.onWebSocketError = () => {
            lastError.value = "WebSocket error";
        };
        clientRef.value = client;
        client.activate();
    };

    const disconnect = async () => {
        if (!clientRef.value) {
            return;
        }
        try {
            Array.from(activeSubscriptions.values()).forEach((sub) => {
                try {
                    sub?.unsubscribe?.();
                } catch (e) {
                }
            });
            activeSubscriptions.clear();
            subscriptionSpecs.splice(0, subscriptionSpecs.length);
            await clientRef.value.deactivate();
        } finally {
            clientRef.value = null;
            connected.value = false;
            connecting.value = false;
        }
    };

    const subscribe = (destination, handler) => {
        if (!destination || typeof handler !== "function") {
            return null;
        }
        const key = keyOf(destination, handler);
        if (!subscriptionSpecs.some((spec) => keyOf(spec.destination, spec.handler) === key)) {
            subscriptionSpecs.push({destination, handler});
        }
        const client = clientRef.value;
        if (!client || !connected.value || !client.connected) {
            return null;
        }
        return bindSubscription(client, destination, handler);
    };

    const send = (destination, body) => {
        const client = clientRef.value;
        if (!client || !connected.value) {
            return false;
        }
        client.publish({destination, body: JSON.stringify(body || {})});
        return true;
    };

    onBeforeUnmount(() => {
        disconnect();
    });

    return {connect, disconnect, subscribe, send, status, connected, connecting, lastError};
};
