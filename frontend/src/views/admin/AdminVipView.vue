<script setup>
import {AdminVipPage} from "@/legacy/pages";
import AdminLayout from "@/components/AdminLayout.vue";
import AdminTableContainer from "@/components/AdminTableContainer.vue";
import {computed, reactive, ref} from "vue";
import Swal from "sweetalert2";
import "sweetalert2/dist/sweetalert2.min.css";


const {rows, load, money} = AdminVipPage.setup();
const date = (value) => value ? new Date(value).toLocaleDateString("vi-VN") : "";
const formatCurrencyVND = (value) => {
    if (value === null || value === undefined) {
        return "0 VND";
    }
    const num = Number(value);
    if (Number.isNaN(num)) {
        return "0 VND";
    }
    return num.toLocaleString('vi-VN') + ' VND';
};

const imageModalOpen = ref(false);
const previewImage = ref("");
const exporting = ref(false);
const searchQuery = ref("");



const togglableColumns = [
    { key: 'email' },
    { key: 'phone' },
    { key: 'totalAmount' },
    { key: 'lastOrder' }
];

const showColumn = (key) => columnVisible[key];

// Tab state
const activeTab = ref("customers"); // customers | levels

// VIP Levels state
const vipLevels = ref([
    { id: 1, name: "Bạc", minPoints: 0, maxPoints: 999, discount: 5 },
    { id: 2, name: "Vàng", minPoints: 1000, maxPoints: 4999, discount: 10 },
    { id: 3, name: "Bạch kim", minPoints: 5000, maxPoints: 9999, discount: 15 },
    { id: 4, name: "Kim cương", minPoints: 10000, maxPoints: null, discount: 20 }
]);
const levelModalOpen = ref(false);
const levelForm = reactive({
    id: null,
    name: "",
    minPoints: 0,
    maxPoints: null,
    discount: 0
});
const editingLevel = ref(false);

// Points history modal state
const historyModalOpen = ref(false);
const historyLoading = ref(false);
const historyData = ref([]);
const historyUserId = ref("");
const historyUserName = ref("");

// Pagination state
const currentPage = ref(0);
const pageSize = ref(10);
const totalPages = computed(() => Math.ceil((filteredRows.value?.length || 0) / pageSize.value));
const paginatedRows = computed(() => {
    const start = currentPage.value * pageSize.value;
    const end = start + pageSize.value;
    return filteredRows.value?.slice(start, end) || [];
});

