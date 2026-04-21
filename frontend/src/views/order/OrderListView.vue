<script setup>
import {OrderListPage} from "@/legacy/pages";
import {api} from "@/api";
import {computed, ref, onMounted, onUnmounted} from "vue";
import {useRouter} from "vue-router";

const {orders, error, load, dateTime} = OrderListPage.setup();
const router = useRouter();
const actionMessage = ref("");
const repayingOrderId = ref(null);
const refundingOrderId = ref(null);
const cancellingOrderId = ref(null);
const showCancelModal = ref(false);
const orderToCancel = ref(null);
const activeTab = ref("pending");
const refundRequests = ref([]);
const statusLabel = (status) => {
    const map = {
        PENDING_PAYMENT: "Đang chờ thanh toán",
        PLACED_PAID: "Đã đặt - đã TT",
        PLACED_UNPAID: "Đã đặt - chưa TT",
        NEW: "Đã đặt - chưa TT",
        PLACED: "Đã đặt - chưa TT",
        SHIPPING_PAID: "Đang giao - đã TT",
        SHIPPING_UNPAID: "Đang giao - chưa TT",
        SHIPPING: "Đang giao - chưa TT",
        DONE: "Giao thành công",
        DELIVERED_SUCCESS: "Giao thành công",
        CANCEL: "Giao thất bại",
        DELIVERY_FAILED: "Giao thất bại",
        REFUND_REQUEST: "Đang yêu cầu hoàn tiền"
    };
    return map[status] || status;
};
const canOnlyDetail = (status) => status === "PLACED_UNPAID" || status === "PLACED_PAID" || status === "REFUND_REQUEST";
const isPendingPayment = (status) => status === "PENDING_PAYMENT";
const canCancel = (status) => status === "PLACED_UNPAID" || status === "PENDING_PAYMENT" || status === "NEW" || status === "PLACED";
const formatExpectedDelivery = (order) => {
    const date = String(order?.expectedDeliveryDate || "").trim();
    const distanceM = Number(order?.deliveryDistanceM || 0);
    if (!date) {
        return "Chưa có";
    }
    const [year, month, day] = date.split("-");
    const dateLabel = year && month && day ? `${day}/${month}/${year}` : date;
    const km = distanceM > 0 ? `${(distanceM / 1000).toFixed(distanceM >= 10000 ? 0 : 1)} km` : "";
    return km ? `${dateLabel} • ${km}` : dateLabel;
};
const formatDeliveredTime = (value) => {
    const raw = String(value || "").trim();
    if (!raw) {
        return "Chưa có";
    }
    const dt = new Date(raw.replace(" ", "T"));
    if (Number.isNaN(dt.getTime())) {
        return raw;
    }
    const day = String(dt.getDate()).padStart(2, "0");
    const month = String(dt.getMonth() + 1).padStart(2, "0");
    const year = dt.getFullYear();
    const hh = String(dt.getHours()).padStart(2, "0");
    const mm = String(dt.getMinutes()).padStart(2, "0");
    const ss = String(dt.getSeconds()).padStart(2, "0");
    return `${hh}:${mm}:${ss} ${day}/${month}/${year}`;
};
const tabOrders = computed(() => {
    const rows = Array.isArray(orders.value) ? orders.value : [];
    let filtered = [];
    if (activeTab.value === "pending") {
        filtered = rows.filter((item) => item?.status === "PENDING_PAYMENT");
    }
    if (activeTab.value === "placed") {
        filtered = rows.filter((item) => item?.status === "PLACED_UNPAID" || item?.status === "PLACED_PAID" || item?.status === "REFUND_REQUEST");
    }
    if (activeTab.value === "delivered") {
        filtered = rows.filter((item) => item?.status === "DELIVERED_SUCCESS");
    }
    // Sort by createDate descending (newest first)
    return filtered.sort((a, b) => {
        const dateA = new Date(a.createDate || 0);
        const dateB = new Date(b.createDate || 0);
        return dateB.getTime() - dateA.getTime();
    });
});
const refundRows = computed(() => Array.isArray(refundRequests.value) ? refundRequests.value : []);
const refundStatusLabel = (status) => {
    const key = String(status || "").toUpperCase();
    if (key === "PENDING") return "pending";
    if (key === "SUCCESS") return "success";
    if (key === "DECLINED") return "decline";
    return key || "pending";
};
const retryPayment = async (order) => {
    if (!order?.id || repayingOrderId.value) {
        return;
    }
    repayingOrderId.value = order.id;
    actionMessage.value = "";
    try {
        // Always go to bank transfer page for existing orders
        await router.push(`/order/bank-transfer?id=${order.id}`);
    } catch (e) {
        actionMessage.value = e.message || "Không thể chuyển đến trang thanh toán.";
    } finally {
        repayingOrderId.value = null;
    }
};
const requestRefund = async (order) => {
    if (!order?.id || refundingOrderId.value) {
        return;
    }
    refundingOrderId.value = order.id;
    actionMessage.value = "";
    try {
        await api.orderWorkflow.requestRefund(order.id);
        await load();
        const list = await api.orderWorkflow.refundRequests();
        refundRequests.value = Array.isArray(list.data) ? list.data : [];
        actionMessage.value = "Đã gửi yêu cầu hoàn tiền.";
    } catch (e) {
        actionMessage.value = e.message || "Không thể gửi yêu cầu hoàn tiền.";
    } finally {
        refundingOrderId.value = null;
    }
};

