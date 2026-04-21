<script setup>
import {computed, onMounted, reactive, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {OrderDetailPage} from "@/legacy/pages";
import {api, openSupportChat} from "@/api";
import router from "@/router";
import {isValidVnPhone10, normalizePhone} from "@/utils/phone";

const {orderId, data, error, load, money} = OrderDetailPage.setup();
const route = useRoute();
const reviewForms = reactive({});
const reviewMessage = ref("");
const removingItems = ref(new Set());
const reviewedSet = computed(() => new Set(data.value?.reviewedProductIds || []));
const isReviewable = computed(() => !!data.value?.reviewable);
const orderStatus = computed(() => String(data.value?.order?.status || ""));
const isUnpaidPlaced = computed(() => orderStatus.value === "PLACED_UNPAID");
const isPendingPayment = computed(() => orderStatus.value === "PENDING_PAYMENT");
const canEditShipping = computed(() => orderStatus.value === "PLACED_UNPAID" || orderStatus.value === "PLACED_PAID");
const shippingModalOpen = ref(false);
const shippingSaving = ref(false);
const shippingError = ref("");
const shippingForm = reactive({address: "", shippingPhone: ""});
const confirmModalOpen = ref(false);
const confirmModalTitle = ref("");
const confirmModalMessage = ref("");
const confirmModalAction = ref(null);
const pendingDeleteDetail = ref(null);
const pendingExchangeRow = ref(null);
const exchangeOpen = ref(false);
const exchangeTarget = ref(null);
const exchangeRows = ref([]);
const exchangeCategories = ref([]);
const exchangePage = ref(0);
const exchangeTotalPages = ref(0);
const exchangeLoading = ref(false);
const exchangeError = ref("");
const exchangeFilters = reactive({keyword: "", categoryId: "", minPrice: "", maxPrice: ""});
const exchangeSelection = reactive({});
const previewImage = ref("");
const cancellingOrderId = ref(null);
const showCancelModal = ref(false);

const expectedDeliveryDate = computed(() => {
    const base = new Date();
    base.setDate(base.getDate() + 3);
    return base.toLocaleDateString("vi-VN");
});

const formatOrderDateTime = (value) => {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    const dd = String(date.getDate()).padStart(2, "0");
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const yyyy = date.getFullYear();
    const hh = String(date.getHours()).padStart(2, "0");
    const min = String(date.getMinutes()).padStart(2, "0");
    return `${dd}/${mm}/${yyyy} - ${hh}:${min}`;
};

const totalItems = computed(() => data.value?.details?.reduce((sum, d) => sum + (d.quantity || 0), 0) || 0);
const shippingFee = computed(() => 0);

// Cart-style calculations
const lineDiscount = (detail) => {
    const price = Number(detail?.price || 0);
    const quantity = Number(detail?.quantity || 0);
    const discountPercent = Number(detail?.product?.discount || detail?.discount || 0);
    if (!price || !quantity || !discountPercent) return 0;
    return (price * quantity * discountPercent) / 100;
};

const totalDiscount = computed(() => {
    return (data.value?.details || []).reduce((sum, d) => sum + lineDiscount(d), 0);
});

const totalBeforeDiscount = computed(() => {
    const total = Number(data.value?.totalAmount || 0);
    return total + Number(totalDiscount.value || 0);
});

// Quantity control methods
const updatingQty = ref({});

const decreaseQty = async (detail) => {
    if (detail.quantity <= 1) return;
    const key = detail.id;
    if (updatingQty.value[key]) return;
    updatingQty.value[key] = true;
    detail.quantity--;
    try {
        await api.orderWorkflow.updateDetailQuantity(data.value.order.id, detail.id, detail.quantity);
    } catch (e) {
        detail.quantity++;
        reviewMessage.value = e.message || "Không thể cập nhật số lượng.";
    } finally {
        updatingQty.value[key] = false;
    }
};

const increaseQty = async (detail) => {
    const key = detail.id;
    if (updatingQty.value[key]) return;
    updatingQty.value[key] = true;
    detail.quantity++;
    try {
        await api.orderWorkflow.updateDetailQuantity(data.value.order.id, detail.id, detail.quantity);
    } catch (e) {
        detail.quantity--;
        reviewMessage.value = e.message || "Không thể cập nhật số lượng.";
    } finally {
        updatingQty.value[key] = false;
    }
};

const reviewKey = (detail) => String(detail.product?.id || detail.productId || "");
const ensureReviewForm = (detail) => {
    const key = reviewKey(detail);
    if (!reviewForms[key]) {
        reviewForms[key] = {starRating: 5, reviewContent: "", images: []};
    }
    return reviewForms[key];
};

const setReviewStar = (detail, star) => {
    const form = ensureReviewForm(detail);
    form.starRating = star;
};

const onReviewImagesChange = (detail, event) => {
    const form = ensureReviewForm(detail);
    form.images = Array.from(event.target.files || []);
};

const submitReview = async (detail) => {
    const productId = detail.product?.id || detail.productId;
    if (!productId || !data.value?.order?.id) {
        reviewMessage.value = "Không xác định được sản phẩm hoặc đơn hàng.";
        return;
    }
    const form = ensureReviewForm(detail);
    try {
        const response = await api.reviews.createFromOrder({
            orderId: data.value.order.id,
            productId,
            starRating: Number(form.starRating || 5),
            reviewContent: form.reviewContent || "",
            images: form.images || []
        });
        reviewMessage.value = response.message || "Đã gửi đánh giá.";
        await load();
    } catch (e) {
        reviewMessage.value = e.message;
    }
};

const buyAgain = async (detail) => {
    const productId = detail.product?.id || detail.productId;
    const sizeId = detail.sizeId;
    const quantity = Number(detail.quantity || 1);
    if (!productId || !sizeId) return;
    try {
        await api.cart.addDetail(productId, sizeId, quantity > 0 ? quantity : 1);
    } catch (e) {}
    await router.push(`/product/detail?id=${productId}`);
};

const removeAllItems = () => {
    confirmModalTitle.value = "Xác nhận xóa tất cả";
    confirmModalMessage.value = `Bạn có chắc muốn xóa tất cả ${totalItems.value} sản phẩm khỏi đơn hàng?\n\nThao tác này không thể hoàn tác.`;
    confirmModalAction.value = "deleteAll";
    confirmModalOpen.value = true;
};

const finalPrice = (row) => {
    const p = Number(row?.finalPrice ?? row?.price ?? 0);
    return Number.isFinite(p) ? p : 0;
};

const selectedSizeId = (row) => Number(exchangeSelection[row.id]?.sizeId || 0);
const selectedQty = (row) => Number(exchangeSelection[row.id]?.quantity || 1);
const lineTotal = (row) => finalPrice(row) * selectedQty(row);

const ensureSelection = (row) => {
    if (!exchangeSelection[row.id]) {
        const firstSize = Array.isArray(row.sizes) && row.sizes.length ? row.sizes[0].sizeId : "";
        exchangeSelection[row.id] = {sizeId: firstSize, quantity: 1};
    }
    return exchangeSelection[row.id];
};

const showProductImage = (image) => {
    if (!image) return;
    previewImage.value = image.startsWith("/") ? image : `/images/${image}`;
};

const closeImagePreview = () => {
    previewImage.value = "";
};

const loadExchangeCatalog = async (page = 0) => {
    exchangeLoading.value = true;
    exchangeError.value = "";
    try {
        const res = await api.orderWorkflow.exchangeCatalog({
            page,
            size: 8,
            keyword: exchangeFilters.keyword || undefined,
            categoryId: exchangeFilters.categoryId || undefined,
            minPrice: exchangeFilters.minPrice || undefined,
            maxPrice: exchangeFilters.maxPrice || undefined
        });
        const payload = res.data || {};
        exchangeRows.value = Array.isArray(payload.rows) ? payload.rows : [];
        exchangeCategories.value = Array.isArray(payload.categories) ? payload.categories : [];
        exchangePage.value = Number(payload.page || 0);
        exchangeTotalPages.value = Number(payload.totalPages || 0);
        exchangeRows.value.forEach((row) => ensureSelection(row));
    } catch (e) {
        exchangeError.value = e.message || "Không tải được danh sách sản phẩm để đổi.";
    } finally {
        exchangeLoading.value = false;
    }
};

const openExchange = async (detail) => {
    exchangeTarget.value = detail;
    exchangeOpen.value = true;
    await loadExchangeCatalog(0);
};

const closeExchange = () => {
    exchangeOpen.value = false;
    exchangeTarget.value = null;
    exchangeRows.value = [];
    exchangeError.value = "";
};

const applyExchange = async (row) => {
    if (!exchangeTarget.value || !data.value?.order?.id) return;
    const selected = ensureSelection(row);
    const payload = {
        productId: row.id,
        sizeId: Number(selected.sizeId || 0),
        quantity: Number(selected.quantity || 1)
    };
    if (!payload.productId || !payload.sizeId || payload.quantity <= 0) {
        exchangeError.value = "Vui lòng chọn size và số lượng hợp lệ.";
        return;
    }
    pendingExchangeRow.value = { row, payload };
    confirmModalTitle.value = "Xác nhận đổi sản phẩm";
    confirmModalMessage.value = `Bạn có chắc muốn đổi sang sản phẩm "${row.name}"?\n\nThao tác này không thể hoàn tác.`;
    confirmModalAction.value = "exchange";
    confirmModalOpen.value = true;
};

const executeExchange = async () => {
    if (!pendingExchangeRow.value || !exchangeTarget.value || !data.value?.order?.id) return;
    const { payload } = pendingExchangeRow.value;
    try {
        await api.orderWorkflow.exchangeDetail(data.value.order.id, exchangeTarget.value.id, payload);
        await load();
        closeExchange();
        closeConfirmModal();
    } catch (e) {
        exchangeError.value = e.message || "Không thể đổi sản phẩm.";
        closeConfirmModal();
    }
};

const removeOrderDetail = async (detail) => {
    if (!data.value?.order?.id) return;
    const productName = detail.product?.name || detail.productName || "sản phẩm này";
    pendingDeleteDetail.value = detail;
    confirmModalTitle.value = "Xác nhận xóa sản phẩm";
    confirmModalMessage.value = `Bạn có chắc muốn xóa "${productName}" khỏi đơn hàng?\n\nThao tác này không thể hoàn tác.`;
    confirmModalAction.value = "delete";
    confirmModalOpen.value = true;
};

const executeDelete = async () => {
    if (!pendingDeleteDetail.value || !data.value?.order?.id) return;
    const detail = pendingDeleteDetail.value;
    
    // Add fade-out animation
    removingItems.value.add(detail.id);
    
    // Wait for animation
    await new Promise(resolve => setTimeout(resolve, 300));
    
    try {
        const res = await api.orderWorkflow.removeDetail(data.value.order.id, detail.id);
        if (res.data?.orderDeleted) {
            closeConfirmModal();
            await router.push("/order/order-list");
            return;
        }
        removingItems.value.delete(detail.id);
        await load();
        closeConfirmModal();
    } catch (e) {
        removingItems.value.delete(detail.id);
        reviewMessage.value = e.message || "Không thể xoá sản phẩm khỏi đơn hàng.";
        closeConfirmModal();
    }
};

const executeDeleteAll = async () => {
    if (!data.value?.order?.id || !data.value?.details?.length) return;
    try {
        for (const detail of data.value.details) {
            const res = await api.orderWorkflow.removeDetail(data.value.order.id, detail.id);
            if (res.data?.orderDeleted) {
                await router.push("/order/order-list");
                return;
            }
        }
        await load();
        closeConfirmModal();
    } catch (e) {
        reviewMessage.value = e.message || "Không thể xoá sản phẩm khỏi đơn hàng.";
        closeConfirmModal();
    }
};

const closeConfirmModal = () => {
    confirmModalOpen.value = false;
    confirmModalTitle.value = "";
    confirmModalMessage.value = "";
    confirmModalAction.value = null;
    pendingDeleteDetail.value = null;
    pendingExchangeRow.value = null;
};

const handleConfirmAction = () => {
    if (confirmModalAction.value === "delete") {
        executeDelete();
    } else if (confirmModalAction.value === "exchange") {
        executeExchange();
    } else if (confirmModalAction.value === "deleteAll") {
        executeDeleteAll();
    }
};

const openShippingModal = () => {
    shippingForm.address = String(data.value?.order?.address || "").trim();
    shippingForm.shippingPhone = String(data.value?.order?.shippingPhone || "").trim();
    shippingError.value = "";
    shippingModalOpen.value = true;
};

const closeShippingModal = () => {
    if (shippingSaving.value) return;
    shippingModalOpen.value = false;
    shippingError.value = "";
};

const submitShippingUpdate = async () => {
    if (!data.value?.order?.id) return;
    const address = String(shippingForm.address || "").trim();
    const phone = normalizePhone(shippingForm.shippingPhone);
    if (!address) {
        shippingError.value = "Vui lòng nhập địa chỉ giao hàng.";
        return;
    }
    if (!isValidVnPhone10(phone)) {
        shippingError.value = "Số điện thoại phải gồm 10 số, bắt đầu bằng 0 và không được là 10 số 0.";
        return;
    }
    shippingSaving.value = true;
    shippingError.value = "";
    try {
        await api.orderWorkflow.updateShipping(data.value.order.id, {address, shippingPhone: phone});
        await load();
        closeShippingModal();
    } catch (e) {
        shippingError.value = e.message || "Không thể cập nhật thông tin giao hàng.";
    } finally {
        shippingSaving.value = false;
    }
};

const contactSeller = (detail) => {
    const productId = Number(detail?.product?.id || detail?.productId || 0);
    if (!productId) return;
    openSupportChat({
        productId,
        productName: detail?.product?.name || detail?.productName || "",
        thumbnailUrl: detail?.product?.image ? `/images/${detail.product.image}` : ""
    });
};

const continuePayment = () => {
    if (!data.value?.order?.id) return;
    router.push(`/order/bank-transfer?id=${data.value.order.id}`);
};

const cancelOrder = () => {
    if (!data.value?.order?.id || cancellingOrderId.value) {
        return;
    }
    showCancelModal.value = true;
};

const confirmCancelOrder = async () => {
    if (!data.value?.order?.id || cancellingOrderId.value) {
        return;
    }
    cancellingOrderId.value = data.value.order.id;
    try {
        await api.orderWorkflow.cancelAndDelete(data.value.order.id);
        await router.push("/order/order-list");
    } catch (e) {
        reviewMessage.value = e.message || "Không thể hủy đơn hàng.";
    } finally {
        cancellingOrderId.value = null;
        showCancelModal.value = false;
    }
};

const closeCancelModal = () => {
    showCancelModal.value = false;
};

const statusLabel = (status) => {
    const map = {
        PLACED_PAID: "Đã đặt - đã TT",
        PLACED_UNPAID: "Đã đặt - chưa TT",
        NEW: "Đã đặt - chưa TT",
        PLACED: "Đã đặt - chưa TT",
        PENDING_PAYMENT: "Đang chờ thanh toán",
        SHIPPING_PAID: "Đang giao - đã TT",
        SHIPPING_UNPAID: "Đang giao - chưa TT",
        SHIPPING: "Đang giao - chưa TT",
        DONE: "Giao thành công",
        DELIVERED_SUCCESS: "Giao thành công",
        CANCEL: "Giao thất bại",
        DELIVERY_FAILED: "Giao thất bại",
        REFUND_REQUEST: "Yêu cầu hoàn tiền"
    };
    return map[status] || status;
};

const statusBadgeClass = (status) => {
    const classes = {
        PLACED_PAID: "status-placed",
        PLACED_UNPAID: "status-pending",
        NEW: "status-pending",
        PLACED: "status-pending",
        PENDING_PAYMENT: "status-pending",
        SHIPPING_PAID: "status-shipping",
        SHIPPING_UNPAID: "status-shipping",
        SHIPPING: "status-shipping",
        DONE: "status-delivered",
        DELIVERED_SUCCESS: "status-delivered",
        CANCEL: "status-cancelled",
        DELIVERY_FAILED: "status-cancelled",
        REFUND_REQUEST: "status-refund"
    };
    return classes[status] || "status-pending";
};

const initOrderByQuery = async () => {
    const queryId = Number(route.query.id || route.query.orderId || "");
    if (!Number.isFinite(queryId) || queryId <= 0) return;
    orderId.value = String(queryId);
    await load();
};

onMounted(initOrderByQuery);
watch(() => route.query.id, initOrderByQuery);
watch(() => route.query.orderId, initOrderByQuery);
</script>

<template>
    <div class="ttshop-layout">
        <!-- Main Content Area -->
        <main class="ttshop-main">
            <div v-if="error" class="ttshop-alert ttshop-alert-error">{{ error }}</div>
            <div v-if="reviewMessage" class="ttshop-alert" :class="reviewMessage.includes('thành công') || reviewMessage.includes('Đã gửi') ? 'ttshop-alert-success' : 'ttshop-alert-error'">{{ reviewMessage }}</div>

            <!-- Two Column Layout -->
            <div class="ttshop-container" v-if="data">
                <!-- Left Column: Products Table (65%) -->
                <div class="ttshop-left-col">
                    <!-- Order Header -->
                    <div class="ttshop-page-header">
                        <h1 class="ttshop-page-title">Chi tiết đơn hàng</h1>
                        <span class="ttshop-order-code">#{{ data?.order?.id }}</span>
                        <div class="ttshop-status-badge" :class="statusBadgeClass(data.order?.status)">
                            {{ statusLabel(data.order?.status) }}
                        </div>
                    </div>

                    <!-- Products Table -->
                    <div class="ttshop-products-table" v-if="data.details?.length">
                        <table class="ttshop-table">
                            <thead>
                                <tr>
                                    <th class="col-product">Sản phẩm</th>
                                    <th class="col-price">Giá</th>
                                    <th class="col-discount">Giảm giá</th>
                                    <th class="col-qty">Số lượng</th>
                                    <th class="col-size">Size</th>
                                    <th class="col-action"></th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="d in (data.details||[])" :key="d.id" 
                                    class="ttshop-table-row"
                                    :class="{ 'ttshop-item-removing': removingItems.has(d.id) }">
                                    <td>
                                        <div class="ttshop-table-product">
                                            <div class="ttshop-table-img">
                                                <img v-if="d.product?.image" :src="`/images/${d.product.image}`" alt="product">
                                                <span v-else>Image</span>
                                            </div>
                                            <span class="ttshop-table-name">{{ d.product?.name || d.productName }}</span>
                                        </div>
                                    </td>
                                    <td class="ttshop-table-price">{{ money(d.price) }} đ</td>
                                    <td class="ttshop-table-discount">
                                        {{ money((d.price * d.quantity * (d.product?.discount || d.discount || 0)) / 100) }} đ
                                    </td>
                                    <td>
                                        <div class="ttshop-qty-control" v-if="isUnpaidPlaced">
                                            <button type="button" class="ttshop-qty-btn" @click="decreaseQty(d)" :disabled="d.quantity <= 1">−</button>
                                            <span class="ttshop-qty-value">{{ d.quantity }}</span>
                                            <button type="button" class="ttshop-qty-btn" @click="increaseQty(d)">+</button>
                                        </div>
                                        <span v-else class="ttshop-qty-static">x{{ d.quantity }}</span>
                                    </td>
                                    <td class="ttshop-table-size">{{ d.sizeName || '-' }}</td>
                                    <td>
                                        <div class="ttshop-row-actions">
                                            <button class="ttshop-row-btn ttshop-row-contact" type="button" @click="contactSeller(d)">
                                                Liên hệ
                                            </button>
                                            <button v-if="isUnpaidPlaced" class="ttshop-row-btn ttshop-row-delete" type="button" @click="removeOrderDetail(d)">
                                                Xóa
                                            </button>
                                            <button v-if="isUnpaidPlaced" class="ttshop-row-btn ttshop-row-exchange" type="button" @click="openExchange(d)">
                                                Đổi
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <!-- Empty State -->
                    <div v-else class="ttshop-empty-state">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
                        </svg>
                        <p>Chưa có sản phẩm nào trong đơn hàng</p>
                    </div>

                    <!-- Reviewable Products Section -->
                    <div v-if="isReviewable" class="ttshop-section">
                        <h2 class="ttshop-section-title">Đánh giá sản phẩm</h2>
                        <div class="ttshop-review-list">
                            <template v-for="d in (data.details||[])" :key="d.id">
                                <div v-if="!reviewedSet.has(d.product?.id)" class="ttshop-review-card">
                                    <div class="ttshop-review-product">
                                        <img :src="d.product?.image ? `/images/${d.product.image}` : ''" alt="product" class="ttshop-review-img">
                                        <div class="ttshop-review-info">
                                            <span class="ttshop-review-name">{{ d.product?.name || d.productName }}</span>
                                            <span class="ttshop-review-meta">Size: {{ d.sizeName }} | SL: {{ d.quantity }}</span>
                                        </div>
                                    </div>
                                    <form @submit.prevent="submitReview(d)" class="ttshop-review-form">
                                        <div class="ttshop-star-rating">
                                            <button v-for="star in 5" :key="star" type="button"
                                                class="ttshop-star-btn" :class="{ active: star <= ensureReviewForm(d).starRating }"
                                                @click="setReviewStar(d, star)">★</button>
                                        </div>
                                        <textarea v-model="ensureReviewForm(d).reviewContent" class="ttshop-review-input" rows="2" placeholder="Chia sẻ trải nghiệm của bạn..."></textarea>
                                        <div class="ttshop-review-actions">
                                            <label class="ttshop-file-label">
                                                <input type="file" multiple accept="image/*" @change="onReviewImagesChange(d, $event)">
                                                <span>+ Thêm ảnh</span>
                                            </label>
                                            <button type="submit" class="ttshop-btn-submit">Gửi đánh giá</button>
                                        </div>
                                    </form>
                                </div>
                                <div v-else class="ttshop-reviewed-badge">
                                    ✓ Đã đánh giá {{ d.product?.name || d.productName }}
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

                <!-- Right Column: Order Info & Summary (35%) -->
                <div class="ttshop-right-col">
                    <div class="ttshop-sidebar">
                        <!-- Order Info -->
                        <div class="ttshop-info-section">
                            <h3 class="ttshop-section-title">Thông tin đơn hàng</h3>
                            <div class="ttshop-info-row">
                                <span class="ttshop-info-label">Ngày đặt</span>
                                <span class="ttshop-info-value">{{ formatOrderDateTime(data.order?.createDate) }}</span>
                            </div>
                            <div class="ttshop-info-row">
                                <span class="ttshop-info-label">Địa chỉ</span>
                                <span class="ttshop-info-value">{{ data.order?.address || "Chưa cập nhật" }}</span>
                            </div>
                            <div class="ttshop-info-row">
                                <span class="ttshop-info-label">Điện thoại</span>
                                <span class="ttshop-info-value">{{ data.order?.shippingPhone || "0916874771" }}</span>
                            </div>
                            <div class="ttshop-info-row">
                                <span class="ttshop-info-label">Dự kiến giao</span>
                                <span class="ttshop-info-value ttshop-highlight">{{ expectedDeliveryDate }}</span>
                            </div>
                            <button v-if="canEditShipping" class="ttshop-btn-update" type="button" @click="openShippingModal">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                                </svg>
                                Cập nhật thông tin giao hàng
                            </button>
                        </div>

                        <div class="ttshop-divider"></div>

                        <!-- Summary -->
                        <div class="ttshop-summary-section">
                            <h3 class="ttshop-section-title">Tổng kết</h3>
                            <div class="ttshop-summary-row">
                                <span>Tổng tiền hàng</span>
                                <strong>{{ money(totalBeforeDiscount) }} đ</strong>
                            </div>
                            <div class="ttshop-summary-row">
                                <span>Giảm giá</span>
                                <strong class="ttshop-discount">-{{ money(totalDiscount) }} đ</strong>
                            </div>
                            <div class="ttshop-summary-row">
                                <span>Phí vận chuyển</span>
                                <span class="ttshop-free">Miễn phí</span>
                            </div>
                            <div class="ttshop-summary-row ttshop-total">
                                <span>Cần thanh toán</span>
                                <strong class="ttshop-total-price">{{ money(data.totalAmount) }} đ</strong>
                            </div>
                        </div>

                        <!-- Actions -->
                        <div class="ttshop-sidebar-actions">
                            <div v-if="isPendingPayment" class="ttshop-action-buttons">
                                <button class="ttshop-btn-cancel" type="button" @click="cancelOrder" :disabled="cancellingOrderId">
                                    {{ cancellingOrderId ? "Đang hủy..." : "HỦY ĐƠN" }}
                                </button>
                                <button class="ttshop-btn-payment" type="button" @click="continuePayment">
                                    THANH TOÁN
                                </button>
                            </div>
                            <button v-else-if="isUnpaidPlaced && data.details?.length > 0" class="ttshop-btn-clear" type="button" @click="removeAllItems">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                                </svg>
                                Xóa tất cả
                            </button>
                            <button v-else-if="isUnpaidPlaced" class="ttshop-btn-payment" type="button" @click="continuePayment">
                                THANH TOÁN
                            </button>
                            <button v-else class="ttshop-btn-back" type="button" @click="router.push('/order/order-list')">
                                QUAY LẠI ĐƠN HÀNG
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </main>

        <!-- Exchange Modal -->
        <div class="ttshop-modal-backdrop" :class="{open: exchangeOpen}" v-if="exchangeOpen" @click.self="closeExchange">
            <div class="ttshop-modal-panel ttshop-modal-large">
                <header class="ttshop-modal-header">
                    <h3>Đổi sản phẩm</h3>
                    <button class="ttshop-btn-close" type="button" @click="closeExchange">×</button>
                </header>
                <div class="ttshop-modal-body">
                    <div class="ttshop-filter-bar">
                        <input v-model="exchangeFilters.keyword" placeholder="Tìm sản phẩm..." class="ttshop-filter-input">
                        <select v-model="exchangeFilters.categoryId" class="ttshop-filter-select">
                            <option value="">Tất cả thể loại</option>
                            <option v-for="c in exchangeCategories" :key="c.id" :value="c.id">{{ c.name }}</option>
                        </select>
                        <input type="number" v-model="exchangeFilters.minPrice" placeholder="Giá từ" class="ttshop-filter-input ttshop-filter-price">
                        <input type="number" v-model="exchangeFilters.maxPrice" placeholder="Giá đến" class="ttshop-filter-input ttshop-filter-price">
                        <button class="ttshop-btn-outline" type="button" @click="loadExchangeCatalog(0)">Lọc</button>
                    </div>
                    <div v-if="exchangeError" class="ttshop-alert ttshop-alert-error">{{ exchangeError }}</div>
                    <div class="ttshop-exchange-list">
                        <div v-for="row in exchangeRows" :key="row.id" class="ttshop-exchange-item">
                            <div class="ttshop-exchange-info">
                                <div class="ttshop-exchange-name">{{ row.name }}</div>
                                <div class="ttshop-exchange-price">
                                    <span class="ttshop-price-old">{{ money(row.price) }} đ</span>
                                    <span class="ttshop-price-new">{{ money(finalPrice(row)) }} đ</span>
                                </div>
                            </div>
                            <div class="ttshop-exchange-actions">
                                <button class="ttshop-btn-outline ttshop-btn-sm" type="button" @click="showProductImage(row.image)">Xem ảnh</button>
                                <select v-model="ensureSelection(row).sizeId" class="ttshop-filter-select">
                                    <option v-for="s in row.sizes || []" :key="s.sizeId" :value="s.sizeId">{{ s.sizeName }} ({{ s.stock }})</option>
                                </select>
                                <input type="number" min="1" v-model.number="ensureSelection(row).quantity" class="ttshop-filter-input ttshop-filter-qty">
                                <span class="ttshop-exchange-total">{{ money(lineTotal(row)) }} đ</span>
                                <button class="ttshop-btn-primary ttshop-btn-sm" type="button" @click="applyExchange(row)">Chọn</button>
                            </div>
                        </div>
                        <div v-if="!exchangeRows.length && !exchangeLoading" class="ttshop-exchange-empty">Không có sản phẩm phù hợp</div>
                    </div>
                    <div class="ttshop-pagination" v-if="exchangeTotalPages > 1">
                        <button class="ttshop-btn-outline ttshop-btn-sm" type="button" :disabled="exchangePage<=0" @click="loadExchangeCatalog(exchangePage-1)">← Trước</button>
                        <span class="ttshop-page-info">{{ exchangePage + 1 }} / {{ exchangeTotalPages }}</span>
                        <button class="ttshop-btn-outline ttshop-btn-sm" type="button" :disabled="exchangePage+1>=exchangeTotalPages" @click="loadExchangeCatalog(exchangePage+1)">Sau →</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Image Preview Modal -->
        <div class="ttshop-modal-backdrop" :class="{open: !!previewImage}" v-if="previewImage" @click.self="closeImagePreview">
            <div class="ttshop-modal-panel ttshop-modal-image">
                <button class="ttshop-btn-close" type="button" @click="closeImagePreview">×</button>
                <img :src="previewImage" alt="preview" class="ttshop-preview-img">
            </div>
        </div>

        <!-- Shipping Modal -->
        <div class="ttshop-modal-backdrop" :class="{open: shippingModalOpen}" v-if="shippingModalOpen" @click.self="closeShippingModal">
            <div class="ttshop-modal-panel">
                <header class="ttshop-modal-header">
                    <h3>Cập nhật thông tin giao hàng</h3>
                    <button class="ttshop-btn-close" type="button" @click="closeShippingModal">×</button>
                </header>
                <div class="ttshop-modal-body">
                    <div class="ttshop-form-group">
                        <label class="ttshop-form-label">Địa chỉ giao hàng</label>
                        <input v-model="shippingForm.address" type="text" class="ttshop-form-input" placeholder="Nhập địa chỉ giao hàng">
                    </div>
                    <div class="ttshop-form-group">
                        <label class="ttshop-form-label">Số điện thoại</label>
                        <input v-model="shippingForm.shippingPhone" type="text" class="ttshop-form-input" placeholder="0xxxxxxxxx">
                    </div>
                    <div v-if="shippingError" class="ttshop-alert ttshop-alert-error">{{ shippingError }}</div>
                    <div class="ttshop-modal-actions">
                        <button class="ttshop-btn-primary" type="button" :disabled="shippingSaving" @click="submitShippingUpdate">
                            {{ shippingSaving ? "Đang lưu..." : "Lưu thay đổi" }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Confirmation Modal -->
        <div class="ttshop-modal-backdrop" :class="{open: confirmModalOpen}" v-if="confirmModalOpen" @click.self="closeConfirmModal">
            <div class="ttshop-modal-panel ttshop-modal-confirm">
                <header class="ttshop-modal-header">
                    <h3>{{ confirmModalTitle }}</h3>
                    <button class="ttshop-btn-close" type="button" @click="closeConfirmModal">×</button>
                </header>
                <div class="ttshop-modal-body">
                    <p class="ttshop-confirm-message">{{ confirmModalMessage }}</p>
                    <div class="ttshop-modal-actions ttshop-confirm-actions">
                        <button class="ttshop-btn-outline" type="button" @click="closeConfirmModal">Hủy</button>
                        <button class="ttshop-btn-primary" type="button" @click="handleConfirmAction">Xác nhận</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Cancel Order Modal -->
        <div class="ttshop-modal-backdrop" :class="{open: showCancelModal}" v-if="showCancelModal" @click.self="closeCancelModal">
            <div class="ttshop-modal-panel ttshop-modal-confirm">
                <header class="ttshop-modal-header">
                    <h3>Xác nhận hủy đơn hàng</h3>
                    <button class="ttshop-btn-close" type="button" @click="closeCancelModal">×</button>
                </header>
                <div class="ttshop-modal-body">
                    <p class="ttshop-confirm-message">Bạn có chắc muốn hủy đơn hàng #{{ data?.order?.id }}?</p>
                    <p class="ttshop-confirm-warning">Hành động này không thể hoàn tác.</p>
                    <div class="ttshop-modal-actions ttshop-confirm-actions">
                        <button class="ttshop-btn-outline" type="button" @click="closeCancelModal" :disabled="cancellingOrderId">
                            Đóng
                        </button>
                        <button class="ttshop-btn-danger" type="button" @click="confirmCancelOrder" :disabled="cancellingOrderId">
                            {{ cancellingOrderId ? "Đang hủy..." : "Xác nhận hủy" }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* TikTok Shop Style - Order Detail Page */

/* Layout */
.ttshop-layout {
    min-height: calc(100vh - 64px);
    background: #F9FAFB;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    padding: 24px;
}

.ttshop-main {
    max-width: 1400px;
    margin: 0 auto;
}

.ttshop-container {
    display: flex;
    gap: 24px;
    align-items: flex-start;
}

/* Left Column (65%) */
.ttshop-left-col {
    flex: 0 0 65%;
    overflow-y: auto;
    max-height: calc(100vh - 112px);
    padding-right: 8px;
}

.ttshop-left-col::-webkit-scrollbar {
    width: 4px;
}

.ttshop-left-col::-webkit-scrollbar-track {
    background: transparent;
}

.ttshop-left-col::-webkit-scrollbar-thumb {
    background: #E5E7EB;
    border-radius: 2px;
}

.ttshop-left-col::-webkit-scrollbar-thumb:hover {
    background: #D1D5DB;
}

/* Right Column (35%) */
.ttshop-right-col {
    flex: 0 0 35%;
    min-width: 360px;
    max-width: 450px;
    position: sticky;
    top: 24px;
}

.ttshop-sidebar {
    display: flex;
    flex-direction: column;
    background: #FFFFFF;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    overflow: hidden;
    max-height: calc(100vh - 112px);
    border: 1px solid #F3F4F6;
}

/* Page Header */
.ttshop-page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
}

.ttshop-page-title {
    font-size: 28px;
    font-weight: 700;
    color: #111827;
    margin: 0;
    letter-spacing: -0.5px;
}

.ttshop-order-code {
    font-family: 'SF Mono', Monaco, monospace;
    font-size: 14px;
    color: #6B7280;
    background: #F3F4F6;
    padding: 6px 12px;
    border-radius: 8px;
    font-weight: 500;
}

/* Products Table - Cart Style */
.ttshop-products-table {
    background: #FFFFFF;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid #F3F4F6;
    overflow: hidden;
    margin-bottom: 24px;
}

.ttshop-table {
    width: 100%;
    border-collapse: collapse;
}

.ttshop-table thead {
    background: #FAFAFA;
    border-bottom: 1px solid #F3F4F6;
}

.ttshop-table th {
    padding: 16px 12px;
    font-size: 13px;
    font-weight: 600;
    color: #6B7280;
    text-align: left;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.ttshop-table th.col-product { width: 35%; }
.ttshop-table th.col-price { width: 15%; text-align: right; }
.ttshop-table th.col-discount { width: 15%; text-align: right; }
.ttshop-table th.col-qty { width: 15%; text-align: center; }
.ttshop-table th.col-size { width: 10%; text-align: center; }
.ttshop-table th.col-action { width: 25%; text-align: right; }

.ttshop-table tbody tr {
    border-bottom: 1px solid #F9FAFB;
    transition: background 0.2s;
}

.ttshop-table tbody tr:hover {
    background: #FAFAFA;
}

.ttshop-table tbody tr:last-child {
    border-bottom: none;
}

.ttshop-table td {
    padding: 16px 12px;
    vertical-align: middle;
}

.ttshop-table-product {
    display: flex;
    align-items: center;
    gap: 12px;
}

.ttshop-table-img {
    width: 64px;
    height: 64px;
    border-radius: 10px;
    overflow: hidden;
    background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #9CA3AF;
    flex-shrink: 0;
}

.ttshop-table-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.ttshop-table-name {
    font-size: 14px;
    font-weight: 600;
    color: #111827;
    line-height: 1.4;
}

.ttshop-table-price,
.ttshop-table-discount,
.ttshop-table-size {
    font-size: 14px;
    font-weight: 600;
    color: #111827;
    text-align: right;
}

.ttshop-table-discount {
    color: #DC2626;
}

/* Quantity Control */
.ttshop-qty-control {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    padding: 4px;
    background: #FFFFFF;
}

.ttshop-qty-btn {
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 6px;
    background: #F3F4F6;
    color: #374151;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
}

.ttshop-qty-btn:hover:not(:disabled) {
    background: #E5E7EB;
}

.ttshop-qty-btn:disabled {
    opacity: 0.4;
    cursor: not-allowed;
}

.ttshop-qty-value {
    min-width: 32px;
    text-align: center;
    font-size: 14px;
    font-weight: 600;
    color: #111827;
}

.ttshop-qty-static {
    font-size: 14px;
    font-weight: 600;
    color: #6B7280;
}

/* Row Actions */
.ttshop-row-actions {
    display: flex;
    gap: 6px;
    justify-content: flex-end;
    flex-wrap: nowrap;
}

.ttshop-row-btn {
    padding: 6px 12px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    white-space: nowrap;
}

.ttshop-row-contact {
    background: #F3F4F6;
    color: #4B5563;
}

.ttshop-row-contact:hover {
    background: #E5E7EB;
}

.ttshop-row-delete {
    background: #FEE2E2;
    color: #DC2626;
}

.ttshop-row-delete:hover {
    background: #FECACA;
}

.ttshop-row-exchange {
    background: #ECFDF5;
    color: #059669;
}

.ttshop-row-exchange:hover {
    background: #D1FAE5;
}

/* Card - Enhanced */
.ttshop-card {
    background: #FFFFFF;
    border-radius: 16px;
    padding: 28px 32px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    margin-bottom: 24px;
    border: 1px solid #F3F4F6;
}

.ttshop-card-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 20px;
}

.ttshop-card-row-2col {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 32px;
    align-items: start;
}

.ttshop-divider {
    height: 1px;
    background: linear-gradient(to right, transparent, #E5E7EB, transparent);
    margin: 24px 0;
}

.ttshop-divider-dashed {
    height: 0;
    border-top: 1px dashed #E5E7EB;
    margin: 20px 0;
}

/* Info Blocks - Enhanced */
.ttshop-info-block {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.ttshop-info-label {
    font-size: 11px;
    font-weight: 600;
    color: #9CA3AF;
    text-transform: uppercase;
    letter-spacing: 0.8px;
}

.ttshop-info-value {
    font-size: 15px;
    font-weight: 600;
    color: #111827;
    line-height: 1.5;
}

.ttshop-info-full {
    grid-column: 1;
}

.ttshop-highlight {
    color: #059669;
    font-weight: 700;
    font-size: 15px;
}

/* Status Badge - Premium */
.ttshop-status-badge {
    display: inline-flex;
    align-items: center;
    padding: 10px 20px;
    border-radius: 24px;
    font-size: 13px;
    font-weight: 700;
    white-space: nowrap;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.status-pending {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    color: #92400E;
    border: 1px solid #FCD34D;
}

.status-placed {
    background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
    color: #1E40AF;
    border: 1px solid #93C5FD;
}

.status-shipping {
    background: linear-gradient(135deg, #E0E7FF 0%, #C7D2FE 100%);
    color: #3730A3;
    border: 1px solid #A5B4FC;
}

.status-delivered {
    background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
    color: #065F46;
    border: 1px solid #6EE7B7;
}

.status-cancelled {
    background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
    color: #991B1B;
    border: 1px solid #F87171;
}

.status-refund {
    background: linear-gradient(135deg, #F3E8FF 0%, #E9D5FF 100%);
    color: #6B21A8;
    border: 1px solid #C084FC;
}

/* Update Button - Refined */
.ttshop-card-actions {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #F3F4F6;
}

.ttshop-btn-update {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 14px 24px;
    border: 1px solid #E5E7EB;
    border-radius: 12px;
    background: #FFFFFF;
    color: #4B5563;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.ttshop-btn-update:hover {
    border-color: #9CA3AF;
    background: #F9FAFB;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.ttshop-btn-update:active {
    transform: translateY(0);
}

/* Sidebar Info & Summary Sections */
.ttshop-info-section,
.ttshop-summary-section {
    padding: 24px;
}

.ttshop-info-section .ttshop-section-title,
.ttshop-summary-section .ttshop-section-title {
    font-size: 16px;
    font-weight: 700;
    color: #111827;
    margin: 0 0 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #F3F4F6;
}

.ttshop-info-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
    gap: 12px;
}

.ttshop-info-row .ttshop-info-label {
    font-size: 13px;
    color: #6B7280;
    font-weight: 500;
    text-transform: none;
    letter-spacing: 0;
}

.ttshop-info-row .ttshop-info-value {
    font-size: 13px;
    font-weight: 600;
    color: #111827;
    text-align: right;
    flex: 1;
}

.ttshop-info-section .ttshop-btn-update {
    width: 100%;
    margin-top: 16px;
    justify-content: center;
}

.ttshop-summary-section .ttshop-summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-size: 14px;
}

.ttshop-summary-section .ttshop-summary-row strong {
    font-weight: 600;
    color: #111827;
}

.ttshop-summary-section .ttshop-summary-row.ttshop-total {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed #E5E7EB;
    font-size: 16px;
}

.ttshop-summary-section .ttshop-total-price {
    color: #DC2626;
    font-size: 20px;
    font-weight: 800;
}

.ttshop-discount {
    color: #DC2626;
}

.ttshop-sidebar-actions {
    padding: 20px 24px 24px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.ttshop-btn-clear {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 16px;
    border: 1px solid #FEE2E2;
    border-radius: 10px;
    background: #FFFFFF;
    color: #DC2626;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.ttshop-btn-clear:hover {
    background: #FEE2E2;
}

/* Sidebar Header - Premium */
.ttshop-sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    border-bottom: 1px solid #F3F4F6;
    flex-shrink: 0;
    background: linear-gradient(to right, #FFFFFF, #FAFAFA);
}

.ttshop-sidebar-title {
    font-size: 17px;
    font-weight: 700;
    color: #111827;
    margin: 0;
    letter-spacing: -0.3px;
}

.ttshop-clear-all {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: none;
    border: none;
    color: #9CA3AF;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s ease;
    padding: 6px 10px;
    border-radius: 8px;
}

.ttshop-clear-all:hover {
    color: #DC2626;
    background: #FEE2E2;
    transform: scale(1.02);
}

/* Product Scroll Area - Premium */
.ttshop-product-scroll {
    flex: 1;
    overflow-y: auto;
    padding: 8px 24px;
    max-height: 480px;
}

.ttshop-product-scroll::-webkit-scrollbar {
    width: 3px;
}

.ttshop-product-scroll::-webkit-scrollbar-track {
    background: transparent;
    margin: 8px 0;
}

.ttshop-product-scroll::-webkit-scrollbar-thumb {
    background: #E5E7EB;
    border-radius: 2px;
}

.ttshop-product-scroll::-webkit-scrollbar-thumb:hover {
    background: #D1D5DB;
}

/* Product Item with Animation */
.ttshop-product-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 0;
    border-bottom: 1px solid #F9FAFB;
    transition: all 0.3s ease;
    animation: fadeInUp 0.4s ease forwards;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(8px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.ttshop-product-item:last-child {
    border-bottom: none;
}

.ttshop-product-item:hover {
    background: #FAFAFA;
    margin: 0 -12px;
    padding: 14px 12px;
    border-radius: 8px;
}

/* Empty State */
.ttshop-empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 24px;
    text-align: center;
    color: #9CA3AF;
}

.ttshop-empty-state svg {
    width: 48px;
    height: 48px;
    margin-bottom: 16px;
    color: #D1D5DB;
}

.ttshop-empty-state p {
    font-size: 14px;
    font-weight: 500;
    margin: 0;
    line-height: 1.5;
}

/* Item Removing Animation */
.ttshop-item-removing {
    animation: fadeOutSlide 0.3s ease forwards !important;
    pointer-events: none;
}

@keyframes fadeOutSlide {
    0% {
        opacity: 1;
        transform: translateX(0);
    }
    100% {
        opacity: 0;
        transform: translateX(-20px);
        height: 0;
        padding: 0;
        margin: 0;
    }
}

.ttshop-product-img {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    overflow: hidden;
    flex-shrink: 0;
    background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #9CA3AF;
    font-weight: 500;
}

.ttshop-product-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.ttshop-product-details {
    flex: 1;
    min-width: 0;
}

.ttshop-product-name {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #111827;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 6px;
}

.ttshop-product-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #6B7280;
    font-weight: 500;
}

/* Product Action Buttons */
.ttshop-product-actions {
    display: flex;
    gap: 8px;
    margin-top: 8px;
    opacity: 0;
    transform: translateY(-4px);
    transition: all 0.25s ease;
}

.ttshop-product-item:hover .ttshop-product-actions {
    opacity: 1;
    transform: translateY(0);
}

.ttshop-btn-action {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    border: none;
    background: transparent;
}

.ttshop-btn-exchange {
    color: #059669;
    background: #ECFDF5;
}

.ttshop-btn-exchange:hover {
    background: #D1FAE5;
    transform: scale(1.05);
}

.ttshop-btn-delete {
    color: #DC2626;
    background: #FEE2E2;
}

.ttshop-btn-delete:hover {
    background: #FECACA;
    transform: scale(1.05);
}

.ttshop-btn-contact {
    color: #4B5563;
    background: #F3F4F6;
}

.ttshop-btn-contact:hover {
    background: #E5E7EB;
    transform: scale(1.05);
}

.ttshop-product-price {
    font-size: 15px;
    font-weight: 800;
    color: #DC2626;
    flex-shrink: 0;
    text-align: right;
}

/* Summary - Premium */
.ttshop-summary {
    padding: 24px;
    background: linear-gradient(to bottom, #FFFFFF, #FAFAFA);
    border-top: 1px solid #F3F4F6;
    flex-shrink: 0;
}

.ttshop-summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-size: 14px;
    color: #6B7280;
    font-weight: 500;
}

.ttshop-summary-row.ttshop-total {
    font-size: 16px;
    font-weight: 700;
    color: #111827;
    margin-top: 16px;
    margin-bottom: 20px;
    padding-top: 16px;
    border-top: 1px dashed #E5E7EB;
}

.ttshop-free {
    color: #059669;
    font-weight: 600;
    background: #ECFDF5;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 13px;
}

.ttshop-total-price {
    color: #DC2626;
    font-size: 22px;
    font-weight: 800;
    letter-spacing: -0.5px;
}

/* Payment Button - Premium */
.ttshop-btn-payment {
    width: 100%;
    padding: 18px;
    background: linear-gradient(135deg, #111827 0%, #1F2937 100%);
    color: #FFFFFF;
    border: none;
    border-radius: 14px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    text-transform: uppercase;
    letter-spacing: 0.8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.ttshop-btn-payment:hover {
    background: linear-gradient(135deg, #1F2937 0%, #374151 100%);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.ttshop-btn-payment:active {
    transform: translateY(-1px);
}

/* Action Buttons Container */
.ttshop-action-buttons {
    display: flex;
    gap: 10px;
    width: 100%;
}

/* Cancel Button */
.ttshop-btn-cancel {
    flex: 1;
    padding: 18px;
    background: #FFFFFF;
    border: 2px solid #DC2626;
    border-radius: 12px;
    color: #DC2626;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    box-shadow: 0 2px 4px rgba(220, 38, 38, 0.1);
}

.ttshop-btn-cancel:hover:not(:disabled) {
    background: #DC2626;
    color: #FFFFFF;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.ttshop-btn-cancel:active:not(:disabled) {
    transform: translateY(-1px);
}

.ttshop-btn-cancel:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none !important;
}

/* Danger Button for Modal */
.ttshop-btn-danger {
    padding: 12px 20px;
    background: #DC2626;
    border: 1px solid #DC2626;
    border-radius: 8px;
    color: #FFFFFF;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.ttshop-btn-danger:hover:not(:disabled) {
    background: #B91C1C;
    border-color: #B91C1C;
}

.ttshop-btn-danger:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Warning Text in Modal */
.ttshop-confirm-warning {
    color: #DC2626 !important;
    font-weight: 600;
    font-size: 13px;
    margin-top: 8px;
}

.ttshop-btn-back {
    width: 100%;
    padding: 18px;
    background: #F3F4F6;
    color: #4B5563;
    border: none;
    border-radius: 14px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s ease;
}

.ttshop-btn-back:hover {
    background: #E5E7EB;
    transform: translateY(-1px);
}

/* Review Section */
.ttshop-section {
    margin-top: 24px;
}

.ttshop-section-title {
    font-size: 18px;
    font-weight: 700;
    color: #111827;
    margin: 0 0 16px;
}

.ttshop-review-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.ttshop-review-card {
    background: #FFFFFF;
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.ttshop-review-product {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
}

.ttshop-review-img {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    object-fit: cover;
}

.ttshop-review-info {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.ttshop-review-name {
    font-size: 14px;
    font-weight: 600;
    color: #111827;
}

.ttshop-review-meta {
    font-size: 12px;
    color: #6B7280;
    margin-top: 2px;
}

.ttshop-review-form {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.ttshop-star-rating {
    display: flex;
    gap: 4px;
}

.ttshop-star-btn {
    background: none;
    border: none;
    font-size: 22px;
    color: #D1D5DB;
    cursor: pointer;
    padding: 0;
    line-height: 1;
    transition: color 0.2s;
}

.ttshop-star-btn.active,
.ttshop-star-btn:hover {
    color: #FBBF24;
}

.ttshop-review-input {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    font-size: 14px;
    resize: vertical;
    font-family: inherit;
    min-height: 60px;
}

.ttshop-review-input:focus {
    outline: none;
    border-color: #9CA3AF;
}

.ttshop-review-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.ttshop-file-label {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    border: 1px dashed #D1D5DB;
    border-radius: 6px;
    cursor: pointer;
    color: #6B7280;
    font-size: 13px;
    transition: all 0.2s;
}

.ttshop-file-label:hover {
    border-color: #9CA3AF;
    color: #374151;
}

.ttshop-file-label input {
    display: none;
}

.ttshop-btn-submit {
    padding: 8px 16px;
    background: #111827;
    color: #FFFFFF;
    border: none;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
}

.ttshop-btn-submit:hover {
    background: #1F2937;
}

.ttshop-reviewed-badge {
    background: #D1FAE5;
    color: #065F46;
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
}

/* Alerts */
.ttshop-alert {
    padding: 12px 16px;
    border-radius: 10px;
    margin-bottom: 16px;
    font-size: 14px;
    font-weight: 500;
}

.ttshop-alert-error {
    background: #FEE2E2;
    border: 1px solid #FECACA;
    color: #991B1B;
}

.ttshop-alert-success {
    background: #D1FAE5;
    border: 1px solid #A7F3D0;
    color: #065F46;
}

/* Modals */
.ttshop-modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s;
    z-index: 1000;
}

.ttshop-modal-backdrop.open {
    opacity: 1;
    visibility: visible;
}

.ttshop-modal-panel {
    background: #FFFFFF;
    border-radius: 16px;
    width: 100%;
    max-width: 500px;
    max-height: 90vh;
    overflow: hidden;
    transform: scale(0.95);
    transition: transform 0.3s;
}

.ttshop-modal-backdrop.open .ttshop-modal-panel {
    transform: scale(1);
}

.ttshop-modal-large {
    max-width: 900px;
}

.ttshop-modal-image {
    max-width: 500px;
    position: relative;
    padding: 0;
}

.ttshop-modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #E5E7EB;
}

.ttshop-modal-header h3 {
    font-size: 18px;
    font-weight: 700;
    color: #111827;
    margin: 0;
}

.ttshop-btn-close {
    background: none;
    border: none;
    font-size: 24px;
    color: #9CA3AF;
    cursor: pointer;
    padding: 0;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    transition: all 0.2s;
}

.ttshop-btn-close:hover {
    background: #F3F4F6;
    color: #374151;
}

.ttshop-modal-body {
    padding: 24px;
    overflow-y: auto;
    max-height: calc(90vh - 80px);
}

.ttshop-modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #E5E7EB;
}

.ttshop-modal-confirm {
    max-width: 420px;
}

.ttshop-confirm-message {
    font-size: 15px;
    line-height: 1.6;
    color: #4B5563;
    margin: 0;
    white-space: pre-line;
}

.ttshop-confirm-actions {
    justify-content: flex-end;
}

/* Form Elements */
.ttshop-form-group {
    margin-bottom: 20px;
}

.ttshop-form-label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #374151;
}

.ttshop-form-input {
    width: 100%;
    padding: 12px 14px;
    border: 1px solid #D1D5DB;
    border-radius: 10px;
    font-size: 14px;
    transition: all 0.2s;
    font-family: inherit;
}

.ttshop-form-input:focus {
    outline: none;
    border-color: #9CA3AF;
    box-shadow: 0 0 0 3px rgba(156, 163, 175, 0.1);
}

/* Filter Bar */
.ttshop-filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    flex-wrap: wrap;
}

.ttshop-filter-input,
.ttshop-filter-select {
    padding: 10px 14px;
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    font-size: 14px;
    background: #FFFFFF;
    font-family: inherit;
}

.ttshop-filter-input:focus,
.ttshop-filter-select:focus {
    outline: none;
    border-color: #9CA3AF;
}

.ttshop-filter-price {
    width: 120px;
}

.ttshop-filter-qty {
    width: 70px;
}

/* Exchange List */
.ttshop-exchange-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.ttshop-exchange-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border: 1px solid #E5E7EB;
    border-radius: 12px;
    gap: 16px;
}

.ttshop-exchange-info {
    flex: 1;
}

.ttshop-exchange-name {
    font-weight: 600;
    color: #111827;
    margin-bottom: 4px;
}

.ttshop-exchange-price {
    display: flex;
    gap: 8px;
    font-size: 13px;
}

.ttshop-price-old {
    color: #9CA3AF;
    text-decoration: line-through;
}

.ttshop-price-new {
    color: #DC2626;
    font-weight: 700;
}

.ttshop-exchange-actions {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

.ttshop-exchange-total {
    font-weight: 700;
    min-width: 80px;
    text-align: right;
    color: #111827;
}

.ttshop-exchange-empty {
    text-align: center;
    padding: 40px;
    color: #6B7280;
}

.ttshop-pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    margin-top: 20px;
}

.ttshop-page-info {
    font-size: 14px;
    color: #6B7280;
}

.ttshop-preview-img {
    width: 100%;
    height: auto;
    border-radius: 12px;
}

/* Buttons */
.ttshop-btn-primary {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px 18px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    background: #111827;
    color: #FFFFFF;
}

.ttshop-btn-primary:hover:not(:disabled) {
    background: #1F2937;
}

.ttshop-btn-outline {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px 18px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    background: #FFFFFF;
    color: #374151;
    border: 1px solid #D1D5DB;
}

.ttshop-btn-outline:hover {
    border-color: #9CA3AF;
    background: #F9FAFB;
}

.ttshop-btn-outline:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.ttshop-btn-sm {
    padding: 6px 12px;
    font-size: 13px;
}

/* Responsive */
@media (max-width: 1024px) {
    .ttshop-container {
        flex-direction: column;
    }
    
    .ttshop-left-col,
    .ttshop-right-col {
        flex: 1;
        width: 100%;
        max-width: none;
        position: static;
    }
    
    .ttshop-sidebar {
        max-height: none;
    }
    
    .ttshop-product-scroll {
        max-height: 400px;
    }
}

@media (max-width: 640px) {
    .ttshop-layout {
        padding: 16px;
    }
    
    .ttshop-page-title {
        font-size: 22px;
    }
    
    .ttshop-card-row-2col {
        grid-template-columns: 1fr;
    }
    
    .ttshop-exchange-item {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .ttshop-exchange-actions {
        width: 100%;
        justify-content: flex-start;
    }
    
    .ttshop-filter-bar {
        flex-direction: column;
    }
    
    .ttshop-filter-input,
    .ttshop-filter-select {
        width: 100%;
    }
}
</style>
