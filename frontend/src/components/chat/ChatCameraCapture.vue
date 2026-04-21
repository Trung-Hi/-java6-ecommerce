<script setup>
import {onBeforeUnmount, onMounted, ref} from "vue";

const props = defineProps({
    open: {type: Boolean, default: false}
});
const emit = defineEmits(["close", "capture"]);

const videoRef = ref(null);
const streamRef = ref(null);
const error = ref("");

const stop = () => {
    try {
        const stream = streamRef.value;
        if (stream) {
            stream.getTracks().forEach((t) => t.stop());
        }
    } catch (e) {
    }
    streamRef.value = null;
};

const start = async () => {
    error.value = "";
    stop();
    if (!props.open) {
        return;
    }
    try {
        const stream = await navigator.mediaDevices.getUserMedia({video: true, audio: false});
        streamRef.value = stream;
        if (videoRef.value) {
            videoRef.value.srcObject = stream;
            await videoRef.value.play();
        }
    } catch (e) {
        error.value = "Không thể truy cập camera.";
    }
};

const takePhoto = async () => {
    const video = videoRef.value;
    if (!video) {
        return;
    }
    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext("2d");
    if (!ctx) {
        return;
    }
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    const blob = await new Promise((resolve) => canvas.toBlob(resolve, "image/jpeg", 0.85));
    if (blob) {
        emit("capture", blob);
    }
    emit("close");
};

onMounted(start);
onBeforeUnmount(stop);
</script>

<template>
    <div v-if="open" class="cc-overlay" role="dialog" aria-modal="true">
        <div class="cc-modal">
            <div class="cc-header">
                <div class="cc-title">Chụp ảnh</div>
                <button class="cc-close" type="button" @click="$emit('close')">✕</button>
            </div>
            <div class="cc-body">
                <div v-if="error" class="cc-error">{{ error }}</div>
                <video v-else ref="videoRef" class="cc-video" playsinline></video>
            </div>
            <div class="cc-actions">
                <button class="cc-btn" type="button" @click="start">Bật lại camera</button>
                <button class="cc-btn cc-primary" type="button" @click="takePhoto" :disabled="!!error">Chụp</button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.cc-overlay{position:fixed;inset:0;background:rgba(0,0,0,.55);display:flex;align-items:center;justify-content:center;z-index:9999;padding:16px}
.cc-modal{width:min(520px,100%);background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,.25);display:flex;flex-direction:column}
.cc-header{display:flex;align-items:center;justify-content:space-between;padding:12px 14px;border-bottom:1px solid #e5e7eb}
.cc-title{font-size:14px;font-weight:700;color:#111827}
.cc-close{border:0;background:transparent;font-size:18px;cursor:pointer;color:#374151}
.cc-body{padding:12px}
.cc-video{width:100%;height:320px;object-fit:cover;border-radius:10px;background:#0b1220}
.cc-error{padding:12px;border:1px solid #fecaca;background:#fff1f2;color:#991b1b;border-radius:10px;font-size:13px}
.cc-actions{display:flex;justify-content:flex-end;gap:10px;padding:12px 14px;border-top:1px solid #e5e7eb}
.cc-btn{border:1px solid #e5e7eb;background:#fff;border-radius:10px;padding:8px 12px;font-size:13px;cursor:pointer}
.cc-primary{border-color:#2563eb;background:#2563eb;color:#fff}
.cc-primary:disabled{opacity:.5;cursor:not-allowed}
</style>