const cancelOrder = async (order) => {
    if (!order?.id || cancellingOrderId.value) {
        return;
    }
    orderToCancel.value = order;
    showCancelModal.value = true;
};

const confirmCancelOrder = async () => {
    if (!orderToCancel.value?.id || cancellingOrderId.value) {
        return;
    }
    cancellingOrderId.value = orderToCancel.value.id;
    actionMessage.value = "";
    try {
        await api.orderWorkflow.cancelAndDelete(orderToCancel.value.id);
        await load();
        actionMessage.value = "Đã hủy đơn hàng thành công.";
    } catch (e) {
        actionMessage.value = e.message || "Không thể hủy đơn hàng.";
    } finally {
        cancellingOrderId.value = null;
        showCancelModal.value = false;
        orderToCancel.value = null;
    }
};

const closeCancelModal = () => {
    showCancelModal.value = false;
    orderToCancel.value = null;
};

const loadRefundRequests = async () => {
    try {
        const res = await api.orderWorkflow.refundRequests();
        refundRequests.value = Array.isArray(res.data) ? res.data : [];
    } catch (e) {
        refundRequests.value = [];
    }
};
loadRefundRequests();
const onTabChange = async (tab) => {
    activeTab.value = tab;
    await load(); // Reload orders when tab changes
    if (tab === "refund") {
        await loadRefundRequests();
    }
};

// Auto-reload when page becomes visible (e.g., returning from payment page)
const handleVisibilityChange = async () => {
    if (!document.hidden) {
        await load();
        if (activeTab.value === "refund") {
            await loadRefundRequests();
        }
    }
};

// Auto-reload periodically
let reloadInterval = null;

onMounted(() => {
    // Add visibility change listener
    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    // Set up periodic reload every 30 seconds
    reloadInterval = setInterval(async () => {
        await load();
        if (activeTab.value === "refund") {
            await loadRefundRequests();
        }
    }, 30000);
});

