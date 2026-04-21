<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue";
import {api, redirectToLoginByFeature, SUPPORT_CHAT_OPEN_EVENT} from "@/api";
import {useSession} from "@/composables/useSession";
import {useChatSocket} from "@/composables/useChatSocket";
import ProductModal from "@/components/chat/ProductModal.vue";
import ChatCameraCapture from "@/components/chat/ChatCameraCapture.vue";

const {state: session, isAuthenticated, isAdmin, refreshSession} = useSession();
const {connect, subscribe, send, status} = useChatSocket();

const isOpen = ref(false);
const isMinimized = ref(false);
const productModalOpen = ref(false);
const cameraOpen = ref(false);

const activeProduct = ref({productId: null, productName: "", thumbnailUrl: ""});
const messages = ref([]);
const loadingHistory = ref(false);
const historyError = ref("");

const draft = ref("");
const uploading = ref(false);
const composerError = ref("");
const scrollRef = ref(null);
const fileInputRef = ref(null);

const canUseChat = computed(() => isAuthenticated.value && !isAdmin.value);
const hideWidgetForAdmin = computed(() => session.loaded && isAdmin.value);
const hasProduct = computed(() => Number(activeProduct.value.productId || 0) > 0);
const wsConnected = computed(() => String(status.value || "").toUpperCase() === "CONNECTED");
const headerTitle = computed(() => {
    if (!hasProduct.value) return "Chat hỗ trợ";
    return activeProduct.value.productName ? activeProduct.value.productName : `Sản phẩm #${activeProduct.value.productId}`;
});

const scrollToBottom = async () => {
    await nextTick();
    const el = scrollRef.value;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
};

const open = async () => {
    if (!isAuthenticated.value) {
        await redirectToLoginByFeature("Chat hỗ trợ");
        return;
    }
    if (isAdmin.value) {
        return;
    }
    isOpen.value = true;
    isMinimized.value = false;
    connect();
    await scrollToBottom();
};

const openWithProduct = async (payload) => {
    await open();
    await setProduct(payload);
    await loadHistory();
};

const toggleMinimize = () => {
    isMinimized.value = !isMinimized.value;
};
const close = () => {
    isOpen.value = false;
    isMinimized.value = false;
};

const setProduct = async (payload) => {
    activeProduct.value = {
        productId: payload?.productId || null,
        productName: payload?.productName || "",
        thumbnailUrl: payload?.thumbnailUrl || ""
    };
};

const loadHistory = async () => {
    if (!canUseChat.value || !hasProduct.value) {
        messages.value = [];
        return;
    }
    loadingHistory.value = true;
    historyError.value = "";
    try {
        const res = await api.chat.messages(activeProduct.value.productId);
        const list = Array.isArray(res.data) ? res.data : [];
        messages.value = list;
        await scrollToBottom();
    } catch (e) {
        historyError.value = e?.message || "Không thể tải lịch sử chat.";
    } finally {
        loadingHistory.value = false;
    }
};

const onIncoming = async (payload) => {
    const pid = Number(payload?.productId || 0);
    if (!pid || pid !== Number(activeProduct.value.productId || 0)) {
        return;
    }
    const incomingId = Number(payload?.id || 0);
    if (incomingId > 0 && messages.value.some((item) => Number(item?.id || 0) === incomingId)) {
        return;
    }
    if (!incomingId) {
        const same = messages.value.some((item) =>
            String(item?.senderRole || "") === String(payload?.senderRole || "")
            && String(item?.content || "") === String(payload?.content || "")
            && String(item?.mediaUrl || "") === String(payload?.mediaUrl || "")
            && String(item?.createdAt || "") === String(payload?.createdAt || "")
            && Number(item?.productId || 0) === Number(payload?.productId || 0)
            && String(item?.customerId || "") === String(payload?.customerId || "")
        );
        if (same) {
            return;
        }
    }
    messages.value = [...messages.value, payload];
    await scrollToBottom();
};

