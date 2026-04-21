<script setup>
import {computed, nextTick, onMounted, reactive, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import {api} from "@/api";
import AdminLayout from "@/components/AdminLayout.vue";

const route = useRoute();
const router = useRouter();
const revenueTableRef = ref(null);
const rows = ref([]);
const loading = ref(false);
const exporting = ref(false);
const error = ref("");
const now = new Date();
const currentYear = now.getFullYear();
const currentMonth = now.getMonth() + 1;
const currentQuarter = Math.floor((currentMonth - 1) / 3) + 1;
const summaryParams = reactive({
    fromDate: "",
    toDate: "",
    sortField: "orderId",
    sortDir: "asc"
});
const periodParams = reactive({
    day: formatDateInput(now),
    week: formatWeekInput(now),
    month: currentMonth,
    quarter: currentQuarter,
    year: currentYear,
    sortField: "orderId",
    sortDir: "asc"
});
const months = Array.from({length: 12}, (_, i) => i + 1);
const quarters = [1, 2, 3, 4];
const money = (value) => Number(value || 0).toLocaleString("vi-VN");
const viewMode = computed(() => route.meta.revenueView || "summary");
const isSummaryMode = computed(() => viewMode.value === "summary");
const isDayMode = computed(() => viewMode.value === "day");
const showLineChart = computed(() => !isSummaryMode.value && !isDayMode.value);
const total = computed(() => rows.value.reduce((sum, row) => sum + Number(row.lineTotal || 0), 0));
const activeRange = computed(() => {
    if (isSummaryMode.value) {
        return {fromDate: summaryParams.fromDate, toDate: summaryParams.toDate};
    }
    return buildRange(viewMode.value, periodParams);
});
const chartPalette = ["#1a1a1a", "#404040", "#666666", "#8c8c8c", "#b3b3b3", "#d9d9d9", "#e6e6e6", "#f2f2f2"];
const categoryBreakdown = computed(() => {
    const map = new Map();
    for (const row of rows.value) {
        const key = (row.categoryName || "Khác").trim() || "Khác";
        map.set(key, (map.get(key) || 0) + Number(row.lineTotal || 0));
    }
    const list = Array.from(map.entries()).map(([name, amount], index) => ({
        name,
        amount,
        color: chartPalette[index % chartPalette.length]
    }));
    return list.sort((a, b) => b.amount - a.amount);
});
const pieTotal = computed(() => categoryBreakdown.value.reduce((sum, item) => sum + item.amount, 0));
const hoveredSliceName = ref("");
const pieTooltip = reactive({
    visible: false,
    text: "",
    x: 0,
    y: 0
});
const pieSlices = computed(() => {
    const totalValue = pieTotal.value;
    if (!totalValue) {
        return [];
    }
    let acc = 0;
    return categoryBreakdown.value.map((item) => {
        const ratio = item.amount / totalValue;
        const startAngle = acc * 360;
        const endAngle = (acc + ratio) * 360;
        acc += ratio;
        return {
            ...item,
            ratio,
            path: describePieArc(100, 100, 86, startAngle, endAngle)
        };
    });
});
const lineSeries = computed(() => {
    if (!showLineChart.value) {
        return [];
    }
    const map = new Map();
    for (const row of rows.value) {
        const date = parseDate(row.orderCreateDate);
        if (!date) {
            continue;
        }
        if (viewMode.value === "quarter" || viewMode.value === "year") {
            const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
            map.set(monthKey, (map.get(monthKey) || 0) + Number(row.lineTotal || 0));
        } else {
            const dayKey = formatDateInput(date);
            map.set(dayKey, (map.get(dayKey) || 0) + Number(row.lineTotal || 0));
        }
    }
    const labels = buildLineLabels(viewMode.value, activeRange.value);
    return labels.map((item) => ({
        key: item.key,
        label: item.label,
        value: map.get(item.key) || 0
    }));
});
const lineChart = computed(() => {
    const data = lineSeries.value;
    if (!data.length) {
        return {points: "", circles: [], labels: []};
    }
    const width = 760;
    const height = 280;
    const paddingLeft = 56;
    const paddingRight = 24;
    const paddingTop = 24;
    const paddingBottom = 48;
    const innerWidth = width - paddingLeft - paddingRight;
    const innerHeight = height - paddingTop - paddingBottom;
    const maxValue = Math.max(...data.map((item) => item.value), 1);
    const stepX = data.length > 1 ? innerWidth / (data.length - 1) : 0;
    const circles = data.map((item, index) => {
        const x = paddingLeft + stepX * index;
        const y = paddingTop + (1 - item.value / maxValue) * innerHeight;
        return {x, y, ...item};
    });
    const points = circles.map((item) => `${item.x},${item.y}`).join(" ");
    const labels = circles.map((item, index) => ({
        x: item.x,
        y: height - 22,
        text: shouldShowLineLabel(index, circles.length) ? item.label : ""
    }));
    const yTicks = buildYAxisTicks(maxValue, 5).map((tick) => ({
        value: tick,
        y: paddingTop + (1 - tick / maxValue) * innerHeight
    }));
    const xAxisTitle = axisLabelByMode(viewMode.value);
    return {points, circles, labels, yTicks, xAxisTitle};
});
const chartTitle = computed(() => {
    if (viewMode.value === "week") {
        return "Biểu đồ doanh thu theo ngày trong tuần";
    }
    if (viewMode.value === "month") {
        return "Biểu đồ doanh thu theo ngày trong tháng";
    }
    if (viewMode.value === "quarter") {
        return "Biểu đồ doanh thu theo tháng trong quý";
    }
    return "Biểu đồ doanh thu theo tháng trong năm";
});
const modeTitle = computed(() => {
    if (viewMode.value === "day") {
        return "Doanh thu theo ngày";
    }
    if (viewMode.value === "week") {
        return "Doanh thu theo tuần";
    }
    if (viewMode.value === "month") {
        return "Doanh thu theo tháng";
    }
    if (viewMode.value === "quarter") {
        return "Doanh thu theo quý";
    }
    if (viewMode.value === "year") {
        return "Doanh thu theo năm";
    }
    return "Doanh thu theo đơn hàng";
});
const revenueTabs = [
    {mode: "summary", label: "Tổng quan", to: "/admin/revenue"},
    {mode: "day", label: "Ngày", to: "/admin/revenue/day"},
    {mode: "week", label: "Tuần", to: "/admin/revenue/week"},
    {mode: "month", label: "Tháng", to: "/admin/revenue/month"},
    {mode: "quarter", label: "Quý", to: "/admin/revenue/quarter"},
    {mode: "year", label: "Năm", to: "/admin/revenue/year"}
];
const openRevenueTab = async (to) => {
    if (!to || route.path === to) {
        return;
    }
    await router.push(to);
};
const scrollToRevenueTable = async () => {
    await nextTick();
    revenueTableRef.value?.scrollIntoView({behavior: "smooth", block: "start"});
};
const load = async () => {
    loading.value = true;
    error.value = "";
    try {
        const payload = (await api.admin.reports.revenue({
            fromDate: activeRange.value.fromDate,
            toDate: activeRange.value.toDate,
            sortField: isSummaryMode.value ? summaryParams.sortField : periodParams.sortField,
            sortDir: isSummaryMode.value ? summaryParams.sortDir : periodParams.sortDir
        })).data || {};
        rows.value = payload.rows || [];
    } catch (e) {
        rows.value = [];
        error.value = e.message || "Không tải được dữ liệu doanh thu";
    } finally {
        loading.value = false;
    }
};
const applyFilters = async () => {
    await load();
};
const exportExcel = async () => {
    exporting.value = true;
    try {
        const params = new URLSearchParams();
        if (activeRange.value.fromDate) params.append("fromDate", activeRange.value.fromDate);
        if (activeRange.value.toDate) params.append("toDate", activeRange.value.toDate);
        params.append("sortField", isSummaryMode.value ? summaryParams.sortField : periodParams.sortField);
        params.append("sortDir", isSummaryMode.value ? summaryParams.sortDir : periodParams.sortDir);
        params.append("mode", viewMode.value);
        params.append("format", "xlsx");
        const response = await fetch(`/api/admin/reports/revenue/export?${params.toString()}`, {
            method: "GET",
            credentials: "include"
        });
        if (!response.ok) {
            let message = "Không thể xuất file Excel";
            try {
                const payload = await response.json();
                message = payload?.message || message;
            } catch (e) {
            }
            throw new Error(message);
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        const cd = response.headers.get("content-disposition") || "";
        const match = cd.match(/filename="?([^";]+)"?/i);
        link.download = match?.[1] || `doanh-thu-${viewMode.value}.xlsx`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
    } catch (e) {
        error.value = e.message || "Không thể xuất file Excel";
    } finally {
        exporting.value = false;
    }
};
const clearSummaryFilters = async () => {
    summaryParams.fromDate = "";
    summaryParams.toDate = "";
    summaryParams.sortField = "orderId";
    summaryParams.sortDir = "asc";
    await load();
};
const clearPeriodFilters = async () => {
    const today = new Date();
    periodParams.day = formatDateInput(today);
    periodParams.week = formatWeekInput(today);
    periodParams.month = today.getMonth() + 1;
    periodParams.quarter = Math.floor(today.getMonth() / 3) + 1;
    periodParams.year = today.getFullYear();
    periodParams.sortField = "orderId";
    periodParams.sortDir = "asc";
    await load();
};
onMounted(load);
watch(() => route.fullPath, load);

function formatDateInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}
function parseDate(text) {
    if (!text) {
        return null;
    }
    const date = new Date(text);
    if (Number.isNaN(date.getTime())) {
        return null;
    }
    return date;
}
function buildRange(periodValue, state) {
    const today = new Date();
    if (periodValue === "day") {
        return {fromDate: state.day, toDate: state.day};
    }
    if (periodValue === "week") {
        const {start, end} = getWeekRangeFromInput(state.week);
        return {fromDate: formatDateInput(start), toDate: formatDateInput(end)};
    }
    if (periodValue === "month") {
        const from = new Date(state.year, Number(state.month) - 1, 1);
        const to = new Date(state.year, Number(state.month), 0);
        return {fromDate: formatDateInput(from), toDate: formatDateInput(to)};
    }
    if (periodValue === "quarter") {
        const startMonth = (Number(state.quarter) - 1) * 3;
        const from = new Date(state.year, startMonth, 1);
        const to = new Date(state.year, startMonth + 3, 0);
        return {fromDate: formatDateInput(from), toDate: formatDateInput(to)};
    }
    return {
        fromDate: `${state.year}-01-01`,
        toDate: `${state.year}-12-31`
    };
}
function startOfWeek(date) {
    const clone = new Date(date);
    const day = clone.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    clone.setDate(clone.getDate() + diff);
    clone.setHours(0, 0, 0, 0);
    return clone;
}
function formatWeekInput(date) {
    const monday = startOfWeek(date);
    const thursday = new Date(monday);
    thursday.setDate(monday.getDate() + 3);
    const weekYear = thursday.getFullYear();
    const jan4 = new Date(weekYear, 0, 4);
    const firstWeekMonday = startOfWeek(jan4);
    const diffDays = Math.round((monday.getTime() - firstWeekMonday.getTime()) / 86400000);
    const weekNumber = 1 + Math.floor(diffDays / 7);
    return `${weekYear}-W${String(weekNumber).padStart(2, "0")}`;
}
function getWeekRangeFromInput(value) {
    const match = /^(\d{4})-W(\d{2})$/.exec(value || "");
    if (!match) {
        const start = startOfWeek(new Date());
        const end = new Date(start);
        end.setDate(start.getDate() + 6);
        return {start, end};
    }
    const year = Number(match[1]);
    const week = Number(match[2]);
    const jan4 = new Date(year, 0, 4);
    const firstWeekMonday = startOfWeek(jan4);
    const start = new Date(firstWeekMonday);
    start.setDate(firstWeekMonday.getDate() + (week - 1) * 7);
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    return {start, end};
}
function buildLineLabels(periodValue, range) {
    const fromDate = parseDate(range.fromDate);
    const toDate = parseDate(range.toDate);
    if (!fromDate || !toDate) {
        return [];
    }
    if (periodValue === "quarter" || periodValue === "year") {
        const labels = [];
        const current = new Date(fromDate.getFullYear(), fromDate.getMonth(), 1);
        const end = new Date(toDate.getFullYear(), toDate.getMonth(), 1);
        while (current <= end) {
            labels.push({
                key: `${current.getFullYear()}-${String(current.getMonth() + 1).padStart(2, "0")}`,
                label: `T${current.getMonth() + 1}`
            });
            current.setMonth(current.getMonth() + 1);
        }
        return labels;
    }
    const labels = [];
    const current = new Date(fromDate);
    while (current <= toDate) {
        labels.push({
            key: formatDateInput(current),
            label: `${String(current.getDate()).padStart(2, "0")}/${String(current.getMonth() + 1).padStart(2, "0")}`
        });
        current.setDate(current.getDate() + 1);
    }
    return labels;
}
function shouldShowLineLabel(index, totalCount) {
    if (totalCount <= 8) {
        return true;
    }
    if (totalCount <= 15) {
        return index === 0 || index === totalCount - 1 || index % 2 === 0;
    }
    const every = Math.ceil(totalCount / 6);
    return index === 0 || index === totalCount - 1 || index % every === 0;
}
function polarToCartesian(cx, cy, r, angle) {
    const rad = ((angle - 90) * Math.PI) / 180;
    return {x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad)};
}
function describePieArc(cx, cy, r, startAngle, endAngle) {
    const start = polarToCartesian(cx, cy, r, endAngle);
    const end = polarToCartesian(cx, cy, r, startAngle);
    const largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";
    return `M ${cx} ${cy} L ${start.x} ${start.y} A ${r} ${r} 0 ${largeArcFlag} 0 ${end.x} ${end.y} Z`;
}
function buildYAxisTicks(maxValue, steps = 5) {
    const safeMax = Math.max(1, Number(maxValue || 0));
    const list = [];
    for (let i = 0; i <= steps; i++) {
        list.push((safeMax / steps) * i);
    }
    return list;
}
function axisLabelByMode(mode) {
    if (mode === "week" || mode === "month") {
        return "Ngày";
    }
    if (mode === "quarter" || mode === "year") {
        return "Tháng";
    }
    return "Thời gian";
}
function showPieTooltip(slice, event) {
    hoveredSliceName.value = slice?.name || "";
    pieTooltip.text = `${slice?.name || ""}: ${money(slice?.amount || 0)} VND (${((slice?.ratio || 0) * 100).toFixed(1)}%)`;
    pieTooltip.visible = true;
    movePieTooltip(event);
}
function movePieTooltip(event) {
    if (!event) return;
    const gap = 14;
    pieTooltip.x = Number(event.clientX || 0) + gap;
    pieTooltip.y = Number(event.clientY || 0) + gap;
}
function hidePieTooltip() {
    hoveredSliceName.value = "";
    pieTooltip.visible = false;
    pieTooltip.text = "";
}
</script>