onUnmounted(() => {
    // Clean up listeners and intervals
    document.removeEventListener('visibilitychange', handleVisibilityChange);
    if (reloadInterval) {
        clearInterval(reloadInterval);
    }
});
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Đơn hàng của tôi</h3>
        <div v-if="error" class="alert alert-danger">{{ error }}</div>
        <div v-if="actionMessage" class="status-message" :class="actionMessage.includes('thành công') ? 'status-success' : 'status-error'">{{ actionMessage }}</div>
        <div class="order-tabs">
            <button class="order-tab-btn" :class="{active: activeTab === 'pending'}" type="button" @click="onTabChange('pending')">Đơn chờ thanh toán</button>
            <button class="order-tab-btn" :class="{active: activeTab === 'placed'}" type="button" @click="onTabChange('placed')">Đơn đã đặt</button>
            <button class="order-tab-btn" :class="{active: activeTab === 'delivered'}" type="button" @click="onTabChange('delivered')">Đơn đã giao</button>
            <button class="order-tab-btn" :class="{active: activeTab === 'refund'}" type="button" @click="onTabChange('refund')">Yêu cầu hoàn tiền</button>
        </div>
        <div class="card" v-if="activeTab !== 'refund'">
            <table>
                <thead><tr><th>Mã đơn</th><th>Ngày đặt  </th><th>Trạng thái</th><th v-if="activeTab === 'placed'">Dự kiến nhận hàng</th><th v-if="activeTab === 'delivered'">Thời gian giao</th><th>Địa chỉ giao hàng</th><th></th></tr></thead>
                <tbody>
                <tr v-for="o in tabOrders" :key="o.id">
                    <td>{{ o.id }}</td>
                    <td>{{ dateTime(o.createDate) }}</td>
                    <td><span class="badge" style="color:black;">{{ statusLabel(o.status) }}</span></td>
                    <td v-if="activeTab === 'placed'">{{ formatExpectedDelivery(o) }}</td>
                    <td v-if="activeTab === 'delivered'">{{ formatDeliveredTime(o.deliveredAt) }}</td>
                    <td>
                        <div class="order-address-scroll">{{ o.address }}</div>
                    </td>
                    <td class="table-actions">
                        <template v-if="isPendingPayment(o.status)">
                            <button
                                class="btn btn-outline"
                                type="button"
                                :disabled="repayingOrderId === o.id"
                                @click="retryPayment(o)"
                            >
                                {{ repayingOrderId === o.id ? "Đang xử lý..." : "Thanh toán" }}
                            </button>
                            <button v-if="canCancel(o.status)" class="btn btn-danger" type="button" :disabled="cancellingOrderId === o.id" @click="cancelOrder(o)">
                                {{ cancellingOrderId === o.id ? "Đang hủy..." : "Hủy đơn" }}
                            </button>
                        </template>
                        <template v-else-if="canOnlyDetail(o.status)">
                            <router-link class="btn btn-outline" :to="'/order/order-detail?id=' + o.id">Xem chi tiết</router-link>
                            <button v-if="canCancel(o.status)" class="btn btn-danger" type="button" :disabled="cancellingOrderId === o.id" @click="cancelOrder(o)">
                                {{ cancellingOrderId === o.id ? "Đang hủy..." : "Hủy đơn" }}
                            </button>
                            <button v-if="o.status === 'PLACED_PAID'" class="btn btn-outline" type="button" :disabled="refundingOrderId === o.id" @click="requestRefund(o)">
                                {{ refundingOrderId === o.id ? "Đang gửi..." : "Yêu cầu hoàn tiền" }}
                            </button>
                        </template>
                        <template v-else>
                            <router-link class="btn btn-outline" :to="'/order/order-detail?id=' + o.id">Xem chi tiết</router-link>
                            <router-link class="btn btn-outline" to="/order/my-product-list">Mua lại</router-link>
                        </template>
                    </td>
                </tr>
                <tr v-if="!tabOrders.length">
                    <td :colspan="activeTab === 'placed' || activeTab === 'delivered' ? 6 : 5" class="order-empty-row">Không có đơn hàng trong mục này.</td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="card" v-else>
            <table>
                <thead><tr><th>Mã đơn</th><th>Ngày yêu cầu</th><th>Trạng thái</th><th>Lý do từ chối</th></tr></thead>
                <tbody>
                <tr v-for="r in refundRows" :key="r.orderId + '_' + r.createdAt">
                    <td>{{ r.orderId }}</td>
                    <td>{{ dateTime(r.createdAt) }}</td>
                    <td><span class="badge" style="color:black;">{{ refundStatusLabel(r.status) }}</span></td>
                    <td>{{ r.declineReason || "-" }}</td>
                </tr>
                <tr v-if="!refundRows.length">
                    <td colspan="4" class="order-empty-row">Chưa có yêu cầu hoàn tiền.</td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="table-actions" style="margin-top:10px;">
            <button class="btn" type="button" @click="load">Tải danh sách</button>
        </div>
        
        <!-- Cancel Order Modal -->
        <div v-if="showCancelModal" class="modal-overlay" @click="closeCancelModal">
            <div class="modal-content" @click.stop>
                <div class="modal-header">
                    <h4>Xác nhận hủy đơn hàng</h4>
                    <button class="modal-close" @click="closeCancelModal">&times;</button>
                </div>
                <div class="modal-body">
                    <p>Bạn có chắc muốn hủy đơn hàng #{{ orderToCancel?.id }}?</p>
                    <p class="modal-warning">Hành động này không thể hoàn tác.</p>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-outline" @click="closeCancelModal" :disabled="cancellingOrderId">
                        Đóng
                    </button>
                    <button class="btn btn-danger" @click="confirmCancelOrder" :disabled="cancellingOrderId">
                        {{ cancellingOrderId ? "Đang hủy..." : "Xác nhận hủy" }}
                    </button>
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
.order-tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 12px;
}