const ensureSubscriptions = () => {
    subscribe("/user/queue/messages", onIncoming);
};

const tryConnect = () => {
    if (!canUseChat.value) {
        return;
    }
    connect();
    const timer = setInterval(() => {
        if (status.value === "CONNECTED") {
            clearInterval(timer);
            ensureSubscriptions();
        }
    }, 250);
    setTimeout(() => clearInterval(timer), 8000);
};

const sendText = async () => {
    composerError.value = "";
    if (!hasProduct.value) {
        productModalOpen.value = true;
        return;
    }
    const text = String(draft.value || "").trim();
    if (!text) {
        composerError.value = "Vui lòng nhập nội dung.";
        return;
    }
    const ok = send("/app/chat.send", {productId: activeProduct.value.productId, content: text});
    if (!ok) {
        composerError.value = "Chưa kết nối được chat. Vui lòng thử lại.";
        return;
    }
    draft.value = "";
};

const chooseFile = () => {
    composerError.value = "";
    if (!hasProduct.value) {
        productModalOpen.value = true;
        return;
    }
    fileInputRef.value?.click?.();
};

const uploadAndSend = async (file) => {
    if (!file || !hasProduct.value) {
        return;
    }
    uploading.value = true;
    composerError.value = "";
    try {
        const res = await api.chat.upload(file);
        const mediaUrl = res?.data?.mediaUrl;
        if (!mediaUrl) {
            throw new Error("Upload thất bại.");
        }
        const ok = send("/app/chat.send", {productId: activeProduct.value.productId, mediaUrl});
        if (!ok) {
            throw new Error("Chưa kết nối được chat.");
        }
    } catch (e) {
        composerError.value = e?.message || "Không thể gửi ảnh.";
    } finally {
        uploading.value = false;
        if (fileInputRef.value) {
            fileInputRef.value.value = "";
        }
    }
};

const onFileChange = async (event) => {
    const file = event?.target?.files?.[0] || null;
    await uploadAndSend(file);
};

const openCamera = () => {
    composerError.value = "";
    if (!hasProduct.value) {
        productModalOpen.value = true;
        return;
    }
    cameraOpen.value = true;
};

const onCapture = async (blob) => {
    const file = new File([blob], `camera-${Date.now()}.jpg`, {type: blob.type || "image/jpeg"});
    await uploadAndSend(file);
};

const onComposerKeydown = async (event) => {
    if (event?.isComposing || event?.keyCode === 229) {
        return;
    }
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        await sendText();
    }
};

onMounted(async () => {
    await refreshSession();
    if (canUseChat.value) {
        tryConnect();
    }
    if (typeof window !== "undefined") {
        window.addEventListener(SUPPORT_CHAT_OPEN_EVENT, onExternalOpenEvent);
    }
});

const onExternalOpenEvent = async (event) => {
    const payload = event?.detail || {};
    // Wait for session to be loaded with retry
    let attempts = 0;
    while (!session.loaded && attempts < 20) {
        await new Promise(r => setTimeout(r, 100));
        attempts++;
    }
    const productId = Number(payload?.productId || 0);
    if (!productId) {
        await open();
        return;
    }
    await openWithProduct({
        productId,
        productName: payload?.productName || "",
        thumbnailUrl: payload?.thumbnailUrl || ""
    });
};

onBeforeUnmount(() => {
    if (typeof window !== "undefined") {
        window.removeEventListener(SUPPORT_CHAT_OPEN_EVENT, onExternalOpenEvent);
    }
});

watch(() => canUseChat.value, (value) => {
    if (value) {
        tryConnect();
    }
});

watch(() => activeProduct.value.productId, async () => {
    await loadHistory();
});

watch(() => isOpen.value, async (value) => {
    if (value) {
        await loadHistory();
    }
});
</script>

