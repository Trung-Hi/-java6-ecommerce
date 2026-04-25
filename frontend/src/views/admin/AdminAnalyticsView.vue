<script setup>
import {ref, onMounted, onUnmounted} from "vue";
import {api} from "@/api";
import AdminLayout from "@/components/AdminLayout.vue";
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const loading = ref(false);
const error = ref("");
const summary = ref({
    totalRevenue: 0,
    totalOrders: 0,
    totalCustomers: 0,
    totalProducts: 0
});
const revenueTrend = ref([]);
const topProducts = ref([]);
const orderStatus = ref([]);

let revenueChart = null;
let productChart = null;
let statusChart = null;

const money = (value) => Number(value || 0).toLocaleString("vi-VN");

const loadSummary = async () => {
    try {
        const response = await api.admin.analytics.getSummary();
        summary.value = response.data;
    } catch (err) {
        console.error("Error loading summary:", err);
        error.value = "Không thể tải tóm tắt thống kê";
    }
};

const loadRevenueTrend = async () => {
    try {
        const response = await api.admin.analytics.getRevenueTrend();
        revenueTrend.value = response.data;
        if (import.meta.env.DEV) console.log("Revenue trend data:", revenueTrend.value);
        setTimeout(() => renderRevenueChart(), 100);
    } catch (err) {
        console.error("Error loading revenue trend:", err);
        error.value = "Không thể tải xu hướng doanh thu";
    }
};

const loadTopProducts = async () => {
    try {
        const response = await api.admin.analytics.getTopProducts();
        topProducts.value = response.data;
        if (import.meta.env.DEV) console.log("Top products data:", topProducts.value);
        setTimeout(() => renderProductChart(), 100);
    } catch (err) {
        console.error("Error loading top products:", err);
        error.value = "Không thể tải top sản phẩm";
    }
};

const loadOrderStatus = async () => {
    try {
        const response = await api.admin.analytics.getOrderStatus();
        orderStatus.value = response.data;
        if (import.meta.env.DEV) console.log("Order status data:", orderStatus.value);
        setTimeout(() => renderStatusChart(), 100);
    } catch (err) {
        console.error("Error loading order status:", err);
        error.value = "Không thể tải trạng thái đơn hàng";
    }
};

const renderRevenueChart = () => {
    if (revenueChart) {
        revenueChart.destroy();
    }

    const ctx = document.getElementById("revenueChart");
    if (!ctx) return;

    const months = ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"];
    const data = Array(12).fill(0);
    revenueTrend.value.forEach(item => {
        if (item.month >= 1 && item.month <= 12) {
            data[item.month - 1] = item.revenue;
        }
    });

    revenueChart = new Chart(ctx, {
        type: "line",
        data: {
            labels: months,
            datasets: [{
                label: "Doanh thu",
                data: data,
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                borderColor: 'rgba(99, 102, 241, 1)',
                borderWidth: 2,
                pointBackgroundColor: 'rgba(99, 102, 241, 1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,0.05)' },
                    ticks: {
                        callback: function(value) {
                            return money(value);
                        }
                    }
                },
                x: {
                    grid: { display: false }
                }
            }
        }
    });
};

const renderProductChart = () => {
    if (productChart) {
        productChart.destroy();
    }

    const ctx = document.getElementById("productChart");
    if (!ctx) return;

    const labels = topProducts.value.map(p => p.name);
    const data = topProducts.value.map(p => p.totalSold);

    productChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: "Số lượng bán",
                data: data,
                backgroundColor: [
                    'rgba(99, 102, 241, 0.8)',
                    'rgba(16, 185, 129, 0.8)',
                    'rgba(245, 158, 11, 0.8)',
                    'rgba(239, 68, 68, 0.8)',
                    'rgba(59, 130, 246, 0.8)'
                ],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                x: {
                    grid: { display: false }
                }
            }
        }
    });
};