<template>
    <AdminLayout>
        <div class="revenue-page">
            <!-- Header -->
            <div class="page-header">
                <h2 class="page-title">Báo cáo doanh thu</h2>
            </div>

            <!-- Tabs -->
            <div class="revenue-tabs">
                <button
                    v-for="tab in revenueTabs"
                    :key="tab.mode"
                    class="tab-btn"
                    :class="{active: viewMode === tab.mode}"
                    type="button"
                    @click="openRevenueTab(tab.to)"
                >
                    {{ tab.label }}
                </button>
            </div>

            <!-- Subtitle -->
            <h4 class="mode-subtitle">{{ modeTitle }}</h4>

            <!-- Error -->
            <div v-if="error" class="error-message">{{ error }}</div>

            <!-- Filters -->
            <div class="filter-section">
                <form class="filter-form" @submit.prevent="applyFilters" v-if="isSummaryMode">
                    <div class="filter-row">
                        <div class="filter-field">
                            <label>Từ ngày</label>
                            <input type="date" v-model="summaryParams.fromDate" class="form-input">
                        </div>
                        <div class="filter-field">
                            <label>Đến ngày</label>
                            <input type="date" v-model="summaryParams.toDate" class="form-input">
                        </div>
                        <div class="filter-field">
                            <label>Sắp xếp theo</label>
                            <select v-model="summaryParams.sortField" class="form-select">
                                <option value="orderId">Mã đơn hàng</option>
                                <option value="productName">Tên sản phẩm</option>
                                <option value="quantity">Số lượng</option>
                                <option value="unitPrice">Đơn giá</option>
                                <option value="discountAmount">Giảm giá</option>
                                <option value="lineTotal">Thành tiền</option>
                            </select>
                        </div>
                        <div class="filter-field">
                            <label>Chiều sắp xếp</label>
                            <select v-model="summaryParams.sortDir" class="form-select">
                                <option value="asc">Tăng dần</option>
                                <option value="desc">Giảm dần</option>
                            </select>
                        </div>
                    </div>
                    <div class="filter-actions">
                        <button class="btn btn-dark" type="submit" :disabled="loading">Lọc</button>
                        <button class="btn btn-outline" type="button" @click="clearSummaryFilters" :disabled="loading">Xoá lọc</button>
                        <button class="btn btn-outline" type="button" @click="exportExcel" :disabled="exporting || loading">{{ exporting ? "Đang xuất..." : "Xuất Excel" }}</button>
                    </div>
                </form>
                <form class="filter-form" @submit.prevent="applyFilters" v-else>
                    <div class="filter-row">
                        <div class="filter-field" v-if="viewMode === 'day'">
                            <label>Ngày</label>
                            <input type="date" v-model="periodParams.day" class="form-input">
                        </div>
                        <div class="filter-field" v-if="viewMode === 'week'">
                            <label>Chọn tuần</label>
                            <input type="week" v-model="periodParams.week" class="form-input">
                        </div>
                        <div class="filter-field" v-if="viewMode === 'month'">
                            <label>Tháng</label>
                            <select v-model.number="periodParams.month" class="form-select">
                                <option v-for="m in months" :key="`m-${m}`" :value="m">Tháng {{ m }}</option>
                            </select>
                        </div>
                        <div class="filter-field" v-if="viewMode === 'quarter'">
                            <label>Quý</label>
                            <select v-model.number="periodParams.quarter" class="form-select">
                                <option v-for="q in quarters" :key="`q-${q}`" :value="q">Quý {{ q }}</option>
                            </select>
                        </div>
                        <div class="filter-field" v-if="viewMode === 'month' || viewMode === 'quarter' || viewMode === 'year'">
                            <label>Năm</label>
                            <input type="number" min="2000" max="2100" v-model.number="periodParams.year" class="form-input">
                        </div>
                        <div class="filter-field">
                            <label>Sắp xếp theo</label>
                            <select v-model="periodParams.sortField" class="form-select">
                                <option value="orderId">Mã đơn hàng</option>
                                <option value="productName">Tên sản phẩm</option>
                                <option value="quantity">Số lượng</option>
                                <option value="unitPrice">Đơn giá</option>
                                <option value="discountAmount">Giảm giá</option>
                                <option value="lineTotal">Thành tiền</option>
                            </select>
                        </div>
                        <div class="filter-field">
                            <label>Chiều sắp xếp</label>
                            <select v-model="periodParams.sortDir" class="form-select">
                                <option value="asc">Tăng dần</option>
                                <option value="desc">Giảm dần</option>
                            </select>
                        </div>
                    </div>
                    <div class="filter-actions">
                        <button class="btn btn-dark" type="submit" :disabled="loading">Lọc dữ liệu</button>
                        <button class="btn btn-outline" type="button" @click="clearPeriodFilters" :disabled="loading">Xoá lọc</button>
                        <button class="btn btn-outline" type="button" @click="exportExcel" :disabled="exporting || loading">{{ exporting ? "Đang xuất..." : "Xuất Excel" }}</button>
                    </div>
                </form>
            </div>

            <!-- Charts -->
            <div class="charts-section" v-if="!isSummaryMode && !isDayMode">
                <div class="chart-card">
                    <h4>{{ chartTitle }}</h4>
                    <div v-if="lineChart.circles.length" class="chart-container">
                        <svg viewBox="0 0 760 280" class="line-chart">
                            <line x1="56" y1="232" x2="736" y2="232" stroke="#e5e5e5" stroke-width="1"></line>
                            <line x1="56" y1="24" x2="56" y2="232" stroke="#e5e5e5" stroke-width="1"></line>
                            <line
                                v-for="(tick, index) in lineChart.yTicks"
                                :key="'ytick-' + index"
                                x1="56"
                                :y1="tick.y"
                                x2="736"
                                :y2="tick.y"
                                stroke="#f5f5f5"
                                stroke-width="1"
                            />
                            <text
                                v-for="(tick, index) in lineChart.yTicks"
                                :key="'ylabel-' + index"
                                x="48"
                                :y="tick.y + 4"
                                text-anchor="end"
                                class="axis-value"
                            >
                                {{ money(tick.value) }}
                            </text>
                            <polyline :points="lineChart.points" fill="none" stroke="#111111" stroke-width="2"></polyline>
                            <circle v-for="(point, index) in lineChart.circles" :key="`pt-${index}`" :cx="point.x" :cy="point.y" r="4" fill="#111111"></circle>
                            <text v-for="(item, index) in lineChart.labels" :key="`lb-${index}`" :x="item.x" :y="item.y" text-anchor="middle" class="axis-label">
                                {{ item.text }}
                            </text>
                            <text x="396" y="274" text-anchor="middle" class="axis-title">{{ lineChart.xAxisTitle }}</text>
                            <text x="8" y="16" text-anchor="start" class="axis-title">Doanh thu (VND)</text>
                        </svg>
                    </div>
                    <div v-else class="no-data">Chưa có dữ liệu chuỗi thời gian.</div>
                </div>
            </div>

            <!-- Table -->
            <div class="table-section" ref="revenueTableRef">
                <div class="table-header">
                    <h5>Danh sách đơn hàng</h5>
                    <div class="table-total">Tổng: <strong>{{ money(total) }} VND</strong></div>
                </div>
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 6%;">STT</th>
                                <th style="width: 15%;">Mã đơn</th>
                                <th style="width: 29%;">Tên sản phẩm</th>
                                <th style="width: 10%;" class="text-center">SL</th>
                                <th style="width: 13%;" class="text-right">Đơn giá</th>
                                <th style="width: 12%;" class="text-right">Giảm giá</th>
                                <th style="width: 15%;" class="text-right">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(r, i) in rows" :key="`${r.orderId}-${i}`">
                                <td class="text-center">{{ i + 1 }}</td>
                                <td class="text-center">#{{ r.orderId }}</td>
                                <td>{{ r.productName }}</td>
                                <td class="text-center">{{ r.quantity }}</td>
                                <td class="text-right">{{ money(r.unitPrice) }}</td>
                                <td class="text-right">{{ money(r.discountAmount) }}</td>
                                <td class="text-right font-semibold">{{ money(r.lineTotal) }}</td>
                            </tr>
                            <tr v-if="rows.length === 0 && !loading">
                                <td colspan="7" class="text-center text-gray">Chưa có dữ liệu</td>
                            </tr>
                        </tbody>
                        <tfoot v-if="rows.length > 0">
                            <tr>
                                <td colspan="6" class="text-right total-label">Tổng cộng</td>
                                <td class="text-right total-value">{{ money(total) }}</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </AdminLayout>
