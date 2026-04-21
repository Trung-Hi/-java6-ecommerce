<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {api} from "@/api";

const props = defineProps({
    open: {type: Boolean, default: false}
});
const emit = defineEmits(["close", "change-product"]);

const tab = ref("cart");
const loading = ref(false);
const error = ref("");
const rows = ref([]);

const title = computed(() => (tab.value === "cart" ? "Giỏ hàng" : "Đã mua"));

const load = async () => {
    if (!props.open) {
        return;
    }
    loading.value = true;
    error.value = "";
    rows.value = [];
    try {
        if (tab.value === "cart") {
            const payload = (await api.cart.get()).data || {};
            const items = Array.isArray(payload.items) ? payload.items : [];
            rows.value = items.map((item) => ({
                productId: item.productId,
                name: item.name,
                image: item.image
            })).filter((x) => Number(x.productId) > 0);
        } else {
            const payload = (await api.orderWorkflow.myDeliveredProducts()).data || [];
            const items = Array.isArray(payload) ? payload : [];
            const map = new Map();
            for (const detail of items) {
                const pid = detail?.product?.id || detail?.productId;
                const name = detail?.product?.name || detail?.productName || "";
                const image = detail?.product?.image || detail?.image || "";
                if (!pid || map.has(pid)) {
                    continue;
                }
                map.set(pid, {productId: pid, name, image});
            }
            rows.value = Array.from(map.values());
        }
    } catch (e) {
        error.value = e?.message || "Không thể tải danh sách sản phẩm.";
    } finally {
        loading.value = false;
    }
};

const choose = (row) => {
    emit("change-product", {
        productId: row.productId,
        productName: row.name || "",
        thumbnailUrl: row.image ? "/images/" + row.image : ""
    });
    emit("close");
};

watch(() => props.open, (value) => {
    if (value) {
        load();
    }
});
watch(tab, () => load());
onMounted(() => {
    if (props.open) {
        load();
    }
});
</script>

<template>
    <div v-if="open" class="pm-overlay" role="dialog" aria-modal="true">
        <div class="pm-modal">
            <div class="pm-header">
                <div>
                    <div class="pm-title">Chọn sản phẩm để hỗ trợ</div>
                    <div class="pm-subtitle">Tab: {{ title }}</div>
                </div>
                <button class="pm-close" type="button" @click="$emit('close')">✕</button>
            </div>

            <div class="pm-tabs">
                <button class="pm-tab" type="button" :class="{active: tab === 'cart'}" @click="tab = 'cart'">Giỏ hàng</button>
                <button class="pm-tab" type="button" :class="{active: tab === 'purchased'}" @click="tab = 'purchased'">Đã mua</button>
            </div>

            <div class="pm-body">
                <div v-if="loading" class="pm-status">Đang tải...</div>
                <div v-else-if="error" class="pm-status pm-error">{{ error }}</div>
                <div v-else-if="rows.length === 0" class="pm-status">Không có sản phẩm.</div>
                <div v-else class="pm-list">
                    <div class="pm-item" v-for="row in rows" :key="row.productId">
                        <img class="pm-thumb" :src="row.image ? '/images/' + row.image : '/images/product1.jpg'" :alt="row.name">
                        <div class="pm-meta">
                            <div class="pm-name">{{ row.name }}</div>
                            <div class="pm-id">Mã: {{ row.productId }}</div>
                        </div>
                        <button class="pm-pick" type="button" @click="choose(row)">Chọn</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pm-overlay{position:fixed;inset:0;background:rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;z-index:9998;padding:16px}
.pm-modal{width:min(840px,100%);max-height:80vh;background:#fff;border-radius:12px;box-shadow:0 20px 60px rgba(0,0,0,.25);display:flex;flex-direction:column;overflow:hidden}
.pm-header{display:flex;align-items:flex-start;justify-content:space-between;padding:16px 16px 12px;border-bottom:1px solid #e5e7eb}
.pm-title{font-size:16px;font-weight:700;color:#111827}
.pm-subtitle{font-size:12px;color:#6b7280;margin-top:2px}
.pm-close{border:0;background:transparent;font-size:18px;line-height:1;cursor:pointer;color:#374151;padding:6px}
.pm-tabs{display:flex;gap:8px;padding:12px 16px;border-bottom:1px solid #e5e7eb}
.pm-tab{border:1px solid #e5e7eb;background:#fff;border-radius:999px;padding:8px 12px;font-size:13px;cursor:pointer;color:#111827}
.pm-tab.active{border-color:#2563eb;background:#eff6ff;color:#1d4ed8}
.pm-body{padding:12px 16px;overflow:auto}
.pm-status{padding:16px;border:1px dashed #e5e7eb;border-radius:10px;color:#374151;font-size:14px}
.pm-error{border-color:#fecaca;background:#fff1f2;color:#991b1b}
.pm-list{display:flex;flex-direction:column;gap:10px}
.pm-item{display:flex;align-items:center;gap:12px;border:1px solid #e5e7eb;border-radius:12px;padding:10px}
.pm-thumb{width:48px;height:48px;border-radius:10px;object-fit:cover;background:#f3f4f6}
.pm-meta{flex:1;min-width:0}
.pm-name{font-size:14px;font-weight:600;color:#111827;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.pm-id{font-size:12px;color:#6b7280;margin-top:2px}
.pm-pick{border:1px solid #2563eb;background:#2563eb;color:#fff;border-radius:10px;padding:8px 12px;font-size:13px;cursor:pointer}
.pm-pick:hover{filter:brightness(1.05)}
@media (max-width:480px){.pm-item{padding:8px}.pm-thumb{width:40px;height:40px}.pm-pick{padding:7px 10px}}
</style>