const filteredRows = computed(() => {
    if (!searchQuery.value.trim()) {
        return rows.value || [];
    }
    const query = searchQuery.value.toLowerCase();
    return (rows.value || []).filter(r => 
        r?.username?.toLowerCase().includes(query) ||
        r?.fullname?.toLowerCase().includes(query) ||
        r?.email?.toLowerCase().includes(query) ||
        r?.phone?.toLowerCase().includes(query)
    );
});
const columnVisible = reactive({
    email: true,
    phone: true,
    totalAmount: true,
    lastOrder: true
});
const openImage = (value) => {
    const resolved = resolvePhotoUrl(value);
    if (!resolved) {
        return;
    }
    previewImage.value = resolved;
    imageModalOpen.value = true;
};
const closeImage = () => {
    imageModalOpen.value = false;
    previewImage.value = "";
};
const exportVip = async () => {
    if (exporting.value) {
        return;
    }
    exporting.value = true;
    try {
        const res = await fetch("/api/admin/reports/vip/export?format=xlsx", {credentials: "include"});
        if (!res.ok) {
            throw new Error("Không thể xuất file VIP");
        }
        const blob = await res.blob();
        const a = document.createElement("a");
        const u = window.URL.createObjectURL(blob);
        a.href = u;
        const cd = res.headers.get("content-disposition") || "";
        const match = cd.match(/filename=\"?([^\";]+)\"?/i);
        a.download = match?.[1] || "khach-vip.xlsx";
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(u);
        
        Swal.fire({
            title: "Đã xuất!",
            text: "File VIP đã được xuất thành công.",
            icon: "success",
            timer: 1500,
            timerProgressBar: true,
            position: "top-end",
            toast: true,
            showConfirmButton: false
        });
    } catch (e) {
        Swal.fire("Lỗi", e.message || "Xuất file thất bại", "error");
    } finally {
        exporting.value = false;
    }
};
const resolvePhotoUrl = (photo) => {
    const raw = String(photo || "").trim();
    if (!raw) {
        return "";
    }
    if (/^https?:\/\//i.test(raw)) {
        return raw;
    }
    if (raw.startsWith("/")) {
        return raw;
    }
    const backendUrl = String(import.meta.env.VITE_BACKEND_URL || "").trim().replace(/\/$/, "");
    if (backendUrl) {
        return `${backendUrl}/images/${encodeURIComponent(raw)}`;
    }
    return `/images/${encodeURIComponent(raw)}`;
};
const tableColspan = computed(() => {
    let total = 3; // Username, Họ tên, Lịch sử điểm
    for (const item of togglableColumns) {
        if (showColumn(item.key)) {
            total += 1;
        }
    }
    return total;
});

// VIP Levels CRUD functions
const openLevelModal = () => {
    levelForm.id = null;
    levelForm.name = "";
    levelForm.minPoints = 0;
    levelForm.maxPoints = null;
    levelForm.discount = 0;
    editingLevel.value = false;
    levelModalOpen.value = true;
};

const editLevel = (level) => {
    levelForm.id = level.id;
    levelForm.name = level.name;
    levelForm.minPoints = level.minPoints;
    levelForm.maxPoints = level.maxPoints;
    levelForm.discount = level.discount;
    editingLevel.value = true;
    levelModalOpen.value = true;
};

const saveLevel = async () => {
    if (!levelForm.name.trim()) {
        Swal.fire("Lỗi", "Vui lòng nhập tên cấp bậc", "error");
        return;
    }
    if (levelForm.minPoints < 0) {
        Swal.fire("Lỗi", "Điểm tối thiểu không được âm", "error");
        return;
    }
    if (levelForm.discount < 0 || levelForm.discount > 100) {
        Swal.fire("Lỗi", "% giảm giá phải từ 0 đến 100", "error");
        return;
    }

    try {
        // Note: API calls commented out as backend endpoints may not exist yet
        // Uncomment when backend API is ready
        /*
        if (editingLevel.value) {
            await api.admin.vip.updateLevel(levelForm.id, levelForm);
        } else {
            await api.admin.vip.createLevel(levelForm);
        }
        */
        
        if (editingLevel.value) {
            const index = vipLevels.value.findIndex(l => l.id === levelForm.id);
            if (index !== -1) {
                vipLevels.value[index] = { ...levelForm };
            }
            Swal.fire({
                title: "Đã cập nhật!",
                text: "Cấp bậc VIP đã được cập nhật thành công.",
                icon: "success",
                timer: 1500,
                timerProgressBar: true,
                position: "top-end",
                toast: true,
                showConfirmButton: false
            });
        } else {
            const newId = Math.max(...vipLevels.value.map(l => l.id), 0) + 1;
            vipLevels.value.push({ ...levelForm, id: newId });
            Swal.fire({
                title: "Đã thêm!",
                text: "Cấp bậc VIP đã được thêm thành công.",
                icon: "success",
                timer: 1500,
                timerProgressBar: true,
                position: "top-end",
                toast: true,
                showConfirmButton: false
            });
        }
        levelModalOpen.value = false;
    } catch (e) {
        Swal.fire("Lỗi", e.message || "Lỗi khi lưu cấp bậc", "error");
    }
};

const deleteLevel = async (level) => {
    const result = await Swal.fire({
        title: "Bạn có chắc chắn muốn xóa?",
        html: `Cấp bậc <strong>"${level.name}"</strong> sẽ bị xóa.`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Xác nhận xóa",
        cancelButtonText: "Hủy",
        confirmButtonColor: "#dc2626",
        cancelButtonColor: "#6b7280",
        reverseButtons: true,
        focusCancel: true
    });

    if (result.isConfirmed) {
        // Note: API call commented out as backend endpoint may not exist yet
        // Uncomment when backend API is ready
        // await api.admin.vip.deleteLevel(level.id);
        
        vipLevels.value = vipLevels.value.filter(l => l.id !== level.id);
        Swal.fire({
            title: "Đã xóa!",
            text: "Cấp bậc VIP đã được xóa thành công.",
            icon: "success",
            timer: 1500,
            timerProgressBar: true,
            position: "top-end",
            toast: true,
            showConfirmButton: false
        });
    }
};

const closeLevelModal = () => {
    levelModalOpen.value = false;
};

// Points history function
const viewPointsHistory = async (userId, userName) => {
    if (import.meta.env.DEV) console.log('viewPointsHistory called', userId, userName);
    historyUserId.value = userId;
    historyUserName.value = `${userName} · @${userId}`;
    historyLoading.value = true;
    historyModalOpen.value = true;
    if (import.meta.env.DEV) console.log('historyModalOpen set to true', historyModalOpen.value);

    try {
        // Mock data - replace with actual API call
        await new Promise(resolve => setTimeout(resolve, 500));
        historyData.value = [
            { date: "20/04/2026", action: "Đặt hàng", pointsChange: 100, balance: 1500 },
            { date: "18/04/2026", action: "Đổi điểm", pointsChange: -500, balance: 1400 },
            { date: "15/04/2026", action: "Đặt hàng", pointsChange: 200, balance: 1900 },
            { date: "10/04/2026", action: "Đăng ký", pointsChange: 100, balance: 1700 }
        ];
    } catch (e) {
        Swal.fire("Lỗi", e.message || "Không thể tải lịch sử điểm", "error");
    } finally {
        historyLoading.value = false;
    }
};

// Helper functions for new UI
const AVICOLORS = [
    ["#E6F1FB", "#042C53"],
    ["#EEEDFE", "#26215C"],
    ["#E1F5EE", "#04342C"],
    ["#FAEEDA", "#412402"],
    ["#FAECE7", "#4A1B0C"],
    ["#F1EFE8", "#2C2C2A"]
];

const getInitials = (name) => {
    if (!name) return "?";
    const parts = name.trim().split(" ");
    return (parts[0][0] + (parts[parts.length - 1][0] || "")).toUpperCase();
};

const getAvatarColor = (index) => {
    return AVICOLORS[index % AVICOLORS.length][0];
};

const getAvatarTextColor = (index) => {
    return AVICOLORS[index % AVICOLORS.length][1];
};

const getBadgeClass = (level) => {
    const badgeMap = {
        "Bạc": "bac",
        "Vàng": "gol",
        "Bạch kim": "plt",
        "Kim cương": "dia"
    };
    return badgeMap[level] || "bac";
};

const toggleColumn = (column) => {
    columnVisible[column] = !columnVisible[column];
};

const handleSearch = () => {
    currentPage.value = 0;
};

// Computed properties for stats
const totalVipRevenue = computed(() => {
    return filteredRows.value.reduce((sum, r) => sum + (r.totalAmount || 0), 0);
});

const averagePoints = computed(() => {
    const totalPoints = filteredRows.value.reduce((sum, r) => sum + (r.points || 0), 0);
    return filteredRows.value.length ? Math.round(totalPoints / filteredRows.value.length) : 0;
});

const closeHistoryModal = () => {
    historyModalOpen.value = false;
    historyData.value = [];
    historyUserId.value = "";
};

// Pagination functions
const goToPage = (page) => {
    currentPage.value = page;
};

const nextPage = () => {
    if (currentPage.value < totalPages.value - 1) {
        currentPage.value++;
    }
};

const prevPage = () => {
    if (currentPage.value > 0) {
        currentPage.value--;
    }
};
</script>

<template>
    <AdminLayout>
        <div class="wrap">
            <!-- Header -->
            <div class="hdr">
                <div class="hdr-left">
                    <h1>Quản lý VIP</h1>
                    <p>Khách hàng thành viên & cấp bậc ưu đãi</p>
                </div>
                <div class="hdr-right">
                    <button class="btn" @click="exportVip" :disabled="exporting">
                        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M2 11v3h12v-3M8 2v8M5 7l3 3 3-3"/>
                        </svg>
                        {{ exporting ? 'Đang xuất...' : 'Xuất Excel' }}
                    </button>
                </div>
            </div>

            <!-- Stats Cards -->
            <div class="stats">
                <div class="sc">
                    <div class="sc-lbl">Tổng khách VIP</div>
                    <div class="sc-val">{{ filteredRows.length }}</div>
                    <div class="sc-sub g">Đang hoạt động</div>
                </div>
                <div class="sc">
                    <div class="sc-lbl">Doanh thu VIP</div>
                    <div class="sc-val">{{ formatCurrencyVND(totalVipRevenue) }}</div>
                    <div class="sc-sub g">Tổng doanh thu</div>
                </div>
                <div class="sc">
                    <div class="sc-lbl">Điểm trung bình</div>
                    <div class="sc-val">{{ averagePoints }}</div>
                    <div class="sc-sub b">Điểm tích lũy</div>
                </div>
                <div class="sc">
                    <div class="sc-lbl">Cấp bậc</div>
                    <div class="sc-val">{{ vipLevels.length }}</div>
                    <div class="sc-sub b">Đang hoạt động</div>
                </div>
            </div>

            <!-- Tabs -->
            <div class="tabs">
                <button class="tab" :class="{ on: activeTab === 'customers' }" @click="activeTab = 'customers'">Khách hàng</button>
                <button class="tab" :class="{ on: activeTab === 'levels' }" @click="activeTab = 'levels'">Cấp bậc</button>
            </div>

            <!-- Customers Panel -->
            <div id="p-cust" class="panel" :class="{ on: activeTab === 'customers' }">
                <div class="toolbar">
                    <div class="srch">
                        <input v-model="searchQuery" placeholder="Tìm theo tên, email, SĐT..." @input="handleSearch">
                    </div>
                    <div class="tbr-right">
                        <div class="chips">
                            <button class="chip" :class="{ on: columnVisible.email }" @click="toggleColumn('email')">
                                <span class="chip-dot"></span>Email
                            </button>
                            <button class="chip" :class="{ on: columnVisible.phone }" @click="toggleColumn('phone')">
                                <span class="chip-dot"></span>SĐT
                            </button>
                            <button class="chip" :class="{ on: columnVisible.totalAmount }" @click="toggleColumn('totalAmount')">
                                <span class="chip-dot"></span>Tổng mua
                            </button>
                            <button class="chip" :class="{ on: columnVisible.lastOrder }" @click="toggleColumn('lastOrder')">
                                <span class="chip-dot"></span>Lần cuối
                            </button>
                        </div>
                        <button class="btn btn-sm" @click="load">
                            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                                <path d="M2 8a6 6 0 1 0 1.5-4M2 4v4h4"/>
                            </svg>
                        </button>
                    </div>
                </div>

                <div class="tcard">
                    <div class="tscroll">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 190px">Khách hàng</th>
                                    <th class="ce email" :class="{ hidden: !columnVisible.email }" style="width: 170px">Email</th>
                                    <th class="ce phone" :class="{ hidden: !columnVisible.phone }" style="width: 120px">SĐT</th>
                                    <th style="width: 100px">Hạng</th>
                                    <th class="ce amt r" :class="{ hidden: !columnVisible.totalAmount }" style="width: 130px">Tổng mua</th>
                                    <th class="ce lo" :class="{ hidden: !columnVisible.lastOrder }" style="width: 110px">Lần cuối</th>
                                    <th style="width: 70px; text-align: center">Chi tiết</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(r, i) in paginatedRows" :key="r.username">
                                    <td>
                                        <div class="ucell">
                                            <div class="avi" :style="{ background: getAvatarColor(i), color: getAvatarTextColor(i) }">
                                                {{ getInitials(r.fullname || r.username) }}
                                            </div>
                                            <div class="uinfo">
                                                <div class="uname">{{ r.fullname || r.username }}</div>
                                                <div class="usub">@{{ r.username }}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="ce email" :class="{ hidden: !columnVisible.email }">
                                        <span class="trunc" style="max-width: 150px" :title="r.email">{{ r.email || '' }}</span>
                                    </td>
                                    <td class="ce phone" :class="{ hidden: !columnVisible.phone }">{{ r.phone || '' }}</td>
                                    <td><span class="bdg" :class="getBadgeClass(r.vipLevel)">{{ r.vipLevel || 'Bạc' }}</span></td>
                                    <td class="ce amt r" :class="{ hidden: !columnVisible.totalAmount }">
                                        <span class="amt">{{ formatCurrencyVND(r.totalAmount) }}</span>
                                    </td>
                                    <td class="ce lo" :class="{ hidden: !columnVisible.lastOrder }">
                                        <span class="dt">{{ date(r.lastOrderDate || r.lastOrder) }}</span>
                                    </td>
                                    <td style="text-align: center">
                                        <button class="ibtn" @click="viewPointsHistory(r.username, r.fullname || r.username)" title="Lịch sử điểm">
                                            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" width="12" height="12">
                                                <circle cx="8" cy="8" r="6"/>
                                                <path d="M8 5v3.5l2 1.5"/>
                                            </svg>
                                        </button>
                                    </td>
                                </tr>
                                <tr v-if="!paginatedRows.length">
                                    <td colspan="7" style="text-align: center; padding: 32px; color: #999; font-size: 13px">
                                        Không tìm thấy kết quả
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="pag" v-if="totalPages > 1">
                        <div class="pag-info">{{ paginatedRows.length ? currentPage * pageSize + 1 : 0 }}–{{ Math.min((currentPage + 1) * pageSize, filteredRows.length) }} / {{ filteredRows.length }} khách hàng</div>
                        <div class="pag-btns">
                            <button class="pb" @click="prevPage" :disabled="currentPage === 0">
                                <svg width="10" height="10" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M10 4L6 8l4 4"/>
                                </svg>
                            </button>
                            <button v-for="page in totalPages" :key="page" class="pb" :class="{ on: currentPage === page - 1 }" @click="goToPage(page - 1)">{{ page }}</button>
                            <button class="pb" @click="nextPage" :disabled="currentPage >= totalPages - 1">
                                <svg width="10" height="10" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M6 4l4 4-4 4"/>
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Levels Panel -->
            <div id="p-lvls" class="panel" :class="{ on: activeTab === 'levels' }">
                <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
                    <button class="btn btn-solid" @click="openLevelModal">
                        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M8 3v10M3 8h10"/>
                        </svg>
                        Thêm cấp bậc
                    </button>
                </div>
                <div class="lgrid">
                    <div v-for="level in vipLevels" :key="level.id" class="lcard">
                        <div class="lcard-top" style="justify-content: center">
                            <div class="lcard-ico" :style="{ background: level.ico, width: '32px', height: '32px', borderRadius: '8px' }"></div>
                            <div style="flex: 1">
                                <div class="lcard-name">{{ level.name }}</div>
                            </div>
                        </div>
                        <div class="lcard-rows">
                            <div class="lrow">
                                <span class="lrow-k">Điểm tối thiểu</span>
                                <span class="lrow-v">{{ level.minPoints.toLocaleString('vi-VN') }}</span>
                            </div>
                            <div class="lrow">
                                <span class="lrow-k">Điểm tối đa</span>
                                <span class="lrow-v">{{ level.maxPoints ? level.maxPoints.toLocaleString('vi-VN') : 'Không giới hạn' }}</span>
                            </div>
                            <div class="lrow">
                                <span class="lrow-k">Giảm giá</span>
                                <span class="lrow-v g">-{{ level.discount }}%</span>
                            </div>
                        </div>
                        <div class="lcard-foot">
                            <button class="ibtn" @click="editLevel(level)" title="Sửa">
                                <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" width="12" height="12">
                                    <path d="M11 2l3 3-8 8H3v-3l8-8z"/>
                                </svg>
                            </button>
                            <button class="ibtn del" @click="deleteLevel(level)" title="Xóa">
                                <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" width="12" height="12">
                                    <path d="M3 4h10M6 4V2h4v2M5 4v8h6V4"/>
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Level Modal -->
        <div class="mover" :class="{ on: levelModalOpen }" @click.self="closeLevelModal">
            <div class="modal">
                <div class="mhd">
                    <span class="mhd-t">{{ editingLevel ? 'Cập nhật cấp bậc' : 'Thêm cấp bậc' }}</span>
                    <button class="ibtn" @click="closeLevelModal">
                        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M3 3l10 10M13 3L3 13"/>
                        </svg>
                    </button>
                </div>
                <div class="mbdy">
                    <div class="frow">
                        <label class="flbl">Tên cấp bậc</label>
                        <input class="finp" v-model="levelForm.name" placeholder="Vd: Vàng">
                    </div>
                    <div class="fgrid">
                        <div class="frow">
                            <label class="flbl">Điểm tối thiểu</label>
                            <input class="finp" v-model.number="levelForm.minPoints" type="number" placeholder="0">
                        </div>
                        <div class="frow">
                            <label class="flbl">Điểm tối đa</label>
                            <input class="finp" v-model.number="levelForm.maxPoints" type="number" placeholder="Không giới hạn">
                        </div>
                    </div>
                    <div class="frow">
                        <label class="flbl">% Giảm giá</label>
                        <input class="finp" v-model.number="levelForm.discount" type="number" placeholder="0" min="0" max="100">
                    </div>
                </div>
                <div class="mft">
                    <button class="btn" @click="closeLevelModal">Hủy</button>
                    <button class="btn btn-solid" @click="saveLevel">{{ editingLevel ? 'Cập nhật' : 'Lưu' }}</button>
                </div>
            </div>
        </div>

        <!-- History Modal -->
        <div class="mover" :class="{ on: historyModalOpen }" @click.self="closeHistoryModal">
            <div class="modal hmodal">
                <div class="mhd">
                    <div>
                        <div class="mhd-t">Lịch sử điểm</div>
                        <div style="font-size: 11px; color: #666; margin-top: 2px">{{ historyUserName }}</div>
                    </div>
                    <button class="ibtn" @click="closeHistoryModal">
                        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M3 3l10 10M13 3L3 13"/>
                        </svg>
                    </button>
                </div>
                <div style="overflow-x: auto">
                    <table style="width: 100%; border-collapse: collapse; font-size: 13px">
                        <thead>
                            <tr style="background: #f5f5f5">
                                <th style="padding: 9px 14px; text-align: left; font-size: 11px; font-weight: 500; color: #666; text-transform: uppercase; letter-spacing: 0.04em; border-bottom: 0.5px solid #f0f0f0">Ngày</th>
                                <th style="padding: 9px 14px; text-align: left; font-size: 11px; font-weight: 500; color: #666; text-transform: uppercase; letter-spacing: 0.04em; border-bottom: 0.5px solid #f0f0f0">Hành động</th>
                                <th style="padding: 9px 14px; text-align: right; font-size: 11px; font-weight: 500; color: #666; text-transform: uppercase; letter-spacing: 0.04em; border-bottom: 0.5px solid #f0f0f0">Điểm</th>
                                <th style="padding: 9px 14px; text-align: right; font-size: 11px; font-weight: 500; color: #666; text-transform: uppercase; letter-spacing: 0.04em; border-bottom: 0.5px solid #f0f0f0">Số dư</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in historyData" :key="index" style="border-bottom: 0.5px solid #f0f0f0">
                                <td style="padding: 9px 14px; font-size: 12px; color: #666">{{ item.date }}</td>
                                <td style="padding: 9px 14px; font-size: 13px">{{ item.action }}</td>
                                <td style="padding: 9px 14px; text-align: right">
                                    <span class="htag" :class="item.pointsChange > 0 ? 'p' : 'm'">{{ item.pointsChange > 0 ? '+' : '' }}{{ item.pointsChange }}</span>
                                </td>
                                <td style="padding: 9px 14px; text-align: right; font-weight: 500; font-size: 13px">{{ item.balance.toLocaleString('vi-VN') }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="mft">
                    <button class="btn" @click="closeHistoryModal">Đóng</button>
                </div>
            </div>
        </div>
    </AdminLayout>
</template>

<style>
/* Base */
.wrap {
    padding: 0;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Header */
.hdr {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px 20px 0 20px;
}

.hdr-left h1 {
    font-size: 18px;
    font-weight: 500;
    color: #1a1a1a;
    margin: 0;
}

.hdr-left p {
    font-size: 12px;
    color: #666666;
    margin-top: 2px;
    margin-bottom: 0;
}

.hdr-right {
    display: flex;
    gap: 8px;
}

/* Stats Cards */
.stats {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
    margin-bottom: 20px;
    padding: 0 20px;
}

.sc {
    background: #f5f5f5;
    border-radius: 8px;
    padding: 14px 16px;
}

.sc-lbl {
    font-size: 11px;
    color: #999999;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    margin-bottom: 6px;
}

.sc-val {
    font-size: 20px;
    font-weight: 500;
    color: #1a1a1a;
    line-height: 1;
}

.sc-sub {
    font-size: 11px;
    margin-top: 5px;
}

.sc-sub.g {
    color: #16a34a;
}

.sc-sub.b {
    color: #2563eb;
}

/* Tabs */
.tabs {
    display: flex;
    gap: 0;
    border-bottom: 0.5px solid #f0f0f0;
    margin-bottom: 16px;
    padding: 0 20px;
}

.tab {
    padding: 9px 18px;
    font-size: 13px;
    font-weight: 500;
    color: #666666;
    background: transparent;
    border: none;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    margin-bottom: -0.5px;
    transition: all 0.12s;
}

.tab.on {
    color: #1a1a1a;
    border-bottom-color: #1a1a1a;
}

.tab:hover:not(.on) {
    color: #1a1a1a;
}

/* Panel */
.panel {
    display: none;
    padding: 0 20px 20px 20px;
}

.panel.on {
    display: block;
}

/* Toolbar */
.toolbar {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 10px;
    align-items: center;
    margin-bottom: 12px;
}

.srch {
    position: relative;
    width: 100%;
}

.srch input {
    width: 100%;
    height: 34px;
    padding: 0 10px;
    border: 0.5px solid #e0e0e0;
    border-radius: 8px;
    background: #ffffff;
    color: #1a1a1a;
    font-size: 13px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    outline: none;
}

.srch input:focus {
    border-color: #1a1a1a;
}

.tbr-right {
    display: flex;
    gap: 8px;
    align-items: center;
}

/* Button */
.btn {
    height: 34px;
    padding: 0 14px;
    border: 0.5px solid #e0e0e0;
    border-radius: 8px;
    background: #ffffff;
    color: #1a1a1a;
    font-size: 13px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    white-space: nowrap;
    transition: background 0.12s;
}

.btn:hover {
    background: #f5f5f5;
}

.btn svg {
    width: 13px;
    height: 13px;
    flex-shrink: 0;
}

.btn-solid {
    background: #1a1a1a;
    color: #ffffff;
    border-color: transparent;
}

.btn-solid:hover {
    opacity: 0.85;
}

.btn-sm {
    height: 28px;
    padding: 0 10px;
    font-size: 12px;
}

/* Table Card */
.tcard {
    background: #ffffff;
    border: 0.5px solid #f0f0f0;
    border-radius: 12px;
    overflow: hidden;
}

.tscroll {
    overflow-x: auto;
}

table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
    table-layout: fixed;
}

thead tr {
    background: #f5f5f5;
}

th {
    padding: 9px 14px;
    text-align: left;
    font-size: 11px;
    font-weight: 500;
    color: #666666;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    border-bottom: 0.5px solid #f0f0f0;
    white-space: nowrap;
}

th.r,
td.r {
    text-align: right;
}

td {
    padding: 10px 14px;
    border-bottom: 0.5px solid #f0f0f0;
    color: #1a1a1a;
    vertical-align: middle;
}

tbody tr:last-child td {
    border-bottom: none;
}

tbody tr:hover {
    background: #f5f5f5;
}

/* Avatar */
.avi {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    font-weight: 500;
    flex-shrink: 0;
}

.ucell {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
}

.uinfo {
    min-width: 0;
}

.uname {
    font-size: 13px;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.usub {
    font-size: 11px;
    color: #666666;
}

/* Badge */
.bdg {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: 500;
    white-space: nowrap;
}

.bac {
    background: #F1EFE8;
    color: #444441;
}

.gol {
    background: #FAEEDA;
    color: #633806;
}

.plt {
    background: #E6F1FB;
    color: #0C447C;
}

.dia {
    background: #EEEDFE;
    color: #3C3489;
}

/* Amount */
.amt {
    font-size: 13px;
    font-weight: 500;
    color: #16a34a;
}

.dt {
    font-size: 12px;
    color: #666666;
}

/* Icon Button */
.ibtn {
    width: 26px;
    height: 26px;
    border: 0.5px solid #f0f0f0;
    border-radius: 8px;
    background: transparent;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #666666;
    transition: all 0.12s;
}

.ibtn:hover {
    background: #f5f5f5;
    border-color: #e0e0e0;
    color: #1a1a1a;
}

.ibtn.del:hover {
    background: #FCEBEB;
    border-color: #F09595;
    color: #A32D2D;
}

.ibtn svg {
    width: 12px;
    height: 12px;
}

.ibtns {
    display: flex;
    gap: 4px;
}

/* Pagination */
.pag {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    border-top: 0.5px solid #f0f0f0;
}

.pag-info {
    font-size: 12px;
    color: #666666;
}

.pag-btns {
    display: flex;
    gap: 3px;
}

.pb {
    width: 26px;
    height: 26px;
    border: 0.5px solid #f0f0f0;
    border-radius: 8px;
    background: transparent;
    cursor: pointer;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666666;
    transition: all 0.12s;
}

.pb.on {
    background: #1a1a1a;
    color: #ffffff;
    border-color: transparent;
}

.pb:hover:not(.on) {
    background: #f5f5f5;
}

.pb:disabled {
    opacity: 0.35;
    cursor: default;
}

/* Chips */
.chips {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
}

.chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 9px;
    border: 0.5px solid #e0e0e0;
    border-radius: 20px;
    font-size: 11px;
    cursor: pointer;
    background: #ffffff;
    color: #666666;
    user-select: none;
    transition: all 0.12s;
}

.chip.on {
    background: #f5f5f5;
    color: #1a1a1a;
    border-color: #1a1a1a;
}

.chip-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: #e0e0e0;
    flex-shrink: 0;
}

.chip.on .chip-dot {
    background: #16a34a;
}

/* Level Grid */
.lgrid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
}