<template>
    <template v-if="!hideWidgetForAdmin">
        <ProductModal
            :open="productModalOpen"
            @close="productModalOpen = false"
            @change-product="setProduct"
        />
        <ChatCameraCapture :open="cameraOpen" @close="cameraOpen = false" @capture="onCapture" />

        <button v-if="!isOpen && session.loaded && !isAdmin" class="cb-fab" type="button" @click="open">
            Chat hỗ trợ
        </button>

        <section v-else class="cb" :class="{min: isMinimized}">
            <header class="cb-header">
                <div class="cb-head-left">
                    <img v-if="activeProduct.thumbnailUrl" class="cb-thumb" :src="activeProduct.thumbnailUrl" :alt="headerTitle" />
                    <div class="cb-head-text">
                        <div class="cb-title">{{ headerTitle }}</div>
                        <div class="cb-sub">
                            <span v-if="hasProduct">Mã: {{ activeProduct.productId }}</span>
                            <span v-else>Chọn sản phẩm để bắt đầu</span>
                            <span class="cb-dot">•</span>
                            <span class="cb-ws-indicator" :title="wsConnected ? 'Đã kết nối' : 'Chưa kết nối'">
                                <span class="cb-ws-light" :class="{on: wsConnected, off: !wsConnected}"></span>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="cb-actions">
                    <button class="cb-action" type="button" @click="productModalOpen = true">Thay đổi</button>
                    <button class="cb-action" type="button" @click="toggleMinimize">{{ isMinimized ? "Mở" : "-" }}</button>
                    <button class="cb-action" type="button" @click="close">✕</button>
                </div>
            </header>

            <div v-if="!isMinimized" class="cb-body">
                <div class="cb-history" ref="scrollRef">
                    <div v-if="loadingHistory" class="cb-status">Đang tải lịch sử…</div>
                    <div v-else-if="historyError" class="cb-status cb-error">{{ historyError }}</div>
                    <div v-else-if="!hasProduct" class="cb-status">Hãy chọn sản phẩm để chat.</div>
                    <template v-else>
                        <div v-for="m in messages" :key="m.id || (m.createdAt + '_' + m.content + '_' + m.mediaUrl)" class="cb-msg" :class="{me: m.senderRole === 'USER'}">
                            <div class="cb-bubble">
                                <div v-if="m.content" class="cb-text">{{ m.content }}</div>
                                <img v-if="m.mediaUrl" class="cb-img" :src="m.mediaUrl" alt="chat" />
                            </div>
                            <div class="cb-time">{{ new Date(m.createdAt || Date.now()).toLocaleString() }}</div>
                        </div>
                    </template>
                </div>

                <div class="cb-composer">
                    <div v-if="composerError" class="cb-composer-error">{{ composerError }}</div>
                    <textarea
                        v-model="draft"
                        class="cb-input"
                        :disabled="uploading"
                        rows="2"
                        placeholder="Nhập tin nhắn…"
                        @keydown="onComposerKeydown"
                    />
                    <div class="cb-tools">
                        <input ref="fileInputRef" class="cb-file" type="file" accept="image/*" @change="onFileChange" />
                        <button class="cb-tool" type="button" @click="chooseFile" :disabled="uploading">Ảnh</button>
                        <button class="cb-tool" type="button" @click="openCamera" :disabled="uploading">Camera</button>
                        <button class="cb-send" type="button" @click="sendText" :disabled="uploading">Gửi</button>
                    </div>
                    <div v-if="uploading" class="cb-uploading">Đang tải ảnh…</div>
                </div>
            </div>
        </section>
    </template>
</template>