.table-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    align-items: center;
}

.btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 8px 16px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    text-decoration: none;
    border: 1px solid #D1D5DB;
    background: #FFFFFF;
    color: #374151;
}

.btn:hover:not(:disabled) {
    background: #F3F4F6;
}

.btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.btn-danger {
    background: #FEE2E2;
    border-color: #FECACA;
    color: #DC2626;
}

.btn-danger:hover:not(:disabled) {
    background: #EF4444;
    border-color: #EF4444;
    color: #FFFFFF;
}

.order-tab-btn {
    border: 1px solid #d1d5db;
    border-radius: 10px;
    background: #fff;
    color: #111827;
    font-weight: 600;
    padding: 8px 14px;
}

.order-tab-btn.active {
    background: #111827;
    color: #fff;
    border-color: #111827;
}

.status-message {
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 12px;
    font-size: 14px;
}

.status-success {
    background: #ECFDF5;
    color: #059669;
    border: 1px solid #A7F3D0;
}

.status-error {
    background: #FEE2E2;
    color: #DC2626;
    border: 1px solid #FECACA;
}

.order-empty-row {
    text-align: center;
    color: #6b7280;
    padding: 16px;
}

.order-address-scroll {
    max-width: 460px;
    overflow-x: auto;
    white-space: nowrap;
    padding-bottom: 4px;
}

/* Modal Styles */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    border-radius: 12px;
    max-width: 400px;
    width: 90%;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 20px 0 20px;
    border-bottom: 1px solid #e5e7eb;
    margin-bottom: 20px;
}

.modal-header h4 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #111827;
}

.modal-close {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #6b7280;
    padding: 0;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
}

.modal-close:hover {
    background: #f3f4f6;
    color: #111827;
}

.modal-body {
    padding: 0 20px 20px 20px;
}

.modal-body p {
    margin: 0 0 12px 0;
    color: #374151;
    font-size: 14px;
    line-height: 1.5;
}

.modal-warning {
    color: #dc2626 !important;
    font-weight: 500;
}

.modal-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    padding: 20px;
    border-top: 1px solid #e5e7eb;
}

.modal-footer .btn {
    min-width: 100px;
}
</style>