.lcard {
    background: #ffffff;
    border: 0.5px solid #f0f0f0;
    border-radius: 12px;
    padding: 16px;
    text-align: left;
}

.lcard-top {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;
    padding-bottom: 12px;
    border-bottom: 0.5px solid #f0f0f0;
    justify-content: space-between;
}

.lcard-ico {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.lcard-name {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.lcard-rows {
}

.lrow {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
}

.lrow:not(:last-of-type) {
    border-bottom: 0.5px solid #f0f0f0;
}

.lrow-k {
    font-size: 12px;
    color: #888;
}

.lrow-v {
    font-size: 14px;
    font-weight: 600;
    text-align: right;
}

.lrow-v.g {
    color: #16a34a;
}

.lcard-foot {
    display: flex;
    gap: 6px;
    margin-top: 12px;
    padding-top: 10px;
    border-top: 0.5px solid #f0f0f0;
}

/* Modal */
.mover {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.15s;
}

.mover.on {
    opacity: 1;
    pointer-events: all;
}

.modal {
    background: #ffffff;
    border: 0.5px solid #e0e0e0;
    border-radius: 12px;
    width: min(90vw, 400px);
}

.mhd {
    padding: 14px 16px;
    border-bottom: 0.5px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.mhd-t {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
}

.mbdy {
    padding: 16px;
}

.mft {
    padding: 12px 16px;
    border-top: 0.5px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
}

.frow {
    margin-bottom: 12px;
}

.flbl {
    font-size: 11px;
    font-weight: 500;
    color: #666666;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    display: block;
    margin-bottom: 5px;
}

.finp {
    width: 100%;
    height: 34px;
    padding: 0 10px;
    border: 0.5px solid #e0e0e0;
    border-radius: 8px;
    background: #ffffff;
    color: #1a1a1a;
    font-size: 13px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    outline: none;
}

.finp:focus {
    border-color: #1a1a1a;
}

.fgrid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
}

/* History Modal */
.hmodal {
    width: min(90vw, 520px);
}

.htag {
    display: inline-flex;
    padding: 2px 7px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: 500;
}

.htag.p {
    background: #EAF3DE;
    color: #27500A;
}

.htag.m {
    background: #FCEBEB;
    color: #791F1F;
}

/* Truncate */
.trunc {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: block;
}

/* Column Visibility */
.ce {
    transition: display 0.2s ease;
}

.ce.hidden {
    display: none;
}

/* Responsive */
@media (max-width: 1024px) {
    .stats {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .lgrid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 768px) {
    .stats {
        grid-template-columns: 1fr;
    }

    .toolbar {
        grid-template-columns: 1fr;
    }

    .lgrid {
        grid-template-columns: 1fr;
    }
}

/* ==================== VIP TABLE STYLES ==================== */
.vip-toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
    margin-bottom: 10px;
}

.vip-column-filter {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px 10px;
    margin-right: auto;
}

.vip-column-filter-title {
    font-weight: 700;
    color: #374151;
}

.vip-column-dropdown {
    position: relative;
    margin-right: auto;
}

.vip-column-dropdown-inline {
    position: relative;
}

.vip-column-dropdown-inline summary {
    cursor: pointer;
    list-style: none;
}

.vip-column-dropdown-inline summary::-webkit-details-marker {
    display: none;
}

.vip-column-dropdown-trigger {
    list-style: none;
    border: 1px solid #d1d5db;
    border-radius: 10px;
    background: #fff;
    color: #111827;
    font-weight: 600;
    padding: 8px 12px;
    cursor: pointer;
}

.vip-column-dropdown-trigger::-webkit-details-marker {
    display: none;
}

.vip-column-dropdown[open] .vip-column-dropdown-trigger {
    border-color: #111827;
}

.vip-column-dropdown-menu {
    position: absolute;
    top: calc(100% + 6px);
    left: 0;
    min-width: 220px;
    background: #fff;
    border: 1px solid #d1d5db;
    border-radius: 10px;
    box-shadow: 0 12px 26px rgba(15, 23, 42, 0.12);
    padding: 10px;
    display: grid;
    gap: 8px;
    z-index: 10;
}

.vip-column-option {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
}

.vip-table-wrap {
    overflow-x: auto;
}

.vip-table {
    min-width: 1300px;
}

.sticky-col {
    position: sticky;
    z-index: 2;
    background: #fff;
}

.sticky-col-1 {
    left: 0;
    min-width: 150px;
}

.sticky-col-2 {
    left: 150px;
    min-width: 190px;
}

.vip-table thead .sticky-col {
    z-index: 3;
    background: #f9fafb;
}

.vip-address-scroll {
    max-width: 320px;
    overflow-x: auto;
    white-space: nowrap;
}

.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1500;
}

.modal-content {
    width: min(92vw, 360px);
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 14px;
    border-bottom: 1px solid #e5e7eb;
}

.modal-header h5 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
}