const renderStatusChart = () => {
    if (statusChart) {
        statusChart.destroy();
    }

    const ctx = document.getElementById("statusChart");
    if (!ctx) return;

    const statusLabels = {
        "CANCEL": "Đã hủy",
        "DELIVERED_SUCCESS": "Giao thành công",
        "DELIVERY_FAILED": "Giao thất bại",
        "PENDING_PAYMENT": "Chờ thanh toán",
        "PLACED_UNPAID": "Đã đặt hàng (chưa thanh toán)",
        "SHIPPING_UNPAID": "Đang giao (chưa thanh toán)"
    };

    const statusColorMap = {
        'DELIVERED': '#10B981',
        'DELIVERED_SUCCESS': '#10B981',
        'PLACED': '#3B82F6',
        'PLACED_UNPAID': '#3B82F6',
        'PENDING': '#F59E0B',
        'PENDING_PAYMENT': '#F59E0B',
        'CANCELLED': '#EF4444',
        'CANCEL': '#EF4444',
        'SHIPPING': '#6366F1',
        'SHIPPING_UNPAID': '#6366F1',
        'FAILED': '#6B7280',
        'DELIVERY_FAILED': '#6B7280'
    };

    const sortedData = [...orderStatus.value].sort((a, b) => b.count - a.count);
    const labels = sortedData.map(s => statusLabels[s.status] || s.status);
    const data = sortedData.map(s => s.count);
    const colors = sortedData.map(item => statusColorMap[item.status] || '#9CA3AF');

    statusChart = new Chart(ctx, {
        type: "pie",
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { padding: 20, usePointStyle: true }
                }
            }
        }
    });
};

const loadAllData = async () => {
    loading.value = true;
    error.value = "";
    try {
        await Promise.all([
            loadSummary(),
            loadRevenueTrend(),
            loadTopProducts(),
            loadOrderStatus()
        ]);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadAllData();
});

onUnmounted(() => {
    if (revenueChart) revenueChart.destroy();
    if (productChart) productChart.destroy();
    if (statusChart) statusChart.destroy();
});
</script>

<template>
    <AdminLayout>
        <div class="container-fluid py-4">
            <h2 class="mb-4">Thống kê & Phân tích</h2>

            <div v-if="error" class="alert alert-danger" role="alert">
                {{ error }}
            </div>

            <div v-if="loading" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Đang tải...</span>
                </div>
            </div>

            <div v-else>
                <!-- Summary Cards -->
                <div class="row mb-4">
                    <div class="col-md-3">
                        <div class="card shadow-sm h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-shrink-0">
                                        <i class="bi bi-currency-dollar fs-3 text-success"></i>
                                    </div>
                                    <div class="flex-grow-1 ms-3">
                                        <h6 class="card-subtitle mb-1 text-muted">Tổng doanh thu</h6>
                                        <h4 class="card-title mb-0">{{ money(summary?.totalRevenue) }} đ</h4>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card shadow-sm h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-shrink-0">
                                        <i class="bi bi-bag fs-3 text-primary"></i>
                                    </div>
                                    <div class="flex-grow-1 ms-3">
                                        <h6 class="card-subtitle mb-1 text-muted">Tổng đơn hàng</h6>
                                        <h4 class="card-title mb-0">{{ summary?.totalOrders }}</h4>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card shadow-sm h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-shrink-0">
                                        <i class="bi bi-people fs-3 text-info"></i>
                                    </div>
                                    <div class="flex-grow-1 ms-3">
                                        <h6 class="card-subtitle mb-1 text-muted">Tổng khách hàng</h6>
                                        <h4 class="card-title mb-0">{{ summary?.totalCustomers }}</h4>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card shadow-sm h-100">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="flex-shrink-0">
                                        <i class="bi bi-box fs-3 text-warning"></i>
                                    </div>
                                    <div class="flex-grow-1 ms-3">
                                        <h6 class="card-subtitle mb-1 text-muted">Tổng sản phẩm</h6>
                                        <h4 class="card-title mb-0">{{ summary?.totalProducts }}</h4>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Charts -->
                <div class="row">
                    <div class="col-md-8 mb-4">
                        <div class="card shadow-sm h-100">
                            <div class="card-header">
                                <h5 class="card-title mb-0">Doanh thu theo tháng</h5>
                            </div>
                            <div class="card-body">
                                <div style="height: 300px;">
                                    <canvas id="revenueChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4 mb-4">
                        <div class="card shadow-sm h-100">
                            <div class="card-header">
                                <h5 class="card-title mb-0">Tỷ lệ trạng thái đơn hàng</h5>
                            </div>
                            <div class="card-body">
                                <div style="height: 300px;">
                                    <canvas id="statusChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <div class="card shadow-sm">
                            <div class="card-header">
                                <h5 class="card-title mb-0">Top 5 sản phẩm bán chạy</h5>
                            </div>
                            <div class="card-body">
                                <div style="height: 300px;">
                                    <canvas id="productChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </AdminLayout>
</template>

<style scoped>
.card {
    border: none;
    border-radius: 8px;
}

.card-header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #dee2e6;
    font-weight: 500;
}
</style>