<style scoped>
.cb-fab{position:fixed;right:16px;bottom:16px;z-index:9997;border:1px solid #2563eb;background:#2563eb;color:#fff;border-radius:999px;padding:10px 14px;font-size:14px;font-weight:600;cursor:pointer;box-shadow:0 14px 40px rgba(37,99,235,.35)}
.cb-fab:hover{filter:brightness(1.05)}
.cb{position:fixed;right:16px;bottom:16px;width:420px;height:500px;z-index:9997;background:#fff;border:1px solid #e5e7eb;border-radius:14px;overflow:hidden;display:flex;flex-direction:column;box-shadow:0 20px 60px rgba(0,0,0,.18)}
.cb.min{height:62px}
.cb-header{height:62px;display:flex;align-items:center;justify-content:space-between;gap:10px;padding:10px 10px 10px 12px;background:#0b1220;color:#fff}
.cb-head-left{display:flex;align-items:center;gap:10px;min-width:0}
.cb-thumb{width:34px;height:34px;border-radius:8px;object-fit:cover;background:#111827}
.cb-head-text{min-width:0}
.cb-title{font-size:14px;font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:220px}
.cb-sub{font-size:12px;color:rgba(255,255,255,.75);display:flex;align-items:center;gap:6px;flex-wrap:nowrap;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.cb-sub span{white-space:nowrap}
.cb-dot{opacity:.7}
.cb-ws-indicator{display:inline-flex;align-items:center;justify-content:center}
.cb-ws-light{width:10px;height:10px;border-radius:999px;border:1px solid rgba(255,255,255,.45);box-shadow:0 0 0 2px rgba(255,255,255,.08) inset}
.cb-ws-light.on{background:#22c55e}
.cb-ws-light.off{background:#ef4444}
.cb-actions{display:flex;align-items:center;gap:6px;flex:0 0 auto}
.cb-action{border:1px solid rgba(255,255,255,.2);background:rgba(255,255,255,.08);color:#fff;border-radius:10px;padding:6px 9px;font-size:12px;cursor:pointer;white-space:nowrap;flex:0 0 auto;line-height:1}
.cb-action:hover{background:rgba(255,255,255,.12)}
.cb-body{display:flex;flex-direction:column;min-height:0;flex:1}
.cb-history{flex:1;min-height:0;overflow:auto;padding:10px;background:#f8fafc;display:flex;flex-direction:column;gap:10px}
.cb-status{padding:10px;border:1px dashed #e5e7eb;border-radius:10px;background:#fff;color:#374151;font-size:13px}
.cb-error{border-color:#fecaca;background:#fff1f2;color:#991b1b}
.cb-msg{display:flex;flex-direction:column;align-items:flex-start;gap:3px}
.cb-msg.me{align-items:flex-end}
.cb-bubble{max-width:78%;background:#dbeafe;border:1px solid #bfdbfe;border-radius:12px;padding:8px 10px;color:#111827}
.cb-msg.me .cb-bubble{background:#f3f4f6;border-color:#e5e7eb}
.cb-text{white-space:pre-wrap;word-break:break-word;font-size:13px}
.cb-img{margin-top:6px;max-width:100%;border-radius:10px;border:1px solid #e5e7eb;background:#fff}
.cb-time{font-size:11px;color:#6b7280}
.cb-composer{border-top:1px solid #e5e7eb;background:#fff;padding:10px}
.cb-composer-error{margin-bottom:8px;padding:8px;border-radius:10px;border:1px solid #fecaca;background:#fff1f2;color:#991b1b;font-size:12px}
.cb-input{width:100%;resize:none;border:1px solid #e5e7eb;border-radius:10px;padding:8px 10px;font-size:13px;outline:none}
.cb-input:focus{border-color:#93c5fd;box-shadow:0 0 0 3px rgba(59,130,246,.15)}
.cb-tools{margin-top:8px;display:flex;gap:8px;justify-content:flex-end}
.cb-file{display:none}
.cb-tool{border:1px solid #e5e7eb;background:#fff;border-radius:10px;padding:7px 10px;font-size:12px;cursor:pointer}
.cb-tool:hover{background:#f9fafb}
.cb-send{border:1px solid #2563eb;background:#2563eb;color:#fff;border-radius:10px;padding:7px 12px;font-size:12px;font-weight:700;cursor:pointer}
.cb-send:hover{filter:brightness(1.05)}
.cb-uploading{margin-top:6px;font-size:12px;color:#6b7280}
</style>