.modal-close {
    border: 1px solid #d1d5db;
    background: #fff;
    width: 28px;
    height: 28px;
    line-height: 1;
    border-radius: 6px;
}

.modal-body {
    padding: 14px;
}

/* VIP Tabs */
.vip-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
}

.vip-tab {
    padding: 10px 20px;
    border: 1px solid #d1d5db;
    background: #fff;
    color: #374151;
    border-radius: 10px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.vip-tab:hover {
    background: #f3f4f6;
}

.vip-tab.active {
    background: #2563eb;
    color: #fff;
    border-color: #2563eb;
}

/* History Table */
.history-table {
    width: 100%;
    border-collapse: collapse;
}

.history-table thead {
    background: #f9fafb;
}

.history-table th {
    padding: 10px;
    text-align: left;
    font-weight: 600;
    border-bottom: 1px solid #e5e7eb;
}

.history-table td {
    padding: 10px;
    border-bottom: 1px solid #e5e7eb;
}

/* Form Groups */
.form-group {
    margin-bottom: 16px;
}

.form-group label {
    display: block;
    margin-bottom: 6px;
    font-weight: 600;
    color: #374151;
    font-size: 0.875rem;
}

.form-group .form-control {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 0.875rem;
}

.admin-form-actions {
    display: flex;
    gap: 8px;
    margin-top: 20px;
}

/* Action Buttons */
.actionButtons {
    display: flex;
    gap: 6px;
}

.actionBtn {
    padding: 6px 10px;
    border: 1px solid #d1d5db;
    background: #fff;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.875rem;
    transition: all 0.2s ease;
}

.actionEditBtn:hover {
    background: #dbeafe;
    border-color: #2563eb;
}

.actionDeleteBtn:hover {
    background: #fee2e2;
    border-color: #dc2626;
}

/* ==================== RESPONSIVE MODALS ==================== */
@media (max-width: 768px) {
    .modal-content {
        width: 95vw;
        max-width: none;
        margin: 10px;
        max-height: calc(100vh - 20px);
        overflow-y: auto;
    }
    
    .modal-content h4,
    .modal-content h5 {
        font-size: 1rem;
    }
    
    .form-group {
        margin-bottom: 12px;
    }
    
    .form-group label {
        font-size: 14px;
    }
    
    .form-control {
        font-size: 14px;
        padding: 10px;
    }
    
    .admin-form-actions {
        flex-direction: column;
    }
    
    .admin-form-actions .btn {
        width: 100%;
    }
    
    .vip-tabs {
        flex-wrap: wrap;
    }
    
    .vip-tab {
        padding: 8px 16px;
        font-size: 0.875rem;
    }
    
    .stats-cards {
        grid-template-columns: 1fr;
    }
    
    .paginationControls {
        flex-direction: column;
        gap: 12px;
    }
    
    .paginationWrapper {
        width: 100%;
        justify-content: center;
    }
}

@media (max-width: 480px) {
    .modal-content {
        width: 95vw;
        margin: 5px;
    }
    
    .modal-content h4,
    .modal-content h5 {
        font-size: 0.9rem;
    }
    
    .form-group label {
        font-size: 14px;
    }
    
    .form-control {
        font-size: 14px;
    }
    
    .vip-tab {
        padding: 6px 12px;
        font-size: 0.8rem;
    }
}
</style>