</template>

<style scoped>
.revenue-page {
    padding: 0;
}

.page-header {
    margin-bottom: 20px;
}

.page-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: #111111;
    margin: 0;
}

/* Tabs */
.revenue-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e5e5e5;
}

.tab-btn {
    padding: 8px 16px;
    border: 1px solid #e5e5e5;
    border-radius: 6px;
    background: #ffffff;
    color: #666666;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
}

.tab-btn:hover {
    border-color: #111111;
    color: #111111;
}

.tab-btn.active {
    background: #111111;
    color: #ffffff;
    border-color: #111111;
}

/* Subtitle */
.mode-subtitle {
    font-size: 1rem;
    font-weight: 600;
    color: #333333;
    margin: 0 0 16px 0;
}

/* Error */
.error-message {
    padding: 12px 16px;
    background: #fee2e2;
    color: #dc2626;
    border-radius: 6px;
    margin-bottom: 16px;
    font-size: 0.875rem;
}

/* Filters */
.filter-section {
    background: #f5f5f5;
    padding: 20px;
    border-radius: 0;
    margin-bottom: 24px;
}

.filter-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.filter-row {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
}

.filter-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 140px;
    flex: 1;
}

.filter-field label {
    font-size: 0.75rem;
    font-weight: 600;
    color: #666666;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.form-input,
.form-select {
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    font-size: 0.875rem;
    background: #ffffff;
    transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus {
    outline: none;
    border-color: #111111;
}

.filter-actions {
    display: flex;
    flex-direction: row;
    gap: 8px;
    flex-wrap: nowrap;
    align-items: flex-end;
}

.filter-actions .btn {
    min-width: 110px;
    padding: 10px 16px;
}

/* Buttons */
.btn {
    padding: 10px 20px;
    border-radius: 6px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid transparent;
}

.btn-dark {
    background: #111111;
    color: #ffffff;
    border-color: #111111;
}

.btn-dark:hover {
    background: #333333;
}

.btn-dark:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.btn-outline {
    background: #ffffff;
    color: #111111;
    border-color: #111111;
}

.btn-outline:hover {
    background: #111111;
    color: #ffffff;
}

.btn-outline:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Charts Section */
.charts-section {
    display: block;
    margin-bottom: 24px;
}

.charts-section .chart-card {
    width: 100%;
    max-width: 800px;
    margin: 0 auto;
}

.chart-card {
    background: #f5f5f5;
    padding: 20px;
    border-radius: 0;
}

.chart-card h4 {
    font-size: 0.875rem;
    font-weight: 600;
    color: #333333;
    margin: 0 0 16px 0;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.chart-container {
    overflow-x: auto;
}

.line-chart {
    width: 100%;
    min-width: 600px;
    height: auto;
}

.axis-value {
    font-size: 10px;
    fill: #666666;
}

.axis-label {
    font-size: 11px;
    fill: #333333;
}

.axis-title {
    font-size: 12px;
    font-weight: 600;
    fill: #333333;
}

/* Pie Chart */
.pie-container {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
}

.pie-chart {
    width: 200px;
    height: 200px;
}

.pie-chart path {
    transition: opacity 0.2s, transform 0.2s;
    cursor: pointer;
}

.pie-chart path:hover,
.pie-chart path.active {
    opacity: 0.9;
    transform-origin: 100px 100px;
    transform: scale(1.02);
}

.pie-tooltip {
    position: fixed;
    z-index: 9999;
    pointer-events: none;
    padding: 8px 12px;
    border-radius: 6px;
    background: #111111;
    color: #ffffff;
    font-size: 12px;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.pie-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.pie-legend {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
    max-width: 280px;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 0.875rem;
}

.legend-color {
    width: 16px;
    height: 16px;
    border-radius: 3px;
    flex-shrink: 0;
    border: 1px solid rgba(0,0,0,0.1);
}

.legend-name {
    flex: 1;
    color: #333333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.legend-percent {
    font-weight: 600;
    color: #111111;
    min-width: 50px;
    text-align: right;
}

.no-data {
    padding: 40px;
    text-align: center;
    color: #999999;
    font-size: 0.875rem;
}

/* Table Section */
.table-section {
    background: #ffffff;
    border: 1px solid #e5e5e5;
}

.table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e5e5e5;
    background: #f5f5f5;
}

.table-header h5 {
    font-size: 0.875rem;
    font-weight: 600;
    color: #333333;
    margin: 0;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.table-total {
    font-size: 0.875rem;
    color: #666666;
}

.table-total strong {
    color: #111111;
    font-size: 1rem;
}

.table-wrapper {
    overflow-x: auto;
}

.data-table {
    width: 100%;
    min-width: 800px;
    border-collapse: collapse;
    font-size: 0.875rem;
}

.data-table thead th {
    padding: 12px 16px;
    text-align: left;
    font-size: 0.75rem;
    font-weight: 600;
    color: #666666;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    background: #f8f8f8;
    border-bottom: 1px solid #e5e5e5;
    white-space: nowrap;
}

.data-table thead th.text-center {
    text-align: center;
}

.data-table thead th.text-right {
    text-align: right;
}

.data-table tbody td {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    color: #333333;
    vertical-align: middle;
}

.data-table tbody tr:hover {
    background: #fafafa;
}

.data-table tbody td.text-center {
    text-align: center;
}

.data-table tbody td.text-right {
    text-align: right;
}

.data-table tbody td.text-gray {
    color: #999999;
}

.data-table tfoot td {
    padding: 16px;
    background: #f5f5f5;
    font-weight: 600;
    border-top: 2px solid #111111;
}

.total-label {
    text-align: right;
    color: #333333;
    font-size: 0.875rem;
}

.total-value {
    text-align: right;
    color: #111111;
    font-size: 1rem;
    font-weight: 700;
}

.font-semibold {
    font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
    .filter-row {
        flex-direction: column;
    }

    .filter-field {
        min-width: 100%;
    }
}
</style>
